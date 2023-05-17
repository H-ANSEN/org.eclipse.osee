/*********************************************************************
 * Copyright (c) 2022 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
import {
	CdkVirtualScrollViewport,
	ScrollingModule,
} from '@angular/cdk/scrolling';
import { AsyncPipe, NgFor, NgIf, NgTemplateOutlet } from '@angular/common';
import {
	ChangeDetectionStrategy,
	OnChanges,
	OnDestroy,
	SimpleChanges,
	ViewChild,
} from '@angular/core';
import {
	AfterViewInit,
	Component,
	ContentChild,
	ContentChildren,
	Host,
	Input,
	OnInit,
	Optional,
	QueryList,
	TemplateRef,
	ViewChildren,
} from '@angular/core';
import { MatAutocomplete } from '@angular/material/autocomplete';
import { MatOption, MatOptionModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelect } from '@angular/material/select';
import {
	auditTime,
	BehaviorSubject,
	catchError,
	combineLatest,
	concatMap,
	filter,
	ignoreElements,
	Observable,
	of,
	ReplaySubject,
	scan,
	Subject,
	Subscription,
	switchMap,
	takeUntil,
	tap,
	timer,
} from 'rxjs';
import { debounceTime } from 'rxjs/internal/operators/debounceTime';
import { paginationMode } from '../internal/pagination-options';
/**
 * Component that handles loading, pagination and error states for mat-options
 */
@Component({
	selector: 'osee-mat-option-loading',
	templateUrl: './mat-option-loading.component.html',
	standalone: true,
	imports: [
		MatOptionModule,
		NgIf,
		NgFor,
		AsyncPipe,
		ScrollingModule,
		MatProgressSpinnerModule,
		NgTemplateOutlet,
	],
	changeDetection: ChangeDetectionStrategy.OnPush, //lessen the amount of redrawing necessary to cause less "bounciness"
})
export class MatOptionLoadingComponent<T>
	implements OnInit, AfterViewInit, OnDestroy, OnChanges
{
	/**
	 * Total number of items that should come from the paginated data source
	 */
	@Input() count: number = -1;

	/**
	 * Input data source that is used to display available options, and also the desired observable to paginate
	 *
	 * Note: only use the Observable<T[]> signature when in a non-paginated case.
	 */
	@Input() data!:
		| ((pageNum: number | string) => Observable<T[]>)
		| Observable<T[]>;

	/**
	 * Whether or not to disable the select while loading options
	 */
	@Input() disableSelect = false;

	/**
	 * Name of the objects being returned, eg. Platform Types, Messages etc.
	 */
	@Input() objectName = 'options';

	/**
	 * Pagination mode:
	 *
	 * OFF: No pagination occurs.
	 *
	 * Manual: Shows a "Show More" button for user to interact with
	 *
	 * Auto: Loads more data as user scrolls(250ms poll rate).
	 */
	@Input() paginationMode: paginationMode = 'OFF';

	/**
	 * Number of rows to expect per each query.
	 */
	@Input() paginationSize: number = 5;

	/**
	 * Rate Limit Api requests to once per x ms. Auto mode only.
	 */
	@Input() rateLimit: number = 500;

	private _currentPageNumber = new BehaviorSubject<number | string>(1);
	private _done = new Subject<void>();

	@ViewChildren(MatOption) protected options!: QueryList<MatOption>;
	@ContentChild(TemplateRef) template!: TemplateRef<T>;
	_paginationComplete = new BehaviorSubject<boolean>(false);
	_options!: Observable<T[]>;
	error!: Observable<string>;

	@ContentChildren(MatOption)
	protected resolvedOptions!: QueryList<MatOption>;
	@ViewChild(CdkVirtualScrollViewport, { static: false })
	cdkVirtualScrollViewPort!: CdkVirtualScrollViewport;

	private _autoPaginate = timer(0, 250).pipe(
		filter(
			(_) =>
				this.paginationMode === 'AUTO' &&
				this.cdkVirtualScrollViewPort !== undefined &&
				this.cdkVirtualScrollViewPort.getRenderedRange().end ===
					this.cdkVirtualScrollViewPort.getDataLength()
		),
		switchMap((_) => this._paginationComplete),
		filter((complete) => complete === false),
		auditTime(this.rateLimit),
		tap((_) => this.createPaginationEvent()),
		tap((_) => this.cdkVirtualScrollViewPort.checkViewportSize()),
		takeUntil(this._done)
	);
	private _autoPaginateSubscription: Subscription | undefined = undefined;
	private _dataSubject = new ReplaySubject<
		((pageNum: number | string) => Observable<T[]>) | Observable<T[]>
	>();
	private _count: BehaviorSubject<number> = new BehaviorSubject<number>(-1);

	constructor(
		@Host()
		@Optional()
		private _parentSelect: MatSelect,
		@Host()
		@Optional()
		private _parentAutoComplete: MatAutocomplete // Angular apparently doesn't like DI'ing multiple types
	) {}

	ngOnChanges(changes: SimpleChanges): void {
		//turn subscription on or off based on auto mode to keep the polling off in manual/no-paginate modes.
		if (
			this.paginationMode === 'AUTO' &&
			this._autoPaginateSubscription === undefined
		) {
			this._autoPaginateSubscription = this._autoPaginate.subscribe();
		} else if (
			this._autoPaginateSubscription !== undefined &&
			this.paginationMode !== 'AUTO'
		) {
			this._autoPaginateSubscription.unsubscribe();
			this._autoPaginateSubscription = undefined;
		}
		if (changes.data) {
			this._dataSubject.next(changes.data.currentValue);
		}
		if (changes.count) {
			this._count.next(changes.count.currentValue);
		}
	}
	ngOnDestroy(): void {
		this._done.next();
		this._done.complete();
	}
	/**
	 * @TODO shareReplay observables need to be handled, or mat-select with delayed values needs to be handled
	 */
	ngOnInit(): void {
		this._options = combineLatest([this._dataSubject, this._count]).pipe(
			debounceTime(500),
			tap((_) => {
				this._currentPageNumber.next(1);
				this._paginationComplete.next(false);
			}),
			switchMap(([query, count]) =>
				this._currentPageNumber.pipe(
					switchMap((pageNum) =>
						this._paginationComplete.pipe(
							filter((complete) => complete === false),
							// This needs to be a concatmap in order to complete all of the page emissions
							// in order. The catch with concatmap is if the query does not complete, or the
							// observable stays hot, the it will not move on to the next page.
							// If the options aren't paginating, make sure there's a take(1) on the source
							// observable and that it's completing.
							concatMap((_) => {
								if (this._isNotObservable(query)) {
									return query.call(this, pageNum).pipe(
										tap((results) => {
											if (
												results.length <
												this.paginationSize
											) {
												this._paginationComplete.next(
													true
												);
											}
										})
									);
								}
								this._paginationComplete.next(true);
								return query;
							})
						)
					),
					scan((acc, curr) => {
						// For plain non-paginated observables, return the initial query results.
						if (!this._isNotObservable(query)) {
							return curr;
						}

						if (count < 0) {
							if (acc.length === 0) {
								return [...curr];
							} else if (curr.length === 0) {
								this._paginationComplete.next(true);
								return acc;
							}

							const deDupedArr = [...acc, ...curr].filter(
								(value, index, array) =>
									array.findIndex(
										(value2) =>
											JSON.stringify(value) ===
											JSON.stringify(value2)
									) === index
							);

							if (
								(acc.length + curr.length) /
									this.paginationSize !==
									this._currentPageNumber.getValue() &&
								Number.isInteger(
									(acc.length + curr.length) /
										this.paginationSize
								) &&
								curr.length !== 0
							) {
								//reset the pagination
								this._currentPageNumber.next(1);
								this._paginationComplete.next(false);
								return [];
							} else if (
								deDupedArr.length !==
								acc.length + curr.length
							) {
								const split: T[] = acc.slice(
									this._getDuplicatedIndex(acc) - 1,
									acc.length + curr.length
								);
								return split;
							}
						} else if (acc.length + curr.length >= count) {
							this._paginationComplete.next(true);
						}
						return [...acc, ...curr];
					}, [] as T[])
				)
			)
		);

		this.error = this._options.pipe(
			ignoreElements(),
			catchError((err) =>
				err ? of('Error when fetching data from OSEE server') : of()
			)
		);
	}

	getHeightPx(itemSize: number, optLength: number) {
		return itemSize * Math.min(5, Math.max(optLength, 1));
	}

	private _getDuplicatedIndex(value: T[]) {
		let found: any = {}; //yuck
		for (let i = 0; i < value.length; i++) {
			if (found[value[i]]) {
				return i;
			}
			found[value[i]] = i;
		}
		return -1;
	}

	ngAfterViewInit(): void {
		if (this._parentSelect !== undefined && this._parentSelect !== null) {
			/**leaving disabled for now, will need to figure out test fix. 
			/However, this needs to remain on for autocomplete since the most common pattern for autocomplete
			involves something like (pageNum)=>this.opened.pipe(switchMap(v=>this.filter.pipe(PAGINATED_OBSERVABLE_HERE)))
			*/
			//this.registerSelectOptions(this._parentSelect, this.options);
			combineLatest([
				this.resolvedOptions.changes,
				this.options.changes,
			]).subscribe(([changes]) => {
				this.registerSelectOptions(this._parentSelect, this.options);
				if (this.cdkVirtualScrollViewPort !== undefined) {
					this.cdkVirtualScrollViewPort.scrollToIndex(0);
					this.cdkVirtualScrollViewPort.checkViewportSize();
				}
			});
		}
		if (
			this._parentAutoComplete !== undefined &&
			this._parentAutoComplete !== null
		) {
			this.registerSelectOptions(this._parentAutoComplete, this.options);
			combineLatest([
				this.resolvedOptions.changes,
				this.options.changes,
			]).subscribe(([changes]) => {
				this.registerSelectOptions(
					this._parentAutoComplete,
					this.options
				);
				if (this.cdkVirtualScrollViewPort !== undefined) {
					this.cdkVirtualScrollViewPort.scrollToIndex(0);
					this.cdkVirtualScrollViewPort.checkViewportSize();
				}
			});
		}
	}

	protected registerSelectOptions(
		select: MatSelect | MatAutocomplete,
		options: QueryList<MatOption>
	): void {
		const _options = [
			...new Map(
				[...select.options.toArray(), ...options.toArray()].map((m) => [
					m.id,
					m,
				])
			).values(),
		];
		const __options = _options.filter(
			(v, i) => _options.map((a) => a.value).indexOf(v.value) === i
		);
		select.options.reset([...__options]);
		select.options.notifyOnChanges();
	}

	private _isMatSelect(
		value: MatSelect | MatAutocomplete
	): value is MatSelect {
		return value !== null && value !== undefined && 'disabled' in value;
	}

	setDisabled(value: boolean) {
		if (this._isMatSelect(this._parentSelect)) {
			this._parentSelect.disabled = value;
		}
	}

	private _isNotObservable(
		value: ((pageNum: number | string) => Observable<T[]>) | Observable<T[]>
	): value is (pageNum: number | string) => Observable<T[]> {
		return !('subscribe' in value);
	}

	createPaginationEvent() {
		if (this.paginationMode !== 'OFF') {
			this._currentPageNumber.next(
				Number(this._currentPageNumber.getValue()) + 1
			);
		}
	}
}

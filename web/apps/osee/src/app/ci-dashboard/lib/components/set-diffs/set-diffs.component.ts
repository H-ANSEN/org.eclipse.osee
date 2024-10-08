/*********************************************************************
 * Copyright (c) 2023 Boeing
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
import { AsyncPipe, NgClass } from '@angular/common';
import { Component, effect, signal, viewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import {
	MatCell,
	MatCellDef,
	MatColumnDef,
	MatHeaderCell,
	MatHeaderCellDef,
	MatHeaderRow,
	MatHeaderRowDef,
	MatRow,
	MatRowDef,
	MatTable,
	MatTableDataSource,
} from '@angular/material/table';
import { MatTooltip } from '@angular/material/tooltip';
import { HeaderService } from '@osee/shared/services';
import { SplitStringPipe } from '@osee/shared/utils';
import { combineLatest, tap } from 'rxjs';
import { CiSetDiffService } from '../../services/ci-set-diff.service';
import { setDiffHeaderDetails } from '../../table-headers/set-diff.headers';
import { SetDiff } from '../../types';
import { CiDashboardControlsComponent } from '../ci-dashboard-controls/ci-dashboard-controls.component';
import { SetDropdownMultiComponent } from './set-dropdown-multi/set-dropdown-multi.component';

@Component({
	selector: 'osee-set-diffs',
	standalone: true,
	imports: [
		NgClass,
		AsyncPipe,
		CiDashboardControlsComponent,
		SetDropdownMultiComponent,
		MatTable,
		MatColumnDef,
		MatHeaderCell,
		MatHeaderCellDef,
		MatTooltip,
		MatCell,
		MatCellDef,
		MatHeaderRow,
		MatHeaderRowDef,
		MatRow,
		MatRowDef,
		MatPaginator,
		SplitStringPipe,
	],
	templateUrl: './set-diffs.component.html',
})
export default class SetDiffsComponent {
	private paginator = viewChild.required(MatPaginator);

	dataSource = new MatTableDataSource<SetDiff>();

	private _updateDataSourcePaginator = effect(() => {
		this.dataSource.paginator = this.paginator();
	});

	defaultHeaders = ['name', 'equal'];
	setDiffHeaders = ['passes', 'fails', 'abort'];
	groupHeaders = signal([' ']);
	headers = signal(this.defaultHeaders);

	constructor(
		private diffService: CiSetDiffService,
		private headerService: HeaderService
	) {}

	selectedSets = this.diffService.selectedSets;

	setDiffs = combineLatest([
		this.diffService.setDiffs,
		this.selectedSets,
	]).pipe(
		tap(([setDiffs, sets]) => {
			this.dataSource.data = setDiffs;
			this.groupHeaders.set([' ']);
			this.headers.set(this.defaultHeaders);
			for (let set of sets) {
				this.groupHeaders.update((headers) => [...headers, set.name]);
				const mappedHeaders = this.setDiffHeaders.map(
					(h) => h + '-' + set.id
				);
				this.headers.update((headers) => [
					...headers,
					...mappedHeaders,
				]);
			}
		})
	);

	getTableHeaderByName(header: string) {
		const formattedHeader = header.split('-')[0];
		return this.headerService.getHeaderByName(
			setDiffHeaderDetails,
			formattedHeader
		);
	}
}

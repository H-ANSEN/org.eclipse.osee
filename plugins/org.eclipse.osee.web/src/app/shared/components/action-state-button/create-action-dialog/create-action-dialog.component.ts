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
import { AsyncPipe } from '@angular/common';
import { Component, Inject, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatOptionModule } from '@angular/material/core';
import {
	MatDialogModule,
	MatDialogRef,
	MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { BehaviorSubject, combineLatest, of } from 'rxjs';
import { filter, shareReplay, switchMap, tap } from 'rxjs/operators';
import { ActionUserService } from '../internal/services/action-user.service';
import { ActionStateButtonService } from '../internal/services/action-state-button.service';
import {
	actionableItem,
	PRIORITIES,
	CreateAction,
	WorkType,
	atsLastMod,
} from '@osee/shared/types/configuration-management';
import { user } from '@osee/shared/types/auth';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import {
	MatAutocompleteModule,
	MatAutocompleteSelectedEvent,
} from '@angular/material/autocomplete';
import { LatestActionDropDownComponent } from '../../latest-action-drop-down/latest-action-drop-down.component';
/**
 * Dialog for creating a new action with the correct workType and category.
 */
@Component({
	selector: 'osee-create-action-dialog',
	templateUrl: './create-action-dialog.component.html',
	styles: [],
	standalone: true,
	imports: [
		AsyncPipe,
		FormsModule,
		MatDialogModule,
		MatFormFieldModule,
		MatSelectModule,
		MatOptionModule,
		MatInputModule,
		MatButtonModule,
		MatCheckboxModule,
		MatIconModule,
		MatAutocompleteModule,
		LatestActionDropDownComponent,
	],
})
export class CreateActionDialogComponent {
	actionableItemId = new BehaviorSubject<string>('');
	users = this.userService.usersSorted;
	actionableItemsFilter = signal('');
	actionableItems = toSignal(
		this.actionService.actionableItems.pipe(
			tap((items) => {
				if (items.length === 1) {
					this._selectActionableItem(items[0]);
				}
			})
		)
	);
	filteredActionableItems = computed(
		() =>
			this.actionableItems()?.filter((wt) =>
				wt.name
					.toLowerCase()
					.includes(this.actionableItemsFilter().toLowerCase())
			) || []
	);
	workTypesFilter = signal('');
	workTypes = toSignal(
		this.actionService.workTypes.pipe(
			tap((types) => {
				types.forEach((t) => {
					if (t.name === this.data.defaultWorkType) {
						this.workType = t;
						this.data.createBranchDefault = t.createBranchDefault;
						return;
					}
				});
			})
		)
	);
	filteredWorkTypes = computed(
		() =>
			this.workTypes()?.filter((wt) =>
				wt.name
					.toLowerCase()
					.includes(this.workTypesFilter().toLowerCase())
			) || []
	);
	points = this.actionService.getPoints();
	workType: WorkType = {
		name: '',
		humanReadableName: '',
		description: '',
		createBranchDefault: false,
	};
	selectedAssignees: user[] = [];
	targetedVersions = this.actionableItemId.pipe(
		filter((id) => id !== ''),
		switchMap((id) =>
			combineLatest([
				this.actionService.branchState,
				this.actionService.getVersions(id),
			]).pipe(
				tap(([branch, versions]) => {
					versions.forEach((v) => {
						if (v.name === branch.name) {
							this.data.targetedVersion = v;
							return;
						}
					});
				}),
				switchMap(([_, versions]) => of(versions))
			)
		)
	);
	changeTypes = this.actionableItemId.pipe(
		filter((id) => id !== ''),
		switchMap((id) => this.actionService.getChangeTypes(id))
	);
	additionalFields = this.actionableItemId.pipe(
		filter((id) => id !== ''),
		switchMap((id) => this.actionService.getCreateActionFields(id))
	);
	teamDef = this.actionableItemId.pipe(
		filter((id) => id !== ''),
		switchMap((id) => this.actionService.getTeamDef(id)),
		shareReplay({ bufferSize: 1, refCount: true })
	);
	featureGroups = this.teamDef.pipe(
		filter((t) => t !== undefined && t !== null && t.length > 0),
		switchMap((teams) => this.actionService.getFeatureGroups(teams[0].id))
	);
	sprints = this.teamDef.pipe(
		filter((t) => t !== undefined && t !== null && t.length > 0),
		switchMap((teams) => this.actionService.getSprints(teams[0].id))
	);
	private _priorityKeys = Object.keys(PRIORITIES);
	private _priorityValues = Object.values(PRIORITIES);
	priorities = this._priorityKeys.map((item, row) => {
		return {
			name: item.split(/(?=[A-Z])/).join(' '),
			value: this._priorityValues[row],
		};
	});

	constructor(
		public dialogRef: MatDialogRef<CreateActionDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: CreateAction,
		public actionService: ActionStateButtonService,
		public userService: ActionUserService
	) {}

	onNoClick(): void {
		this.dialogRef.close();
	}
	selectActionableItem(selection: MatAutocompleteSelectedEvent) {
		this._selectActionableItem(selection.option.value);
	}

	private _selectActionableItem(ai: actionableItem) {
		this.data.actionableItem = ai;
		this.actionableItemId.next(ai.id);
	}

	selectWorkType(selection: MatAutocompleteSelectedEvent) {
		this._selectWorkType(selection.option.value);
	}

	private _selectWorkType(workType: WorkType) {
		this.workType = workType;
		this.actionService.workTypeValue = this.workType.name;
		this.data.createBranchDefault = this.workType.createBranchDefault;
		this._selectActionableItem(new actionableItem());
	}

	selectAssignees(selection: MatSelectChange) {
		const selections: user[] = selection.value;
		this.data.assignees = selections.map((s) => s.id).join(',');
	}

	compareUsers(user1: user, user2: user) {
		return user1.id === user2.id;
	}

	clearWorkType(event: Event) {
		event.stopPropagation();
		this.workTypesFilter.set('');
		this._selectWorkType({
			name: '',
			humanReadableName: '',
			description: '',
			createBranchDefault: false,
		});
	}

	clearActionableItem(event: Event) {
		event.stopPropagation();
		this.actionableItemsFilter.set('');
		this._selectActionableItem(new actionableItem());
	}

	updateWorkTypeFilter(event: Event) {
		const value = (event.target as HTMLInputElement).value;
		this.workTypesFilter.set(value);
	}

	updateActionableItemsFilter(event: Event) {
		const value = (event.target as HTMLInputElement).value;
		this.actionableItemsFilter.set(value);
	}

	displayFn(val: WorkType | actionableItem) {
		return val?.name;
	}
}

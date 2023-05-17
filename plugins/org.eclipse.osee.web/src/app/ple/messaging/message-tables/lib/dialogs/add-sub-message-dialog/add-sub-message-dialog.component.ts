/*********************************************************************
 * Copyright (c) 2021 Boeing
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
import { Component, Inject } from '@angular/core';
import {
	MatDialogModule,
	MatDialogRef,
	MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatStepper, MatStepperModule } from '@angular/material/stepper';
import { AddSubMessageDialog } from '../../types/AddSubMessageDialog';
import { FormsModule } from '@angular/forms';
import { AsyncPipe, NgIf } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatOptionModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { TextFieldModule } from '@angular/cdk/text-field';
import { CurrentMessagesService } from '@osee/messaging/shared/services';
import type { subMessage } from '@osee/messaging/shared/types';
import { MatOptionLoadingComponent } from '@osee/shared/components';
import { BehaviorSubject, debounceTime, map, switchMap } from 'rxjs';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
	selector: 'osee-messaging-add-sub-message-dialog',
	templateUrl: './add-sub-message-dialog.component.html',
	standalone: true,
	imports: [
		AsyncPipe,
		NgIf,
		FormsModule,
		MatFormFieldModule,
		MatStepperModule,
		MatOptionLoadingComponent,
		MatOptionModule,
		MatButtonModule,
		MatInputModule,
		TextFieldModule,
		MatInputModule,
		MatDialogModule,
		MatAutocompleteModule,
		MatTooltipModule,
	],
})
export class AddSubMessageDialogComponent {
	constructor(
		public dialogRef: MatDialogRef<AddSubMessageDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: AddSubMessageDialog,
		private messageService: CurrentMessagesService
	) {}

	selectedSubmessage: subMessage | undefined;
	submessageSearch = new BehaviorSubject<string>('');
	paginationSize = 50;

	availableSubMessages = this.submessageSearch.pipe(
		debounceTime(250),
		map(
			(search) => (pageNum: string | number) =>
				this.messageService.getPaginatedSubmessagesByName(
					search,
					this.paginationSize,
					pageNum
				)
		)
	);

	availableSubMessagesCount = this.submessageSearch.pipe(
		debounceTime(250),
		switchMap((search) =>
			this.messageService.getSubmessagesByNameCount(search)
		)
	);

	moveToStep(index: number, stepper: MatStepper) {
		stepper.selectedIndex = index - 1;
	}

	createNew() {
		this.data.subMessage.id = '-1';
		this.selectedSubmessage = undefined;
	}

	moveToReview(stepper: MatStepper) {
		if (this.selectedSubmessage) {
			this.data.subMessage = this.selectedSubmessage;
		}
		this.moveToStep(3, stepper);
	}

	applySearchTerm(searchTerm: Event) {
		const value = (searchTerm.target as HTMLInputElement).value;
		this.submessageSearch.next(value);
	}

	selectExistingSubmessage(structure: subMessage) {
		this.selectedSubmessage = structure;
	}
}

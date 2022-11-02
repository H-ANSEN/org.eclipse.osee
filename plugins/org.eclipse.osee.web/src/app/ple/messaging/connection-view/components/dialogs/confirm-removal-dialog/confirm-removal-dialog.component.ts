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
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { RemovalDialog } from '../../../types/ConfirmRemovalDialog';

@Component({
	selector: 'osee-confirm-removal-dialog',
	templateUrl: './confirm-removal-dialog.component.html',
	styleUrls: ['./confirm-removal-dialog.component.sass'],
})
export class ConfirmRemovalDialogComponent {
	constructor(
		public dialogRef: MatDialogRef<ConfirmRemovalDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: RemovalDialog
	) {}

	onNoClick() {
		this.dialogRef.close();
	}
}

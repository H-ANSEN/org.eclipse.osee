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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonModule } from '@angular/material/button';
import {
	MatDialogModule,
	MatDialogRef,
	MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { dialogRef } from 'src/app/ple/messaging/connection-view/mocks/dialogRef.mock';

import { RemoveMessageDialogComponent } from './remove-message-dialog.component';

describe('RemoveMessageDialogComponent', () => {
	let component: RemoveMessageDialogComponent;
	let fixture: ComponentFixture<RemoveMessageDialogComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			declarations: [RemoveMessageDialogComponent],
			imports: [MatDialogModule, MatButtonModule],
			providers: [
				{ provide: MatDialogRef, useValue: dialogRef },
				{
					provide: MAT_DIALOG_DATA,
					useValue: {
						message: {
							name: 'abcdef',
						},
					},
				},
			],
		}).compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(RemoveMessageDialogComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});

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
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { NgIf, AsyncPipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { RouterLink } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

import { SubElementTableDropdownComponent } from './sub-element-table-dropdown.component';

import {
	CurrentStateServiceMock,
	preferencesUiServiceMock,
	elementsMock,
	elementTableDropdownServiceMock,
} from '@osee/messaging/shared/testing';
import {
	CurrentStructureService,
	PreferencesUIService,
} from '@osee/messaging/shared/services';
import { STRUCTURE_SERVICE_TOKEN } from '@osee/messaging/shared/tokens';
import { MatDividerModule } from '@angular/material/divider';
import { ElementTableDropdownService } from '../../services/element-table-dropdown.service';

describe('SubElementTableDropdownComponent', () => {
	let component: SubElementTableDropdownComponent;
	let fixture: ComponentFixture<SubElementTableDropdownComponent>;
	let loader: HarnessLoader;
	let service: CurrentStructureService;
	let mEvent: MouseEvent;

	beforeEach(async () => {
		await TestBed.overrideComponent(SubElementTableDropdownComponent, {
			set: {
				imports: [
					NgIf,
					AsyncPipe,
					RouterTestingModule,
					RouterLink,
					MatMenuModule,
					MatIconModule,
					MatDialogModule,
					MatFormFieldModule,
					MatDividerModule,
				],
				providers: [
					{
						provide: STRUCTURE_SERVICE_TOKEN,
						useValue: CurrentStateServiceMock,
					},
					{
						provide: CurrentStructureService,
						useValue: CurrentStateServiceMock,
					},
					{
						provide: ElementTableDropdownService,
						useValue: elementTableDropdownServiceMock,
					},
				],
			},
		})
			.configureTestingModule({
				imports: [SubElementTableDropdownComponent],
				providers: [
					{
						provide: STRUCTURE_SERVICE_TOKEN,
						useValue: CurrentStateServiceMock,
					},
					{
						provide: CurrentStructureService,
						useValue: CurrentStateServiceMock,
					},
					{
						provide: ElementTableDropdownService,
						useValue: elementTableDropdownServiceMock,
					},
				],
			})
			.compileComponents();
		service = TestBed.inject(CurrentStructureService);

		fixture = TestBed.createComponent(SubElementTableDropdownComponent);
		component = fixture.componentInstance;
		component.structure = {
			id: '1734890124',
			name: 'sample structure',
			nameAbbrev: '',
			description: '',
			interfaceMaxSimultaneity: '',
			interfaceMinSimultaneity: '',
			interfaceTaskFileType: 0,
			interfaceStructureCategory: '',
		};
		component.element = elementsMock[0];
		fixture.detectChanges();
		loader = TestbedHarnessEnvironment.loader(fixture);
	});

	beforeEach(() => {
		mEvent = document.createEvent('MouseEvent');
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});

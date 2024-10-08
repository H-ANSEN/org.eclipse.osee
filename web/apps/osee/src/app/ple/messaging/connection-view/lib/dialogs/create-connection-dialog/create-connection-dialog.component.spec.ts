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
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonHarness } from '@angular/material/button/testing';
import {
	MatDialogModule,
	MatDialogRef,
	MAT_DIALOG_DATA,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatFormFieldHarness } from '@angular/material/form-field/testing';
import { MatInputModule } from '@angular/material/input';
import { MatInputHarness } from '@angular/material/input/testing';
import { MatSelectModule } from '@angular/material/select';
import { MatSelectHarness } from '@angular/material/select/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { graphServiceMock } from '../../testing/current-graph.service.mock';
import { CurrentGraphService } from '../../services/current-graph.service';

import { CreateConnectionDialogComponent } from './create-connection-dialog.component';
import { MatOptionModule } from '@angular/material/core';
import {
	CurrentTransportTypeService,
	EnumsService,
} from '@osee/messaging/shared/services';
import { MockMatOptionLoadingComponent } from '@osee/shared/components/testing';
import {
	dialogRef,
	enumsServiceMock,
	CurrentTransportTypeServiceMock,
	nonDirectTransportType,
	ethernetTransportType,
} from '@osee/messaging/shared/testing';
import { CommonModule } from '@angular/common';
import { ConnectionNodesCountDirective } from '@osee/messaging/shared/directives';

describe('CreateConnectionDialogComponent', () => {
	let component: CreateConnectionDialogComponent;
	let fixture: ComponentFixture<CreateConnectionDialogComponent>;
	let loader: HarnessLoader;

	beforeEach(async () => {
		await TestBed.overrideComponent(CreateConnectionDialogComponent, {
			set: {
				providers: [
					{ provide: MatDialogRef, useValue: dialogRef },
					{ provide: MAT_DIALOG_DATA, useValue: {} },
					{
						provide: CurrentGraphService,
						useValue: graphServiceMock,
					},
					{ provide: EnumsService, useValue: enumsServiceMock },
					{
						provide: CurrentTransportTypeService,
						useValue: CurrentTransportTypeServiceMock,
					},
				],
				imports: [
					CommonModule,
					MatDialogModule,
					MatFormFieldModule,
					FormsModule,
					MatInputModule,
					MatSelectModule,
					MockMatOptionLoadingComponent,
					MatOptionModule,
					MatButtonModule,
					ConnectionNodesCountDirective,
				],
			},
		})
			.configureTestingModule({
				imports: [
					MatDialogModule,
					MatInputModule,
					MatFormFieldModule,
					MatSelectModule,
					MatButtonModule,
					NoopAnimationsModule,
					FormsModule,
					CreateConnectionDialogComponent,
				],
				declarations: [],
				providers: [
					{ provide: MatDialogRef, useValue: dialogRef },
					{ provide: MAT_DIALOG_DATA, useValue: {} },
					{
						provide: CurrentGraphService,
						useValue: graphServiceMock,
					},
					{ provide: EnumsService, useValue: enumsServiceMock },
					{
						provide: CurrentTransportTypeService,
						useValue: CurrentTransportTypeServiceMock,
					},
				],
			})
			.compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(CreateConnectionDialogComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
		loader = TestbedHarnessEnvironment.loader(fixture);
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it('should close without anything returning', async () => {
		let buttons = await loader.getAllHarnesses(MatButtonHarness);
		let spy = spyOn(component, 'onNoClick').and.callThrough();
		if ((await buttons[0].getText()) === 'Cancel') {
			await buttons[0].click();
			expect(spy).toHaveBeenCalled();
		}
	});

	it('should select a new transport type', async () => {
		let form = loader.getHarness(
			MatFormFieldHarness.with({
				selector: '#connection-transport-type-selector',
			})
		);
		let select = await (await form).getControl(MatSelectHarness);
		await select?.open();
		expect((await select?.getOptions())?.length).toEqual(1);
		await select?.clickOptions({ text: 'ETHERNET' });
		expect(await select?.getValueText()).toEqual('ETHERNET');
	});

	it('should select a new node to connect from', async () => {
		component.newConnection.connection.transportType =
			ethernetTransportType;
		let form = loader.getHarness(
			MatFormFieldHarness.with({
				selector: '#connection-from-node-selector',
			})
		);
		let select = await (await form).getControl(MatSelectHarness);
		await select?.open();
		expect((await select?.getOptions())?.length).toEqual(2);
		await select?.clickOptions({ text: 'Second' });
		expect(await select?.getValueText()).toEqual('Second');
	});

	it('should select a new node to connect to', async () => {
		component.newConnection.connection.transportType =
			ethernetTransportType;
		let form = loader.getHarness(
			MatFormFieldHarness.with({
				selector: '#connection-to-node-selector',
			})
		);
		let select = await (await form).getControl(MatSelectHarness);
		await select?.open();
		expect((await select?.getOptions())?.length).toEqual(2);
		await select?.clickOptions({ text: 'Second' });
		expect(await select?.getValueText()).toEqual('Second');
	});

	it('should select connection nodes', async () => {
		component.newConnection.connection.transportType =
			nonDirectTransportType;
		let form = loader.getHarness(
			MatFormFieldHarness.with({
				selector: '#connection-node-selector',
			})
		);
		let select = await (await form).getControl(MatSelectHarness);
		await select?.open();
		expect((await select?.getOptions())?.length).toEqual(2);
		await select?.clickOptions({ text: 'Second' });
		expect(await select?.getValueText()).toEqual('Second');
	});

	it('should enter a description', async () => {
		let form = loader.getHarness(
			MatFormFieldHarness.with({
				selector: '#connection-description-field',
			})
		);
		let input = await (await form).getControl(MatInputHarness);
		expect(await input?.getType()).toEqual('text');
		await input?.setValue('Description');
		expect(await input?.getValue()).toEqual('Description');
	});

	it('should enter a name', async () => {
		let form = loader.getHarness(
			MatFormFieldHarness.with({ selector: '#connection-name-field' })
		);
		let input = await (await form).getControl(MatInputHarness);
		expect(await input?.getType()).toEqual('text');
		await input?.setValue('Name');
		expect(await input?.getValue()).toEqual('Name');
	});
});

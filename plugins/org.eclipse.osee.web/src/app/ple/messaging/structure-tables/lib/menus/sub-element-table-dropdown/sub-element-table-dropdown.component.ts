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
import { Component, Inject, Input } from '@angular/core';
import { take, switchMap, iif, of } from 'rxjs';
import { applic } from '@osee/shared/types/applicability';
import { difference } from '@osee/shared/types/change-report';
import { RemoveElementDialogData } from '../../dialogs/remove-element-dialog/remove-element-dialog';
import { RemoveElementDialogComponent } from '../../dialogs/remove-element-dialog/remove-element-dialog.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import { AsyncPipe, NgIf } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { STRUCTURE_SERVICE_TOKEN } from '@osee/messaging/shared/tokens';
import {
	type element,
	type structure,
	type PlatformType,
	type elementWithChanges,
	elementSentinel,
} from '@osee/messaging/shared/types';
import {
	CurrentStructureService,
	HeaderService,
} from '@osee/messaging/shared/services';
import { PlatformTypeSentinel } from '@osee/messaging/shared/enumerations';
import { MatDividerModule } from '@angular/material/divider';
import { ElementTableDropdownService } from '../../services/element-table-dropdown.service';

/**
 * Required attributes:
 * element
 * structure
 * header
 * branchId
 * branchType
 * editMode
 */
@Component({
	selector:
		'osee-sub-element-table-dropdown[element][structure][header][branchId][branchType][editMode]',
	standalone: true,
	imports: [
		NgIf,
		AsyncPipe,
		RouterLink,
		MatMenuModule,
		MatIconModule,
		MatDialogModule,
		MatFormFieldModule,
		MatDividerModule,
	],
	templateUrl: './sub-element-table-dropdown.component.html',
	styles: [],
})
export class SubElementTableDropdownComponent {
	@Input() element: element = elementSentinel;

	@Input() structure: structure = {
		id: '-1',
		name: '',
		nameAbbrev: '',
		description: '',
		interfaceMaxSimultaneity: '',
		interfaceMinSimultaneity: '',
		interfaceTaskFileType: 0,
		interfaceStructureCategory: '',
	};

	@Input() header!: keyof element;
	@Input() field?: string | number | boolean | PlatformType | applic;

	@Input('branchId') _branchId: string = '';
	@Input('branchType') _branchType: string = '';

	@Input() editMode: boolean = false;

	constructor(
		public dialog: MatDialog,
		@Inject(STRUCTURE_SERVICE_TOKEN)
		private structureService: CurrentStructureService,
		private headerService: HeaderService,
		private elementDropdownService: ElementTableDropdownService
	) {}

	removeElement(element: element, structure: structure) {
		const dialogData: RemoveElementDialogData = {
			removeType: 'Structure',
			elementName: element.name,
		};
		this.dialog
			.open(RemoveElementDialogComponent, {
				data: dialogData,
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((dialogResult: string) =>
					iif(
						() => dialogResult === 'ok',
						this.structureService.removeElementFromStructure(
							element,
							structure
						),
						of()
					)
				)
			)
			.subscribe();
	}

	deleteElement(element: element) {
		this.elementDropdownService.openDeleteElementDialog(
			element,
			'Structure'
		);
	}

	openAddElementDialog(
		structure: structure | element,
		isArray: boolean,
		allowArray: boolean,
		afterElement?: string,
		copyElement?: element
	) {
		this.elementDropdownService.openAddElementDialog(
			structure,
			isArray,
			allowArray,
			afterElement,
			copyElement
		);
	}

	openEditElementDialog(element: element) {
		this.elementDropdownService.openEditElementDialog(element);
	}

	openEnumDialog(id: string) {
		this.elementDropdownService.openEnumDialog(id, this.editMode);
	}

	openDescriptionDialog(description: string, elementId: string) {
		this.elementDropdownService.openDescriptionDialog(
			description,
			elementId
		);
	}

	/**
	 * Need to verify if type is required
	 */
	openEnumLiteralDialog(enumLiteral: string, elementId: string) {
		this.elementDropdownService.openEnumLiteralDialog(
			enumLiteral,
			elementId
		);
	}

	openNotesDialog(notes: string, elementId: string) {
		this.elementDropdownService.openNotesDialog(notes, elementId);
	}

	getHeaderByName(value: string) {
		return this.headerService.getHeaderByName(value, 'element');
	}

	viewDiff<T>(value: difference<T> | undefined, header: string) {
		this.elementDropdownService.viewDiff(value, header);
	}

	hasChanges(v: element | elementWithChanges): v is elementWithChanges {
		return this.elementDropdownService.hasChanges(v);
	}
}

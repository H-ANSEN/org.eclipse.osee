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
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { AsyncPipe, NgClass, NgFor, NgIf } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { applic } from 'src/app/types/applicability/applic';
import {
	HeaderKeys,
	HeaderKeysEnum,
	HeaderService,
} from '@osee/messaging/shared';
import { HighlightFilteredTextDirective } from '@osee/shared/utils';

@Component({
	selector: 'osee-import-table',
	standalone: true,
	imports: [
		AsyncPipe,
		HighlightFilteredTextDirective,
		NgClass,
		NgIf,
		NgFor,
		MatFormFieldModule,
		MatInputModule,
		MatIconModule,
		MatTableModule,
	],
	templateUrl: './import-table.component.html',
	styleUrls: ['./import-table.component.scss'],
})
export class ImportTableComponent<T extends { [key: string]: any }>
	implements OnChanges
{
	@Input() data: T[] = [];
	@Input() headers: string[] = [];
	@Input() headerKey: HeaderKeys = HeaderKeysEnum.NONE;
	@Input() tableTitle: string = '';

	filteredData: T[] = [];
	filterText: string = '';
	showTableContents: boolean = false;

	constructor(private headerService: HeaderService) {}

	ngOnChanges(changes: SimpleChanges) {
		this.filteredData = this.data;
	}

	getTableHeaderByName(header: string) {
		return this.headerService.getTableHeaderByName(header, this.headerKey);
	}

	applyFilter(event: Event) {
		const filterValue = (event.target as HTMLInputElement).value;
		this.filterText = filterValue;
		if (filterValue === '') {
			this.filteredData = this.data;
			return;
		}
		this.filteredData = [];
		this.data.forEach((d) => {
			this.headers.forEach((header) => {
				if (header === 'applicability') {
					if (
						(d[header] as applic).name
							.toLowerCase()
							.includes(filterValue.toLowerCase())
					) {
						this.filteredData = [...this.filteredData, d];
					}
				} else {
					const val = '' + d[header];
					if (val.toLowerCase().includes(filterValue.toLowerCase())) {
						this.filteredData = [...this.filteredData, d];
					}
				}
			});
		});
	}

	toggleTableContents() {
		this.showTableContents = !this.showTableContents;
	}
}

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
	AfterViewInit,
	Component,
	Inject,
	OnDestroy,
	OnInit,
} from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { combineLatest, from, of, Subject } from 'rxjs';
import {
	concatMap,
	filter,
	map,
	reduce,
	shareReplay,
	switchMap,
	take,
	takeUntil,
	tap,
} from 'rxjs/operators';
import { editPlatformTypeDialogData } from '../../../types/editPlatformTypeDialogData';
import { logicalType } from '../../../types/logicaltype';
import { EnumsService } from '../../../services/http/enums.service';
import { TypesService } from '../../../services/http/types.service';
import { enumerationSet } from '../../../types/enum';
import { enumeratedPlatformType } from '../../../types/enumeratedPlatformType';
import { editPlatformTypeDialogDataMode } from '../../../types/EditPlatformTypeDialogDataMode.enum';
import { ATTRIBUTETYPEID } from '../../../../../../types/constants/AttributeTypeId.enum';
import { ParentErrorStateMatcher } from '../../../../../../shared-matchers/parent-error-state.matcher';

@Component({
	selector: 'osee-edit-type-dialog',
	templateUrl: './edit-type-dialog.component.html',
	styleUrls: ['./edit-type-dialog.component.sass'],
})
export class EditTypeDialogComponent implements AfterViewInit, OnDestroy {
	platform_type: string = '';
	logicalTypes = this.typesService.logicalTypes.pipe(
		switchMap((logicalTypes) =>
			from(logicalTypes).pipe(
				map((type) => {
					let name = type.name.replace('nsigned ', '');
					if (name !== type.name) {
						name =
							name.charAt(0) +
							name.charAt(1).toUpperCase() +
							name.slice(2);
					}
					type.name = name;
					return type;
				})
			)
		),
		reduce((acc, curr) => [...acc, curr], [] as logicalType[])
	);
	units = this.enumService.units;
	enumSet: enumerationSet = {
		name: '',
		applicability: { id: '1', name: 'Base' },
		description: '',
	};
	parentMatcher = new ParentErrorStateMatcher();

	enumUnique = new Subject<string>();
	private _logicalType = new Subject<string>();

	private _done = new Subject();
	private _fieldInfo = combineLatest([
		this._logicalType,
		this.logicalTypes,
	]).pipe(
		filter(([selectedType, types]) =>
			types.map((t) => t.name).includes(selectedType)
		),
		switchMap(([selectedType, types]) =>
			of(types).pipe(
				concatMap((types) => from(types)),
				filter((type) => type.name === selectedType),
				take(1)
			)
		),
		switchMap((type) =>
			this.typesService.getLogicalTypeFormDetail(type.id)
		),
		map((result) => result.fields),
		concatMap((fields) => from(fields)),
		shareReplay({ bufferSize: 1, refCount: true }),
		takeUntil(this._done)
	);

	bitSizeRequired = this._fieldInfo.pipe(
		filter(
			(v) =>
				v.attributeTypeId ===
				ATTRIBUTETYPEID.INTERFACEPLATFORMTYPEBITSIZE
		),
		map((v) => v.required)
	);

	bitResolutionRequired = this._fieldInfo.pipe(
		filter(
			(v) =>
				v.attributeTypeId ===
				ATTRIBUTETYPEID.INTERFACEPLATFORMTYPEBITSRESOLUTION
		),
		map((v) => v.required)
	);

	compRateRequired = this._fieldInfo.pipe(
		filter(
			(v) =>
				v.attributeTypeId ===
				ATTRIBUTETYPEID.INTERFACEPLATFORMTYPECOMPRATE
		),
		map((v) => v.required)
	);

	analogAccRequired = this._fieldInfo.pipe(
		filter(
			(v) =>
				v.attributeTypeId ===
				ATTRIBUTETYPEID.INTERFACEPLATFORMTYPEANALOGACCURACY
		),
		map((v) => v.required)
	);

	unitsRequired = this._fieldInfo.pipe(
		filter(
			(v) =>
				v.attributeTypeId === ATTRIBUTETYPEID.INTERFACEPLATFORMTYPEUNITS
		),
		map((v) => v.required)
	);

	descriptionRequired = this._fieldInfo.pipe(
		filter((v) => v.attributeTypeId === ATTRIBUTETYPEID.DESCRIPTION),
		map((v) => v.required)
	);

	minValRequired = this._fieldInfo.pipe(
		filter(
			(v) =>
				v.attributeTypeId ===
				ATTRIBUTETYPEID.INTERFACEPLATFORMTYPEMINVAL
		),
		map((v) => v.required)
	);

	maxValRequired = this._fieldInfo.pipe(
		filter(
			(v) =>
				v.attributeTypeId ===
				ATTRIBUTETYPEID.INTERFACEPLATFORMTYPEMAXVAL
		),
		map((v) => v.required)
	);

	msbValRequired = this._fieldInfo.pipe(
		filter(
			(v) =>
				v.attributeTypeId ===
				ATTRIBUTETYPEID.INTERFACEPLATFORMTYPEMSBVAL
		),
		map((v) => v.required)
	);

	defaultValRequired = this._fieldInfo.pipe(
		filter(
			(v) =>
				v.attributeTypeId ===
				ATTRIBUTETYPEID.INTERFACEPLATFORMTYPEDEFAULTVAL
		),
		map((v) => v.required)
	);

	constructor(
		public dialogRef: MatDialogRef<EditTypeDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: editPlatformTypeDialogData,
		private typesService: TypesService,
		private enumService: EnumsService
	) {
		this.platform_type = this.data.type.name;
	}
	ngAfterViewInit(): void {
		this._logicalType.next(this.data.type.interfaceLogicalType);
	}
	ngOnDestroy(): void {
		this._done.next('');
	}

	/**
	 * Calculates MSB value based on Bit Resolution, Byte Size and whether or not the type is signed/unsigned
	 * @returns @type {string} MSB Value
	 */
	getMSBValue() {
		if (
			this.data.type.interfacePlatformTypeBitsResolution === '' ||
			this.data.type.interfacePlatformTypeBitSize === ''
		) {
			return '';
		}
		return (
			Number(this.data.type.interfacePlatformTypeBitsResolution) *
			(2 ^
				((Number(this.data.type.interfacePlatformTypeBitSize) * 8 -
					Number(this.data.type.interfacePlatform2sComplement)) /
					2))
		).toString();
	}

	/**
	 * Calculates Resolution based on MSB value, Byte Size and whether or not the type is signed/unsigned
	 * @returns @type {string} Resolution
	 */
	getResolution() {
		if (
			this.data.type.interfacePlatformTypeMsbValue === '' ||
			this.data.type.interfacePlatformTypeBitSize === ''
		) {
			return '';
		}
		return (
			(Number(this.data.type.interfacePlatformTypeMsbValue) * 2) /
			(2 ^
				(Number(this.data.type.interfacePlatformTypeBitSize) * 8 -
					Number(this.data.type.interfacePlatform2sComplement)))
		).toString();
	}

	/**
	 * Returns the bit size which is 8 * byte size
	 */
	get byte_size() {
		return Number(this.data.type.interfacePlatformTypeBitSize) / 8;
	}

	/**
	 * Sets the byte size based on bit size /8
	 */
	set byte_size(value: number) {
		this.data.type.interfacePlatformTypeBitSize = String(value * 8);
	}

	/**
	 * Forcefully closes dialog without returning data
	 */
	onNoClick(): void {
		this.dialogRef.close();
	}

	compareLogicalTypes(o1: string, o2: string) {
		if (o1 === null || o2 === null) return false;
		let val1 = o1.replace('nsigned ', '');
		if (val1 !== o1) {
			val1 =
				val1.charAt(0) + val1.charAt(1).toUpperCase() + val1.slice(2);
		}
		let val2 = o2.replace('nsigned ', '');
		if (val2 !== o2) {
			val2 =
				val2.charAt(0) + val2.charAt(1).toUpperCase() + val2.slice(2);
		}
		return val1 === val2;
	}
	enumUpdate(value: enumerationSet | undefined) {
		if (value) {
			this.enumSet = value;
		}
	}
	get returnValue(): {
		mode: editPlatformTypeDialogDataMode;
		type: enumeratedPlatformType;
	} {
		return {
			mode: this.data.mode,
			type: {
				...this.data.type,
				interfaceLogicalType: 'enumeration',
				enumerationSet: this.enumSet,
			},
		};
	}
	updateUnique(value: boolean) {
		this.enumUnique.next(value.toString());
	}

	updateLogicalType(value: string) {
		this._logicalType.next(value);
	}
}

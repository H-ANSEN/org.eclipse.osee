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

import { PlatformTypeSentinel } from '@osee/messaging/shared/enumerations';
import type {
	structure,
	structureWithChanges,
} from '@osee/messaging/shared/types';

export const structuresMock: Required<structure>[] = [
	{
		id: '1',
		name: 'name',
		nameAbbrev: '',
		elements: [
			{
				id: '1',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
		],
		numElements: 1,
		sizeInBytes: 0,
		bytesPerSecondMaximum: 0,
		bytesPerSecondMinimum: 0,
		applicability: { id: '1', name: 'Base' },
		description: 'description',
		interfaceMaxSimultaneity: '0',
		interfaceMinSimultaneity: '1',
		interfaceTaskFileType: 1,
		incorrectlySized: false,
		interfaceStructureCategory: 'Category 1',
		autogenerated: false,
	},
];

export const structuresPreChanges: structure[] = [
	{
		id: '201364',
		name: 'name',
		nameAbbrev: '',
		elements: [
			{
				id: '201351',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
			{
				id: '201371',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
			{
				id: '201360',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
		],
		numElements: 1,
		sizeInBytes: 0,
		bytesPerSecondMaximum: 0,
		bytesPerSecondMinimum: 0,
		applicability: { id: '1', name: 'Base' },
		description: 'description',
		interfaceMaxSimultaneity: '0',
		interfaceMinSimultaneity: '1',
		interfaceTaskFileType: 1,
		interfaceStructureCategory: 'Category 1',
	},
	{
		id: '201365',
		name: 'name',
		nameAbbrev: '',
		elements: [
			{
				id: '1',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
		],
		numElements: 1,
		sizeInBytes: 0,
		bytesPerSecondMaximum: 0,
		bytesPerSecondMinimum: 0,
		applicability: { id: '1', name: 'Base' },
		description: 'description',
		interfaceMaxSimultaneity: '0',
		interfaceMinSimultaneity: '1',
		interfaceTaskFileType: 1,
		interfaceStructureCategory: 'Category 1',
	},
	{
		id: '201370',
		name: 'name',
		nameAbbrev: '',
		elements: [
			{
				id: '1',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceDefaultValue: '',
				enumLiteral: '',
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
		],
		numElements: 1,
		sizeInBytes: 0,
		bytesPerSecondMaximum: 0,
		bytesPerSecondMinimum: 0,
		applicability: { id: '1', name: 'Base' },
		description: 'description',
		interfaceMaxSimultaneity: '0',
		interfaceMinSimultaneity: '1',
		interfaceTaskFileType: 1,
		interfaceStructureCategory: 'Category 1',
	},
];

export const structuresMockWithChangesMulti: (
	| structure
	| structureWithChanges
)[] = [
	{
		id: '201364',
		name: 'name',
		nameAbbrev: '',
		elements: [
			{
				id: '1',
				name: 'hello',
				description: '',
				notes: '',
				deleted: true,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				logicalType: 'enumeration',
				interfaceDefaultValue: '',
				enumLiteral: '',
				units: '',
				platformType: new PlatformTypeSentinel(),
				arrayElements: [],
				changes: {
					name: {
						previousValue: 'hello',
						currentValue: '',
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					description: {
						previousValue: '',
						currentValue: '',
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					notes: {
						previousValue: '',
						currentValue: '',
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					platformType: {
						previousValue: new PlatformTypeSentinel(),
						currentValue: new PlatformTypeSentinel(),
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementIndexEnd: {
						previousValue: 1,
						currentValue: 1,
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementIndexStart: {
						previousValue: 0,
						currentValue: 0,
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementAlterable: {
						previousValue: true,
						currentValue: true,
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					applicability: {
						previousValue: undefined,
						currentValue: { id: '1', name: 'Base' },
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					enumLiteral: {
						previousValue: '',
						currentValue: '',
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
				},
			},
			{
				id: '201351',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
				added: true,
				changes: {
					applicability: {
						previousValue: undefined,
						currentValue: {
							id: '1',
							name: 'Base',
						},
						transactionToken: {
							id: '1222',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementAlterable: {
						previousValue: false,
						currentValue: true,
						transactionToken: {
							id: '1222',
							branchId: '2780650236653788489',
						},
					},
					platformType: {
						previousValue: new PlatformTypeSentinel(),
						currentValue: {
							...new PlatformTypeSentinel(),
							name: 'boolean',
							id: '9',
						},
						transactionToken: {
							id: '1222',
							branchId: '2780650236653788489',
						},
					},
					name: {
						previousValue: '',
						currentValue: 'name2',
						transactionToken: {
							id: '1222',
							branchId: '2780650236653788489',
						},
					},
					description: {
						previousValue: '',
						currentValue: 'description2',
						transactionToken: {
							id: '1222',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementIndexEnd: {
						previousValue: 0,
						currentValue: 1,
						transactionToken: {
							id: '1222',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementIndexStart: {
						previousValue: 0,
						currentValue: 0,
						transactionToken: {
							id: '1222',
							branchId: '2780650236653788489',
						},
					},
					notes: {
						previousValue: '',
						currentValue: 'notes',
						transactionToken: {
							id: '1222',
							branchId: '2780650236653788489',
						},
					},
					units: {
						previousValue: 'hertz',
						currentValue: 'feet²/second²',
						transactionToken: {
							id: '1322',
							branchId: '2780650236653788489',
						},
					},
					enumLiteral: {
						previousValue: '',
						currentValue: 'notes',
						transactionToken: {
							id: '1222',
							branchId: '2780650236653788489',
						},
					},
				},
			},
			{
				id: '201360',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
				changes: {
					units: {
						previousValue: 'hertz',
						currentValue: 'feet²/second²',
						transactionToken: {
							id: '1322',
							branchId: '2780650236653788489',
						},
					},
					platformType: {
						previousValue: new PlatformTypeSentinel(),
						currentValue: {
							...new PlatformTypeSentinel(),
							name: 'testingUnits',
							id: '9',
						},
						transactionToken: {
							id: '1322',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementIndexEnd: {
						previousValue: 7,
						currentValue: 9,
						transactionToken: {
							id: '1229',
							branchId: '2780650236653788489',
						},
					},
					notes: {
						previousValue: '',
						currentValue: 'testing notes',
						transactionToken: {
							id: '1265',
							branchId: '2780650236653788489',
						},
					},
				},
			},
			{
				id: '201371',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
				changes: {
					units: {
						previousValue: 'hertz',
						currentValue: 'feet²/second²',
						transactionToken: {
							id: '1322',
							branchId: '2780650236653788489',
						},
					},
					applicability: {
						previousValue: undefined,
						currentValue: { id: '1', name: 'Base' },
						transactionToken: {
							id: '1225',
							branchId: '2780650236653788489',
						},
					},
					name: {
						previousValue: '',
						currentValue: 'testaddingelement',
						transactionToken: {
							id: '1225',
							branchId: '2780650236653788489',
						},
					},
					description: {
						previousValue: '',
						currentValue: 'dsfads',
						transactionToken: {
							id: '1225',
							branchId: '2780650236653788489',
						},
					},
					notes: {
						previousValue: '',
						currentValue: '',
						transactionToken: {
							id: '1225',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementAlterable: {
						previousValue: false,
						currentValue: true,
						transactionToken: {
							id: '1225',
							branchId: '2780650236653788489',
						},
					},
					platformType: {
						previousValue: new PlatformTypeSentinel(),
						currentValue: {
							...new PlatformTypeSentinel(),
							name: 'Name',
							id: '9',
						},
						transactionToken: {
							id: '1225',
							branchId: '2780650236653788489',
						},
					},
				},
			},
		],
		numElements: 4,
		hasElementChanges: true,
		sizeInBytes: 0,
		bytesPerSecondMaximum: 0,
		bytesPerSecondMinimum: 0,
		applicability: {
			id: '2119518475782991281',
			name: 'ROBOT_SPEAKER = SPKR_B',
		},
		description: 'description',
		interfaceMaxSimultaneity: '0',
		interfaceMinSimultaneity: '1',
		interfaceTaskFileType: 1,
		interfaceStructureCategory: 'Category 1',
		added: true,
		changes: {
			numElements: true,
			applicability: {
				previousValue: { id: '1', name: 'Base' },
				currentValue: {
					id: '2119518475782991281',
					name: 'ROBOT_SPEAKER = SPKR_B',
				},
				transactionToken: {
					id: '1215',
					branchId: '2780650236653788489',
				},
			},
			name: {
				previousValue: 'teststructuremodify',
				currentValue: 'teststructuremodify2',
				transactionToken: {
					id: '1219',
					branchId: '2780650236653788489',
				},
			},
			description: {
				previousValue: 'adsfas',
				currentValue: 'changed description',
				transactionToken: {
					id: '1214',
					branchId: '2780650236653788489',
				},
			},
			interfaceMaxSimultaneity: {
				previousValue: '0',
				currentValue: '44',
				transactionToken: {
					id: '1218',
					branchId: '2780650236653788489',
				},
			},
			interfaceStructureCategory: {
				previousValue: 'N/A',
				currentValue: 'Tactical Status',
				transactionToken: {
					id: '1216',
					branchId: '2780650236653788489',
				},
			},
			interfaceTaskFileType: {
				previousValue: '0',
				currentValue: '5',
				transactionToken: {
					id: '1217',
					branchId: '2780650236653788489',
				},
			},
		},
	},
	{
		id: '201365',
		name: 'name',
		nameAbbrev: '',
		elements: [
			{
				id: '1',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
				changes: {
					platformType: {
						previousValue: new PlatformTypeSentinel(),
						currentValue: {
							...new PlatformTypeSentinel(),
							name: 'testingUnits',
							id: '9',
						},
						transactionToken: {
							id: '1322',
							branchId: '2780650236653788489',
						},
					},
					units: {
						previousValue: 'hertz',
						currentValue: 'feet²/second²',
						transactionToken: {
							id: '1322',
							branchId: '2780650236653788489',
						},
					},
				},
			},
		],
		numElements: 1,
		hasElementChanges: true,
		sizeInBytes: 0,
		bytesPerSecondMaximum: 0,
		bytesPerSecondMinimum: 0,
		applicability: { id: '1', name: 'Base' },
		description: 'description',
		interfaceMaxSimultaneity: '0',
		interfaceMinSimultaneity: '1',
		interfaceTaskFileType: 1,
		interfaceStructureCategory: 'Category 1',
		changes: {
			name: {
				previousValue: 'teststructuredelete',
				currentValue: 'teststructuredelete',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			description: {
				previousValue: 'dafda',
				currentValue: 'dafda',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			interfaceMaxSimultaneity: {
				previousValue: '0',
				currentValue: '0',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			interfaceMinSimultaneity: {
				previousValue: '6',
				currentValue: '6',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			interfaceStructureCategory: {
				previousValue: 'N/A',
				currentValue: 'N/A',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			interfaceTaskFileType: {
				previousValue: '0',
				currentValue: '0',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
		},
	},
	{
		id: '201370',
		name: 'name',
		nameAbbrev: '',
		added: true,
		elements: [
			{
				id: '1',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
				changes: {
					platformType: {
						previousValue: new PlatformTypeSentinel(),
						currentValue: {
							...new PlatformTypeSentinel(),
							name: 'testingUnits',
							id: '9',
						},
						transactionToken: {
							id: '1322',
							branchId: '2780650236653788489',
						},
					},
					units: {
						previousValue: 'hertz',
						currentValue: 'feet²/second²',
						transactionToken: {
							id: '1322',
							branchId: '2780650236653788489',
						},
					},
				},
			},
		],
		numElements: 1,
		sizeInBytes: 0,
		hasElementChanges: true,
		bytesPerSecondMaximum: 0,
		bytesPerSecondMinimum: 0,
		applicability: { id: '1', name: 'Base' },
		description: 'description',
		interfaceMaxSimultaneity: '0',
		interfaceMinSimultaneity: '1',
		interfaceTaskFileType: 1,
		interfaceStructureCategory: 'Category 1',
		changes: {
			applicability: {
				previousValue: null,
				currentValue: { id: '1', name: 'Base' },
				transactionToken: {
					id: '1220',
					branchId: '2780650236653788489',
				},
			},
			name: {
				previousValue: null,
				currentValue: 'testaddingstruct',
				transactionToken: {
					id: '1220',
					branchId: '2780650236653788489',
				},
			},
			description: {
				previousValue: null,
				currentValue: 'dfaad',
				transactionToken: {
					id: '1220',
					branchId: '2780650236653788489',
				},
			},
			interfaceMaxSimultaneity: {
				previousValue: null,
				currentValue: '5',
				transactionToken: {
					id: '1220',
					branchId: '2780650236653788489',
				},
			},
			interfaceMinSimultaneity: {
				previousValue: null,
				currentValue: '98',
				transactionToken: {
					id: '1220',
					branchId: '2780650236653788489',
				},
			},
			interfaceStructureCategory: {
				previousValue: null,
				currentValue: 'N/A',
				transactionToken: {
					id: '1220',
					branchId: '2780650236653788489',
				},
			},
			interfaceTaskFileType: {
				previousValue: null,
				currentValue: '0',
				transactionToken: {
					id: '1220',
					branchId: '2780650236653788489',
				},
			},
		},
	},
	{
		id: '201364',
		name: 'name',
		nameAbbrev: '',
		interfaceStructureCategory: 'Category 1',
		deleted: true,
		added: true,
		elements: [
			{
				id: '1',
				name: 'hello',
				description: '',
				notes: '',
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				logicalType: 'enumeration',
				interfaceDefaultValue: '',
				enumLiteral: '',
				deleted: true,
				units: '',
				platformType: new PlatformTypeSentinel(),
				arrayElements: [],
				changes: {
					name: {
						previousValue: 'hello',
						currentValue: '',
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					description: {
						previousValue: '',
						currentValue: '',
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					notes: {
						previousValue: '',
						currentValue: '',
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					platformType: {
						previousValue: new PlatformTypeSentinel(),
						currentValue: new PlatformTypeSentinel(),
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementIndexEnd: {
						previousValue: 1,
						currentValue: 1,
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementIndexStart: {
						previousValue: 0,
						currentValue: 0,
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					interfaceElementAlterable: {
						previousValue: true,
						currentValue: true,
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					applicability: {
						previousValue: undefined,
						currentValue: { id: '1', name: 'Base' },
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
					enumLiteral: {
						previousValue: '',
						currentValue: '',
						transactionToken: {
							id: '1223',
							branchId: '2780650236653788489',
						},
					},
				},
			},
			{
				id: '201351',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
			{
				id: '201360',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
			{
				id: '201371',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
		],
		numElements: 4,
		sizeInBytes: 0,
		bytesPerSecondMaximum: 0,
		bytesPerSecondMinimum: 0,
		applicability: { id: '1', name: 'Base' },
		description: 'description',
		interfaceMaxSimultaneity: '0',
		interfaceMinSimultaneity: '1',
		interfaceTaskFileType: 1,
		changes: {
			numElements: true,
			name: {
				previousValue: 'name',
				currentValue: '',
				transactionToken: {
					id: '1230',
					branchId: '2780650236653788489',
				},
			},
			description: {
				previousValue: 'description',
				currentValue: '',
				transactionToken: {
					id: '1230',
					branchId: '2780650236653788489',
				},
			},
			interfaceMaxSimultaneity: {
				previousValue: '0',
				currentValue: '',
				transactionToken: {
					id: '1230',
					branchId: '2780650236653788489',
				},
			},
			interfaceMinSimultaneity: {
				previousValue: '1',
				currentValue: '',
				transactionToken: {
					id: '1230',
					branchId: '2780650236653788489',
				},
			},
			interfaceTaskFileType: {
				previousValue: 1,
				currentValue: '',
				transactionToken: {
					id: '1230',
					branchId: '2780650236653788489',
				},
			},
			interfaceStructureCategory: {
				previousValue: 'Category 1',
				currentValue: '',
				transactionToken: {
					id: '1230',
					branchId: '2780650236653788489',
				},
			},
			applicability: {
				previousValue: { id: '1', name: 'Base' },
				currentValue: '',
				transactionToken: {
					id: '1230',
					branchId: '2780650236653788489',
				},
			},
		},
	},
	{
		id: '201364',
		name: 'name',
		nameAbbrev: '',
		interfaceStructureCategory: 'Category 1',
		deleted: true,
		added: false,
		elements: [
			{
				id: '201351',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
			{
				id: '201371',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
			{
				id: '201360',
				name: 'name2',
				description: 'description2',
				notes: 'notes',
				interfaceElementIndexEnd: 1,
				interfaceElementIndexStart: 0,
				interfaceElementAlterable: true,
				interfaceElementArrayHeader: false,
				interfaceElementWriteArrayHeaderName: false,
				interfaceDefaultValue: '',
				enumLiteral: '',
				platformType: {
					...new PlatformTypeSentinel(),
					name: 'boolean',
					id: '9',
				},
				arrayElements: [],
				units: '',
			},
		],
		numElements: 1,
		sizeInBytes: 0,
		bytesPerSecondMaximum: 0,
		bytesPerSecondMinimum: 0,
		applicability: { id: '1', name: 'Base' },
		description: 'description',
		interfaceMaxSimultaneity: '0',
		interfaceMinSimultaneity: '1',
		interfaceTaskFileType: 1,
		changes: {
			numElements: true,
			name: {
				previousValue: 'name',
				currentValue: '',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			description: {
				previousValue: 'description',
				currentValue: '',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			interfaceMaxSimultaneity: {
				previousValue: '0',
				currentValue: '',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			interfaceMinSimultaneity: {
				previousValue: '1',
				currentValue: '',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			interfaceTaskFileType: {
				previousValue: 1,
				currentValue: '',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			interfaceStructureCategory: {
				previousValue: 'Category 1',
				currentValue: '',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
			applicability: {
				previousValue: { id: '1', name: 'Base' },
				currentValue: '',
				transactionToken: {
					id: '1213',
					branchId: '2780650236653788489',
				},
			},
		},
	},
];
export const structureRepeatingWithChanges: Partial<structureWithChanges> = {
	id: '201364',
	name: 'name',
	elements: [
		{
			id: '1',
			name: 'hello',
			description: '',
			notes: '',
			interfaceElementAlterable: true,
			interfaceElementArrayHeader: false,
			interfaceElementWriteArrayHeaderName: false,
			interfaceElementIndexEnd: 1,
			interfaceElementIndexStart: 0,
			logicalType: 'enumeration',
			interfaceDefaultValue: '',
			enumLiteral: '',
			deleted: true,
			units: '',
			platformType: new PlatformTypeSentinel(),
			arrayElements: [],
			changes: {
				name: {
					previousValue: 'hello',
					currentValue: '',
					transactionToken: {
						id: '1223',
						branchId: '2780650236653788489',
					},
				},
				description: {
					previousValue: '',
					currentValue: '',
					transactionToken: {
						id: '1223',
						branchId: '2780650236653788489',
					},
				},
				notes: {
					previousValue: '',
					currentValue: '',
					transactionToken: {
						id: '1223',
						branchId: '2780650236653788489',
					},
				},
				platformType: {
					previousValue: new PlatformTypeSentinel(),
					currentValue: new PlatformTypeSentinel(),
					transactionToken: {
						id: '1223',
						branchId: '2780650236653788489',
					},
				},
				interfaceElementIndexEnd: {
					previousValue: 1,
					currentValue: 1,
					transactionToken: {
						id: '1223',
						branchId: '2780650236653788489',
					},
				},
				interfaceElementIndexStart: {
					previousValue: 0,
					currentValue: 0,
					transactionToken: {
						id: '1223',
						branchId: '2780650236653788489',
					},
				},
				interfaceElementAlterable: {
					previousValue: true,
					currentValue: true,
					transactionToken: {
						id: '1223',
						branchId: '2780650236653788489',
					},
				},
				applicability: {
					previousValue: undefined,
					currentValue: { id: '1', name: 'Base' },
					transactionToken: {
						id: '1223',
						branchId: '2780650236653788489',
					},
				},
				enumLiteral: {
					previousValue: '',
					currentValue: '',
					transactionToken: {
						id: '1223',
						branchId: '2780650236653788489',
					},
				},
			},
		},
		{
			id: '201351',
			name: 'name2',
			description: 'description2',
			notes: 'notes',
			interfaceElementIndexEnd: 1,
			interfaceElementIndexStart: 0,
			interfaceElementAlterable: true,
			interfaceElementArrayHeader: false,
			interfaceElementWriteArrayHeaderName: false,
			platformType: {
				...new PlatformTypeSentinel(),
				name: 'boolean',
				id: '9',
			},
			arrayElements: [],
			interfaceDefaultValue: '',
			enumLiteral: '',
			units: '',
			added: true,
			changes: {
				applicability: {
					previousValue: { id: '1', name: 'Base' },
					currentValue: { id: '1', name: 'Base' },
					transactionToken: {
						id: '1222',
						branchId: '2780650236653788489',
					},
				},
				interfaceElementAlterable: {
					previousValue: true,
					currentValue: false,
					transactionToken: {
						id: '1227',
						branchId: '2780650236653788489',
					},
				},
				platformType: {
					previousValue: {
						...new PlatformTypeSentinel(),
						name: 'Name',
						id: '0', //? don't know why this is getting this value
						interfaceLogicalType: '',
						interfacePlatformType2sComplement: false,
						interfacePlatformTypeAnalogAccuracy: '',
						interfacePlatformTypeCompRate: '',
						interfaceDefaultValue: '',
						interfacePlatformTypeUnits: '',
						interfacePlatformTypeValidRangeDescription: '',
						applicability: {
							id: '1',
							name: 'Base',
						},
						interfacePlatformTypeBitSize: '8',
						interfacePlatformTypeBitsResolution: '8',
						interfacePlatformTypeMaxval: '1',
						interfacePlatformTypeMinval: '1',
						interfacePlatformTypeMsbValue: '0',
					},
					currentValue: {
						...new PlatformTypeSentinel(),
						name: 'Name',
						id: '9',
					},
					transactionToken: {
						id: '1226',
						branchId: '2780650236653788489',
					},
				},
			},
		},
		{
			id: '201360',
			name: 'name2',
			description: 'description2',
			notes: 'notes',
			interfaceElementIndexEnd: 1,
			interfaceElementIndexStart: 0,
			interfaceElementAlterable: true,
			interfaceElementArrayHeader: false,
			interfaceElementWriteArrayHeaderName: false,
			platformType: {
				...new PlatformTypeSentinel(),
				name: 'boolean',
				id: '9',
			},
			arrayElements: [],
			interfaceDefaultValue: '',
			enumLiteral: '',
			units: '',
			changes: {
				interfaceElementIndexEnd: {
					previousValue: 7,
					currentValue: 9,
					transactionToken: {
						id: '1229',
						branchId: '2780650236653788489',
					},
				},
			},
		},
		{
			id: '201371',
			name: 'name2',
			description: 'description2',
			notes: 'notes',
			interfaceElementIndexEnd: 1,
			interfaceElementIndexStart: 0,
			interfaceElementAlterable: true,
			interfaceElementArrayHeader: false,
			interfaceElementWriteArrayHeaderName: false,
			platformType: {
				...new PlatformTypeSentinel(),
				name: 'boolean',
				id: '9',
			},
			arrayElements: [],
			interfaceDefaultValue: '',
			enumLiteral: '',
			units: '',
			changes: {
				applicability: {
					previousValue: { id: '1', name: 'Base' },
					currentValue: { id: '1', name: 'Base' },
					transactionToken: {
						id: '1225',
						branchId: '2780650236653788489',
					},
				},
				name: {
					previousValue: '',
					currentValue: 'testaddingelement',
					transactionToken: {
						id: '1225',
						branchId: '2780650236653788489',
					},
				},
				description: {
					previousValue: '',
					currentValue: 'dsfads',
					transactionToken: {
						id: '1225',
						branchId: '2780650236653788489',
					},
				},
				interfaceElementAlterable: {
					previousValue: false,
					currentValue: true,
					transactionToken: {
						id: '1225',
						branchId: '2780650236653788489',
					},
				},
				platformType: {
					previousValue: new PlatformTypeSentinel(),
					currentValue: {
						...new PlatformTypeSentinel(),
						name: 'Name',
						id: '9',
					},
					transactionToken: {
						id: '1225',
						branchId: '2780650236653788489',
					},
				},
			},
		},
	],
	numElements: 2,
	hasElementChanges: true,
	sizeInBytes: 0,
	bytesPerSecondMaximum: 0,
	bytesPerSecondMinimum: 0,
	applicability: {
		id: '2119518475782991281',
		name: 'ROBOT_SPEAKER = SPKR_B',
	},
	description: 'description',
	interfaceMaxSimultaneity: '0',
	interfaceMinSimultaneity: '1',
	interfaceTaskFileType: 1,
	interfaceStructureCategory: 'Category 1',
	added: true,
	changes: {
		numElements: true,
		applicability: {
			previousValue: { id: '1', name: 'Base' },
			currentValue: {
				id: '2119518475782991281',
				name: 'ROBOT_SPEAKER = SPKR_B',
			},
			transactionToken: { id: '1215', branchId: '2780650236653788489' },
		},
		name: {
			previousValue: 'teststructuremodify',
			currentValue: 'teststructuremodify2',
			transactionToken: { id: '1219', branchId: '2780650236653788489' },
		},
		description: {
			previousValue: 'adsfas',
			currentValue: 'changed description',
			transactionToken: { id: '1214', branchId: '2780650236653788489' },
		},
		interfaceMaxSimultaneity: {
			previousValue: '0',
			currentValue: '44',
			transactionToken: { id: '1218', branchId: '2780650236653788489' },
		},
		interfaceStructureCategory: {
			previousValue: 'N/A',
			currentValue: 'Tactical Status',
			transactionToken: { id: '1216', branchId: '2780650236653788489' },
		},
		interfaceTaskFileType: {
			previousValue: '0',
			currentValue: '5',
			transactionToken: { id: '1217', branchId: '2780650236653788489' },
		},
	},
};
export const structuresMockWithChanges: structureWithChanges = {
	id: '1',
	name: 'name',
	nameAbbrev: '',
	elements: [
		{
			id: '1',
			name: 'name2',
			description: 'description2',
			notes: 'notes',
			interfaceElementIndexEnd: 1,
			interfaceElementIndexStart: 0,
			interfaceElementAlterable: true,
			interfaceElementArrayHeader: false,
			interfaceElementWriteArrayHeaderName: false,
			platformType: {
				...new PlatformTypeSentinel(),
				name: 'boolean',
				id: '9',
			},
			arrayElements: [],
			interfaceDefaultValue: '',
			enumLiteral: '',
			units: '',
		},
	],
	numElements: 1,
	sizeInBytes: 0,
	bytesPerSecondMaximum: 0,
	bytesPerSecondMinimum: 0,
	applicability: { id: '1', name: 'Base' },
	description: 'description',
	interfaceMaxSimultaneity: '0',
	interfaceMinSimultaneity: '1',
	interfaceTaskFileType: 1,
	interfaceStructureCategory: 'Category 1',
	added: false,
	deleted: false,
	hasElementChanges: false,
	autogenerated: false,
	changes: {
		name: {
			previousValue: 'a',
			currentValue: 'b',
			transactionToken: { id: '1234', branchId: '8' },
		},
	},
	incorrectlySized: false,
};

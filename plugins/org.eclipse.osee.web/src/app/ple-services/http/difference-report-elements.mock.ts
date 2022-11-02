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
import { elementDiffItem } from 'src/app/ple/messaging/shared/types/DifferenceReport';
export const elementDiffsMock: elementDiffItem[] = [
	{
		id: '200435',
		name: 'Element Added',
		beginByte: 0,
		beginWord: 0,
		applicability: {
			id: '1',
			name: 'Base',
		},
		logicalType: 'double',
		autogenerated: false,
		units: 'Feet^2',
		description: 'Added this element to Structure1',
		interfacePlatformTypeDescription: '',
		interfacePlatformTypeDefaultValue: '0',
		interfacePlatformTypeMaxval: '1000',
		interfacePlatformTypeMinval: '0',
		interfaceElementIndexStart: 0,
		elementSizeInBytes: 8,
		interfaceElementAlterable: true,
		interfaceElementIndexEnd: 0,
		enumLiteral: '',
		elementSizeInBits: 64,
		platformTypeName2: 'Double1',
		notes: '',
		platformTypeId: 200424,
		endByte: 3,
		endWord: 1,
		diffInfo: {
			added: true,
			deleted: false,
			fieldsChanged: {},
			url: {
				label: '',
				url: '',
			},
		},
	},
	{
		id: '200413',
		name: 'Element2',
		beginByte: 0,
		beginWord: 0,
		applicability: {
			id: '1',
			name: 'Base',
		},
		logicalType: 'double',
		autogenerated: false,
		units: 'Feet^2',
		description: 'This is element 2',
		interfacePlatformTypeDescription: '',
		interfacePlatformTypeDefaultValue: '0',
		interfacePlatformTypeMaxval: '1000',
		interfacePlatformTypeMinval: '0',
		interfaceElementIndexStart: 0,
		elementSizeInBytes: 8,
		interfaceElementAlterable: true,
		interfaceElementIndexEnd: 0,
		enumLiteral: '',
		elementSizeInBits: 64,
		platformTypeName2: 'Double1',
		notes: 'Changed from int to double',
		platformTypeId: 200424,
		endByte: 3,
		endWord: 1,
		diffInfo: {
			added: false,
			deleted: false,
			fieldsChanged: {
				logicalType: 'integer',
				elementSizeInBits: '32',
				interfacePlatformTypeMaxval: '199',
				units: 'Nm',
				notes: '',
			},
			url: {
				label: '',
				url: '',
			},
		},
	},
	{
		id: '200415',
		name: 'Element D',
		beginByte: 0,
		beginWord: 0,
		applicability: {
			id: '1',
			name: 'Base',
		},
		logicalType: 'integer',
		autogenerated: false,
		units: 'Nm',
		description: 'Delete this element',
		interfacePlatformTypeDescription: 'A 32 bit integer',
		interfacePlatformTypeDefaultValue: '0',
		interfacePlatformTypeMaxval: '199',
		interfacePlatformTypeMinval: '0',
		interfaceElementIndexStart: 0,
		elementSizeInBytes: 4,
		interfaceElementAlterable: false,
		interfaceElementIndexEnd: 0,
		enumLiteral: '',
		elementSizeInBits: 32,
		platformTypeName2: 'Integer1',
		notes: 'To be deleted',
		platformTypeId: 200411,
		endByte: 3,
		endWord: 0,
		diffInfo: {
			added: false,
			deleted: true,
			fieldsChanged: {},
			url: {
				label: '',
				url: '',
			},
		},
	},
	{
		id: '200434',
		name: 'Element A',
		beginByte: 0,
		beginWord: 0,
		applicability: {
			id: '1',
			name: 'Base',
		},
		logicalType: 'integer',
		autogenerated: false,
		units: 'Nm',
		description: 'Added this element',
		interfacePlatformTypeDescription: 'A 32 bit integer',
		interfacePlatformTypeDefaultValue: '0',
		interfacePlatformTypeMaxval: '199',
		interfacePlatformTypeMinval: '0',
		interfaceElementIndexStart: 0,
		elementSizeInBytes: 4,
		interfaceElementAlterable: true,
		interfaceElementIndexEnd: 0,
		enumLiteral: '',
		elementSizeInBits: 32,
		platformTypeName2: 'Integer1',
		notes: '',
		platformTypeId: 200411,
		endByte: 3,
		endWord: 0,
		diffInfo: {
			added: true,
			deleted: false,
			fieldsChanged: {},
			url: {
				label: '',
				url: '',
			},
		},
	},
	{
		id: '200416',
		name: 'Element Array',
		beginByte: 0,
		beginWord: 0,
		applicability: {
			id: '1',
			name: 'Base',
		},
		logicalType: 'integer',
		autogenerated: false,
		units: 'Nm',
		description: 'This is an element array (Edit)',
		interfacePlatformTypeDescription: 'A 32 bit integer',
		interfacePlatformTypeDefaultValue: '0',
		interfacePlatformTypeMaxval: '199',
		interfacePlatformTypeMinval: '0',
		interfaceElementIndexStart: 0,
		elementSizeInBytes: 28,
		interfaceElementAlterable: false,
		interfaceElementIndexEnd: 6,
		enumLiteral: '',
		elementSizeInBits: 224,
		platformTypeName2: 'Integer1',
		notes: '',
		platformTypeId: 200411,
		endByte: 3,
		endWord: 6,
		diffInfo: {
			added: false,
			deleted: false,
			fieldsChanged: {
				interfaceElementIndexEnd: '5',
				description: 'This is an element array',
			},
			url: {
				label: '',
				url: '',
			},
		},
	},
	{
		id: '200412',
		name: 'Element1',
		beginByte: 0,
		beginWord: 0,
		applicability: {
			id: '1',
			name: 'Base',
		},
		logicalType: 'integer',
		autogenerated: false,
		units: 'Nm',
		description: 'This is element 1 (Edited)',
		interfacePlatformTypeDescription: 'A 32 bit integer',
		interfacePlatformTypeDefaultValue: '0',
		interfacePlatformTypeMaxval: '199',
		interfacePlatformTypeMinval: '0',
		interfaceElementIndexStart: 0,
		elementSizeInBytes: 4,
		interfaceElementAlterable: false,
		interfaceElementIndexEnd: 0,
		enumLiteral: '',
		elementSizeInBits: 32,
		platformTypeName2: 'Integer1',
		notes: 'This is a note',
		platformTypeId: 200411,
		endByte: 3,
		endWord: 0,
		diffInfo: {
			added: false,
			deleted: false,
			fieldsChanged: {
				description: 'This is element 1',
			},
			url: {
				label: '',
				url: '',
			},
		},
	},
	{
		id: '200427',
		name: 'Element Edit PT',
		beginByte: 0,
		beginWord: 0,
		applicability: {
			id: '1',
			name: 'Base',
		},
		logicalType: 'float',
		autogenerated: false,
		units: 'dB',
		description: "Edit the platform type's values",
		interfacePlatformTypeDescription: '',
		interfacePlatformTypeDefaultValue: '0',
		interfacePlatformTypeMaxval: '150',
		interfacePlatformTypeMinval: '0',
		interfaceElementIndexStart: 0,
		elementSizeInBytes: 4,
		interfaceElementAlterable: true,
		interfaceElementIndexEnd: 0,
		enumLiteral: '',
		elementSizeInBits: 32,
		platformTypeName2: 'FLOAT1',
		notes: '',
		platformTypeId: 200426,
		endByte: 3,
		endWord: 0,
		diffInfo: {
			added: false,
			deleted: false,
			fieldsChanged: {
				interfacePlatformTypeMaxval: '100',
			},
			url: {
				label: '',
				url: '',
			},
		},
	},
	{
		id: '200423',
		name: 'Element Enum',
		beginByte: 0,
		beginWord: 0,
		applicability: {
			id: '1',
			name: 'Base',
		},
		logicalType: 'enumeration',
		autogenerated: false,
		units: '',
		description: 'Testing enums',
		interfacePlatformTypeDescription: '',
		interfacePlatformTypeDefaultValue: '',
		interfacePlatformTypeMaxval: '',
		interfacePlatformTypeMinval: '',
		interfaceElementIndexStart: 0,
		elementSizeInBytes: 4,
		interfaceElementAlterable: true,
		interfaceElementIndexEnd: 0,
		enumLiteral: '',
		elementSizeInBits: 32,
		platformTypeName2: 'Enum1',
		notes: '',
		platformTypeId: 200417,
		endByte: 3,
		endWord: 0,
		diffInfo: {
			added: false,
			deleted: false,
			fieldsChanged: {
				enumeration: '',
			},
			url: {
				label: '',
				url: '',
			},
		},
		enumeration:
			'0=OPTION 1 [Base]; 1=OPTION 2 [Base]; 2=OPTION 3 [Base]; 3=OPTION 4 [Base]; 4=OPTION 5 [Base]; ',
	},
];

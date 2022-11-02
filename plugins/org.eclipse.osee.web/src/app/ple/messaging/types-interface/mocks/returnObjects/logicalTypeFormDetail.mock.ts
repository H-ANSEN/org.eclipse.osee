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
import { logicalTypeFormDetail } from '../../../shared/types/logicaltype';

export const logicalTypeFormDetailMock: logicalTypeFormDetail = {
	fields: [
		{
			name: 'InterfacePlatformTypeBitSize',
			attributeTypeId: '123',
			attributeType: 'InterfacePlatformTypeBitSize',
			editable: true,
			required: true,
			defaultValue: '8',
			value: '8',
		},
	],
	id: '',
	name: 'enumeration',
	idString: '',
	idIntValue: 0,
};

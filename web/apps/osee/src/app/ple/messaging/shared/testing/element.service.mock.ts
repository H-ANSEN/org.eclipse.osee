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
import type { element } from '@osee/messaging/shared/types';
import { transaction, relation } from '@osee/shared/types';
import {
	transactionMock,
	transactionResultMock,
} from '@osee/shared/transactions/testing';
import { of } from 'rxjs';
import { ElementService } from '../services/http/element.service';
import { elementsMock } from './element.response.mock';

export const elementServiceMock: Partial<ElementService> = {
	changeElement(body: Partial<element>, branchId: string) {
		return of(transactionMock);
	},
	performMutation(body: transaction) {
		return of(transactionResultMock);
	},
	createStructureRelation(structureId: string) {
		return of({
			typeName: 'Interface Structure Content',
			sideA: '10',
		});
	},
	createPlatformTypeRelation(platformTypeId: string) {
		return of({
			typeName: 'Interface Element Platform Type',
			sideB: '10',
		});
	},
	createElement(
		body: Partial<element>,
		branchId: string,
		relations: relation[]
	) {
		return of(transactionMock);
	},
	addRelation(
		branchId: string,
		relation: relation,
		transaction?: transaction
	) {
		return of(transactionMock);
	},
	deleteRelation(
		branchId: string,
		relation: relation,
		transaction?: transaction
	) {
		return of(transactionMock);
	},
	getElement(
		branchId: string,
		messageId: string,
		subMessageId: string,
		structureId: string,
		elementId: string,
		connectionId: string
	) {
		return of(elementsMock[0]);
	},
	getFilteredElements(branchId: string, filter: string) {
		return of(elementsMock);
	},
	deleteElement(branchId: string, elementId: string) {
		return of(transactionMock);
	},
};

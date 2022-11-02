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
import { of } from 'rxjs';
import { relation, transaction } from 'src/app/transactions/transaction';
import {
	transactionMock,
	transactionResultMock,
} from 'src/app/transactions/transaction.mock';
import { response } from '../../../connection-view/mocks/Response.mock';
import { ElementService } from '../../../shared/services/http/element.service';
import { element } from '../../../shared/types/element';
import { elementsMock } from '../../../shared/mocks/element.mock';

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

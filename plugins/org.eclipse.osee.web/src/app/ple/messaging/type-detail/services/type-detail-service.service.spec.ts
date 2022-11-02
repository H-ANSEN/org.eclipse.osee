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
import { TestBed } from '@angular/core/testing';

import { TypeDetailService } from './type-detail-service.service';

describe('TypeDetailServiceService', () => {
	let service: TypeDetailService;

	beforeEach(() => {
		TestBed.configureTestingModule({});
		service = TestBed.inject(TypeDetailService);
	});

	it('should be created', () => {
		expect(service).toBeTruthy();
	});
});

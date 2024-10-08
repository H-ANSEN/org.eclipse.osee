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
	HttpClientTestingModule,
	HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TransactionService } from '@osee/shared/transactions';
import {
	transactionServiceMock,
	transactionResultMock,
} from '@osee/shared/transactions/testing';
import { TestScheduler } from 'rxjs/testing';
import {
	importOptionsMock,
	importSummaryMock,
} from '../testing/import.response.mock';
import { ImportHttpService } from './import-http.service';
import { importHttpServiceMock } from './import-http.service.mock';

import { ImportService } from './import.service';

describe('ImportService', () => {
	let service: ImportService;
	let scheduler: TestScheduler;
	let httpTestingController: HttpTestingController;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [HttpClientTestingModule],
			providers: [
				{
					provide: TransactionService,
					useValue: transactionServiceMock,
				},
				{ provide: ImportHttpService, useValue: importHttpServiceMock },
			],
		});
		service = TestBed.inject(ImportService);
		httpTestingController = TestBed.inject(HttpTestingController);
	});

	beforeEach(
		() =>
			(scheduler = new TestScheduler((actual, expected) => {
				expect(actual).toEqual(expected);
			}))
	);

	it('should be created', () => {
		expect(service).toBeTruthy();
	});

	it('should send transaction', () => {
		scheduler.run(({ expectObservable, cold }) => {
			// The summary request should not be sent until an import option and file are selected
			service.BranchId = '10';
			service.SelectedImportOption = importOptionsMock[0];
			service.ImportFile = new File([], 'testFile.xlsx');
			cold('-a').subscribe(() => (service.ImportInProgress = true));
			cold('-a').subscribe(() => (service.toggleDone = true));
			expectObservable(service.sendTransaction).toBe('-(a|)', {
				a: transactionResultMock,
			});
		});
	});

	it('should get import summary from xlsx', () => {
		scheduler.run(({ cold }) => {
			// The summary request should not be sent until an import option and file are selected
			service.BranchId = '10';
			service.SelectedImportOption = importOptionsMock[0];
			service.ImportFile = new File([], 'testFile.xlsx');
			cold('-a').subscribe(() => (service.ImportInProgress = true));
			scheduler
				.expectObservable(service.importSummary)
				.toBe('-a', { a: importSummaryMock });
		});
	});

	it('should get import summary from zip', () => {
		scheduler.run(({ cold }) => {
			// The summary request should not be sent until an import option and file are selected
			service.BranchId = '10';
			service.SelectedImportOption = importOptionsMock[0];
			service.ImportFile = new File([], 'testFile.zip');
			cold('-a').subscribe(() => (service.ImportInProgress = true));
			scheduler
				.expectObservable(service.importSummary)
				.toBe('-a', { a: importSummaryMock });
		});
	});

	it('should get import summary from json', () => {
		scheduler.run(({ cold }) => {
			// The summary request should not be sent until an import option and file are selected
			service.BranchId = '10';
			service.SelectedImportOption = importOptionsMock[0];
			service.ImportFile = new File([], 'testFile.json');
			cold('-a').subscribe(() => (service.ImportInProgress = true));
			scheduler
				.expectObservable(service.importSummary)
				.toBe('-a', { a: importSummaryMock });
		});
	});

	it('should reset the state of the service', () => {
		scheduler.run(({ cold }) => {
			service.ImportFile = new File([], 'Test');
			service.ImportSuccess = true;
			service.SelectedImportOption = importOptionsMock[0];
			cold('-a').subscribe(() => (service.ImportInProgress = true));

			scheduler
				.expectObservable(service.importInProgress)
				.toBe('-a', { a: true });

			service.reset();

			scheduler
				.expectObservable(service.importFile)
				.toBe('a', { a: undefined });
			scheduler
				.expectObservable(service.importSuccess)
				.toBe('a', { a: undefined });
			scheduler
				.expectObservable(service.selectedImportOption)
				.toBe('a', { a: undefined });

			// Can't get the test to verify that importInProgress is
			// being set to false, but works in app.
		});
	});
});

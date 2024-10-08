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
import {
	HttpClientTestingModule,
	HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TestScheduler } from 'rxjs/testing';
import { BranchInfoService } from '@osee/shared/services';
import { MessageUiService } from './messages-ui.service';
import { ApplicabilityListService } from '../../../../../shared/services/ple_aware/http/applicability-list.service';
import { MessagesService } from '../http/messages.service';
import { MimPreferencesService } from '../http/mim-preferences.service';
import { SubMessagesService } from '../http/sub-messages.service';
import { CurrentMessagesService } from './current-messages.service';
import {
	messageServiceMock,
	subMessageServiceMock,
	MimPreferencesServiceMock,
	messagesMock,
	subMessagesMock,
	connectionNodesMock,
	MimPreferencesMock,
	warningDialogServiceMock,
} from '@osee/messaging/shared/testing';
import type { message, messageWithChanges } from '@osee/messaging/shared/types';
import { transactionResultMock } from '@osee/shared/transactions/testing';
import {
	applicabilityListServiceMock,
	BranchInfoServiceMock,
	changeReportMock,
} from '@osee/shared/testing';
import { WarningDialogService } from './warning-dialog.service';

describe('CurrentMessagesService', () => {
	let service: CurrentMessagesService;
	let httpTestingController: HttpTestingController;
	let uiService: MessageUiService;
	let scheduler: TestScheduler;

	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [
				{ provide: MessagesService, useValue: messageServiceMock },
				{
					provide: SubMessagesService,
					useValue: subMessageServiceMock,
				},
				{
					provide: ApplicabilityListService,
					useValue: applicabilityListServiceMock,
				},
				{
					provide: MimPreferencesService,
					useValue: MimPreferencesServiceMock,
				},
				{ provide: BranchInfoService, useValue: BranchInfoServiceMock },
				{
					provide: WarningDialogService,
					useValue: warningDialogServiceMock,
				},
				{ provide: MessageUiService },
				CurrentMessagesService,
			],
			imports: [HttpClientTestingModule],
		});
		service = TestBed.inject(CurrentMessagesService);
		httpTestingController = TestBed.inject(HttpTestingController);
		uiService = TestBed.inject(MessageUiService);
	});

	beforeEach(
		() =>
			(scheduler = new TestScheduler((actual, expected) => {
				expect(actual).toEqual(expected);
			}))
	);
	describe('no diffs', () => {
		beforeEach(() => {
			uiService.DiffMode = false;
			service.difference = [];
		});
		it('should be created', () => {
			expect(service).toBeTruthy();
		});

		it('should fetch filtered messages', () => {
			scheduler.run(() => {
				service.filter = 'filter';
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: messagesMock };
				let expectedMarble = '500ms a';
				scheduler
					.expectObservable(service.messages)
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('should update the list of all messages twice', () => {
			scheduler.run(({ cold }) => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: messagesMock };
				let expectedMarble = 'a 100ms a';
				let delayMarble = '-a';
				cold(delayMarble).subscribe(
					() => (uiService.updateMessages = true)
				);
				scheduler
					.expectObservable(service.allMessages)
					.toBe(expectedMarble, expectedObservable);
			});
		});
		it('should partially update a message', () => {
			scheduler.run(() => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(service.partialUpdateMessage({}))
					.toBe(expectedMarble, expectedObservable);
			});
		});
		it('should partially update a sub message', () => {
			scheduler.run(() => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(service.partialUpdateSubMessage({}, '10'))
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('should relate a sub message', () => {
			scheduler.run(() => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(service.relateSubMessage('15', '10'))
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('should create a sub message', () => {
			scheduler.run(() => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(
						service.createSubMessage(
							messagesMock[0].subMessages[0],
							'10'
						)
					)
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('should create a message', () => {
			scheduler.run(() => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(
						service.createMessage(
							[{ id: '100', name: 'Node1' }],
							[{ id: '101', name: 'Node2' }],
							messagesMock[0]
						)
					)
					.toBe(expectedMarble, expectedObservable);
			});
		});
		it('should delete a message', () => {
			scheduler.run(() => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(service.deleteMessage(messagesMock[0].id))
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('should remove a message', () => {
			scheduler.run(() => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(service.removeMessage(messagesMock[0].id))
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('should delete a submessage', () => {
			scheduler.run(() => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(
						service.deleteSubMessage(subMessagesMock[0].id)
					)
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('should remove a submessage', () => {
			scheduler.run(() => {
				service.branch = '10';
				service.connection = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(
						service.removeSubMessage(
							subMessagesMock[0].id,
							messagesMock[0].id
						)
					)
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('should fetch connection nodes', () => {
			scheduler.run(() => {
				const expectedObservable = { a: connectionNodesMock };
				const expectedMarble = 'a';
				service.branch = '10';
				service.connection = '10';
				scheduler
					.expectObservable(service.connectionNodes)
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('should fetch preferences', () => {
			scheduler.run(() => {
				const expectedFilterValues = { a: MimPreferencesMock };
				const expectedMarble = 'a';
				service.branch = '10';
				scheduler
					.expectObservable(service.preferences)
					.toBe(expectedMarble, expectedFilterValues);
			});
		});

		it('should update user preferences', () => {
			scheduler.run(() => {
				service.branch = '10';
				let expectedObservable = { a: transactionResultMock };
				let expectedMarble = '(a|)';
				scheduler
					.expectObservable(
						service.updatePreferences({
							branchId: '10',
							allowedHeaders1: [
								'name',
								'description',
								'applicability',
							],
							allowedHeaders2: [
								'name',
								'description',
								'applicability',
							],
							allHeaders1: ['name', 'description'],
							allHeaders2: ['name', 'description'],
							editable: true,
							headers1Label: '',
							headers2Label: '',
							headersTableActive: false,
							wordWrap: false,
						})
					)
					.toBe(expectedMarble, expectedObservable);
			});
		});

		it('done should emit', () => {
			scheduler.run(({ expectObservable, cold }) => {
				const expectedFilterValues = {
					a: true,
					b: undefined,
					c: false,
				};
				const expectedMarble = '-(a)';
				let delayMarble = '-a';
				cold(delayMarble).subscribe(() => (service.toggleDone = true));
				expectObservable(service.done).toBe(
					expectedMarble,
					expectedFilterValues
				);
			});
		});
	});

	it('should get a connection path', () => {
		scheduler.run(({ expectObservable }) => {
			service.branchId = '10';
			service.branchType = 'working';
			expectObservable(service.connectionsRoute).toBe('a', {
				a: '/ple/messaging/connections/working/10',
			});
		});
	});

	describe('diffs', () => {
		beforeEach(() => {});
		it('should get messages,submessages with differences', () => {
			scheduler.run(({ expectObservable }) => {
				service.difference = changeReportMock;
				service.DiffMode = true;
				service.branch = '10';
				const expectedValues: {
					a: (message | messageWithChanges)[];
					b: (message | messageWithChanges)[];
					c: (message | messageWithChanges)[];
					d: (message | messageWithChanges)[];
					e: (message | messageWithChanges)[];
					f: (message | messageWithChanges)[];
				} = {
					a: [
						{
							id: '0',
							name: 'message0',
							description: 'description',
							subMessages: [
								{
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '0',
									id: '1',
									autogenerated: false,
									applicability: {
										id: '1',
										name: 'Base',
									},
									changes: {
										name: {
											previousValue: '',
											currentValue: 'submessage0',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
								},
							],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '0',
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							applicability: {
								id: '1',
								name: 'Base',
							},
							changes: {
								name: {
									previousValue: '',
									currentValue: 'name',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
							},
						},
					],
					b: [
						{
							id: '0',
							name: 'message0',
							description: 'description',
							subMessages: [
								{
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '0',
									id: '1',
									autogenerated: false,
									applicability: {
										id: '1',
										name: 'Base',
									},
									changes: {
										name: {
											previousValue: '',
											currentValue: 'submessage0',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
								},
							],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '0',
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							applicability: {
								id: '1',
								name: 'Base',
							},
							changes: {
								name: {
									previousValue: '',
									currentValue: 'name',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
							},
						},
						{
							id: '1',
							name: 'message1',
							description: 'description',
							subMessages: [],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '1',
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							applicability: {
								id: '1',
								name: 'Base',
							},
						},
					],
					c: [
						{
							id: '0',
							name: 'message0',
							description: 'description',
							subMessages: [
								{
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '0',
									id: '1',
									autogenerated: false,
									applicability: {
										id: '1',
										name: 'Base',
									},
									changes: {
										name: {
											previousValue: '',
											currentValue: 'submessage0',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
								},
							],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '0',
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							applicability: {
								id: '1',
								name: 'Base',
							},
							changes: {
								name: {
									previousValue: '',
									currentValue: 'name',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
							},
						},
						{
							id: '1',
							name: 'message1',
							description: 'description',
							subMessages: [],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '1',
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							applicability: {
								id: '1',
								name: 'Base',
							},
						},
						{
							id: '201289',
							name: 'message4',
							description: 'description',
							subMessages: [
								{
									added: false,
									deleted: true,
									changes: {},
									id: '201300',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: { id: '1', name: 'Base' },
								},
								{
									added: false,
									deleted: true,
									changes: {
										name: {
											previousValue: 'test submessage 6',
											currentValue: 'test submessage 6',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: '762',
											currentValue: '762',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: 'uiop',
											currentValue: 'uiop',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									id: '201302',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: { id: '1', name: 'Base' },
								},
								{
									added: false,
									deleted: true,
									changes: {
										name: {
											previousValue: null,
											currentValue: 'test submessage 8',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: null,
											currentValue: '85',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: null,
											currentValue: 'dfd',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									id: '201305',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: { id: '1', name: 'Base' },
								},
							],
							interfaceMessageRate: '5',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '2',
							added: false,
							hasSubMessageChanges: true,
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							changes: {},
						},
					],
					d: [
						{
							id: '0',
							name: 'message0',
							description: 'description',
							subMessages: [
								{
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '0',
									id: '1',
									autogenerated: false,
									applicability: {
										id: '1',
										name: 'Base',
									},
									changes: {
										name: {
											previousValue: '',
											currentValue: 'submessage0',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
								},
							],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '0',
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							changes: {
								name: {
									previousValue: '',
									currentValue: 'name',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
							},
						},
						{
							id: '1',
							name: 'message1',
							description: 'description',
							subMessages: [],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '1',
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
						},
						{
							id: '201289',
							name: 'message4',
							interfaceMessageRate: '5',
							description: 'description',
							added: false,
							changes: {},
							subMessages: [
								{
									added: false,
									deleted: true,
									changes: {},
									id: '201300',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: { id: '1', name: 'Base' },
								},
								{
									added: false,
									deleted: true,
									changes: {
										name: {
											previousValue: 'test submessage 6',
											currentValue: 'test submessage 6',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: '762',
											currentValue: '762',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: 'uiop',
											currentValue: 'uiop',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									id: '201302',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: { id: '1', name: 'Base' },
								},
								{
									added: false,
									deleted: true,
									changes: {
										name: {
											previousValue: null,
											currentValue: 'test submessage 8',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: null,
											currentValue: '85',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: null,
											currentValue: 'dfd',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									id: '201305',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: { id: '1', name: 'Base' },
								},
							],
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '2',
							hasSubMessageChanges: true,
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
						},
						{
							id: '201300',
							name: 'message3',
							description: 'description',
							subMessages: [
								{
									id: '201305',
									name: 'abcdef',
									description: 'ghijk',
									interfaceSubMessageNumber: '25',
									applicability: { id: '1', name: 'Base' },
									changes: {
										name: {
											previousValue: null,
											currentValue: 'test submessage 8',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: null,
											currentValue: '85',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: null,
											currentValue: 'dfd',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									added: false,
								},
							],
							interfaceMessageRate: '5',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '2',
							hasSubMessageChanges: true,
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
						},
					],
					e: [
						{
							id: '0',
							name: 'message0',
							description: 'description',
							subMessages: [
								{
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '0',
									id: '1',
									autogenerated: false,
									applicability: {
										id: '1',
										name: 'Base',
									},
									changes: {
										name: {
											previousValue: '',
											currentValue: 'submessage0',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
								},
							],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '0',
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							changes: {
								name: {
									previousValue: '',
									currentValue: 'name',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
							},
						},
						{
							id: '1',
							name: 'message1',
							description: 'description',
							subMessages: [],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '1',
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
						},
						{
							id: '201289',
							name: 'message4',
							interfaceMessageRate: '5',
							description: 'description',
							subMessages: [
								{
									id: '201300',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: {
										id: '1',
										name: 'Base',
									},
									added: false,
									deleted: true,
									changes: {},
								},
								{
									added: false,
									deleted: true,
									changes: {
										name: {
											previousValue: 'test submessage 6',
											currentValue: 'test submessage 6',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: '762',
											currentValue: '762',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: 'uiop',
											currentValue: 'uiop',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									id: '201302',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: {
										id: '1',
										name: 'Base',
									},
								},
								{
									added: false,
									deleted: true,
									changes: {
										name: {
											previousValue: null,
											currentValue: 'test submessage 8',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: null,
											currentValue: '85',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: null,
											currentValue: 'dfd',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									id: '201305',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: {
										id: '1',
										name: 'Base',
									},
								},
							],
							changes: {},
							added: false,
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '2',
							hasSubMessageChanges: true,
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
						},
						{
							id: '201300',
							name: 'message3',
							description: 'description',
							subMessages: [
								{
									id: '201305',
									name: 'abcdef',
									description: 'ghijk',
									interfaceSubMessageNumber: '25',
									applicability: {
										id: '1',
										name: 'Base',
									},
									changes: {
										name: {
											previousValue: null,
											currentValue: 'test submessage 8',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: null,
											currentValue: '85',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: null,
											currentValue: 'dfd',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									added: false,
								},
							],
							interfaceMessageRate: '5',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '2',
							hasSubMessageChanges: true,
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
						},
						{
							id: '201303',
							name: 'message0',
							description: 'description',
							subMessages: [],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '0',
							added: false,
							deleted: true,
							hasSubMessageChanges: false,
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [],
							subscriberNodes: [],
							changes: {},
						},
					],
					f: [
						{
							id: '0',
							name: 'message0',
							description: 'description',
							subMessages: [
								{
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '0',
									id: '1',
									autogenerated: false,
									applicability: {
										id: '1',
										name: 'Base',
									},
									changes: {
										name: {
											previousValue: '',
											currentValue: 'submessage0',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
								},
							],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '0',
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							changes: {
								name: {
									previousValue: '',
									currentValue: 'name',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
							},
						},
						{
							id: '1',
							name: 'message1',
							description: 'description',
							subMessages: [],
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '1',
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
						},
						{
							id: '201289',
							name: 'message4',
							interfaceMessageRate: '5',
							description: 'description',
							subMessages: [
								{
									id: '201300',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: {
										id: '1',
										name: 'Base',
									},
									added: false,
									deleted: true,
									changes: {},
								},
								{
									added: false,
									deleted: true,
									changes: {
										name: {
											previousValue: 'test submessage 6',
											currentValue: 'test submessage 6',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: '762',
											currentValue: '762',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: 'uiop',
											currentValue: 'uiop',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									id: '201302',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: {
										id: '1',
										name: 'Base',
									},
								},
								{
									added: false,
									deleted: true,
									changes: {
										name: {
											previousValue: null,
											currentValue: 'test submessage 8',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: null,
											currentValue: '85',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: null,
											currentValue: 'dfd',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									id: '201305',
									name: 'submessage0',
									description: '',
									interfaceSubMessageNumber: '',
									applicability: {
										id: '1',
										name: 'Base',
									},
								},
							],
							changes: {},
							added: false,
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '2',
							hasSubMessageChanges: true,
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
						},
						{
							id: '201300',
							name: 'message3',
							description: 'description',
							subMessages: [
								{
									id: '201305',
									name: 'abcdef',
									description: 'ghijk',
									interfaceSubMessageNumber: '25',
									applicability: {
										id: '1',
										name: 'Base',
									},
									changes: {
										name: {
											previousValue: null,
											currentValue: 'test submessage 8',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										interfaceSubMessageNumber: {
											previousValue: null,
											currentValue: '85',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
										description: {
											previousValue: null,
											currentValue: 'dfd',
											transactionToken: {
												id: '-1',
												branchId: '-1',
											},
										},
									},
									added: false,
								},
							],
							interfaceMessageRate: '5',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '2',
							hasSubMessageChanges: true,
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
						},
						{
							id: '201303',
							name: 'message0',
							description: 'description',
							subMessages: [],
							deleted: true,
							interfaceMessageRate: '1',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '0',
							added: false,
							hasSubMessageChanges: false,
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [],
							subscriberNodes: [],
							changes: {},
						},
						{
							id: '201304',
							name: 'message2',
							description: 'description',
							subMessages: [],
							interfaceMessageRate: '5',
							interfaceMessagePeriodicity: 'Periodic',
							interfaceMessageWriteAccess: true,
							interfaceMessageType: 'Connection',
							interfaceMessageNumber: '2',
							applicability: {
								id: '1',
								name: 'Base',
							},
							interfaceMessageExclude: false,
							interfaceMessageIoMode: '',
							interfaceMessageModeCode: '',
							interfaceMessageRateVer: '',
							interfaceMessagePriority: '',
							interfaceMessageProtocol: '',
							interfaceMessageRptWordCount: '',
							interfaceMessageRptCmdWord: '',
							interfaceMessageRunBeforeProc: false,
							interfaceMessageVer: '',
							publisherNodes: [
								{
									id: '100',
									name: 'Node1',
								},
							],
							subscriberNodes: [
								{
									id: '101',
									name: 'Node2',
								},
							],
							added: false,
							hasSubMessageChanges: false,
							changes: {
								interfaceMessageWriteAccess: {
									previousValue: null,
									currentValue: 'true',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
								name: {
									previousValue: null,
									currentValue: 'test message 7',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
								interfaceMessagePeriodicity: {
									previousValue: null,
									currentValue: 'Periodic',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
								interfaceMessageType: {
									previousValue: null,
									currentValue: 'Operational',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
								description: {
									previousValue: null,
									currentValue: 'dafda',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
								interfaceMessageNumber: {
									previousValue: null,
									currentValue: '741',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
								interfaceMessageRate: {
									previousValue: null,
									currentValue: '20',
									transactionToken: {
										id: '-1',
										branchId: '-1',
									},
								},
							},
						},
					],
				};
				expectObservable(service.messages).toBe(
					'500ms (abcdef)',
					expectedValues
				);
			});
		});
		afterAll(() => {
			service.DiffMode = false;
			service.difference = [];
		});
	});
});

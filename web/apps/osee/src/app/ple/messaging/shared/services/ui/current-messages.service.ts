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
import { Injectable, signal } from '@angular/core';
import { BehaviorSubject, combineLatest, from, iif, of, Subject } from 'rxjs';
import {
	share,
	debounceTime,
	distinctUntilChanged,
	switchMap,
	repeatWhen,
	tap,
	shareReplay,
	take,
	filter,
	map,
	reduce,
	mergeMap,
	scan,
	concatMap,
} from 'rxjs/operators';
import { PreferencesUIService } from './preferences-ui.service';
import { applic } from '@osee/shared/types/applicability';
import { MessagesService } from '../http/messages.service';
import { SubMessagesService } from '../http/sub-messages.service';
import { MessageUiService } from './messages-ui.service';
import {
	changeInstance,
	changeTypeEnum,
	itemTypeIdRelation,
} from '@osee/shared/types/change-report';
import {
	ATTRIBUTETYPEIDENUM,
	ARTIFACTTYPEIDENUM,
	RELATIONTYPEIDENUM,
} from '@osee/shared/types/constants';
import {
	ApplicabilityListUIService,
	CurrentBranchInfoService,
} from '@osee/shared/services';
import type { ConnectionNode } from '@osee/messaging/shared/types';
import type {
	messageWithChanges,
	subMessageWithChanges,
	subMessage,
	message,
	settingsDialogData,
} from '@osee/messaging/shared/types';
import { relation, transaction, transactionToken } from '@osee/shared/types';
import { SideNavService } from '@osee/shared/services/layout';
import { StructuresService } from '../http/structures.service';
import { toObservable } from '@angular/core/rxjs-interop';
import { WarningDialogService } from './warning-dialog.service';

@Injectable({
	providedIn: 'root',
})
export class CurrentMessagesService {
	private _currentPage$ = new BehaviorSubject<number>(0);
	private _currentPageSize$ = new BehaviorSubject<number>(50);
	private _messagesList = combineLatest([
		this.ui.filter,
		this.BranchId,
		this.connectionId,
		this.viewId,
		this.currentPage,
		this.currentPageSize,
	]).pipe(
		filter(
			([filter, branchId, connection, viewId, page, pageSize]) =>
				connection !== '' && branchId !== ''
		),
		share(),
		debounceTime(500),
		distinctUntilChanged(),
		switchMap(([filter, branchId, connection, viewId, page, pageSize]) =>
			this.messageService
				.getFilteredMessages(
					filter,
					branchId,
					connection,
					viewId,
					page + 1,
					pageSize
				)
				.pipe(
					repeatWhen((_) => this.ui.UpdateRequired),
					share()
				)
		),
		shareReplay({ bufferSize: 1, refCount: true })
	);

	private _messagesListCount = combineLatest([
		this.ui.filter,
		this.BranchId,
		this.connectionId,
		this.viewId,
	]).pipe(
		filter(
			([filter, branchId, connection, viewId]) =>
				connection !== '' && branchId !== ''
		),
		share(),
		debounceTime(500),
		distinctUntilChanged(),
		switchMap(([filter, branchId, connection, viewId]) =>
			this.messageService
				.getFilteredMessagesCount(filter, branchId, connection, viewId)
				.pipe(
					repeatWhen((_) => this.ui.UpdateRequired),
					share()
				)
		),
		shareReplay({ bufferSize: 1, refCount: true })
	);

	private _messages = combineLatest([
		this.ui.isInDiff,
		this._messagesList,
		this.viewId,
	]).pipe(
		switchMap(([diffState, messageList, viewId]) =>
			iif(
				() => diffState,
				this.differences.pipe(
					filter((val) => val !== undefined),
					switchMap((differences) =>
						of(
							this.parseIntoMessagesAndSubmessages(
								differences as changeInstance[],
								messageList
							)
						).pipe(
							switchMap((messagesWithDifferences) =>
								from(messagesWithDifferences).pipe(
									mergeMap((message) =>
										iif(
											() =>
												(message as messageWithChanges)
													.deleted,
											this.getMessageFromParent(
												message.id,
												viewId
											).pipe(
												switchMap((parentMessage) =>
													this.mergeMessages(
														message as messageWithChanges,
														parentMessage
													)
												)
											),
											of(message)
										).pipe(
											mergeMap((message) =>
												from(message.subMessages).pipe(
													mergeMap((submessage) =>
														iif(
															() =>
																(
																	submessage as subMessageWithChanges
																).deleted,
															this.getSubMessageFromParent(
																message.id,
																submessage.id ||
																	''
															).pipe(
																switchMap(
																	(
																		parentSubMessage
																	) =>
																		this.mergeSubMessage(
																			submessage as subMessageWithChanges,
																			parentSubMessage
																		)
																)
															), //deleted submessage
															of(submessage) //not deleted submessage
														)
													)
												)
											),
											//find deleted sub message details of all messages and merge their contents with parent branch details
											//merge back into array and set message.subMessages to it
											reduce(
												(acc, curr) => [...acc, curr],
												[] as (
													| subMessage
													| subMessageWithChanges
												)[]
											),
											switchMap((submessagearray) =>
												this.mergeSubmessagesIntoMessage(
													message,
													submessagearray
												)
											)
										)
									)
								)
							)
						)
					),
					scan(
						(acc, curr) => [...acc, curr],
						[] as (message | messageWithChanges)[]
					),
					map((array) =>
						array.sort((a, b) => Number(a.id) - Number(b.id))
					)
					//find deleted messages and merge their contents with parent branch details
				),
				of(messageList)
			)
		)
	);
	private _allMessages = combineLatest([
		this.BranchId,
		this.connectionId,
		this.viewId,
	]).pipe(
		share(),
		switchMap((x) =>
			this.messageService.getFilteredMessages('', x[0], x[1], x[2]).pipe(
				repeatWhen((_) => this.ui.UpdateRequired),
				share()
			)
		)
	);

	private _connectionNodes = combineLatest([
		this.BranchId,
		this.connectionId,
	]).pipe(
		switchMap(([branchId, connectionId]) =>
			this.messageService.getConnectionNodes(branchId, connectionId)
		),
		shareReplay({ bufferSize: 1, refCount: true })
	);

	private _done = new Subject<boolean>();
	private _differences = new BehaviorSubject<changeInstance[] | undefined>(
		undefined
	);

	private _expandedRows = signal<(message | messageWithChanges)[]>([]);
	private _expandedRows$ = toObservable(this._expandedRows);
	constructor(
		private messageService: MessagesService,
		private subMessageService: SubMessagesService,
		private structureService: StructuresService,
		private ui: MessageUiService,
		private applicabilityService: ApplicabilityListUIService,
		private preferenceService: PreferencesUIService,
		private branchInfoService: CurrentBranchInfoService,
		private sideNavService: SideNavService,
		private warningDialogService: WarningDialogService
	) {}

	get currentPage() {
		return this._currentPage$;
	}

	set page(page: number) {
		this._currentPage$.next(page);
	}

	get currentPageSize() {
		return this._currentPageSize$;
	}
	set pageSize(page: number) {
		this._currentPageSize$.next(page);
	}
	get messages() {
		return this._messages;
	}

	get messagesCount() {
		return this._messagesListCount;
	}

	get allMessages() {
		return this._allMessages;
	}

	set filter(filter: string) {
		this.ui.filterString = filter;
		this.page = 0;
	}

	set branch(id: string) {
		this.ui.BranchIdString = id;
	}

	get BranchId() {
		return this.ui.BranchId;
	}

	set branchId(value: string) {
		this.ui.BranchIdString = value;
	}

	set connection(id: string) {
		this.ui.connectionIdString = id;
	}

	get connectionId() {
		return this.ui.connectionId;
	}

	get viewId() {
		return this.ui.viewId;
	}

	set messageId(value: string) {
		this.ui.messageId = value;
	}

	set subMessageId(value: string) {
		this.ui.subMessageId = value;
	}

	set submessageToStructureBreadCrumbs(value: string) {
		this.ui.subMessageToStructureBreadCrumbs = value;
	}

	set singleStructureId(value: string) {
		this.ui.singleStructureId = value;
	}

	get connectionNodes() {
		return this._connectionNodes;
	}

	get applic() {
		return this.applicabilityService.applic;
	}

	get preferences() {
		return this.preferenceService.preferences;
	}

	get BranchPrefs() {
		return this.preferenceService.BranchPrefs;
	}

	get sideNavContent() {
		return this.sideNavService.rightSideNavContent;
	}

	set sideNav(value: {
		opened: boolean;
		field: string;
		currentValue: string | number | applic | boolean;
		previousValue?: string | number | applic | boolean;
		transaction?: transactionToken;
		user?: string;
		date?: string;
	}) {
		this.sideNavService.rightSideNav = value;
	}

	set DiffMode(value: boolean) {
		this.ui.DiffMode = value;
	}
	get isInDiff() {
		return this.ui.isInDiff;
	}

	get differences() {
		return this._differences;
	}
	set difference(value: changeInstance[]) {
		this._differences.next(value);
	}

	set branchType(value: 'working' | 'baseline' | '') {
		this.ui.typeValue = value;
	}

	get expandedRows() {
		return this._expandedRows$;
	}

	set addExpandedRow(value: message | messageWithChanges) {
		this._expandedRows.update((rows) => [...rows, value]);
	}

	set removeExpandedRow(value: message | messageWithChanges) {
		this._expandedRows.update((rows) =>
			rows.filter((v) => v.id !== value.id)
		);
	}

	clearRows() {
		this._expandedRows.set([]);
	}

	getPaginatedSubMessages(pageNum: string | number) {
		return this.BranchId.pipe(
			take(1),
			switchMap((id) =>
				this.subMessageService.getPaginatedFilteredSubMessages(
					id,
					'',
					pageNum
				)
			)
		);
	}

	getPaginatedSubmessagesByName(
		name: string,
		count: number,
		pageNum: string | number
	) {
		return this.BranchId.pipe(
			take(1),
			switchMap((id) =>
				this.subMessageService.getPaginatedSubmessagesByName(
					id,
					name,
					count,
					pageNum
				)
			)
		);
	}

	getSubmessagesByNameCount(name: string) {
		return this.BranchId.pipe(
			take(1),
			switchMap((id) =>
				this.subMessageService.getSubmessagesByNameCount(id, name)
			)
		);
	}

	private mergeMessages(message: messageWithChanges, parentMessage: message) {
		message.name = parentMessage.name;
		message.description = parentMessage.description;
		message.interfaceMessageNumber = parentMessage.interfaceMessageNumber;
		message.interfaceMessagePeriodicity =
			parentMessage.interfaceMessagePeriodicity;
		message.interfaceMessageRate = parentMessage.interfaceMessageRate;
		message.interfaceMessageType = parentMessage.interfaceMessageType;
		message.interfaceMessageWriteAccess =
			parentMessage.interfaceMessageWriteAccess;
		return of(message);
	}

	private mergeSubMessage(
		submessage: subMessageWithChanges,
		parentSubMessage: subMessage
	) {
		submessage.name = parentSubMessage.name;
		submessage.description = parentSubMessage.description;
		submessage.interfaceSubMessageNumber = parentSubMessage.description;
		submessage.applicability = parentSubMessage.applicability;
		return of(submessage);
	}
	private mergeSubmessagesIntoMessage(
		message: message | messageWithChanges,
		submessages: (subMessage | subMessageWithChanges)[]
	) {
		message.subMessages = submessages;
		return of(message);
	}
	getMessageFromParent(messageId: string, viewId: string) {
		return combineLatest([
			this.branchInfoService.currentBranch,
			this.connectionId,
		]).pipe(
			take(1),
			switchMap(([details, connectionId]) =>
				this.messageService.getMessage(
					details.parentBranch.id,
					messageId,
					connectionId,
					viewId
				)
			)
		);
	}

	getSubMessageFromParent(messageId: string, subMessageId: string) {
		return combineLatest([
			this.branchInfoService.currentBranch,
			this.connectionId,
		]).pipe(
			take(1),
			switchMap(([details, connectionId]) =>
				this.subMessageService.getSubMessage(
					details.parentBranch.id,
					connectionId,
					messageId,
					subMessageId
				)
			)
		);
	}
	partialUpdateSubMessage(body: Partial<subMessage>, messageId: string) {
		return combineLatest([this.BranchId, this.connectionId]).pipe(
			take(1),
			switchMap(([branch, connection]) =>
				this.warningDialogService
					.openSubMessageDialog(body)
					.pipe(map((_) => [branch, connection]))
			),
			switchMap(([branch, connection]) =>
				this.subMessageService.changeSubMessage(branch, body).pipe(
					take(1),
					switchMap((transaction) =>
						this.subMessageService
							.performMutation(
								branch,
								connection,
								messageId,
								transaction
							)
							.pipe(
								tap(() => {
									this.ui.updateMessages = true;
								})
							)
					)
				)
			)
		);
	}

	/**
	 * @TODO update to query and decide to launch dialog yay/nay
	 * @param body
	 * @returns
	 */
	partialUpdateMessage(body: Partial<message>) {
		return this.BranchId.pipe(
			take(1),
			switchMap((branchId) =>
				this.warningDialogService
					.openMessageDialog(body)
					.pipe(map((_) => branchId))
			),
			switchMap((branchId) =>
				this.messageService.changeMessage(branchId, body).pipe(
					take(1),
					switchMap((transaction) =>
						this.messageService.performMutation(transaction).pipe(
							tap(() => {
								this.ui.updateMessages = true;
							})
						)
					)
				)
			)
		);
	}

	relateSubMessage(
		messageId: string,
		subMessageId: string,
		afterSubMessage?: string
	) {
		return combineLatest([
			this.BranchId,
			this.connectionId,
			this.viewId,
		]).pipe(
			take(1),
			switchMap(([branch, connection, viewId]) =>
				this.warningDialogService
					.openMessageDialog({ id: messageId })
					.pipe(map((_) => [branch, connection, viewId]))
			),
			switchMap(([branch, connection, viewId]) =>
				this.messageService
					.getMessage(branch, connection, messageId, viewId)
					.pipe(
						take(1),
						switchMap((foundMessage) =>
							this.subMessageService
								.createMessageRelation(
									foundMessage.id,
									subMessageId,
									afterSubMessage
								)
								.pipe(
									take(1),
									switchMap((relation) =>
										this.subMessageService
											.addRelation(branch, relation)
											.pipe(
												take(1),
												switchMap((transaction) =>
													this.subMessageService
														.performMutation(
															branch,
															connection,
															messageId,
															transaction
														)
														.pipe(
															tap(() => {
																this.ui.updateMessages =
																	true;
															})
														)
												)
											)
									)
								)
						)
					)
			)
		);
	}

	createSubMessage(
		body: subMessage,
		messageId: string,
		afterSubMessage?: string
	) {
		return combineLatest([this.BranchId, this.connectionId]).pipe(
			take(1),
			switchMap(([branch, connection]) =>
				this.warningDialogService
					.openMessageDialog({ id: messageId })
					.pipe(map((_) => [branch, connection]))
			),
			switchMap(([branch, connection]) =>
				this.subMessageService
					.createMessageRelation(
						messageId,
						undefined,
						afterSubMessage
					)
					.pipe(
						take(1),
						switchMap((relation) =>
							this.subMessageService
								.createSubMessage(branch, body, [relation])
								.pipe(
									take(1),
									switchMap((transaction) =>
										this.subMessageService
											.performMutation(
												branch,
												connection,
												messageId,
												transaction
											)
											.pipe(
												tap(() => {
													this.ui.updateMessages =
														true;
												})
											)
									)
								)
						)
					)
			)
		);
	}
	copySubMessage(
		body: subMessage,
		messageId: string,
		afterSubMessage?: string
	) {
		const branchId = this.ui.BranchId.pipe(
			take(1),
			filter((id) => id !== '' && id !== '-1')
		);
		const connectionId = this.connectionId.pipe(
			take(1),
			filter((id) => id !== '' && id !== '-1')
		);
		const structures = combineLatest([branchId, connectionId]).pipe(
			switchMap(([id, connection]) =>
				this.structureService.getFilteredStructures(
					'',
					id,
					messageId,
					body.id || '-1',
					connection,
					'-1',
					1,
					0
				)
			)
		);
		const structureIds = structures.pipe(
			concatMap((st) => from(st).pipe(map((structure) => structure.id))),
			reduce((acc, curr) => [...acc, curr], [] as string[])
		);
		const structureRelations = structureIds.pipe(
			concatMap((st) =>
				from(st).pipe(
					switchMap((structure) =>
						this.structureService.createSubMessageRelation(
							undefined,
							structure
						)
					)
				)
			),
			reduce((acc, curr) => [...acc, curr], [] as relation[])
		);
		const messageRelation = this.subMessageService.createMessageRelation(
			messageId,
			undefined,
			afterSubMessage
		);
		const transaction = combineLatest([
			structureRelations,
			messageRelation,
			branchId,
		]).pipe(
			switchMap(([structureRelations, messageRelation, branchId]) =>
				this.subMessageService.createSubMessage(branchId, body, [
					...structureRelations,
					messageRelation,
				])
			)
		);
		return transaction.pipe(
			switchMap((tx) =>
				this.warningDialogService
					.openMessageDialog({ id: messageId })
					.pipe(map((_) => tx))
			),
			switchMap((tx) =>
				this.subMessageService.performMutation('', '', '', tx)
			),
			tap((_) => (this.ui.updateMessages = true))
		);
	}

	createMessage(
		publisherNodes: ConnectionNode | ConnectionNode[],
		subscriberNodes: ConnectionNode | ConnectionNode[],
		body: message,
		subMessages?: subMessage[]
	) {
		const key = '1b91f809-783c-415e-8825-7920c76be31e'; //random string for message
		const pubNodes = Array.isArray(publisherNodes)
			? publisherNodes
			: [publisherNodes];
		const subNodes = Array.isArray(subscriberNodes)
			? subscriberNodes
			: [subscriberNodes];

		const connectionRelation = this.connectionId.pipe(
			take(1),
			switchMap((connectionId) =>
				this.messageService.createConnectionRelation(connectionId)
			)
		);
		const pubNodeRelations = from(pubNodes).pipe(
			concatMap((node) =>
				this.messageService.createMessageNodeRelation(
					body.id,
					node.id,
					true
				)
			),
			reduce((acc, curr) => [...acc, curr], [] as relation[])
		);
		const subNodeRelations = from(subNodes).pipe(
			concatMap((node) =>
				this.messageService.createMessageNodeRelation(
					body.id,
					node.id,
					false
				)
			),
			reduce((acc, curr) => [...acc, curr], [] as relation[])
		);
		const subMessageRelations = subMessages
			? from(subMessages).pipe(
					filter((v) => v.id !== '0' && v.id !== '-1'),
					concatMap((subMessage) =>
						this.messageService.createSubMessageRelation(
							subMessage.id
						)
					),
					reduce((acc, curr) => [...acc, curr], [] as relation[])
			  )
			: of<relation[]>([]);

		return combineLatest([
			this.BranchId,
			connectionRelation,
			pubNodeRelations,
			subNodeRelations,
			subMessageRelations,
		]).pipe(
			take(1),
			switchMap(
				([
					branch,
					connectionRelation,
					pubRelations,
					subRelations,
					subMessageRelations,
				]) =>
					this.messageService
						.createMessage(
							branch,
							body,
							[
								connectionRelation,
								...pubRelations,
								...subRelations,
								...subMessageRelations,
							],
							undefined,
							key
						)
						.pipe(
							take(1),
							switchMap((transaction) =>
								this.messageService
									.performMutation(transaction)
									.pipe(
										tap(() => {
											this.ui.updateMessages = true;
										})
									)
							)
						)
			)
		);
	}

	deleteMessage(messageId: string) {
		return this.BranchId.pipe(
			take(1),
			switchMap((branch) =>
				this.warningDialogService
					.openMessageDialog({ id: messageId })
					.pipe(map((_) => branch))
			),
			switchMap((branchId) =>
				this.messageService.deleteMessage(branchId, messageId).pipe(
					switchMap((transaction) =>
						this.messageService.performMutation(transaction).pipe(
							tap(() => {
								this.ui.updateMessages = true;
							})
						)
					)
				)
			)
		);
	}

	removeMessage(messageId: string) {
		return combineLatest([this.connectionId, this.BranchId]).pipe(
			take(1),
			switchMap(([branch, connection]) =>
				this.warningDialogService
					.openMessageDialog({ id: messageId })
					.pipe(map((_) => [branch, connection]))
			),
			switchMap(([connectionId, branchId]) =>
				this.messageService
					.createConnectionRelation(connectionId, messageId)
					.pipe(
						switchMap((relation) =>
							this.messageService
								.deleteRelation(branchId, relation)
								.pipe(
									switchMap((transaction) =>
										this.messageService
											.performMutation(transaction)
											.pipe(
												tap(() => {
													this.ui.updateMessages =
														true;
												})
											)
									)
								)
						)
					)
			)
		);
	}

	removeSubMessage(submessageId: string, messageId: string) {
		return this.BranchId.pipe(
			take(1),
			switchMap((id) =>
				this.warningDialogService
					.openSubMessageDialog({ id: submessageId })
					.pipe(map((_) => id))
			),
			switchMap((branchId) =>
				this.subMessageService
					.createMessageRelation(messageId, submessageId)
					.pipe(
						switchMap((relation) =>
							this.subMessageService
								.deleteRelation(branchId, relation)
								.pipe(
									switchMap((transaction) =>
										this.subMessageService
											.performMutation(
												branchId,
												'',
												'',
												transaction
											)
											.pipe(
												tap(() => {
													this.ui.updateMessages =
														true;
												})
											)
									)
								)
						)
					)
			)
		);
	}
	deleteSubMessage(submessageId: string) {
		return this.BranchId.pipe(
			take(1),
			switchMap((id) =>
				this.warningDialogService
					.openSubMessageDialog({ id: submessageId })
					.pipe(map((_) => id))
			),
			switchMap((branchId) =>
				this.subMessageService
					.deleteSubMessage(branchId, submessageId)
					.pipe(
						switchMap((transaction) =>
							this.subMessageService
								.performMutation(branchId, '', '', transaction)
								.pipe(
									tap(() => {
										this.ui.updateMessages = true;
									})
								)
						)
					)
			)
		);
	}

	updatePreferences(preferences: settingsDialogData) {
		return this.createUserPreferenceBranchTransaction(
			preferences.editable
		).pipe(
			take(1),
			switchMap((transaction) =>
				this.messageService.performMutation(transaction).pipe(
					take(1),
					tap(() => {
						this.ui.updateMessages = true;
					})
				)
			)
		);
	}

	changeMessageRelationOrder(
		connectionId: string,
		messageId: string,
		afterArtifactId: string
	) {
		const branchId = this.ui.BranchId.pipe(take(1));
		const deleteRelation = this.messageService.createConnectionRelation(
			connectionId,
			messageId
		);
		const createRelation = this.messageService.createConnectionRelation(
			connectionId,
			messageId,
			afterArtifactId
		);
		const tx = combineLatest([
			branchId,
			deleteRelation,
			createRelation,
		]).pipe(
			switchMap(([branchId, deleteRel, createRel]) =>
				this.messageService
					.deleteRelation(branchId, deleteRel)
					.pipe(
						switchMap((tx) =>
							this.messageService.addRelation(
								branchId,
								createRel,
								tx
							)
						)
					)
			)
		);
		return tx.pipe(
			switchMap((_tx) =>
				this.messageService.performMutation(_tx).pipe(
					tap(() => {
						this.ui.updateMessages = true;
					})
				)
			)
		);
	}

	private createUserPreferenceBranchTransaction(editMode: boolean) {
		return combineLatest(
			this.preferences,
			this.BranchId,
			this.BranchPrefs
		).pipe(
			take(1),
			switchMap(([prefs, branch, branchPrefs]) =>
				iif(
					() => prefs.hasBranchPref,
					of<transaction>({
						branch: '570',
						txComment: 'Updating MIM User Preferences',
						modifyArtifacts: [
							{
								id: prefs.id,
								setAttributes: [
									{
										typeName: 'MIM Branch Preferences',
										value: [
											...branchPrefs,
											`${branch}:${editMode}`,
										],
									},
								],
							},
						],
					}),
					of<transaction>({
						branch: '570',
						txComment: 'Updating MIM User Preferences',
						modifyArtifacts: [
							{
								id: prefs.id,
								addAttributes: [
									{
										typeName: 'MIM Branch Preferences',
										value: `${branch}:${editMode}`,
									},
								],
							},
						],
					})
				)
			)
		);
	}

	set toggleDone(value: unknown) {
		this._done.next(true);
	}

	get done() {
		return this._done;
	}

	parseIntoMessagesAndSubmessages(
		changes: changeInstance[],
		_oldMessageList: (message | messageWithChanges)[]
	) {
		let messageList = JSON.parse(JSON.stringify(_oldMessageList)) as (
			| message
			| messageWithChanges
		)[];
		let newMessages: changeInstance[] = [];
		let newMessagesId: string[] = [];
		let newSubmessages: changeInstance[] = [];
		let newSubmessagesId: string[] = [];
		changes.forEach((change) => {
			//this loop is solely just for building a list of deleted nodes/connections
			if (
				change.itemTypeId === ARTIFACTTYPEIDENUM.SUBMESSAGE &&
				!newMessagesId.includes(change.artId) &&
				!newSubmessagesId.includes(change.artId)
			) {
				//deleted submessage
				newSubmessagesId.push(change.artId);
			} else if (
				change.itemTypeId === ARTIFACTTYPEIDENUM.MESSAGE &&
				!newMessagesId.includes(change.artId) &&
				!newSubmessagesId.includes(change.artId)
			) {
				//deleted message
				newMessagesId.push(change.artId);
			} else if (
				typeof change.itemTypeId === 'object' &&
				'id' in change.itemTypeId &&
				change.itemTypeId.id ===
					RELATIONTYPEIDENUM.INTERFACECONNECTIONCONTENT
			) {
				if (!newMessagesId.includes(change.artId)) {
					newMessagesId.push(change.artId);
				} else if (!newMessagesId.includes(change.artIdB)) {
					newMessagesId.push(change.artIdB);
				}
			} else if (
				typeof change.itemTypeId === 'object' &&
				'id' in change.itemTypeId &&
				change.itemTypeId.id ===
					RELATIONTYPEIDENUM.INTERFACEMESSAGECONTENT
			) {
				if (!newSubmessagesId.includes(change.artId)) {
					newSubmessagesId.push(change.artId);
				}
			}
		});
		changes
			.sort(
				(a, b) =>
					['111', '333', '222', '444'].indexOf(a.changeType.id) -
					['111', '333', '222', '444'].indexOf(b.changeType.id)
			)
			.forEach((change) => {
				if (messageList.find((val) => val.id === change.artId)) {
					//logic for message update
					const messageIndex = messageList.indexOf(
						messageList.find(
							(val) => val.id === change.artId
						) as message
					);
					messageList[messageIndex] = this.messageChange(
						change,
						messageList[messageIndex]
					);
					const messageChanges = (
						messageList[messageIndex] as messageWithChanges
					).changes;
					if (
						messageChanges.applicability !== undefined &&
						messageChanges.name !== undefined &&
						messageChanges.description !== undefined &&
						messageChanges.interfaceMessageNumber !== undefined &&
						messageChanges.interfaceMessagePeriodicity !==
							undefined &&
						messageChanges.interfaceMessageRate !== undefined &&
						messageChanges.interfaceMessageType !== undefined &&
						messageChanges.interfaceMessageWriteAccess !==
							undefined &&
						(messageList[messageIndex] as messageWithChanges)
							.deleted !== true
					) {
						(
							messageList[messageIndex] as messageWithChanges
						).added = true;
						messageList[messageIndex] as messageWithChanges;
					} else {
						(
							messageList[messageIndex] as messageWithChanges
						).added = false;
					}
					if (
						!(messageList[messageIndex] as messageWithChanges)
							.hasSubMessageChanges
					) {
						(
							messageList[messageIndex] as messageWithChanges
						).hasSubMessageChanges = false;
					}
				} else if (
					messageList.find(
						(val) =>
							val.subMessages.find(
								(val2) => val2.id === change.artId
							) !== undefined
					)
				) {
					//logic for submessage update
					let filteredMessages = messageList.filter((val) =>
						val.subMessages.find((val2) => val2.id === change.artId)
					);
					filteredMessages.forEach((value, index) => {
						let subMessageIndex = filteredMessages[
							index
						].subMessages.indexOf(
							filteredMessages[index].subMessages.find(
								(val2) => val2.id === change.artId
							) as Required<subMessage>
						);
						filteredMessages[index].subMessages[subMessageIndex] =
							this.subMessageChange(
								change,
								filteredMessages[index].subMessages[
									subMessageIndex
								]
							);
						(
							filteredMessages[index] as messageWithChanges
						).hasSubMessageChanges = true;
						let messageChanges = (
							filteredMessages[index].subMessages[
								subMessageIndex
							] as subMessageWithChanges
						).changes;
						if (
							messageChanges.name !== undefined &&
							messageChanges.description !== undefined &&
							messageChanges.interfaceSubMessageNumber !==
								undefined &&
							messageChanges.applicability !== undefined &&
							(
								filteredMessages[index].subMessages[
									subMessageIndex
								] as subMessageWithChanges
							).deleted !== true
						) {
							(
								filteredMessages[index].subMessages[
									subMessageIndex
								] as subMessageWithChanges
							).added = true;
						} else {
							(
								filteredMessages[index].subMessages[
									subMessageIndex
								] as subMessageWithChanges
							).added = false;
						}
						///update main list
						let messageIndex = messageList.indexOf(
							messageList.find(
								(val) => val.id === filteredMessages[index].id
							) as message | messageWithChanges
						);
						messageList[messageIndex] = filteredMessages[index];
					});
				} else if (
					(newMessagesId.includes(change.artId) ||
						newMessagesId.includes(change.artIdB)) &&
					change.deleted
				) {
					newMessages.push(change);
				} else if (
					(newSubmessagesId.includes(change.artId) ||
						newSubmessagesId.includes(change.artIdB)) &&
					change.deleted
				) {
					newSubmessages.push(change);
				}
			});
		newMessages.sort((a, b) => Number(a.artId) - Number(b.artId));
		newSubmessages.sort((a, b) => Number(a.artId) - Number(b.artId));
		let messages = this.splitByArtId(newMessages);
		messages.forEach((value) => {
			//create deleted messages
			let tempMessage = this.messageDeletionChanges(value);
			if (!isNaN(+tempMessage.id) && tempMessage.id !== '') {
				messageList.push(tempMessage);
			}
		});
		let submessages = this.splitByArtId(newSubmessages);
		submessages.forEach((value) => {
			//create deleted submessages
		});
		messageList.forEach((m) => {
			m.subMessages = m.subMessages.sort(
				(a, b) => Number(a.id) - Number(b.id)
			);
		});
		return messageList.sort((a, b) => Number(a.id) - Number(b.id));
	}

	private splitByArtId(changes: changeInstance[]): changeInstance[][] {
		let returnValue: changeInstance[][] = [];
		let prev: Partial<changeInstance> | undefined = undefined;
		let tempArray: changeInstance[] = [];
		changes.forEach((value, index) => {
			if (prev !== undefined) {
				if (prev.artId === value.artId) {
					//condition where equal, add to array
					tempArray.push(value);
				} else {
					prev = Object.assign(prev, value);
					returnValue.push(tempArray);
					tempArray = [];
					tempArray.push(value);
					//condition where not equal, set prev to value, push old array onto returnValue, create new array
				}
			} else {
				tempArray = [];
				tempArray.push(value);
				prev = {};
				prev = Object.assign(prev, value);
				//create new array, push prev onto array, set prev
			}
		});
		if (tempArray.length !== 0) {
			returnValue.push(tempArray);
		}
		return returnValue;
	}

	private messageDeletionChanges(changes: changeInstance[]) {
		let tempMessage: messageWithChanges = {
			added: false,
			deleted: true,
			hasSubMessageChanges: false,
			changes: {},
			id: '',
			name: '',
			description: '',
			applicability: {
				id: '1',
				name: 'Base',
			},
			subMessages: [],
			interfaceMessageRate: '',
			interfaceMessagePeriodicity: '',
			interfaceMessageWriteAccess: false,
			interfaceMessageType: '',
			interfaceMessageNumber: '',
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
		};
		changes.forEach((value) => {
			tempMessage = this.parseMessageDeletionChange(value, tempMessage);
		});
		return tempMessage;
	}
	parseMessageDeletionChange(
		change: changeInstance,
		message: messageWithChanges
	): messageWithChanges {
		message.id = change.artId;
		if (message.changes === undefined) {
			message.changes = {};
		}
		if (change.changeType.name === changeTypeEnum.ATTRIBUTE_CHANGE) {
			let changes = {
				previousValue: change.baselineVersion.value,
				currentValue: change.destinationVersion.value,
				transactionToken: change.currentVersion.transactionToken,
			};
			if (change.itemTypeId === ATTRIBUTETYPEIDENUM.NAME) {
				message.changes.name = changes;
			} else if (change.itemTypeId === ATTRIBUTETYPEIDENUM.DESCRIPTION) {
				message.changes.description = changes;
			} else if (
				change.itemTypeId === ATTRIBUTETYPEIDENUM.INTERFACEMESSAGENUMBER
			) {
				message.changes.interfaceMessageNumber = changes;
			} else if (
				change.itemTypeId ===
				ATTRIBUTETYPEIDENUM.INTERFACEMESSAGEPERIODICITY
			) {
				message.changes.interfaceMessagePeriodicity = changes;
			} else if (
				change.itemTypeId === ATTRIBUTETYPEIDENUM.INTERFACEMESSAGERATE
			) {
				message.changes.interfaceMessageRate = changes;
			} else if (
				change.itemTypeId === ATTRIBUTETYPEIDENUM.INTERFACEMESSAGETYPE
			) {
				message.changes.interfaceMessageType = changes;
			} else if (
				change.itemTypeId ===
				ATTRIBUTETYPEIDENUM.INTERFACEMESSAGEWRITEACCESS
			) {
				message.changes.interfaceMessageWriteAccess = changes;
			}
		} else if (change.changeType.name === changeTypeEnum.ARTIFACT_CHANGE) {
			message.changes.applicability = {
				previousValue: change.baselineVersion.applicabilityToken,
				currentValue: change.currentVersion.applicabilityToken,
				transactionToken: change.currentVersion.transactionToken,
			};
		} else if (change.changeType.name === changeTypeEnum.RELATION_CHANGE) {
			message.id = change.artIdB;
			message.applicability = change.currentVersion
				.applicabilityToken as applic;
		}
		return message;
	}

	private messageChange(
		change: changeInstance,
		message: message | messageWithChanges
	) {
		return this.parseMessageChange(change, this.initializeMessage(message));
	}
	private parseMessageChange(
		change: changeInstance,
		message: messageWithChanges
	) {
		if (change.changeType.name === changeTypeEnum.ATTRIBUTE_CHANGE) {
			let changes = {
				previousValue: change.baselineVersion.value,
				currentValue: change.currentVersion.value,
				transactionToken: change.currentVersion.transactionToken,
			};
			if (change.itemTypeId === ATTRIBUTETYPEIDENUM.NAME) {
				message.changes.name = changes;
			} else if (change.itemTypeId === ATTRIBUTETYPEIDENUM.DESCRIPTION) {
				message.changes.description = changes;
			} else if (
				change.itemTypeId === ATTRIBUTETYPEIDENUM.INTERFACEMESSAGENUMBER
			) {
				message.changes.interfaceMessageNumber = changes;
			} else if (
				change.itemTypeId ===
				ATTRIBUTETYPEIDENUM.INTERFACEMESSAGEPERIODICITY
			) {
				message.changes.interfaceMessagePeriodicity = changes;
			} else if (
				change.itemTypeId === ATTRIBUTETYPEIDENUM.INTERFACEMESSAGERATE
			) {
				message.changes.interfaceMessageRate = changes;
			} else if (
				change.itemTypeId === ATTRIBUTETYPEIDENUM.INTERFACEMESSAGETYPE
			) {
				message.changes.interfaceMessageType = changes;
			} else if (
				change.itemTypeId ===
				ATTRIBUTETYPEIDENUM.INTERFACEMESSAGEWRITEACCESS
			) {
				message.changes.interfaceMessageWriteAccess = changes;
			}
		} else if (change.changeType.name === changeTypeEnum.ARTIFACT_CHANGE) {
			if (change.currentVersion.transactionToken.id !== '-1') {
				message.changes.applicability = {
					previousValue: change.baselineVersion.applicabilityToken,
					currentValue: change.currentVersion.applicabilityToken,
					transactionToken: change.currentVersion.transactionToken,
				};
			}
		} else if (change.changeType.name === changeTypeEnum.RELATION_CHANGE) {
			//do nothing currently
			if (
				((change.itemTypeId as itemTypeIdRelation).id =
					RELATIONTYPEIDENUM.INTERFACEMESSAGECONTENT)
			) {
				message.hasSubMessageChanges = true;
				let submessageIndex = message.subMessages.findIndex(
					(val) => val.id === change.artIdB
				);
				if (submessageIndex !== -1) {
					message.subMessages[submessageIndex] =
						this.subMessageChange(
							change,
							message.subMessages[submessageIndex]
						);
				} else {
					let submessage: subMessageWithChanges = {
						added: false,
						deleted: true,
						changes: {},
						applicability: {
							id: '1',
							name: 'Base',
						},
						id: change.artIdB,
						name: '',
						description: '',
						interfaceSubMessageNumber: '',
					};
					message.subMessages.push(submessage);
				}
			}
		}
		return message;
	}
	private isMessageWithChanges(
		message: message | messageWithChanges
	): message is messageWithChanges {
		return (message as messageWithChanges).changes !== undefined;
	}
	private initializeMessage(message: message | messageWithChanges) {
		let tempMessage: messageWithChanges;
		if (!this.isMessageWithChanges(message)) {
			tempMessage = message as messageWithChanges;
			tempMessage.changes = {};
		} else {
			tempMessage = message;
		}
		return tempMessage;
	}

	private subMessageChange(
		change: changeInstance,
		submessage: subMessage | subMessageWithChanges
	) {
		return this.parseSubMessageChange(
			change,
			this.initializeSubMessage(submessage)
		);
	}
	parseSubMessageChange(
		change: changeInstance,
		submessage: subMessageWithChanges
	) {
		if (change.changeType.name === changeTypeEnum.ATTRIBUTE_CHANGE) {
			let changes = {
				previousValue: change.baselineVersion.value,
				currentValue: change.currentVersion.value,
				transactionToken: change.currentVersion.transactionToken,
			};
			if (change.itemTypeId === ATTRIBUTETYPEIDENUM.NAME) {
				submessage.changes.name = changes;
			} else if (change.itemTypeId === ATTRIBUTETYPEIDENUM.DESCRIPTION) {
				submessage.changes.description = changes;
			} else if (
				change.itemTypeId ===
				ATTRIBUTETYPEIDENUM.INTERFACESUBMESSAGENUMBER
			) {
				submessage.changes.interfaceSubMessageNumber = changes;
			}
		} else if (change.changeType.name === changeTypeEnum.ARTIFACT_CHANGE) {
			if (change.currentVersion.transactionToken.id !== '-1') {
				submessage.changes.applicability = {
					previousValue: change.baselineVersion.applicabilityToken,
					currentValue: change.currentVersion.applicabilityToken,
					transactionToken: change.currentVersion.transactionToken,
				};
			}
		} else if (change.changeType.name === changeTypeEnum.RELATION_CHANGE) {
			//do nothing currently
			submessage.added = true;
		}
		return submessage;
	}
	initializeSubMessage(submessage: subMessage | subMessageWithChanges) {
		let tempMessage: subMessageWithChanges;
		if (!this.isSubMessageWithChanges(submessage)) {
			tempMessage = submessage as subMessageWithChanges;
			tempMessage.changes = {};
		} else {
			tempMessage = submessage;
		}
		return tempMessage;
	}
	isSubMessageWithChanges(
		submessage: subMessage | subMessageWithChanges
	): submessage is subMessageWithChanges {
		return (submessage as subMessageWithChanges)?.changes !== undefined;
	}

	get initialRoute() {
		return combineLatest([
			this.ui.type,
			this.BranchId,
			this.connectionId,
		]).pipe(
			switchMap(([type, id, connection]) =>
				of(
					'/ple/messaging/' +
						'connections/' +
						type +
						'/' +
						id +
						'/' +
						connection +
						'/messages/'
				)
			)
		);
	}

	/**
	 *
	 * @param message
	 * @param newNodes
	 * @param type - true = publisher node, false = subscriber node
	 */
	updateMessageNodeRelations(
		message: message,
		newNodes: ConnectionNode[],
		type: boolean
	) {
		const currentNodes = type
			? message.publisherNodes
			: message.subscriberNodes;
		const removeNodes: ConnectionNode[] = [];
		const addNodes: ConnectionNode[] = [];

		currentNodes.forEach((oldNode) => {
			if (newNodes.filter((n) => n.id === oldNode.id).length === 0) {
				removeNodes.push(oldNode);
			}
		});

		newNodes.forEach((newNode) => {
			if (currentNodes.filter((n) => n.id === newNode.id).length === 0) {
				addNodes.push(newNode);
			}
		});

		const removeRelations = from(removeNodes).pipe(
			concatMap((node) =>
				this.messageService
					.createMessageNodeRelation(message.id, node.id, type)
					.pipe()
			),
			reduce((acc, curr) => [...acc, curr], [] as relation[])
		);

		const addRelations = from(addNodes).pipe(
			concatMap((node) =>
				this.messageService
					.createMessageNodeRelation(message.id, node.id, type)
					.pipe()
			),
			reduce((acc, curr) => [...acc, curr], [] as relation[])
		);

		return combineLatest([
			this.BranchId,
			removeRelations,
			addRelations,
		]).pipe(
			filter(
				([branchId, removeRels, addRels]) =>
					removeRels.length > 0 || addRels.length > 0
			),
			switchMap(([branchId, removeRels, addRels]) =>
				of({
					branch: branchId,
					txComment: 'Update Message Nodes',
				} as transaction).pipe(
					concatMap((tx) =>
						iif(
							() => removeRels.length > 0,
							from(removeRels).pipe(
								concatMap((rel) =>
									this.messageService.deleteRelation(
										branchId,
										rel,
										tx
									)
								),
								reduce(() => tx)
							),
							of(tx)
						)
					),
					concatMap((tx) =>
						iif(
							() => addRels.length > 0,
							from(addRels).pipe(
								concatMap((rel) =>
									this.messageService.addRelation(
										branchId,
										rel,
										tx
									)
								),
								reduce(() => tx)
							),
							of(tx)
						)
					),
					switchMap((tx) =>
						this.messageService.performMutation(tx).pipe(
							tap((res) => {
								this.ui.updateMessages = true;
							})
						)
					)
				)
			)
		);
	}

	get endOfRoute() {
		return this.isInDiff.pipe(
			switchMap((val) => iif(() => val, of('/diff'), of('')))
		);
	}

	get connectionsRoute() {
		return combineLatest([this.ui.type, this.BranchId]).pipe(
			switchMap(([type, BranchId]) =>
				of('/ple/messaging/connections/' + type + '/' + BranchId)
			)
		);
	}
}

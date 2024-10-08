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
import { A11yModule, CdkMonitorFocus } from '@angular/cdk/a11y';
import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormField } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import {
	CurrentMessagesService,
	WarningDialogService,
} from '@osee/messaging/shared/services';
import type { subMessage } from '@osee/messaging/shared/types';
import { ApplicabilitySelectorComponent } from '@osee/shared/components';
import { applic } from '@osee/shared/types/applicability';
import { Subject, combineLatest, iif, of } from 'rxjs';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	scan,
	share,
	switchMap,
	tap,
} from 'rxjs/operators';

@Component({
	selector: 'osee-messaging-edit-sub-message-field',
	templateUrl: './edit-sub-message-field.component.html',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [
		AsyncPipe,
		FormsModule,
		A11yModule,
		CdkMonitorFocus,
		MatFormField,
		MatInput,
		ApplicabilitySelectorComponent,
	],
})
export class EditSubMessageFieldComponent<
	R extends keyof subMessage = any,
	T extends Pick<subMessage, keyof subMessage> = any,
> {
	@Input() messageId!: string;
	@Input() subMessageId!: string;
	@Input() header: R = {} as R;
	@Input() value: T = {} as T;
	private _value: Subject<T> = new Subject();
	_subMessage: Partial<subMessage> = {
		id: this.subMessageId,
	};
	private _sendValue = this._value.pipe(
		share(),
		debounceTime(500),
		distinctUntilChanged(),
		map(
			(x: any) => (this._subMessage[this.header as keyof subMessage] = x)
		),
		tap(() => {
			this._subMessage.id = this.subMessageId;
		})
	);

	private _focus = new Subject<string | null>();
	private _updateValue = combineLatest([this._sendValue, this._focus]).pipe(
		scan(
			(acc, curr) => {
				if (acc.type === curr[1]) {
					acc.count++;
				} else {
					acc.count = 0;
					acc.type = curr[1];
				}
				acc.value = curr[0];
				return acc;
			},
			{ count: 0, type: '', value: undefined } as {
				count: number;
				type: string | null;
				value: T | undefined;
			}
		),
		switchMap((update) =>
			iif(
				() => update.type === null,
				of(true).pipe(
					switchMap((value) =>
						this.warningService.openSubMessageDialog(
							this._subMessage
						)
					),
					switchMap((value) =>
						this.messageService.partialUpdateSubMessage(
							value,
							this.messageId
						)
					)
				),
				of(false)
			)
		)
	);
	constructor(
		private messageService: CurrentMessagesService,
		private warningService: WarningDialogService
	) {
		this._updateValue.subscribe();
	}
	updateSubMessage(value: T) {
		// this is kind of a hack, need to rethink how to update on focus/ not on focus
		if (this.header === 'applicability') {
			this.focusChanged('applicability');
		}
		this._value.next(value);
		if (this.header === 'applicability') {
			this.focusChanged(null);
		}
	}
	focusChanged(event: string | null) {
		this._focus.next(event);
	}

	/**
	 * Note, this is a hack until we improve the types, don't use unless you know what you are doing
	 */
	isApplic(value: unknown): value is applic {
		return (
			value !== null &&
			value !== undefined &&
			typeof value === 'object' &&
			'id' in value &&
			'name' in value &&
			typeof value.id === 'string' &&
			typeof value.name === 'string'
		);
	}

	/**
	 * Note, this is a hack until we improve the types, don't use unless you know what you are doing
	 */
	returnAsT(value: unknown): T {
		return value as T;
	}
}

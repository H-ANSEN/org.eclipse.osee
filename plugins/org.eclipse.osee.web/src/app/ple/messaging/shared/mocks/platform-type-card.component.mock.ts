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
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PlatformTypeCardComponent } from '../components/platform-type-card/platform-type-card.component';
import { PlatformType } from '../types/platformType';

@Component({
	selector: 'osee-messaging-types-platform-type-card',
	template: '<p>Dummy</p>',
})
// eslint-disable-next-line @angular-eslint/component-class-suffix
export class MockPlatformTypeCard
	implements Partial<PlatformTypeCardComponent>
{
	@Input() typeData!: PlatformType;
}

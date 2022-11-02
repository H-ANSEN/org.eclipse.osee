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
import { Component, OnInit } from '@angular/core';

@Component({
	selector: 'osee-scroll-to-top-button',
	templateUrl: './scroll-to-top-button.component.html',
	styleUrls: ['./scroll-to-top-button.component.sass'],
})
export class ScrollToTopButtonComponent {
	constructor() {}

	scrollToTop() {
		document.getElementById('app-top')?.scrollIntoView({
			behavior: 'smooth',
			block: 'start',
		});
	}
}

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
import { Injectable } from '@angular/core';
import { iif, of } from 'rxjs';
import {
	filter,
	repeatWhen,
	share,
	shareReplay,
	switchMap,
	take,
	tap,
} from 'rxjs/operators';
import { PlConfigBranchListingBranchImpl } from '../../ple/plconfig/types/pl-config-branch';
import { BranchInfoService } from '../http/branch-info.service';
import { UiService } from '../ui/ui.service';

@Injectable({
	providedIn: 'root',
})
export class CurrentBranchInfoService {
	private readonly _currentBranchDetail = this._uiService.id.pipe(
		filter((val) => val !== '0'),
		switchMap((branchId) =>
			iif(
				() => branchId !== '0' && branchId !== '',
				this._branchService.getBranch(branchId).pipe(
					repeatWhen((_) => this._uiService.update),
					share()
				),
				of(new PlConfigBranchListingBranchImpl())
			)
		),
		share(),
		shareReplay({ bufferSize: 1, refCount: true })
	);
	constructor(
		private _branchService: BranchInfoService,
		private _uiService: UiService
	) {}

	get currentBranchDetail() {
		return this._currentBranchDetail;
	}

	commitBranch(body: { committer: string; archive: string }) {
		return this.currentBranchDetail.pipe(
			take(1),
			switchMap((detail) =>
				iif(
					() =>
						detail.parentBranch.id.length > 0 &&
						detail.id.length > 0,
					this._branchService
						.commitBranch(detail.id, detail.parentBranch.id, body)
						.pipe(
							tap((val) => {
								if (val.results.results.length > 0) {
									this._uiService.error =
										val.results.results[0];
								}
							})
						),

					of() // @todo replace with a false response
				)
			)
		);
	}
}

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
import { NamedIdAndDescription } from '@osee/shared/types';
import { difference } from '@osee/shared/types/change-report';
import { showable } from './base-types/showable';
import { extendedFeature, extendedFeatureWithChanges } from './features/base';
import { configGroup } from './pl-config-configurations';
import { branchInfo } from '@osee/shared/types';

export interface PlConfigApplicUIBranchMapping {
	associatedArtifactId: string;
	branch: branchInfo;
	editable: boolean;
	features: (extendedFeature | extendedFeatureWithChanges)[];
	groups: configGroup[];
	parentBranch: branchInfo;
	views: (view | viewWithChanges)[];
}
export class PlConfigApplicUIBranchMappingImpl
	implements PlConfigApplicUIBranchMapping
{
	associatedArtifactId: string = '-1';
	branch: branchInfo = {
		idIntValue: 0,
		name: '',
		id: '0',
		viewId: '-1',
	};
	editable: boolean = false;
	features: (extendedFeature | extendedFeatureWithChanges)[] = [];
	groups: configGroup[] = [];
	parentBranch: branchInfo = {
		idIntValue: 0,
		name: '',
		id: '0',
		viewId: '-1',
	};
	views: (view | viewWithChanges)[] = [];
}

export interface ConfigGroup extends NamedIdAndDescription, showable {}
export interface view extends NamedIdAndDescription, showable {
	hasFeatureApplicabilities: boolean;
	productApplicabilities?: string[];
}

export interface viewWithChanges extends view {
	deleted: boolean;
	added: boolean;
	changes: {
		name?: difference;
		hasFeatureApplicabilities?: difference;
		description?: difference<string>;
		productApplicabilities?: difference[];
	};
}
export interface viewWithChangesAndGroups extends viewWithChanges {
	groups: configGroup[];
}
export interface viewWithGroups extends view {
	groups: configGroup[];
}
export interface viewWithDescription extends view {
	groups: configGroup[];
	description: string;
}

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
//Base angular imports
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

//Base component imports
import { PlconfigRoutingModule } from './plconfig-routing.module';
import { PlconfigComponent } from './plconfig.component';

//Angular material imports & forms
import { MatRadioModule, MAT_RADIO_DEFAULT_OPTIONS } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
//import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';
import { FormsModule } from '@angular/forms';

//Sub-component imports
import { BranchTypeSelectorComponent } from './components/branch-type-selector/branch-type-selector.component';
import { BranchSelectorComponent } from './components/branch-selector/branch-selector.component';
import { ApplicabilityTableComponent } from './components/applicability-table/applicability-table.component';
import { ConfigurationDropdownComponent } from './components/configuration-dropdown/configuration-dropdown.component';
import { EditConfigurationDialogComponent } from './components/edit-config-dialog/edit-config-dialog.component';
import { AddConfigurationDialogComponent } from './components/add-configuration-dialog/add-configuration-dialog.component';
import { FeatureDropdownComponent } from './components/feature-dropdown/feature-dropdown.component';
import { AddFeatureDialogComponent } from './components/add-feature-dialog/add-feature-dialog.component';
import { EditFeatureDialogComponent } from './components/edit-feature-dialog/edit-feature-dialog.component';
import { ActionDropDownComponent } from './components/action-drop-down/action-drop-down.component';
import { CreateActionDialogComponent } from './components/create-action-dialog/create-action-dialog.component';
import { TransitionActionToReviewDialogComponent } from './components/transition-action-to-review-dialog/transition-action-to-review-dialog.component';
import { CommitBranchDialogComponent } from './components/commit-branch-dialog/commit-branch-dialog.component';
import { CopyConfigurationDialogComponent } from './components/copy-configuration-dialog/copy-configuration-dialog.component';
import { ConfigurationGroupDropdownComponent } from './components/configuration-group-dropdown/configuration-group-dropdown.component';
import { AddConfigurationGroupDialogComponent } from './components/add-configuration-group-dialog/add-configuration-group-dialog.component';
import { ConfigGroupDialogComponent } from './components/config-group-dialog/config-group-dialog.component';

import { PleSharedMaterialModule } from '../ple-shared-material/ple-shared-material.module'
import { DiffViewsModule } from '../diff-views/diff-views/diff-views.module';
import { FeatureMenuComponent } from './components/menus/feature-menu/feature-menu.component';
import { ArrayDiffMenuComponent } from './components/menus/array-diff-menu/array-diff-menu.component';
import { ConfigMenuComponent } from './components/menus/config-menu/config-menu.component';
import { ConfigGroupMenuComponent } from './components/menus/config-group-menu/config-group-menu.component';
import { ValueMenuComponent } from './components/menus/value-menu/value-menu.component';



@NgModule({
  declarations: [PlconfigComponent, BranchTypeSelectorComponent, BranchSelectorComponent, ApplicabilityTableComponent, ConfigurationDropdownComponent, EditConfigurationDialogComponent, AddConfigurationDialogComponent, FeatureDropdownComponent, AddFeatureDialogComponent, EditFeatureDialogComponent, ActionDropDownComponent, CreateActionDialogComponent, TransitionActionToReviewDialogComponent, CommitBranchDialogComponent, CopyConfigurationDialogComponent, ConfigurationGroupDropdownComponent, AddConfigurationGroupDialogComponent, ConfigGroupDialogComponent, FeatureMenuComponent, ArrayDiffMenuComponent, ConfigMenuComponent, ConfigGroupMenuComponent, ValueMenuComponent],
  imports: [
    CommonModule,
    FormsModule,
    MatRadioModule,
    MatSelectModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatInputModule,
    MatMenuModule,
    //MatButtonModule,
    PleSharedMaterialModule,
    MatDialogModule,
    MatSlideToggleModule,
    MatTooltipModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatListModule,
    DiffViewsModule,
    PlconfigRoutingModule
  ],
  providers: [{
    provide: MAT_RADIO_DEFAULT_OPTIONS,
    useValue: { color: 'primary' },
}]
})
export class PlconfigModule { }

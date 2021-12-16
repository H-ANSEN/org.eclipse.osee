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
import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CurrentGraphService } from '../../../services/current-graph.service';
import { map, share, shareReplay, switchMap, tap } from 'rxjs/operators';
import { transportType } from 'src/app/ple/messaging/shared/types/connection';
import { applic } from 'src/app/types/applicability/applic';
import { iif, of } from 'rxjs';

@Component({
  selector: 'osee-connectionview-base',
  templateUrl: './base.component.html',
  styleUrls: ['./base.component.sass']
})
export class BaseComponent implements OnInit {

  preferences = this.graphService.preferences;
  inEditMode = this.graphService.preferences.pipe(
    map((r) => r.inEditMode),
    share(),
    shareReplay(1)
  )
  inDiffMode = this.graphService.InDiff.pipe(
    switchMap((val) => iif(() => val, of('true'), of('false'))),
  );
  sideNav = this.graphService.sideNavContent;
  sideNavOpened = this.sideNav.pipe(
    map((value) => value.opened),
  )
  constructor (public dialog: MatDialog, private graphService: CurrentGraphService) {
   }
  
  ngOnInit(): void {}

  viewDiff(open:boolean,value:string|number|applic|transportType, header:string) {
    this.graphService.sideNav = { opened: open,field:header, currentValue: value };
  }

}

/*********************************************************************
 * Copyright (c) 2020 Robert Bosch Engineering and Business Solutions Ltd India
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Robert Bosch Engineering and Business Solutions Ltd India - initial API and implementation
 **********************************************************************/
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpEvent } from '@angular/common/http';

@Injectable()
export class UserDashboardService {

  constructor(private http: HttpClient) { }

  getUserSpecificTasks(artifact: any) {
    return this.http.post('service?url=/getproject/UserDashboard/getUserSpecificTasks', artifact);
  }

}

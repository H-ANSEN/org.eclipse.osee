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
import { of } from "rxjs";
import { UserDataAccountService } from "src/app/userdata/services/user-data-account.service";
import { testDataUser } from "./mockTypes";

export const userDataAccountServiceMock: Partial<UserDataAccountService> = {
    user:of(testDataUser)
  }
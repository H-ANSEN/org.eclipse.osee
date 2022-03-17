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
export interface logicalType {
    id: string,
    name: string,
    idString: string,
    idIntValue:number
}

export interface logicalTypeFormDetail extends logicalType {
    fields:logicalTypeFieldInfo[]
}
interface logicalTypeFieldInfo {
    attributeType: string,
    editable: boolean,
    name:string,
    required: boolean,
    defaultValue: string,
    value?:string
}
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
import { structure } from "../../types/structure";
import { elementsMock } from "./element.mock";

export const structuresMock: Required<structure>[] = [
    {
        id: "1",
        name: 'hello',
        description: '',
        interfaceMaxSimultaneity: "1",
        interfaceMinSimultaneity: "0",
        interfaceStructureCategory: "Miscellaneous",
        interfaceTaskFileType: 0,
        elements: elementsMock,
        numElements: 0,
        sizeInBytes: 0,
        bytesPerSecondMaximum: 0,
        bytesPerSecondMinimum: 0,
        applicability:{id:'1',name:'Base'}
    }
]

export const structuresMock2: Required<structure>[] = [
    {
        id: "2",
        name: 'hello',
        description: '',
        interfaceMaxSimultaneity: "1",
        interfaceMinSimultaneity: "0",
        interfaceStructureCategory: "Miscellaneous",
        interfaceTaskFileType: 0,
        elements: elementsMock,
        numElements: 0,
        sizeInBytes: 0,
        bytesPerSecondMaximum: 0,
        bytesPerSecondMinimum: 0,
        applicability:{id:'1',name:'Base'}
    }
]
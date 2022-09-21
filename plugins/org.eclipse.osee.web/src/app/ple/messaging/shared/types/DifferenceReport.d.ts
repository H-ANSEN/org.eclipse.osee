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
import { applic } from "src/app/types/applicability/applic";
import { changeInstance } from "src/app/types/change-report/change-report";

export interface DifferenceReport {
    changeItems: DifferenceReportItemMap,
    nodes: string[],
    connections: string[],
    messages: string[],
    subMessages: string[],
    structures: string[],
    elements: string[],
    enumSets: string[],
}

export interface DifferenceReportItemMap {
    [key: string]: DifferenceReportItem
}

export interface DifferenceReportItem {
    item: diffItem;
    changes: changeInstance[];
    parents: string[];
}

export interface nodeDiffItem {
    address: string,
    applicability: applic,
    color: string,
    description: string,
    id: string,
    name: string,
    diffInfo?: diffInfo
}

export interface connectionDiffItem {
    id: string,
    name: string,
    primaryNode: number,
    secondaryNode: number,
    applicability: applic,
    description: string,
    transportType: string,
    diffInfo?: diffInfo
}

export interface messageDiffItem {
    id: string,
    name: string,
    subMessages: any[],
    applicability: applic,
    initiatingNode: string | undefined | null,
    description: string,
    interfaceMessageNumber: string,
    interfaceMessageType: string,
    interfaceMessagePeriodicity: string,
    interfaceMessageRate: string,
    interfaceMessageWriteAccess: boolean,
    diffInfo?: diffInfo
}

export interface submessageDiffItem {
    id: string,
    name: string,
    applicability: applic,
    description: string,
    interfaceSubMessageNumber: string,
    diffInfo?: diffInfo
}

export interface structureDiffItem {
    id: string,
    name: string,
    elements: any[],
    applicability: applic,
    description: string,
    sizeInBytes: number,
    numElements: number,
    interfaceMaxSimultaneity: string,
    interfaceTaskFileType: number,
    incorrectlySized: boolean,
    interfaceMinSimultaneity: string,
    bytesPerSecondMinimum: number,
    bytesPerSecondMaximum: number,
    interfaceStructureCategory: string,
    elementChanges?: elementDiffItem[],
    diffInfo?: diffInfo
}

export interface elementDiffItem {
    id: string,
    name: string,
    beginByte: number,
    beginWord: number,
    applicability: applic,
    logicalType: string,
    autogenerated: boolean,
    units: string,
    description: string,
    interfacePlatformTypeDescription: string,
    interfacePlatformTypeDefaultValue: string,
    endByte: number,
    platformTypeId: number,
    endWord: number,
    notes: string,
    elementSizeInBytes: number,
    elementSizeInBits: number,
    interfaceElementIndexStart: number,
    interfaceElementAlterable: boolean,
    interfaceElementIndexEnd: number,
    platformTypeName2: string,
    interfacePlatformTypeMinval: string,
    interfacePlatformTypeMaxval: string,
    enumLiteral: string,
    enumeration?: string,
    diffInfo?: diffInfo
}

export interface platformTypeDiffItem {
    id: string,
    name: string,
    description: string,
    interfaceLogicalType: string,
    interfacePlatformTypeBitSize: string,
    interfacePlatformTypeDefaultValue: string,
    interfacePlatformTypeMaxval: string,
    interfacePlatformTypeMinval: string,
    interfacePlatformTypeUnits: string,
    interfacePlatformType2sComplement: string,
    interfacePlatformTypeAnalogAccuracy: string,
    interfacePlatformTypeBitsResolution: string,
    interfacePlatformTypeValidRangeDescription: string,
    interfacePlatformTypeCompRate: string,
    interfacePlatformTypeMsbValue: string,
    diffInfo?: diffInfo
}

export interface enumSetDiffItem {
    id: string,
    name: string,
    description: string,
    applicability: applic,
    enumerations: enumDiffItem[],
    diffInfo?: diffInfo
}

export interface enumDiffItem {
    id: string,
    applicability: applic,
    name: string,
    ordinal: number
}

export interface diffInfo {
    added: boolean,
    deleted: boolean,
    fieldsChanged: fieldsChanged,
    url: diffUrl
}

export interface diffUrl {
    label?: string,
    url?: string
}

export interface branchSummary {
    pcrNo: string,
    description: string,
    compareBranch: string,
    reportDate?: string,
}

export interface diffReportSummaryItem {
    id: string,
    changeType: string,
    action: string,
    name: string,
    details: string[]
}

export type diffItem = nodeDiffItem|connectionDiffItem|messageDiffItem|submessageDiffItem|structureDiffItem|elementDiffItem|platformTypeDiffItem|enumSetDiffItem;
export type diffItemKey = keyof nodeDiffItem|keyof connectionDiffItem|keyof messageDiffItem|keyof submessageDiffItem|keyof structureDiffItem|keyof elementDiffItem|keyof platformTypeDiffItem|keyof enumSetDiffItem;
export type fieldsChanged = Partial<Record<diffItemKey,string|number|boolean|undefined|null|applic>>;

export const enum DiffHeaderType {
    NODE = 'nodeDiff',
    CONNECTION = 'connectionDiff',
    MESSAGE = 'messageDiff',
    SUBMESSAGE = 'subMessageDiff',
    STRUCTURE = 'structureDiff',
    ELEMENT = 'elementDiff',
    ENUMSET = 'enumSetDiff'
}
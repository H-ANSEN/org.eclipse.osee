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
import { transportType } from '../types/transportType';

export const ethernet: Required<transportType> = {
    id: '1233456',
    name: 'Ethernet',
    byteAlignValidation: true,
    messageGeneration: false,
    byteAlignValidationSize: 4,
    messageGenerationType: '',
    messageGenerationPosition: ''
}
/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.iotbroker.common.util;

import java.util.Optional;

import com.minsait.onesait.platform.commons.model.AccessMode;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPMessageTypes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SSAP2PersintenceUtil {

	public static Optional<AccessMode> formSSAPMessageType2TableAccesMode(SSAPMessageTypes rType) {
		Optional<AccessMode> optional = Optional.empty();
		switch (rType) {
		case INSERT:
			optional = Optional.of(AccessMode.INSERT);
			break;
		case UPDATE:
			optional = Optional.of(AccessMode.UPDATE);
			break;
		case DELETE:
			optional = Optional.of(AccessMode.DELETE);
			break;
		case QUERY:
			optional = Optional.of(AccessMode.SELECT);
			break;
		default:
			log.error("formSSAPMessageType2TableAccesMode " + rType.toString() + " not in case");
			break;
		}

		return optional;
	}

}

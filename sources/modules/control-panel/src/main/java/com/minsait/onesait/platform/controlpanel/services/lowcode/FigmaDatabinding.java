/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.controlpanel.services.lowcode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class FigmaDatabinding {

	public enum DatabindingType {
		INPUT("quxDataBindingDefault"), OUTPUT("quxDataBindingOutput");

		String quxType;

		private DatabindingType(String databindingType) {
			quxType = databindingType;
		}

		String getDatabindingType() {
			return quxType;
		}

		public static DatabindingType fromQuxType(String quxType) {
			for (final DatabindingType d : DatabindingType.values()) {
				if (d.quxType.equalsIgnoreCase(quxType)) {
					return d;
				}
			}
			return null;
		}
	}

	private DatabindingType dataType;

	private String dataVarName;

	private String parentFrame;
}
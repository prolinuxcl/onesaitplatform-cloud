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
package com.minsait.onesait.platform.flowengine.api.rest.pojo;

import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.FlowDomain;
import com.minsait.onesait.platform.config.services.apimanager.operation.OperationJson;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class FlowEngineDeployerApis {
	@Getter
	@Setter
	Map<Api, List<OperationJson>> deployedApis;
	@Getter
	@Setter
	Map<String, List<OperationJson>> apiOperations;
	@Getter
	@Setter
	FlowDomain domain;
}

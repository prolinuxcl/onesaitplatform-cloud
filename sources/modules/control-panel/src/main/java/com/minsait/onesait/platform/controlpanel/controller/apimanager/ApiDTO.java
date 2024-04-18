/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.apimanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.UserApi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public class ApiDTO implements Serializable {

	public ApiDTO() {
	}

	public ApiDTO(Api api, List<ApiOperation> apiops, List<UserApi> usersapi) {
		id = api.getId();
		identification = api.getIdentification();
		version = api.getNumversion();
		type = api.getApiType().toString();
		isPublic = api.isPublic();
		category = api.getCategory().toString();
		if (type.contains("EXTERNAL")) {
			externalApi = true;
			ontologyId = null;
		} else {
			externalApi = false;
			if (type.equals(Api.ApiType.NODE_RED.toString()) && api.getOntology() == null) {
				ontologyId = null;
			} else {
				ontologyId = api.getOntology().getId();
			}
		}
		apiLimit = api.getApilimit();
		endpointExt = api.getEndpointExt();
		description = api.getDescription();
		metainf = api.getMetaInf();
		imageType = api.getImageType();
		status = api.getState().toString();
		creationDate = api.getCreatedAt().toString();
		userId = api.getUser().getUserId();
		swaggerJson = api.getSwaggerJson();
		authentications = new ArrayList<>();
		operations = new ArrayList<>();
		for (final UserApi apiauth : usersapi) {
			final UserApiDTO userapiDTO = new UserApiDTO(apiauth);
			authentications.add(userapiDTO);
		}
		for (final ApiOperation apiop : apiops) {
			final ApiOperationDTO apiopDTO = new ApiOperationDTO(apiop);
			operations.add(apiopDTO);
		}

	}

	private static final long serialVersionUID = 1L;

	@Schema(description = "API Id")
	@Getter
	@Setter
	private String id;

	@Schema(description = "API Identification", required = true)
	@Getter
	@Setter
	private String identification;

	@Schema(description = "API Version Number")
	@Getter
	@Setter
	private Integer version;

	@Schema(description = "API Type", required = true)
	@Getter
	@Setter
	private String type;

	@Schema(description = "API Public/Private")
	@Getter
	@Setter
	private Boolean isPublic;

	@Schema(description = "API Category", required = true)
	@Getter
	@Setter
	private String category;

	@Schema(description = "API External")
	@Getter
	@Setter
	private Boolean externalApi;

	@Schema(description = "Ontology Identification for OntologyAPI")
	@Getter
	@Setter
	private String ontologyId;

	@Schema(description = "QPS API limit")
	@Getter
	@Setter
	private Integer apiLimit;

	@Schema(description = "External Endpoint for invoking API")
	@Getter
	@Setter
	private String endpointExt;

	@Schema(description = "API Description")
	@Getter
	@Setter
	private String description;

	@Schema(description = "Tags Meta-inf for API", required = true)
	@Getter
	@Setter
	private String metainf;

	@Schema(description = "Image Type")
	@Getter
	@Setter
	private String imageType;

	@Schema(description = "API Status")
	@Getter
	@Setter
	private String status;

	@Schema(description = "creation Date", required = true)
	@Getter
	@Setter
	private String creationDate;

	@Schema(description = "API Propietary", required = true)
	@Getter
	@Setter
	private String userId;

	@Schema(description = "API Swagger Json", required = true)
	@Getter
	@Setter
	private String swaggerJson;

	@Schema(description = "API Operations")
	@Getter
	@Setter
	private ArrayList<ApiOperationDTO> operations;

	@Schema(description = "API Authentication")
	@Getter
	@Setter
	private ArrayList<UserApiDTO> authentications;

}

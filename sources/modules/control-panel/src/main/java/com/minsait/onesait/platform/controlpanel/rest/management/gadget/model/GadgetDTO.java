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
package com.minsait.onesait.platform.controlpanel.rest.management.gadget.model;

import java.util.List;

import javax.persistence.Lob;

import com.minsait.onesait.platform.controlpanel.rest.management.gadgettemplate.model.GadgetTemplateDTOList;

import lombok.Getter;
import lombok.Setter;

public class GadgetDTO {

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private GadgetDatasourceDTO datasource;

	@Lob
	@Getter
	@Setter
	private String config;

	@Lob
	@Getter
	@Setter
	private List<String> gadgetMeasures;

	@Getter
	@Setter
	private String type;

	@Getter
	@Setter
	private GadgetTemplateDTOList template;

	@Getter
	@Setter
	private String user;

	@Getter
	@Setter
	private Boolean instance;
	
	@Getter
	@Setter
	private String createdAt;

	@Getter
	@Setter
	private String updatedAt;
	
	@Getter
    @Setter
    private String category;
	
	@Getter
    @Setter
    private String subcategory;

}

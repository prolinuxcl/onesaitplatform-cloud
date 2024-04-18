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
package com.minsait.onesait.platform.controlpanel.rest.management.opendata.model;

import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataPackage;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataResource;

import lombok.Getter;
import lombok.Setter;

public class OpenDataResourceResponseDTO {
	
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
	private String format;

	@Getter
	@Setter
	private OpenDataDatasetResponseSimplifiedDTO dataset;
	
	@Getter
	@Setter
	private String url;
	
	
	public OpenDataResourceResponseDTO(OpenDataResource o, OpenDataPackage dataset) {
		this.id = o.getId();
		this.identification = o.getName();
		this.description = o.getDescription();
		this.dataset = new OpenDataDatasetResponseSimplifiedDTO(dataset);
		this.format = o.getFormat();
		this.url = o.getUrl();
	}

}

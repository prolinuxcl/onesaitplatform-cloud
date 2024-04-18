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
package com.minsait.onesait.platform.controlpanel.rest.management.ontology.model;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.AggregationFunction;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class OntologyTimeSeriesWindowDTO {

	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private AggregationFunction aggregationFunction;
	
	@NotNull
	@Getter
	@Setter
	private boolean bdh;
	
	@NotNull
	@Getter
	@Setter
	private int frecuency;
	
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private FrecuencyUnit frecuencyUnit;
	
	@Getter
	@Setter
	private int retentionBefore;
	
	@Getter
	@Setter
	private String retentionUnit;
	
	@NotNull
	@Getter
	@Setter
	private String windowType;
	
	@NotNull
	@Getter
	@Setter
	private String ontologTimeSeriesId;

	public OntologyTimeSeriesWindowDTO(OntologyTimeSeriesWindow ontologyTimeSeriesWindow) {
		this.aggregationFunction = ontologyTimeSeriesWindow.getAggregationFunction();
		this.bdh = ontologyTimeSeriesWindow.isBdh();
		this.frecuency = ontologyTimeSeriesWindow.getFrecuency();
		this.frecuencyUnit = ontologyTimeSeriesWindow.getFrecuencyUnit();
		if (ontologyTimeSeriesWindow.getRetentionBefore() != null)
			this.retentionBefore = ontologyTimeSeriesWindow.getRetentionBefore();
		if (ontologyTimeSeriesWindow.getRetentionUnit() != null)
			this.retentionUnit = ontologyTimeSeriesWindow.getRetentionUnit().name();
		this.windowType = ontologyTimeSeriesWindow.getWindowType().name();
	}

}

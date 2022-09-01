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
package com.minsait.onesait.platform.business.services.ontology.timeseries;

import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.dto.OntologyTimeSeriesServiceDTO;
import com.minsait.onesait.platform.config.services.ontology.dto.TimescaleContinuousAggregateRequest;

public interface TimeSeriesOntologyBusinessService {

	public Ontology createOntology(OntologyTimeSeriesServiceDTO ontology, OntologyConfiguration config,
			boolean parseProperties, boolean parseWindow) throws TimeSerieOntologyBusinessServiceException;

	public void updateOntology(OntologyTimeSeriesServiceDTO ontologyTimeSeriesDTO, String sessionUserId,
			OntologyConfiguration config, boolean hasDocuments) throws TimeSerieOntologyBusinessServiceException;

	public void createContinuousAggregate(String ontologyIdentification, String sessionUser, TimescaleContinuousAggregateRequest request) throws TimeSerieOntologyBusinessServiceException;
	
	public void deleteContinuousAggregate(String ontologyIdentification, String sessionUser, String name) throws TimeSerieOntologyBusinessServiceException;
	
	public void deleteOntology(String id, String userId);

}
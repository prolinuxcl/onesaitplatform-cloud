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
package com.minsait.onesait.platform.persistence.mindsdb.service;

import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.mindsdb.MindsDBPredictorGeneric;
import com.minsait.onesait.platform.commons.mindsdb.PredictorDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MindsDBPredictorGenericImpl implements MindsDBPredictorGeneric {

	@Override
	public void createPredictor(PredictorDTO predictor) {
		MindsDBPredictorManagerFacade.getInstanceBySource(predictor.getConnName()).createPredictor(predictor);
	}

}

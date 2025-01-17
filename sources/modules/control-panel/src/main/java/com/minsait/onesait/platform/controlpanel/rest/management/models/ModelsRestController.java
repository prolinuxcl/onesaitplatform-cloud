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
package com.minsait.onesait.platform.controlpanel.rest.management.models;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



@CrossOrigin(origins = "*")
@Tag(name = "Model Management")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
		@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
public interface ModelsRestController {

	public ResponseEntity<?> getByCategoryAndSubcategory(String category, String subcategory);

	public ResponseEntity<?> getByUserHeaderAndModelId(String modelName);

	public ResponseEntity<?> getByUserAndCategoryAndSubcaegory(String userId, String category, String subcategory);

	public ResponseEntity<?> getByUserAndModelId(String userId, String modelName);

	public ResponseEntity<?> executeModel(String userId, String params, String modelName, boolean returnData);

	public ResponseEntity<?> saveExecution(String userId, String params, String modelName, String executionName, String executionDescription, String executionId);

	public ResponseEntity<?> getExecutions(String userId);

	public ResponseEntity<?> showExecution(String userId, String executionName);

}

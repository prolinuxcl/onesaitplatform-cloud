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
package com.minsait.onesait.platform.api.processor.impl;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.minsait.onesait.platform.api.audit.aop.ApiManagerAuditable;
import com.minsait.onesait.platform.api.cache.ApiCacheService;
import com.minsait.onesait.platform.api.processor.ApiProcessor;
import com.minsait.onesait.platform.api.processor.ScriptProcessorFactory;
import com.minsait.onesait.platform.api.processor.utils.ApiProcessorUtils;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.impl.ApiServiceImpl.ChainProcessingStatus;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.model.InsertResult;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.Api.ApiType;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterService;

import jakarta.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InternalOntologyApiProcessor implements ApiProcessor {

	@Autowired
	private RouterService routerService;

	@Value("${onesaitplatform.apimanager.cacheable:false}")
	private static boolean cacheable;

	@Autowired
	private ApiCacheService apiCacheService;

	@Autowired
	private ScriptProcessorFactory scriptEngine;

	@Override
	@ApiManagerAuditable
	public Map<String, Object> process(Map<String, Object> data) throws GenericOPException {
		final StopWatch watch = new StopWatch();
		try {
			final Api api = (Api) data.get(Constants.API);
			if (api.getApicachetimeout() != null) {
				data = apiCacheService.getCache(data, api.getApicachetimeout());
			}

			if (data.get(Constants.OUTPUT) == null) {
				watch.start();
				data = processQuery(data);
				watch.stop();
				log.info("API Process in {} ms", watch.getTotalTimeMillis());
				watch.start();
				data = postProcess(data);
				watch.stop();
				log.info("API PostProcess in {} ms", watch.getTotalTimeMillis());
			}

			if (api.getApicachetimeout() != null) {
				apiCacheService.putCache(data, api.getApicachetimeout());
			}

			return data;
		} catch (final Exception e) {
			watch.stop();
			log.info("API Error Process in {} ms", watch.getTotalTimeMillis());
			throw new GenericOPException(e);
		}

	}

	@ApiManagerAuditable
	private Map<String, Object> processQuery(Map<String, Object> data) throws JSONException, JsonLdError {

		final Ontology ontology = (Ontology) data.get(Constants.ONTOLOGY);
		final User user = (User) data.get(Constants.USER);
		final String METHOD = (String) data.get(Constants.METHOD);
		final byte[] BODY = (byte[]) data.get(Constants.BODY);
		String queryType = (String) data.get(Constants.QUERY_TYPE);

		if (queryType.toUpperCase().indexOf("SQL") != -1) {
			queryType = "sql";
		}

		final String QUERY = (String) data.get(Constants.QUERY);
		final String OBJECT_ID = (String) data.get(Constants.OBJECT_ID);

		OperationType operationType = OperationType.GET;

		String body;
		if (METHOD.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
			body = QUERY;
			operationType = getOperationType(METHOD, QUERY);
		} else {
			operationType = getOperationType(METHOD, QUERY);
			body = BODY == null ? "" : new String(BODY);
		}

		if (data.get(Constants.CONTENT_TYPE_INPUT).toString().equalsIgnoreCase("application/ld+json")) {
			String context = ontology.getJsonLdContext();

			Document documentJson = JsonDocument.of(new ByteArrayInputStream(body.getBytes()));
			Document documentContext = JsonDocument.of(new ByteArrayInputStream(context.getBytes()));

			JsonObject jsonObject = JsonLd.compact(documentJson, documentContext).compactToRelative(false).get();
			JSONObject result = new JSONObject(jsonObject.toString());
			result.remove("@type");
			result.remove("@context");
			for (Iterator iterator = result.keys(); iterator.hasNext();) {
				String key = (String) iterator.next();
				JSONObject internal = (JSONObject) result.get(key);
				internal.remove("@type");
			}
			body = result.toString();
		}

		final OperationModel model = OperationModel
				.builder(ontology.getIdentification(), OperationType.valueOf(operationType.name()), user.getUserId(),
						OperationModel.Source.APIMANAGER)
				.body(body).queryType(QueryType.valueOf(queryType.toUpperCase())).objectId(OBJECT_ID).deviceTemplate("")
				.cacheable(cacheable).build();

		final NotificationModel modelNotification = new NotificationModel();
		modelNotification.setOperationModel(model);
		final OperationResultModel result = routerService.query(modelNotification);

		if (result.isStatus()) {
			processQueryResult(result, data, operationType);
		} else {
			data.put(Constants.STATUS, ChainProcessingStatus.STOP);
			final String messageError = ApiProcessorUtils.generateErrorMessage("ERROR Output from Router Processing",
					"Stopped Execution", result.getMessage() == null ? "Null Result From Router" : result.getMessage());
			data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
			data.put(Constants.REASON, messageError);
		}
		return data;

	}

	private String getQueryMethod(String query) {
		if (query.toLowerCase().indexOf("update") != -1) {
			return "PUT";
		} else if (query.toLowerCase().indexOf("delete") != -1 || query.toLowerCase().indexOf("remove") != -1) {
			return "DELETE";
		}
		return "QUERY";
	}

	private OperationType getOperationType(String method, String query) {
		OperationType operationType = null;
		if (method.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
			if (query.trim().toLowerCase().startsWith("update")
					|| query.trim().toLowerCase().indexOf(".update(") != -1) {
				operationType = OperationType.UPDATE;
			} else if (query.trim().toLowerCase().startsWith("delete")
					|| query.trim().toLowerCase().indexOf(".remove(") != -1) {
				operationType = OperationType.DELETE;
			} else {
				operationType = OperationType.QUERY;
			}
		} else if (method.equalsIgnoreCase(ApiOperation.Type.POST.name())) {
			operationType = OperationType.INSERT;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.PUT.name())) {
			operationType = OperationType.UPDATE;
		} else if (method.equalsIgnoreCase(ApiOperation.Type.DELETE.name())) {
			operationType = OperationType.DELETE;
		} else {
			operationType = OperationType.QUERY;
		}
		return operationType;
	}

	private void processQueryResult(OperationResultModel result, Map<String, Object> data, OperationType operationType)
			throws JSONException {

		String output = "";
		final String METHOD = (String) data.get(Constants.METHOD);
		final String OBJECT_ID = (String) data.get(Constants.OBJECT_ID);

		if ("ERROR".equals(result.getResult())) {
			data.put(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
			data.put(Constants.STATUS, ChainProcessingStatus.STOP);
			data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
			final String messageError = ApiProcessorUtils.generateErrorMessage("ERROR Output from Router Processing",
					"Stopped Execution, Error from Router", result.getMessage());
			data.put(Constants.REASON, messageError);
		} else {
			output = result.getResult();

			if (StringUtils.isEmpty(output) && !METHOD.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
				data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.NO_CONTENT);
				output = "{\"RESULT\" : \"NO RESOURCE FOUND WITH ID: " + OBJECT_ID + "\"}";
			} else if (operationType == OperationType.INSERT) {
				final JSONObject obj = new JSONObject(output);
				if (obj.has(InsertResult.DATA_PROPERTY)) {
					output = obj.get(InsertResult.DATA_PROPERTY).toString();
				}
			}

			data.put(Constants.OUTPUT, output);
		}

	}

	private Map<String, Object> postProcess(Map<String, Object> data) {

		final ApiOperation apiOperation = (ApiOperation) data.get(Constants.API_OPERATION);
		if (apiOperation != null) {
			String postProcessScript = apiOperation.getPostProcess();
			if (postProcessScript != null && !"".equals(postProcessScript)) {
				final User user = (User) data.get(Constants.USER);
				postProcessScript = postProcessScript.replace(Constants.CONTEXT_USER, user.getUserId());
				try {
					final Object result = scriptEngine.invokeScript(postProcessScript, data.get(Constants.OUTPUT));
					data.put(Constants.OUTPUT, result);
				} catch (final ScriptException e) {
					log.error("Execution logic for postprocess error", e);

					data.put(Constants.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
					data.put(Constants.STATUS, ChainProcessingStatus.STOP);
					data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);

					final String messageError = ApiProcessorUtils.generateErrorMessage(
							"ERROR from Scripting Post Process", "Execution logic for Postprocess error",
							e.getCause().getMessage());
					data.put(Constants.REASON, messageError);

				} catch (final Exception e) {
					log.error("Execution logic for postprocess error", e);

					data.put(Constants.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
					data.put(Constants.STATUS, ChainProcessingStatus.STOP);
					data.put(Constants.HTTP_RESPONSE_CODE, HttpStatus.INTERNAL_SERVER_ERROR);

					final String messageError = ApiProcessorUtils.generateErrorMessage(
							"ERROR from Scripting Post Process", "Exception detected", e.getCause().getMessage());
					data.put(Constants.REASON, messageError);

				}
			}
		}
		return data;

	}

	@Override
	public List<ApiType> getApiProcessorTypes() {
		return Arrays.asList(ApiType.IOT, ApiType.INTERNAL_ONTOLOGY);
	}

}

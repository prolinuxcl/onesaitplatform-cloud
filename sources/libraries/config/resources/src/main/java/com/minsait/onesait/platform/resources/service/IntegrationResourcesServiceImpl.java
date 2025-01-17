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
package com.minsait.onesait.platform.resources.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.components.AllConfiguration;
import com.minsait.onesait.platform.config.components.GlobalConfiguration;
import com.minsait.onesait.platform.config.components.ModulesUrls;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Lazy(value = true)
public class IntegrationResourcesServiceImpl implements IntegrationResourcesService {

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ActiveProfileDetector profileDetector;

	private String profile;

	private Urls urls;

	@Getter
	private GlobalConfiguration globalConfiguration;

	private static final String ENDPOINTS_PREFIX_FILE = "endpoints-";
	private static final String GLOBAL_CONFIG_PREFIX_FILE = "global-config-";
	private static final String YML_SUFFIX = ".yml";

	public enum ServiceUrl {
		BASE, ADVICE, ROUTER, HAWTIO, SWAGGERUI, API, SWAGGERUIMANAGEMENT, SWAGGERJSON, EMBEDDED, UI, GATEWAY,
		MANAGEMENT, DEPLOYMENT, URL, EDIT, VIEW, ONLYVIEW, PROXYURL, ADMIN;
	}
	public enum Module {
		IOTBROKER("iotbroker"), SCRIPTINGENGINE("scriptingEngine"), FLOWENGINE("flowEngine"),
		ROUTERSTANDALONE("routerStandAlone"), APIMANAGER("apiManager"), CONTROLPANEL("controlpanel"),
		DIGITALTWINBROKER("digitalTwinBroker"), DOMAIN("domain"), MONITORINGUI("monitoringUI"), GRAVITEE("gravitee"),
		RULES_ENGINE("rulesEngine"), BPM_ENGINE("bpmEngine"), NOTEBOOK("notebook"), DASHBOARDENGINE("dashboardEngine"),
		GIS_VIEWER("gisViewer"), REPORT_ENGINE("reportEngine"), OPEN_DATA("openData"), DATACLEANERUI("dataCleanerUI"),
		LOGCENTRALIZER("logCentralizer"), KEYCLOAK_MANAGER("keycloakManager"), EDGE("edge"), MINIO("minio"),
		PRESTO("presto"), PROMETHEUS("prometheus"), SERVERLESS("serverless"), MODELJSONLD("modeljsonld");


		String moduleString;

		Module(String module) {
			moduleString = module;
		}

		String getModule() {
			return moduleString;
		}
	}

	public static final String SWAGGER_UI_SUFFIX = "swagger-ui.html";
	public static final String LOCALHOST = "localhost";

	@PostConstruct
	public void getActiveProfile() {

		profile = profileDetector.getActiveProfile();
		loadConfigurations();
	}

	@Override
	public String getUrl(Module module, ServiceUrl service) {
		try {
			switch (module) {
			case CONTROLPANEL:
				switch (service) {
				case BASE:
					return urls.getControlpanel().getBase();
				default:
					break;
				}
				break;
			case IOTBROKER:
				switch (service) {
				case BASE:
					return urls.getIotbroker().getBase();
				case ADVICE:
					return urls.getIotbroker().getAdvice();
				default:
					break;
				}
				break;
			case SCRIPTINGENGINE:
				switch (service) {
				case BASE:
					return urls.getScriptingEngine().getBase();
				case ADVICE:
					return urls.getScriptingEngine().getAdvice();
				default:
					break;
				}
				break;
			case FLOWENGINE:
				switch (service) {
				case BASE:
					return urls.getFlowEngine().getBase();
				case ADVICE:
					return urls.getFlowEngine().getAdvice();
				case PROXYURL:
					return urls.getFlowEngine().getProxyurl();
				default:
					break;
				}
				break;
			case ROUTERSTANDALONE:
				switch (service) {
				case BASE:
					return urls.getRouterStandAlone().getBase();
				case ADVICE:
					return urls.getRouterStandAlone().getAdvice();
				case MANAGEMENT:
					return urls.getRouterStandAlone().getManagement();
				case ROUTER:
					return urls.getRouterStandAlone().getRouter();
				case HAWTIO:
					return urls.getRouterStandAlone().getHawtio();
				case SWAGGERUI:
					return urls.getRouterStandAlone().getSwaggerUI();
				default:
					break;
				}
				break;
			case APIMANAGER:
				switch (service) {
				case BASE:
					return urls.getApiManager().getBase();
				case API:
					return urls.getApiManager().getApi();
				case SWAGGERUI:
					return urls.getApiManager().getSwaggerUI();
				case SWAGGERUIMANAGEMENT:
					return urls.getApiManager().getSwaggerUIManagement();
				case SWAGGERJSON:
					return urls.getApiManager().getSwaggerJson();

				default:
					break;
				}
				break;
			case NOTEBOOK:
				switch (service) {
				case URL:
					return urls.getNotebook().getUrl();
				default:
					break;
				}
				break;
			case DIGITALTWINBROKER:
				if (service.equals(ServiceUrl.BASE)) {
					return urls.getDigitalTwinBroker().getBase();
				}

				break;
			case DOMAIN:
				if (service.equals(ServiceUrl.BASE)) {
					return urls.getDomain().getBase();
				}
				break;
			case MONITORINGUI:
				switch (service) {
				case BASE:
					return urls.getMonitoringUI().getBase();
				case EMBEDDED:
					return urls.getMonitoringUI().getEmbedded();
				default:
					break;
				}
				break;
			case RULES_ENGINE:
				switch (service) {
				case ADVICE:
					return urls.getRulesEngine().getAdvice();
				case BASE:
					return urls.getRulesEngine().getBase();
				default:
					break;
				}
				break;
			case REPORT_ENGINE:
				switch (service) {
				case BASE:
				default:
					return urls.getReportEngine().getBase();
				}
			case DASHBOARDENGINE:
				switch (service) {
				case EDIT:
					return urls.getDashboardEngine().getEdit();
				case VIEW:
					return urls.getDashboardEngine().getView();
				case ONLYVIEW:
					return urls.getDashboardEngine().getOnlyview();
				default:
					break;
				}
				break;
			case PROMETHEUS:
				switch (service) {
				case BASE:
					return urls.getPrometheus().getBase();
				default:
					break;
				}
			case MINIO:
				switch (service) {
				case BASE:
					return urls.getMinio().getBase();
				default:
					break;
				}
			case PRESTO:
				switch (service) {
				case BASE:
					return urls.getPresto().getBase();
				default:
					break;
				}
			case SERVERLESS:
				switch (service) {
				case BASE:
					return urls.getServerless().getBase();
				default:
					break;
				}
			case MODELJSONLD:
				switch (service) {
				case BASE:
					return urls.getModeljsonld().getBase();
				default:
					break;
				}
			default:
				break;
			}
		} catch (

		final Exception e) {
			log.error("Error : {}", e);
		}
		return "RESOURCE_URL_NOT_FOUND";
	}

	@Override
	public Map<String, String> getSwaggerUrls() {
		final Map<String, String> map = new HashMap<>();
		final String base = urls.getDomain().getBase();
		String controlpanel = base.endsWith("/") ? base.concat("controlpanel") : base.concat("/controlpanel");
		String iotbroker = base.endsWith("/") ? base.concat("iot-broker") : base.concat("/iot-broker");
		String apimanager = base.endsWith("/") ? base.concat("api-manager") : base.concat("/api-manager");
		String router = base.endsWith("/") ? base.concat("router") : base.concat("/router");
		String digitalTwinBroker = base.endsWith("/") ? base.concat("digitaltwinbroker")
				: base.concat("/digitaltwinbroker");
		if (base.contains(LOCALHOST)) {
			controlpanel = urls.getControlpanel().getBase();
			iotbroker = urls.getIotbroker().getBase();
			apimanager = urls.getApiManager().getBase();
			router = urls.getRouterStandAlone().getBase();
			digitalTwinBroker = urls.getDigitalTwinBroker().getBase();
		}
		map.put(Module.CONTROLPANEL.getModule(), controlpanel.endsWith("/") ? controlpanel.concat(SWAGGER_UI_SUFFIX)
				: controlpanel.concat("/").concat(SWAGGER_UI_SUFFIX));
		map.put(Module.IOTBROKER.getModule(), iotbroker.endsWith("/") ? iotbroker.concat(SWAGGER_UI_SUFFIX)
				: iotbroker.concat("/").concat(SWAGGER_UI_SUFFIX));
		map.put(Module.APIMANAGER.getModule(), apimanager.endsWith("/") ? apimanager.concat(SWAGGER_UI_SUFFIX)
				: apimanager.concat("/").concat(SWAGGER_UI_SUFFIX));
		map.put(Module.ROUTERSTANDALONE.getModule(),
				router.endsWith("/") ? router.concat(SWAGGER_UI_SUFFIX) : router.concat("/").concat(SWAGGER_UI_SUFFIX));
		map.put(Module.DIGITALTWINBROKER.getModule(),
				digitalTwinBroker.endsWith("/") ? digitalTwinBroker.concat(SWAGGER_UI_SUFFIX)
						: digitalTwinBroker.concat("/").concat(SWAGGER_UI_SUFFIX));

		return map;
	}

	@Override
	public void reloadConfigurations() {
		loadConfigurations();
	}

	private void loadConfigurations() {
		try {
			urls = configurationService.getEndpointsUrls(profile);
		} catch (final Exception e) {
			log.warn(
					"No configuration found for endpoints, using the one from classpath. If you are not running Config Init module, please contact with and administrator");
			urls = getConfigurationFromResource(ENDPOINTS_PREFIX_FILE + profile + YML_SUFFIX, ModulesUrls.class)
					.getOnesaitplatform().get("urls");
		}

		globalConfiguration = configurationService.getGlobalConfiguration(profile);
		if (globalConfiguration == null) {
			log.warn(
					"No OP global configuration found, using the one from classpath. If you are not running Config Init module, please contact with and administrator");
			globalConfiguration = getConfigurationFromResource(GLOBAL_CONFIG_PREFIX_FILE + profile + YML_SUFFIX,
					AllConfiguration.class).getOnesaitplatform();
		}

	}

	private <T> T getConfigurationFromResource(String name, Class<T> clazz) {
		final String config = loadFromResources(name);
		final Constructor constructor = new Constructor(AllConfiguration.class);
		final Yaml yaml = new Yaml(constructor);
		return yaml.loadAs(config, clazz);
	}

	private String loadFromResources(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(name).toURI())),
					StandardCharsets.UTF_8);

		} catch (final Exception e) {
			try {
				return new String(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(name)).getBytes(),
						StandardCharsets.UTF_8);
			} catch (final IOException e1) {
				log.error("**********************************************");
				log.error("Error loading resource: " + name + ".Please check if this error affect your database");
				log.error(e.getMessage());
				return null;
			}
		}
	}
}

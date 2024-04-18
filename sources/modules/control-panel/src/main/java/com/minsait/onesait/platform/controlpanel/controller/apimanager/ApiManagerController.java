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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.dto.ApiForList;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiOperation.Type;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.ApiQueryParameter.HeaderType;
import com.minsait.onesait.platform.config.model.ProjectResourceAccessParent.ResourceAccessType;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.UserApi;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPage;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.ApiPageResponse;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeApi;
import com.minsait.onesait.platform.controlpanel.gravitee.dto.GraviteeException;
import com.minsait.onesait.platform.controlpanel.helper.apimanager.ApiManagerHelper;
import com.minsait.onesait.platform.controlpanel.multipart.ApiMultipart;
import com.minsait.onesait.platform.controlpanel.services.gravitee.GraviteeService;
import com.minsait.onesait.platform.controlpanel.services.resourcesinuse.ResourcesInUseService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/apimanager")
@Slf4j
public class ApiManagerController {

	@Autowired
	private ApiManagerService apiManagerService;
	@Autowired
	private ApiManagerHelper apiManagerHelper;
	@Autowired
	private AppWebUtils utils;
	@Autowired(required = false)
	private GraviteeService graviteeService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private ResourcesInUseService resourcesInUseService;
	@Autowired
	private HttpSession httpSession;
	@Autowired
	private AppService appService;

	private static final String ERROR_403 = "error/403";
	private static final String ERROR_404 = "error/404";
	private static final String STATUS_OK = "{\"status\" : \"ok\"}";
	private static final String GRAVITEE_MANAGEMENT = "/management";
	private static final String GRAVITEE_APIS = "/apis";
	private static final String APP_ID = "appId";
	private static final String REDIRECT_PROJECT_SHOW = "redirect:/projects/update/";
	

	@GetMapping(value = "/create", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String createForm(Model model) {

		apiManagerHelper.populateApiManagerCreateForm(model);
		final Type[] crud = ApiOperation.Type.values();
		final HeaderType[] paramTypes = ApiQueryParameter.HeaderType.values();
		model.addAttribute("httpMethods", crud);
		model.addAttribute("paramTypes", paramTypes);
		model.addAttribute("applications",
				appService.getAllAppsList().stream().map(AppList::getIdentification).toList());

		final Object projectId = httpSession.getAttribute(APP_ID);
		if (projectId != null) {
			model.addAttribute(APP_ID, projectId.toString());
		}

		return "apimanager/create";
	}

	@GetMapping(value = "/update/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String updateForm(@PathVariable("id") String id, Model model) {
		if (!apiManagerService.hasUserEditAccess(id, utils.getUserId())
				|| !apiManagerService.isApiStateValidForEditAuth(id)) {
			return ERROR_403;
		}
		apiManagerHelper.populateApiManagerUpdateForm(model, id);

		model.addAttribute(ResourcesInUseService.RESOURCEINUSE, resourcesInUseService.isInUse(id, utils.getUserId()));
		model.addAttribute("applications",
				appService.getAllAppsList().stream().map(AppList::getIdentification).toList());
		resourcesInUseService.put(id, utils.getUserId());
		try {
			model.addAttribute("graviteeSwaggerDoc", getGraviteeSwaggerDoc(id));
		} catch (final Exception e) {
			model.addAttribute("graviteeSwaggerDoc", new ApiPageResponse());

		}

		return "apimanager/create";
	}

	@GetMapping(value = "/show/{id}", produces = "text/html")
	public String show(@PathVariable("id") String id, Model model) {
		if (!apiManagerService.hasUserAccess(id, utils.getUserId())) {
			return ERROR_403;
		}
		apiManagerHelper.populateApiManagerShowForm(model, id);
		return "apimanager/show";
	}

	@GetMapping(value = "/gravitee/{id}", produces = "text/html")
	public String graviteeStats(@PathVariable("id") String id, Model model, @RequestParam("iframe") String iframe) {
		if (!apiManagerService.hasUserAccess(id, utils.getUserId())) {
			return ERROR_403;
		}
		final Api api = apiManagerService.getById(id);
		if (null == graviteeService || null == api || !StringUtils.hasText(api.getGraviteeId())) {
			return ERROR_404;
		}
		final String url = graviteeService.getURLIframe(api.getGraviteeId(), iframe);
		apiManagerHelper.populateApiManagerShowForm(model, id);
		model.addAttribute("api", api);
		model.addAttribute("url", url);
		return "apimanager/gravitee";
	}

	@GetMapping(value = "/list", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER,ROLE_USER')")
	public String list(Model model, @RequestParam(required = false) String apiId,
			@RequestParam(required = false) String state, @RequestParam(required = false) String user) {
		try {

			// CLEANING APP_ID FROM SESSION
			httpSession.removeAttribute(APP_ID);

			apiManagerHelper.populateApiManagerListForm(model);
			final List<ApiForList> apis = apiManagerService.loadAPISByFilterForList(apiId, state, user,
					utils.getUserId());
			apis.stream().filter(a -> a.getGraviteeId() != null).forEach(a -> {
				a.setSync(graviteeService.isApiSync(a.getGraviteeId()));
			});
			model.addAttribute("apis", apis);
			if (graviteeService != null) {
				model.addAttribute("graviteeOn", true);
				addGraviteeUrls(model);
			}

		} catch (final Exception e) {
			log.error("Error at /controlpanel/apimanager/list {}", e);
		}

		return "apimanager/list";
	}

	@PostMapping(value = "/create")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String create(ApiMultipart api, BindingResult bindingResult, HttpServletRequest request,
			@RequestParam(required = false) String postProcessFx,
			@RequestParam(required = false, defaultValue = "false") Boolean publish2gravitee,
			@RequestParam(required = false, defaultValue = "false") Boolean graviteeJWTPlan,
			@RequestParam(required = false) String application, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some user properties missing");
			utils.addRedirectMessage("api.create.error", redirect);
			return "redirect:/apimanager/create";
		}

		try {
			final String operationsObject = request.getParameter("operationsObject");
			final String authenticationObject = request.getParameter("authenticationObject");

			final String apiId = apiManagerService.createApi(apiManagerHelper.apiMultipartMap(api), operationsObject,
					authenticationObject);
			if (StringUtils.hasText(postProcessFx)) {
				apiManagerService.updateApiPostProcess(apiId, postProcessFx);
			}

			if (graviteeService != null && publish2gravitee) {
				publish2Gravitee(apiId, graviteeJWTPlan, application);
			}

			final Object projectId = httpSession.getAttribute(APP_ID);
			if (projectId != null) {
				httpSession.setAttribute("resourceTypeAdded", OPResource.Resources.API.toString());
				httpSession.setAttribute("resourceIdentificationAdded", api.getIdentification());
				httpSession.removeAttribute(APP_ID);
				return REDIRECT_PROJECT_SHOW + projectId.toString();
			}

			return "redirect:/apimanager/list/";
		} catch (final Exception e) {
			log.error("Could not create API : {}", e);
			utils.addRedirectMessage("api.create.error", redirect);
			return "redirect:/apimanager/create";
		}
	}

	@PostMapping(value = "/update/{id}", produces = "text/html")
	public String update(@PathVariable("id") String id, ApiMultipart api, BindingResult bindingResult,
			@RequestParam(required = false) String operationsObject,
			@RequestParam(required = false) String authenticationObject,
			@RequestParam(required = false) String deprecateApis, @RequestParam(required = false) String postProcessFx,
			@RequestParam(required = false, defaultValue = "false") Boolean publish2gravitee,
			@RequestParam(required = false, defaultValue = "false") Boolean graviteeJWTPlan,
			@RequestParam(required = false) String application, RedirectAttributes redirect) {
		if (!apiManagerService.hasUserEditAccess(id, utils.getUserId())
				|| !apiManagerService.isApiStateValidForEdit(id)) {
			return ERROR_403;
		}
		if (bindingResult.hasErrors()) {
			utils.addRedirectMessage("api.update.error", redirect);
			return "redirect:/apimanager/update";
		}

		try {

			apiManagerService.updateApi(apiManagerHelper.apiMultipartMap(api), deprecateApis, operationsObject,
					authenticationObject);
			if (StringUtils.hasText(postProcessFx)) {
				apiManagerService.updateApiPostProcess(api.getId(), postProcessFx);
			}

			if (graviteeService != null) {
				final Api apiDB = apiManagerService.getById(api.getId());
				if (StringUtils.hasText(apiDB.getGraviteeId())) {
					graviteeService.updateApiFromSwagger(apiDB);
				}
				if (publish2gravitee) {
					publish2Gravitee(id, graviteeJWTPlan, application);
				}
				if (!publish2gravitee && graviteeJWTPlan) {
					graviteeService.createOauthPlan(apiDB.getGraviteeId(), application);
				}
			}
			resourcesInUseService.removeByUser(id, utils.getUserId());
			return "redirect:/apimanager/show/" + api.getId();
		} catch (final Exception e) {
			log.error("Could not update API: {}", e);
			utils.addRedirectMessage("api.update.error", redirect);
			return "redirect:/apimanager/update";
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	@Transactional
	public @ResponseBody String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		try {
			if (!apiManagerService.hasUserEditAccess(id, utils.getUserId())) {
				return ERROR_403;
			}
			final Api api = apiManagerService.getById(id);
			if (null != api) {
				apiManagerService.removeAPI(id);
				if (StringUtils.hasText(api.getGraviteeId()) && graviteeService != null) {
					graviteeService.deleteApi(api.getGraviteeId());
				}
			}

		} catch (final RuntimeException e) {
			utils.addRedirectException(e, redirect);
			return e.getMessage();
		} catch (final Exception e) {
			log.error("Exception reached " + e.getMessage(), e);
			utils.addRedirectMessage("apimanager.delete.error", redirect);
			return e.getMessage();
		}

		return "/controlpanel/apimanager/list";
	}

	// AUTHORIZATIONS//
	@GetMapping(value = "/authorize/list", produces = "text/html")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public String index(@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size, Model model) {
		apiManagerHelper.populateAutorizationForm(model);
		return "apimanager/authorize";
	}

	@PostMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<UserApiDTO> createAuthorization(@RequestParam String api, @RequestParam String user) {
		try {
			if (!apiManagerService.hasUserEditAccess(api, utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			final UserApi userApi = apiManagerService.updateAuthorization(api, user);
			final UserApiDTO userApiDTO = new UserApiDTO(userApi);

			return new ResponseEntity<>(userApiDTO, HttpStatus.CREATED);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/authorizationOnOntology", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<Void> userHasAuthorizationOnOntology(@RequestParam String api, @RequestParam String user) {
		try {

			if (apiManagerService.permision(api, user)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@PostMapping(value = "/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) {
		try {
			final UserApi userApi = apiManagerService.getUserApiAccessById(id);
			if (userApi == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			if (!apiManagerService.hasUserEditAccess(userApi.getApi().getId(), utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			apiManagerService.removeAuthorizationById(id);
			return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/token/list", produces = "text/html")
	public String token(@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "size", required = false) Integer size,
			@RequestParam(value = "access", required = false) String access, Model model, HttpServletRequest request) {
		apiManagerHelper.populateUserTokenForm(model, access);
		return "apimanager/token";
	}

	@GetMapping(value = "/invoke/{id}", produces = "text/html")
	public String invoker(Model model, @PathVariable("id") String apiId) {

		apiManagerHelper.populateApiManagerInvokeForm(model, apiId);

		return "apimanager/invoke";
	}

	@PostMapping(value = "numVersion")
	public @ResponseBody Integer numVersion(@RequestBody String numversionData) {
		return apiManagerService.calculateNumVersion(numversionData);
	}

	@GetMapping(value = "/{id}/getImage")
	public void showImg(@PathVariable("id") String id, HttpServletResponse response) {
		final byte[] buffer = apiManagerService.getImgBytes(id);
		if (buffer.length > 0) {
			try (OutputStream output = response.getOutputStream();) {
				response.setContentLength(buffer.length);
				output.write(buffer);
			} catch (final Exception e) {
				log.error("showImg error: " + e.getMessage());
			}
		}
	}

	@RequestMapping("/gravitee/im/recreate")
	public ResponseEntity<String> recreateGraviteeIM() {
		if (graviteeService != null) {
			graviteeService.createUpdateIdentityProvider();
		}
		return ResponseEntity.ok().build();

	}

	@GetMapping(value = "/updateState/{id}/{state}")
	public String updateState(@PathVariable("id") String id, @PathVariable("state") String state, Model uiModel)
			throws GenericOPException {
		if (!apiManagerService.hasUserEditAccess(id, utils.getUserId())) {
			return ERROR_403;
		}
		apiManagerService.updateState(id, state);
		final Api api = apiManagerService.getById(id);
		if (StringUtils.hasText(api.getGraviteeId())) {
			switch (api.getState()) {
			case PUBLISHED:
			case DEPRECATED:
				graviteeService.changeLifeCycleState(api.getGraviteeId(), api.getState());
				break;
			case DELETED:
				graviteeService.changeLifeCycleState(api.getGraviteeId(), api.getState());
				break;
			default:
				break;
			}
		}
		return "redirect:/apimanager/list";
	}

	@PostMapping(value = "/generateToken")
	public @ResponseBody ResponseEntity<String> generateToken() {
		try {
			apiManagerService.generateToken(utils.getUserId());
			return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(value = "/removeToken")
	public @ResponseBody ResponseEntity<String> removeToken(@RequestBody String token) {
		try {
			apiManagerService.removeToken(utils.getUserId(), token);
			return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
		} catch (final Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	private void addGraviteeUrls(Model model) {

		final String UI = resourcesService.getUrl(Module.GRAVITEE, ServiceUrl.UI);
		model.addAttribute("graviteeUI", UI);

	}

	private void publish2Gravitee(String apiId, boolean jwtPlan, String clientId) throws GenericOPException {
		final Api apiDb = apiManagerService.getById(apiId);
		try {
			final GraviteeApi graviteeApi = graviteeService.processApi(apiDb, jwtPlan, clientId);
			apiDb.setGraviteeId(graviteeApi.getApiId());
			apiManagerService.updateApi(apiDb);
		} catch (final GraviteeException e) {
			log.error("Could not publish API to Gravitee {}", e.getMessage());
		}
	}

	@GetMapping(value = "/freeResource/{id}")
	public @ResponseBody void freeResource(@PathVariable("id") String id) {
		resourcesInUseService.removeByUser(id, utils.getUserId());
		log.info("free resource", id);
	}

	@PostMapping(value = "/clone")
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public @ResponseBody ResponseEntity<String> cloneApi(Model model, @RequestParam String id,
			@RequestParam String identification, RedirectAttributes redirect) {
		try {

			final String apiId = apiManagerService.cloneApi(id, identification, utils.getUserId());
			if (graviteeService != null && apiManagerService.isGraviteeApi(id)) {
				// TO-DO Clone gravitee api
				publish2Gravitee(apiId, false, null);
			}

			return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
		} catch (final Exception e) {
			log.error("Error clonning API : {}", e);
			final Map<String, String> response = new HashMap<>();
			response.put("status", "error");
			response.put("cause", e.getMessage());
			return new ResponseEntity<>(response.toString(), HttpStatus.BAD_REQUEST);
		}
	}

	private ApiPageResponse getGraviteeSwaggerDoc(String apiId) throws GenericOPException {
		final Api apiDb = apiManagerService.getById(apiId);
		final String apiGraviteeId = apiDb.getGraviteeId();

		if (graviteeService != null && apiManagerService.isGraviteeApi(apiId)) {
			final List<ApiPageResponse> list = graviteeService.getPublishedApiPages(apiGraviteeId);
			if (!list.isEmpty()) {
				return list.stream().filter(apr -> apr.getType().equals(ApiPage.Type.SWAGGER.name())
						&& apr.getName().equalsIgnoreCase("swagger")).findFirst().orElse(new ApiPageResponse());
			}
		}

		return new ApiPageResponse();
	}

	@PostMapping(value = "/updateGraviteeSwaggerDoc", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DEVELOPER')")
	public ResponseEntity<String> updateGraviteeSwagger(@RequestParam String apiId, @RequestParam String content) {
		try {
			if (!apiManagerService.hasUserEditAccess(apiId, utils.getUserId())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			updateGraviteeSwaggerDoc(apiId, content);

			return new ResponseEntity<>(STATUS_OK, HttpStatus.OK);
		} catch (final RuntimeException | GenericOPException e) {
			log.error("Error updating Gravitee Swagger documentation : {}", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	private ApiPageResponse updateGraviteeSwaggerDoc(String apiId, String content) throws GenericOPException {
		final Api apiDb = apiManagerService.getById(apiId);
		final String apiGraviteeId = apiDb.getGraviteeId();

		if (graviteeService != null && apiManagerService.isGraviteeApi(apiId)) {
			return graviteeService.processUpdateAPIDocs(apiDb, content);
		}

		return new ApiPageResponse();
	}

	@GetMapping(value = "/token/list")
	public @ResponseBody List<String> getUserTokens() {
		return apiManagerHelper.getUserTokenList();
	}

}

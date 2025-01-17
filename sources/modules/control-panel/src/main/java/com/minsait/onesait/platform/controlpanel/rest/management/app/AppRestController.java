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
package com.minsait.onesait.platform.controlpanel.rest.management.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppList;
import com.minsait.onesait.platform.config.model.AppRole;
import com.minsait.onesait.platform.config.model.AppRoleList;
import com.minsait.onesait.platform.config.model.AppRoleListOauth;
import com.minsait.onesait.platform.config.model.AppUserListOauth;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.AppUserRepository;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.app.dto.Realm;
import com.minsait.onesait.platform.config.services.app.dto.RealmAssociation;
import com.minsait.onesait.platform.config.services.app.dto.RealmCreate;
import com.minsait.onesait.platform.config.services.app.dto.RealmRole;
import com.minsait.onesait.platform.config.services.app.dto.RealmUpdate;
import com.minsait.onesait.platform.config.services.app.dto.RealmUser;
import com.minsait.onesait.platform.config.services.app.dto.RealmUserAuth;
import com.minsait.onesait.platform.config.services.app.dto.UserAppCreateDTO;
import com.minsait.onesait.platform.config.services.exceptions.AppServiceException;
import com.minsait.onesait.platform.config.services.exceptions.UserServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.controller.dashboard.dto.UserDTO;
import com.minsait.onesait.platform.controlpanel.helper.app.AppHelper;
import com.minsait.onesait.platform.controlpanel.rest.management.model.ErrorValidationResponse;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Realm Management")
@RestController
@RequestMapping("api/realms")
@ApiResponses({ @ApiResponse(responseCode = "400", description = "Bad request"),
	@ApiResponse(responseCode = "500", description = "Internal server error"), @ApiResponse(responseCode = "403", description = "Forbidden") })
public class AppRestController {

	private static final String USER_STR = "User \"";
	private static final String NOT_EXIST = "\" does not exist in realm";
	private static final String REALM_STR = "Realm \"";
	private static final String NOT_AUTH = "\" not authorized";
	private static final String IN_REALM_STR = "\" in Realm \"";

	private static final String NOT_FOUND = "\" not found";

	@Autowired
	private AppService appService;
	@Autowired
	private AppHelper appHelper;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private AppUserRepository appUserRepository;


	@Operation(summary = "Get single realm info")
	@GetMapping("/{identification}")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=Realm.class)), responseCode = "200", description = "OK"))
	@Transactional
	public ResponseEntity<?> getRealm(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification) {

		final App app = appService.getAppByIdentification(identification);
		if (app != null) {
			final Realm realm = appService.getRealmByAppIdentification(app.getId());
			// user not administrator and not owner is not allowed to get
			if (!utils.isAdministrator()
					&& (null == realm.getUser() || !realm.getUser().getUserId().equals(utils.getUserId()))) {
				return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}
			return new ResponseEntity<>(realm, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Get realm's configured user extra fields")
	@GetMapping("/{identification}" + "/user-extra-fields")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=JsonNode.class)), responseCode = "200", description = "OK"))
	@Transactional
	public ResponseEntity<?> getRealmUserExtraFields(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification)
					throws IOException {

		final App realm = appService.getAppByIdentification(identification);
		if (realm != null) {
			// user not administrator and not owner is not allowed to get
			if (!utils.isAdministrator()
					&& (null == realm.getUser() || !realm.getUser().getUserId().equals(utils.getUserId()))) {
				return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}

			if (StringUtils.isEmpty(realm.getUserExtraFields())) {
				return new ResponseEntity<>(REALM_STR + identification + "\" does not have user extra fields defined",
						HttpStatus.OK);
			} else {
				return new ResponseEntity<>(mapper.readValue(realm.getUserExtraFields(), JsonNode.class),
						HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Get all active realm users filtered by 'extra_fields' attribute")
	@GetMapping("/{identification}/users/filter/extra-fields/{jsonPath}/{value}")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=RealmUser[].class)), responseCode = "200", description = "OK"))
	public ResponseEntity<?> getAllFilter(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification,
			@PathVariable(value = "jsonPath") String jsonPath, @PathVariable(value = "value") String value,
			@RequestParam(required = false, name = "realmRole") String realmRole) {

		final AppList appDb = appService.getAppListByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Realm's users
		if (!utils.isAdministrator() && !appDb.getUser().getUserId().equals(utils.getUserId())
				&& !appService.isUserInApp(utils.getUserId(), appDb.getIdentification())) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}
		final List<RealmUser> users = new ArrayList<>();
		List<RealmUser> filteredUsers = new ArrayList<>();
		final List<AppRoleList> arl = appService.getRolesByAppIdentification(identification);

		arl.forEach(r -> users.addAll(r.getAppUsers().stream().filter(u -> {
			if (!StringUtils.isEmpty(u.getUser().getExtraFields())) {
				try {
					final String formatedJsonPath = jsonPath.startsWith("$.") ? jsonPath : "$." + jsonPath;
					final Object v = JsonPath.parse(u.getUser().getExtraFields()).read(formatedJsonPath, Object.class);
					final Pattern p = Pattern.compile(value);
					final Matcher m = p.matcher(String.valueOf(v));
					return m.matches();
				} catch (final Exception e) {
					log.debug("Error while reading extra_fields for user {}", u.getUser().getUserId(), e);
					return false;
				}

			}
			return false;
		}).map(u -> RealmUser.builder().avatar(u.getUser().getAvatar()).extraFields(u.getUser().getExtraFields())
				.fullName(u.getUser().getFullName()).mail(u.getUser().getEmail()).role(u.getRole().getName())
				.username(u.getUser().getUserId()).build()).collect(Collectors.toList())));
		for (final RealmUser realmUser : users) {
			usersWithRole(realmUser, filteredUsers);
		}
		if (!StringUtils.isEmpty(realmRole)) {
			filteredUsers = filteredUsers.stream().filter(ru -> ru.getRole().contains(realmRole))
					.collect(Collectors.toList());
		}
		return new ResponseEntity<>(filteredUsers, HttpStatus.OK);

	}

	@Operation(summary = "Get all active realm users filtered by 'extra_fields' attribute and Json Path expression language")
	@PostMapping("/{identification}/users/filter/extra-fields/json-path-evaluation")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=RealmUser[].class)), responseCode = "200", description = "OK"))
	public ResponseEntity<?> getAllFilterJsonPathExpression(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification,
			@RequestBody String jsonPathExpression,
			@RequestParam(required = false, name = "realmRole") String realmRole) {

		final AppList appDb = appService.getAppListByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Realm's users
		if (!utils.isAdministrator() && !appDb.getUser().getUserId().equals(utils.getUserId())
				&& !appService.isUserInApp(utils.getUserId(), appDb.getIdentification())) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}
		final List<RealmUser> users = new ArrayList<>();
		List<RealmUser> filteredUsers = new ArrayList<>();
		final List<AppRoleList> arl = appService.getRolesByAppIdentification(identification);

		arl.forEach(r -> users.addAll(r.getAppUsers().stream().filter(u -> {
			if (!StringUtils.isEmpty(u.getUser().getExtraFields())) {
				try {
					final List<Map<String, Object>> results = JsonPath.parse(u.getUser().getExtraFields()).read(jsonPathExpression);
					return !results.isEmpty();
				} catch (final Exception e) {
					log.debug("Error while reading extra_fields for user {}", u.getUser().getUserId(), e);
					return false;
				}

			}
			return false;
		}).map(u -> RealmUser.builder().avatar(u.getUser().getAvatar()).extraFields(u.getUser().getExtraFields())
				.fullName(u.getUser().getFullName()).mail(u.getUser().getEmail()).role(u.getRole().getName())
				.username(u.getUser().getUserId()).build()).collect(Collectors.toList())));
		for (final RealmUser realmUser : users) {
			usersWithRole(realmUser, filteredUsers);
		}
		if (!StringUtils.isEmpty(realmRole)) {
			filteredUsers = filteredUsers.stream().filter(ru -> ru.getRole().contains(realmRole))
					.collect(Collectors.toList());
		}
		return new ResponseEntity<>(filteredUsers, HttpStatus.OK);

	}

	@Operation(summary = "Update realm's user extra fields JSON config")
	@PatchMapping("/{identification}" + "/user-extra-fields")
	@Transactional
	public ResponseEntity<String> patchRealmUserExtraFields(
			@ApiParam("Realm user's extra fields") @RequestBody String userExtraFields,
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification,
			Errors errors) {

		final App realm = appService.getAppByIdentification(identification);
		if (realm != null) {

			// user not administrator and not owner is not allowed to update
			if (!utils.isAdministrator()
					&& (null == realm.getUser() || !realm.getUser().getUserId().equals(utils.getUserId()))) {
				return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}

			try {
				final JsonNode extrasJson = mapper.readTree(userExtraFields);
				realm.setUserExtraFields(mapper.writeValueAsString(extrasJson));
			} catch (final IOException e) {
				return new ResponseEntity<>("Input is not valid JSON", HttpStatus.BAD_REQUEST);
			}
			appService.updateApp(realm);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Get all realms info")
	@GetMapping
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=Realm[].class)), responseCode = "200", description = "OK"))
	@Transactional
	public ResponseEntity<?> getRealms() {
		final List<AppRole> allRoles = appService.getAllRoles();
		if (utils.isAdministrator()) {
			final List<Realm> realms = appService.getAllApps().stream()
					.map(a -> new Realm(appUserRepository, a, allRoles)).collect(Collectors.toList());
			return new ResponseEntity<>(realms, HttpStatus.OK);
		} else {
			final List<Realm> realms = appService.getAppsByUser(utils.getUserId(), null).stream()
					.map(a -> new Realm(appUserRepository, a, allRoles)).collect(Collectors.toList());
			return new ResponseEntity<>(realms, HttpStatus.OK);
		}
	}

	@Operation(summary = "Create a realm")
	@PostMapping
	public ResponseEntity<?> create(@Parameter(description= "Realm", required = true) @RequestBody @Valid RealmCreate realm,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final App app = new App();
		try {
			app.setIdentification(getRealmIdentification(realm));
		} catch (final Exception e) {
			return new ResponseEntity<>("Error assigning identification", HttpStatus.BAD_REQUEST);
		}

		if (!app.getIdentification().matches(AppWebUtils.IDENTIFICATION_PATERN)) {
			return new ResponseEntity<>("Identification Error: Use alphanumeric characters and '-', '_'",
					HttpStatus.BAD_REQUEST);
		}

		app.setDescription(realm.getDescription());
		app.setUser(userService.getUser(utils.getUserId()));
		if (null != realm.getSecret()) {
			app.setSecret(realm.getSecret());
		} else {
			app.setSecret(null);
		}
		if (null != realm.getTokenValiditySeconds()) {
			app.setTokenValiditySeconds(realm.getTokenValiditySeconds());
		}
		try {
			realm.getRoles().stream().map(r -> realmRole2AppRole(r, app)).forEach(r -> app.getAppRoles().add(r));
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		appService.createApp(app);
		return new ResponseEntity<>(HttpStatus.CREATED);

	}

	private String getRealmIdentification(RealmCreate realm) throws Exception {
		if (realm.getIdentification() != null) {
			return realm.getIdentification();
		}

		if (realm.getRealmId() != null) {
			return realm.getRealmId();
		}
		if (realm.getName() != null) {
			return realm.getName();
		}
		throw new Exception();
	}

	@Operation(summary = "Updates a realm")
	@PutMapping("/{identification}")
	public ResponseEntity<?> update(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "New Realm Description", required = true) @RequestBody @Valid RealmUpdate realm,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		try {
			final AppList appDb = appService.getAppListByIdentification(identification);

			if (appDb == null) {
				return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
			}

			// user not administrator and not owner is not allowed to update
			if (!utils.isAdministrator()
					&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
				return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
			}

			if (null != realm.getDescription()) {
				appDb.setDescription(realm.getDescription());
			}

			if (null != realm.getSecret()) {
				appDb.setSecret(realm.getSecret());
			} else {
				appDb.setSecret(null);
			}
			if (null != realm.getTokenValiditySeconds()) {
				appDb.setTokenValiditySeconds(realm.getTokenValiditySeconds());
			}
			appService.updateApp(appDb);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Deletes a realm")
	@DeleteMapping("/{identification}")
	public ResponseEntity<?> delete(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to delete
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		try {
			appService.deleteApp(appDb);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final RuntimeException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Get Authorizations for a realm")
	@GetMapping("/{identification}/authorizations/{filter}")
	public ResponseEntity<Object> getAuthorizations(@PathVariable("identification") String identification,
			@PathVariable(required = false, value = "filter") String filter) {
		final AppList appDb = appService.getAppListByIdentification(identification);
		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.NOT_FOUND);
		}
		if (!utils.isAdministrator() && !appDb.getUser().getUserId().equals(utils.getUserId())
				&& !appService.isUserInApp(utils.getUserId(), appDb.getIdentification())) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}
		final List<UserAppCreateDTO> authorizations = appHelper.getAuthorizations(identification, filter);

		return ResponseEntity.ok().body(authorizations);

	}

	@Operation(summary = "Authorizes user with a role in a existing Realm")
	@PostMapping("/authorization")
	@Transactional
	public ResponseEntity<?> createAuthorization(
			@Parameter(description= "Realm Authorization", required = true) @Valid @RequestBody RealmUserAuth authorization,
			Errors errors) {
		log.debug("New realm authorization with user: {} and realm: {}", authorization.getUserId(),
				authorization.getRealmId());
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final AppList appDb = appService.getAppListByIdentification(authorization.getRealmId());

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + authorization.getRealmId() + NOT_EXIST, HttpStatus.NOT_FOUND);
		}

		final User user = userService.getUserByIdentification(authorization.getUserId());
		// user not administrator and not owner is not allowed to authorize
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		if (user == null) {
			log.warn("End realm authorization user does not exist");
			return new ResponseEntity<>(USER_STR + authorization.getUserId() + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
		try {
			final AppRoleListOauth role = appService.getByRoleNameAndAppListOauth(authorization.getRoleName(), appDb);
			if (role == null) {
				return new ResponseEntity<>("Role \"" + authorization.getRoleName() + "\" does not exist in Realm "
						+ authorization.getRealmId(), HttpStatus.BAD_REQUEST);
			}
			final List<AppUserListOauth> authList = appService.getAppUsersByUserIdAndRoleAndApp(authorization.getUserId(),
					role.getName(), appDb.getIdentification());
			if (!authList.isEmpty()) {
				return new ResponseEntity<>("This association already exists.", HttpStatus.BAD_REQUEST);
			}
			appService.createUserAccess(appDb.getId(), authorization.getUserId(), role.getId());
			log.debug("End realm authorization successfully");
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (final AppServiceException e) {
			log.warn("End realm authorization with exception: {}", e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Invalidates user authorization for a Realm")
	@DeleteMapping("/authorization/realm/{identification}/user/{userId}")
	@Transactional
	public ResponseEntity<?> deleteAuthorization(
			@Parameter(description= "Realm identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "User id", required = true) @PathVariable("userId") String userId) {
		log.debug("Removing realm authorization for user: {} and realm: {}", userId, identification);
		final AppList appDb = appService.getAppListByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to unauthorize
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		try {
			appService.removeAuthorizations(userId, identification);
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Creates a realm association given parent and child realms, as well as respective roles")
	@PostMapping("/association")
	@Transactional
	public ResponseEntity<?> createAssociation(
			@Parameter(description= "Realm Association", required = true) @Valid @RequestBody RealmAssociation association,
			Errors errors) {
		if (errors.hasErrors()) {
			return ErrorValidationResponse.generateValidationErrorResponse(errors);
		}

		final App appDbChild = appService.getAppByIdentification(association.getChildRealmId());
		final App appDbParent = appService.getAppByIdentification(association.getParentRealmId());

		if (appDbChild == null || appDbParent == null) {
			return new ResponseEntity<>("Any of the specified realms does not exist", HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to associate this realms
		if (!utils.isAdministrator() && (null == appDbChild.getUser()
				|| !appDbChild.getUser().getUserId().equals(utils.getUserId()) || null == appDbParent.getUser()
				|| !appDbParent.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		if (null == appService.getByRoleNameAndApp(association.getParentRoleName(), appDbParent)
				|| null == appService.getByRoleNameAndApp(association.getChildRoleName(), appDbChild)) {
			return new ResponseEntity<>("Any Role does not exists.", HttpStatus.BAD_REQUEST);
		}

		try {
			appService.createAssociation(association.getParentRoleName(), association.getChildRoleName(),
					association.getParentRealmId(), association.getChildRealmId());
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Deletes a realm's association")
	@DeleteMapping("/association/parent-realm/{parentRealmId}/parent-role/{parentRole}/child-realm/{childRealmId}/child-role/{childRole}")
	@Transactional
	public ResponseEntity<?> deleteAssociation(
			@Parameter(description= "Parent Realm id", required = true) @PathVariable("parentRealmId") String parentRealmId,
			@Parameter(description= "Child Realm id", required = true) @PathVariable("childRealmId") String childRealmId,
			@Parameter(description= "Parent role", required = true) @PathVariable("parentRole") String parentRole,
			@Parameter(description= "Child role", required = true) @PathVariable("childRole") String childRole) {

		final App appDbChild = appService.getAppByIdentification(childRealmId);
		final App appDbParent = appService.getAppByIdentification(parentRealmId);

		if (appDbChild == null || appDbParent == null) {
			return new ResponseEntity<>("Any of the specified realms does not exist", HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to delete an association
		// of this realms
		if (!utils.isAdministrator() && (null == appDbChild.getUser()
				|| !appDbChild.getUser().getUserId().equals(utils.getUserId()) || null == appDbParent.getUser()
				|| !appDbParent.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		if (null == appService.getByRoleNameAndApp(parentRole, appDbParent)
				|| null == appService.getByRoleNameAndApp(childRole, appDbChild)) {
			return new ResponseEntity<>("Any Role does not exists.", HttpStatus.BAD_REQUEST);
		}

		try {
			appService.deleteAssociation(parentRole, childRole, parentRealmId, childRealmId);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Get all roles in a Realm")
	@GetMapping("/{identification}/roles")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=RealmRole[].class)), responseCode = "200", description = "OK"))
	@Transactional
	public ResponseEntity<?> getRoles(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification) {

		final AppList appDb = appService.getAppListByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Roles
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final List<RealmRole> roles = appService.getAppRolesListOauth(identification).stream()
				.map(r -> new RealmRole(r.getName(), r.getDescription())).collect(Collectors.toList());
		return new ResponseEntity<>(roles, HttpStatus.OK);
	}

	@Operation(summary = "Creates a role in a Realm")
	@PostMapping("/{identification}/roles")
	public ResponseEntity<String> addRole(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "Realm role", required = true) @Valid @RequestBody RealmRole role) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}
		if (appDb.getAppRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(role.getName()))) {
			return new ResponseEntity<>("Role already exists wiht name " + role.getName(), HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Roles
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final AppRole newRole = new AppRole();
		newRole.setApp(appDb);
		newRole.setName(role.getName());
		newRole.setDescription(role.getDescription());
		appDb.getAppRoles().add(newRole);
		appService.updateApp(appDb);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Operation(summary = "Deletes a role in a Realm")
	@DeleteMapping("/{identification}/roles/{roleName}")
	public ResponseEntity<?> deleteRole(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "Role name", required = true) @PathVariable("roleName") String roleName) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Roles
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final AppRole role = appService.getByRoleNameAndApp(roleName, appDb);
		if (role == null) {
			return new ResponseEntity<>("Role \"" + roleName + "\" does not exist in realm \"" + identification + "\"",
					HttpStatus.BAD_REQUEST);
		}
		appDb.getAppRoles().removeIf(ar -> ar.getName().equals(roleName));
		appService.updateApp(appDb);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Get all users in a Realm")
	@GetMapping("/{identification}/users")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=RealmUser[].class)), responseCode = "200", description = "OK"))
	@Transactional
	public ResponseEntity<?> getUsers(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification) {

		final AppList appDb = appService.getAppListByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Realm's users
		if (!utils.isAdministrator() && !appDb.getUser().getUserId().equals(utils.getUserId())
				&& !appService.isUserInApp(utils.getUserId(), appDb.getIdentification())) {

			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		final List<RealmUser> users = new ArrayList<>();
		List<RealmUser> filteredUsers = new ArrayList<>();
		appService.getAppUsersByApp(identification)
		.forEach(u -> users.add(
				RealmUser.builder()
				.avatar(u.getUser().getAvatar())
				.extraFields(u.getUser().getExtraFields())
				.fullName(u.getUser().getFullName())
				.mail(u.getUser().getEmail())
				.role(u.getRole().getName())
				.username(u.getUser().getUserId())
				.creationDate(u.getUser().getCreatedAt())
				.active(u.getUser().isActive())
				.build()));
		for (final RealmUser realmUser : users) {
			filteredUsers = usersWithRole(realmUser, filteredUsers);
		}
		return new ResponseEntity<>(filteredUsers, HttpStatus.OK);

	}

	@Operation(summary = "Gets a user in  a Realm")
	@GetMapping("/{identification}/users/{userId}")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=RealmUser.class)), responseCode = "200", description = "OK"))
	// @Transactional
	public ResponseEntity<?> getUser(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "User id", required = true) @PathVariable("userId") String userId) {

		final AppList appDb = appService.getAppListByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to get Realm's user
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))
				&& !utils.getUserId().equals(userId)) {

			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		if (!appService.isUserInApp(userId, identification)) {
			return new ResponseEntity<>(USER_STR + userId + NOT_EXIST, HttpStatus.NOT_FOUND);
		}

		final List<RealmUser> users = new ArrayList<>();
		List<RealmUser> filteredUsers = new ArrayList<>();
		appService.getAppUsersByUserIdAndApp(userId, identification).forEach(u -> {
			users.add(RealmUser.builder()
					.avatar(u.getUser().getAvatar())
					.extraFields(u.getUser().getExtraFields())
					.fullName(u.getUser().getFullName())
					.mail(u.getUser().getEmail())
					.role(u.getRole().getName())
					.username(u.getUser().getUserId())
					.creationDate(u.getUser().getCreatedAt())
					.active(u.getUser().isActive())
					.build());
		});
		for (final RealmUser realmUser : users) {
			filteredUsers = usersWithRole(realmUser, filteredUsers);
		}

		return new ResponseEntity<>(filteredUsers, HttpStatus.OK);
	}

	@Operation(summary = "Invalidates user authorization for a Realm")
	@DeleteMapping("/authorization/realm/{identification}/user/{userId}/rol/{rolId}")
	@Transactional
	public ResponseEntity<?> deleteUserRolAuthorization(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "User id", required = true) @PathVariable("userId") String userId,
			@Parameter(description= "Rol id", required = true) @PathVariable("rolId") String rolId) {
		log.debug("Removing realm authorization for user: {} and realm: {}", userId, identification);
		final AppList appDb = appService.getAppListByIdentification(identification);

		if (appDb == null) {
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		// user not administrator and not owner is not allowed to unauthorize
		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		try {
			appService.deleteUserAccess(userId, rolId, identification);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final AppServiceException e) {
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Gets a user in a Realm by email")
	@GetMapping("/{identification}/user")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=RealmUser.class)), responseCode = "200", description = "OK"))
	public ResponseEntity<?> getUserByEmail(
			@Parameter(description= "Realm Identification", required = true) @PathVariable("identification") String identification,
			@Parameter(description= "Email", required = true) @RequestParam("email") String email) {

		final App appDb = appService.getAppByIdentification(identification);

		if (appDb == null) {
			log.info(REALM_STR + identification + NOT_EXIST);
			return new ResponseEntity<>(REALM_STR + identification + NOT_EXIST, HttpStatus.BAD_REQUEST);
		}

		if (!utils.isAdministrator()
				&& (null == appDb.getUser() || !appDb.getUser().getUserId().equals(utils.getUserId()))) {
			log.info(USER_STR + utils.getUserId() + NOT_AUTH);
			return new ResponseEntity<>(USER_STR + utils.getUserId() + NOT_AUTH, HttpStatus.UNAUTHORIZED);
		}

		try {
			final User userByEmail = userService.getUserByEmail(email);

			if (userByEmail == null) {
				log.info(USER_STR + " with email: " + email + NOT_FOUND);
				return new ResponseEntity<>(USER_STR + " with email: " + email + NOT_FOUND, HttpStatus.NOT_FOUND);
			}

			if (!appService.isUserInApp(userByEmail.getUserId(), identification)) {
				log.info(USER_STR + userByEmail + NOT_FOUND);
				return new ResponseEntity<>(USER_STR + userByEmail + NOT_FOUND, HttpStatus.NOT_FOUND);

			} else {
				final UserDTO result = new UserDTO();

				result.setUserId(userByEmail.getUserId());
				result.setFullName(userByEmail.getFullName());

				return new ResponseEntity<>(result, HttpStatus.OK);
			}

		} catch (final UserServiceException e) {
			log.error("Internal Server Error", e);
			return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Operation(summary = "Get a list of realms where the current user is member of")
	@GetMapping("/member")
	@ApiResponses(@ApiResponse(content=@Content(schema=@Schema(implementation=String[].class)), responseCode = "200", description = "OK"))
	public ResponseEntity<List<String>> getRealmsForUser(){
		return new ResponseEntity<>(appService.getAppNamesByUserIn(utils.getUserId()), HttpStatus.OK);
	}


	private List<RealmUser> usersWithRole(RealmUser realmUser, List<RealmUser> filteredUsers) {
		boolean exist = false;
		for (final RealmUser user : filteredUsers) {
			if (user.getUsername().equals(realmUser.getUsername())) {
				user.setRole(user.getRole() + "," + realmUser.getRole());
				exist = true;
			}
		}
		if (!exist) {
			filteredUsers.add(realmUser);
		}
		return filteredUsers;
	}

	private AppRole realmRole2AppRole(RealmRole role, App app) {
		if (role.getName().length() > 24) {
			throw new AppServiceException("The maximum size allowed for the role name is 24 characters");
		}
		final AppRole appRole = new AppRole();
		appRole.setApp(app);
		appRole.setDescription(role.getDescription());
		appRole.setName(role.getName());

		return appRole;
	}

}

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
package com.minsait.onesait.platform.bpm.services.impl;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Resources.APPLICATION;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.IdentityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.bpm.model.Groups;
import com.minsait.onesait.platform.bpm.services.AuthorizationManagementService;
import com.minsait.onesait.platform.bpm.services.BPMUserManagementService;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.AppUser;
import com.minsait.onesait.platform.config.services.app.AppService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.Vertical;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BPMUserManagementServiceImpl implements BPMUserManagementService {

	private static final String DEFAULT_CLIENT_ID = "onesaitplatform";
	@Autowired
	private ProcessEngine processEngine;
	@Autowired
	private UserService userService;
	@Autowired
	private AppService appService;
	@Autowired
	private MultitenancyService multitenancyService;
	@Autowired
	private AuthorizationManagementService authorizationManagementService;

	@Value("${onesaitplatform.camunda.import-users:false}")
	private boolean importUsers;

	@Value("${onesaitplatform.camunda.admin-user:administrator}")
	private String adminUser;

	@Value("${security.oauth2.client.clientId}")
	private String clientId;

	private IdentityServiceImpl identityService;
	private AuthorizationService authorizationService;

	@PostConstruct
	void initialize() {
		identityService = (IdentityServiceImpl) processEngine.getIdentityService();
		authorizationService = processEngine.getAuthorizationService();
		createGroups();
		createInitialAuths();
		createAdminUser();
		if (importUsers) {
			syncUsers();
		}
	}

	@Override
	public void syncUsers() {
		if (DEFAULT_CLIENT_ID.equals(clientId)) {
			syncUsersGeneric();
		} else {
			syncUsersRealms();
		}
	}

	private void syncUsersGeneric() {
		multitenancyService.getUsers().stream().forEach(m -> {
			if (!userExistsInDB(m.getUserId())) {
				try {
					final List<Vertical> verticals = multitenancyService.getVerticals(m);
					verticals.forEach(v -> {
						MultitenancyContextHolder.setVerticalSchema(v.getSchema());
						final com.minsait.onesait.platform.config.model.User user = userService.getUser(m.getUserId());
						if (user != null) {
							createUser(m.getUserId());
							return;
						}
					});

				} catch (final Exception e) {
					log.error("Could not create user {}", m.getUserId(), e);
				} finally {
					MultitenancyContextHolder.clear();
				}
			}
//			createTenants(m.getUserId());
		});
	}

	private void syncUsersRealms() {
		multitenancyService.getAllVerticals().forEach(v -> {
			MultitenancyContextHolder.setVerticalSchema(v.getSchema());
			authorizationManagementService.syncRealmMappings();
			final JsonNode config = authorizationManagementService.getConfigMap();
			if (config != null) {
				config.get(AuthorizationManagementServiceImpl.ROOT_NODE_REALMS).fields().forEachRemaining(r -> {
					final String realm = r.getKey();
					final List<String> roles = new ArrayList<>();
					r.getValue().fieldNames().forEachRemaining(roles::add);
					final App app = appService.getAppByIdentification(realm);
					if (app != null) {
						app.getAppRoles().stream().filter(ar -> roles.contains(ar.getName())).forEach(ar -> {
							final Set<AppUser> appUsers = ar.getAppUsers();
							appUsers.forEach(au -> {
								if (!userExistsInDB(au.getUser().getUserId())) {
									createUser(au.getUser().getUserId(), ar.getName());
								}
							});
						});
					}
				});
			}
			MultitenancyContextHolder.clear();
		});

	}

//	private void createTenants(String user) {
//		final String v = Tenant2SchemaMapper
//				.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema());
//		final String t = MultitenancyContextHolder.getTenantName();
//		final String camundaTenantName = v + "_" + t;
//		if (identityService.createTenantQuery().tenantId(BPMConstants.TENANT_PREFIX.concat(user)).count() == 0) {
//			final Tenant tenant = identityService.newTenant(BPMConstants.TENANT_PREFIX.concat(user));
//			tenant.setName("Tenant for user " + user);
//			identityService.saveTenant(tenant);
//			identityService.createTenantUserMembership(tenant.getId(), user);
//		}
//
//	}

	@Override
	public boolean userExistsInDB(String userId) {
		return identityService.createUserQuery().userId(userId).count() > 0;
	}

	@Override
	public void createUser(Authentication auth) {
		if (DEFAULT_CLIENT_ID.equals(clientId)) {
			this.createUser(auth.getName());
		} else {
			this.createUser(auth.getName(), auth.getAuthorities().iterator().next().getAuthority());
		}

	}

	private String assignGroup(com.minsait.onesait.platform.config.model.User user) {
		switch (user.getRole().getId()) {
		case "ROLE_ADMINISTRATOR":
		case "ROLE_SYS_ADMIN":
			return Groups.CAMUNDA_ADMIN.getValue();
		case "ROLE_DEVELOPER":
		case "ROLE_DEVOPS":
			return Groups.CAMUNDA_DEVELOPER.getValue();
		case "ROLE_DATASCIENTIST":
		case "ROLE_DATAVIEWER":
			return Groups.CAMUNDA_DATASCIENTIST.getValue();

		default:
			return Groups.CAMUNDA_USER.getValue();
		}
	}

	private void createGroups() {
		// TO-DO permissions
		if (identityService.createGroupQuery().groupId(Groups.CAMUNDA_DEVELOPER.getValue()).count() == 0) {
			createGroup(Groups.CAMUNDA_DEVELOPER);

		}
		if (identityService.createGroupQuery().groupId(Groups.CAMUNDA_USER.getValue()).count() == 0) {
			createGroup(Groups.CAMUNDA_USER);
		}
		if (identityService.createGroupQuery().groupId(Groups.CAMUNDA_ADMIN.getValue()).count() == 0) {
			createGroup(Groups.CAMUNDA_ADMIN);

		}
		if (identityService.createGroupQuery().groupId(Groups.CAMUNDA_DATASCIENTIST.getValue()).count() == 0) {
			createGroup(Groups.CAMUNDA_DATASCIENTIST);

		}
	}

	@Override
	public void createTenants(Authentication auth) {
		final String v = Tenant2SchemaMapper
				.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema());
		final String t = MultitenancyContextHolder.getTenantName();
		final String camundaTenantName = v + "_" + t;
		if (identityService.createTenantQuery().tenantId(camundaTenantName).count() == 0) {
			final Tenant tenant = identityService.newTenant(camundaTenantName);
			tenant.setName("Tenant for " + camundaTenantName);
			identityService.saveTenant(tenant);
		}
		if (identityService.createTenantQuery().userMember(auth.getName()).list().stream()
				.noneMatch(d -> d.getId().equals(camundaTenantName))) {
			identityService.createTenantUserMembership(camundaTenantName, auth.getName());
		}

	}

	private void createAdminUser() {
		if (!userExistsInDB(adminUser)) {
			this.createUser(adminUser);
		}
	}

	private void createUser(String userId) {

		final com.minsait.onesait.platform.config.model.User userOP = userService.getUser(userId);

		final User user = identityService.newUser(userOP.getUserId());
		user.setFirstName(userOP.getFullName());
		user.setLastName("");
		user.setPassword(userOP.getPassword());
		user.setEmail(userOP.getEmail());
		identityService.saveUser(user);
		identityService.createMembership(user.getId(), assignGroup(userOP));
		// To-DO close somewhere contextholder?

	}

	private void createUser(String userId, String groupName) {
		authorizationManagementService.syncRealmMappings();

		final com.minsait.onesait.platform.config.model.User userOP = userService.getUser(userId);

		final User user = identityService.newUser(userOP.getUserId());
		user.setFirstName(userOP.getFullName());
		user.setLastName("");
		user.setPassword(userOP.getPassword());
		user.setEmail(userOP.getEmail());
		identityService.saveUser(user);
		identityService.createMembership(user.getId(), groupName);

	}

	private void createGroup(Groups group) {
		switch (group) {
		case CAMUNDA_ADMIN:
			final Group adminGroup = identityService.newGroup(Groups.CAMUNDA_ADMIN.getValue());
			adminGroup.setName("Admins");
			adminGroup.setType(org.camunda.bpm.engine.authorization.Groups.GROUP_TYPE_SYSTEM);
			identityService.saveGroup(adminGroup);
			break;
		case CAMUNDA_DEVELOPER:
			final Group developerGroup = identityService.newGroup(Groups.CAMUNDA_DEVELOPER.getValue());
			developerGroup.setName("Developer");
			developerGroup.setType(org.camunda.bpm.engine.authorization.Groups.GROUP_TYPE_WORKFLOW);
			identityService.saveGroup(developerGroup);
			break;
		case CAMUNDA_DATASCIENTIST:
			final Group dsGroup = identityService.newGroup(Groups.CAMUNDA_DATASCIENTIST.getValue());
			dsGroup.setName("Data Scientist");
			dsGroup.setType(org.camunda.bpm.engine.authorization.Groups.GROUP_TYPE_WORKFLOW);
			identityService.saveGroup(dsGroup);
			break;
		case CAMUNDA_USER:
		default:
			final Group userGroup = identityService.newGroup(Groups.CAMUNDA_USER.getValue());
			userGroup.setName("User");
			userGroup.setType(org.camunda.bpm.engine.authorization.Groups.GROUP_TYPE_WORKFLOW);
			identityService.saveGroup(userGroup);
			break;

		}
	}

	private void createInitialAuths() {
		Authorization accountingTasklistAuth;
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DEVELOPER.getValue())
				.resourceId("tasklist").count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DEVELOPER.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId("tasklist");
			accountingTasklistAuth.setResource(APPLICATION);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DEVELOPER.getValue())
				.resourceId("cockpit").count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DEVELOPER.getValue());
			accountingTasklistAuth.setResourceId("cockpit");
			accountingTasklistAuth.setResource(APPLICATION);
			accountingTasklistAuth.addPermission(ALL);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DEVELOPER.getValue())
				.resourceType(Resources.DEPLOYMENT).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DEVELOPER.getValue());
			accountingTasklistAuth.setResource(Resources.DEPLOYMENT);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.addPermission(ALL);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DEVELOPER.getValue())
				.resourceType(Resources.FILTER).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DEVELOPER.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.FILTER);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DEVELOPER.getValue())
				.resourceType(Resources.PROCESS_DEFINITION).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DEVELOPER.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.PROCESS_DEFINITION);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DEVELOPER.getValue())
				.resourceType(Resources.PROCESS_INSTANCE).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DEVELOPER.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.PROCESS_INSTANCE);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DEVELOPER.getValue())
				.resourceType(Resources.TASK).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DEVELOPER.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.TASK);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		////
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DATASCIENTIST.getValue())
				.resourceId("tasklist").count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DATASCIENTIST.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId("tasklist");
			accountingTasklistAuth.setResource(APPLICATION);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DATASCIENTIST.getValue())
				.resourceId("cockpit").count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DATASCIENTIST.getValue());
			accountingTasklistAuth.setResourceId("cockpit");
			accountingTasklistAuth.setResource(APPLICATION);
			accountingTasklistAuth.addPermission(ALL);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DATASCIENTIST.getValue())
				.resourceType(Resources.DEPLOYMENT).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DATASCIENTIST.getValue());
			accountingTasklistAuth.setResource(Resources.DEPLOYMENT);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.addPermission(ALL);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DATASCIENTIST.getValue())
				.resourceType(Resources.FILTER).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DATASCIENTIST.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.FILTER);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DATASCIENTIST.getValue())
				.resourceType(Resources.PROCESS_DEFINITION).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DATASCIENTIST.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.PROCESS_DEFINITION);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DATASCIENTIST.getValue())
				.resourceType(Resources.PROCESS_INSTANCE).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DATASCIENTIST.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.PROCESS_INSTANCE);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_DATASCIENTIST.getValue())
				.resourceType(Resources.TASK).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_DATASCIENTIST.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.TASK);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_USER.getValue())
				.resourceId("tasklist").count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_USER.getValue());
			accountingTasklistAuth.addPermission(ALL);
			accountingTasklistAuth.setResourceId("tasklist");
			accountingTasklistAuth.setResource(APPLICATION);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_USER.getValue())
				.resourceType(Resources.FILTER).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_USER.getValue());
			accountingTasklistAuth.addPermission(Permissions.READ);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.FILTER);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}
		if (authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_USER.getValue())
				.resourceType(Resources.TASK).count() == 0) {
			accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
			accountingTasklistAuth.setGroupId(Groups.CAMUNDA_USER.getValue());
			accountingTasklistAuth.addPermission(Permissions.READ);
			accountingTasklistAuth.setResourceId(ANY);
			accountingTasklistAuth.setResource(Resources.TASK);
			authorizationService.saveAuthorization(accountingTasklistAuth);
		}

	}

	@Override
	public void updateGroups(String userId, List<String> groupNames) {
		// check membership
		final List<String> currentGroups = identityService.createGroupQuery().groupMember(userId).list().stream()
				.map(Group::getId).toList();
		try {
			if (DEFAULT_CLIENT_ID.equals(clientId)) {
				final String group = assignGroup(userService.getUser(userId));
				if (!currentGroups.contains(group)) {
					identityService.createMembership(userId, group);
					currentGroups.forEach(s -> identityService.deleteMembership(userId, s));
				}

			} else {
				groupNames.stream().filter(g -> !currentGroups.contains(g))
						.forEach(s -> identityService.createMembership(userId, s));
				currentGroups.stream()
						.filter(g -> !groupNames.contains(g) && !Groups.CAMUNDA_ADMIN.getValue().equals(g))
						.forEach(s -> identityService.deleteMembership(userId, s));
			}
		} catch (final Exception e) {
			log.error("Could not update roles");
		}
	}

}
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
package authentication;

import java.util.Map;
import org.apache.log4j.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import authentication.server.PlainSaslServerProvider;

public class PlainLoginModule implements LoginModule {

	private static final Logger log = Logger.getLogger(PlainLoginModule.class.getName());

	private static final String USERNAME_CONFIG = "username";
	private static final String PASS_CONFIG = "pass" + "word";
	private static final String DEFAULT_VERTICAL_CONFIG = "default_vertical";
	private static final String DEFAULT_TENANT_CONFIG = "default_tenant";

	private static String URL = "";
	private static String DEFAULT_VERTICAL = "";
	private static String DEFAULT_TENANT = "";

	static {
		PlainSaslServerProvider.initialize();
	}

	public static String getURL() {
		return PlainLoginModule.URL;
	}

	public static String getDefaultVertical() {
		return PlainLoginModule.DEFAULT_VERTICAL;
	}

	public static String getDefaultTenant() {
		return PlainLoginModule.DEFAULT_TENANT;
	}

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options) {

		log.info("PlainModule Initialize:");
		log.info(options.toString());
		String url = (String) options.get("url");
		if (url != null) {
			log.info("URL=" + url);
			PlainLoginModule.URL = url;
		}
		String vertical = (String) options.get(DEFAULT_VERTICAL_CONFIG);
		if (vertical != null) {
			log.info("DEFAULT_VERTICAL="+vertical);
			PlainLoginModule.DEFAULT_VERTICAL = vertical;
		}
		String tenant = (String) options.get(DEFAULT_TENANT_CONFIG);
		if (tenant != null) {
			log.info("DEFAULT_TENANT="+tenant);
			PlainLoginModule.DEFAULT_TENANT = tenant;
		}
		String username = (String) options.get(USERNAME_CONFIG);
		if (username != null) {
			subject.getPublicCredentials().add(username);
		}
		String pass = (String) options.get(PASS_CONFIG);
		if (pass != null) {
			subject.getPrivateCredentials().add(pass);
		}
	}

	@Override
	public boolean login() throws LoginException {
		return true;
	}

	@Override
	public boolean logout() throws LoginException {
		return true;
	}

	@Override
	public boolean commit() throws LoginException {
		return true;
	}

	@Override
	public boolean abort() throws LoginException {
		return false;
	}
}
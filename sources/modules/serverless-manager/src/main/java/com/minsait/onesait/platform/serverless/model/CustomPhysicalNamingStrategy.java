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
package com.minsait.onesait.platform.serverless.model;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

public class CustomPhysicalNamingStrategy extends SpringPhysicalNamingStrategy {

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		if (jdbcEnvironment.getDialect().toString().contains("PostgreSQL") && name.getText().equalsIgnoreCase("USER")) {
			return super.toPhysicalTableName(Identifier.toIdentifier("app_user"), jdbcEnvironment);
		}
		return super.toPhysicalTableName(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		if (jdbcEnvironment.getDialect().toString().contains("PostgreSQL") && name.getText().equalsIgnoreCase("USER")) {
			return super.toPhysicalTableName(Identifier.toIdentifier("app_user"), jdbcEnvironment);
		}
		return super.toPhysicalTableName(name, jdbcEnvironment);
	}

}

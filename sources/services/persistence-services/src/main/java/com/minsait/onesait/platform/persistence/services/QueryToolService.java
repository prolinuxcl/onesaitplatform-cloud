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
package com.minsait.onesait.platform.persistence.services;

import java.util.List;
import java.util.Map;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataUnauthorizedException;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;

import net.sf.jsqlparser.JSQLParserException;

public interface QueryToolService {

	String queryNativeAsJson(String user, String ontology, String query, int offset, int limit);

	String queryNativeAsJson(String user, String ontology, String query);

	String querySQLAsJson(String user, String ontology, String query, int offset)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException;

	String querySQLAsJson(String user, String ontology, String query, int offset, int limit)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException;

	String queryNativeAsJsonForPlatformClient(String clientplatform, String ontology, String query, int offset,
			int limit);

	String querySQLAsJsonForPlatformClient(String clientplatform, String ontology, String query, int offset)
			throws DBPersistenceException, OntologyDataUnauthorizedException, GenericOPException;

	String compileSQLQueryAsJson(String user, Ontology ontology, String query, int offset);

	List<String> getTables();

	Map<String, String> getTableColumns(String tableName);

	List<String> querySQLtoConfigDB(String query) throws JSQLParserException ;

	List<String> updateSQLtoConfigDB(String query) throws JSQLParserException ;
}

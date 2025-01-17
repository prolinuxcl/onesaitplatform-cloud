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
package com.minsait.onesait.platform.persistence.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;

import lombok.extern.slf4j.Slf4j;

@Component("NoPersistenceManageDBRepository")
@Slf4j
public class NoPersistenceManageDBRepository implements ManageDBRepository {
	private static final String NO_OP_CONTROL_ONTOLOGY = "NO-OP control ontology";

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		final Map<String, Boolean> map = new HashMap<>();
		map.put("1", true);
		return map;
	}

	@Override
	public String createTable4Ontology(String ontology, String schema, Map<String, String> config) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return ontology;
	}

	@Override
	public List<String> getListOfTables() {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);

	}

	@Override
	public void createIndex(String ontology, String attribute) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);

	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);

	}

	@Override
	public void createIndex(String sentence) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);

	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);

	}

	@Override
	public List<String> getListIndexes(String ontology) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public String getIndexes(String ontology) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return "[]";
	}

	@Override
	public void validateIndexes(String ontology, String schema) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);

	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String path) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return ExportData.builder().build();
	}

	@Override
	public long deleteAfterExport(String ontology, String query) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return 0;
	}

	@Override
	public List<DescribeColumnData> describeTable(String name) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new ArrayList<>();
	}

	@Override
	public Map<String, String> getAdditionalDBConfig(String ontology) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return new HashMap<>();
	}

	@Override
	public String updateTable4Ontology(String identification, String jsonSchema, Map<String, String> config) {
		log.debug(NO_OP_CONTROL_ONTOLOGY);
		return identification;
	}

}

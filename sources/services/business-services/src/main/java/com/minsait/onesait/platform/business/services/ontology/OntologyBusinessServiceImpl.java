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
package com.minsait.onesait.platform.business.services.ontology;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.business.services.ontology.graph.NebulaGraphBusinessService;
import com.minsait.onesait.platform.business.services.ontology.graph.NebulaGraphEntity;
import com.minsait.onesait.platform.business.services.presto.datasource.PrestoDatasourceConfigurationService;
import com.minsait.onesait.platform.commons.metrics.MetricsManager;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbCleanLapse;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.Ontology.RtdbToHdbStorage;
import com.minsait.onesait.platform.config.model.OntologyVirtual;
import com.minsait.onesait.platform.config.model.OntologyVirtualDatasource.VirtualDatasourceType;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.deletion.EntityDeletionService;
import com.minsait.onesait.platform.config.services.exceptions.OntologyServiceException;
import com.minsait.onesait.platform.config.services.kafka.KafkaAuthorizationServiceImpl;
import com.minsait.onesait.platform.config.services.ontology.OntologyConfiguration;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.OntologyTimeSeriesService;
import com.minsait.onesait.platform.config.services.ontology.dto.VirtualDatasourceInfoDTO;
import com.minsait.onesait.platform.config.services.ontologydata.OntologyDataJsonProblemException;
import com.minsait.onesait.platform.config.services.ontologymqtttopic.OntologyMqttTopicService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.persistence.cosmosdb.CosmosDBBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.cosmosdb.CosmosDBManageDBRepository;
import com.minsait.onesait.platform.persistence.external.virtual.VirtualOntologyOpsDBRepository;
import com.minsait.onesait.platform.persistence.external.virtual.constraints.TableKeysHolder;
import com.minsait.onesait.platform.persistence.historical.minio.HistoricalMinioException;
import com.minsait.onesait.platform.persistence.historical.minio.HistoricalMinioService;
import com.minsait.onesait.platform.persistence.nebula.model.NebulaSpace;
import com.minsait.onesait.platform.persistence.presto.PrestoManageDBRepository;
import com.minsait.onesait.platform.persistence.presto.PrestoOntologyBasicOpsDBRepository;
import com.minsait.onesait.platform.persistence.services.BasicOpsPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.services.util.OntologyLogicService;
import com.minsait.onesait.platform.persistence.services.util.OntologyLogicServiceException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OntologyBusinessServiceImpl implements OntologyBusinessService {

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private OntologyLogicService ontologyLogicService;

	@Autowired
	private KafkaAuthorizationServiceImpl kafkaAuthorizationService;

	@Autowired
	private UserService userService;

	@Autowired(required = false)
	private MetricsManager metricsManager;

	static final String SCHEMA_DRAFT_VERSION = "http://json-schema.org/draft-04/schema#";
	static final String ERROR_KAFKA_TOPIC = "Error creationg kafka topic";

	@Autowired
	private VirtualOntologyOpsDBRepository virtualRepo;

	@Autowired
	private EntityDeletionService entityDeleteionService;

	@Autowired
	private CosmosDBManageDBRepository cosmosManageRepository;

	@Autowired
	private CosmosDBBasicOpsDBRepository cosmosBasicOpsRepository;

	@Autowired
	private OntologyTimeSeriesService ontologyTimeSeriesService;

	@Autowired
	private PrestoOntologyBasicOpsDBRepository prestoDBBasicOpsDBRepository;

	@Autowired
	private PrestoManageDBRepository prestoManageDBRepository;

	@Autowired
	private HistoricalMinioService historicalMinioService;
	
	@Autowired
	private BasicOpsPersistenceServiceFacade basicOpsRepository;

	@Autowired
	private NebulaGraphBusinessService nebulaGraphBusinessService;

	@Autowired
	private PrestoDatasourceConfigurationService prestoDatasourceConfigurationService;

	@Autowired
	private OntologyMqttTopicService mqttTopicService;

	@Override
	public void createOntology(Ontology ontology, String userId, OntologyConfiguration config)
			throws OntologyBusinessServiceException {

		if (!ontologyService.isIdValid(ontology.getIdentification())) {
			throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.ILLEGAL_ARGUMENT,
					"Ontology identification is not valid");
		}

		final User user = userService.getUser(userId);
		ontology.setUser(user);

		try {
			ontologyService.checkOntologySchema(ontology.getJsonSchema());
		} catch (final OntologyDataJsonProblemException e) {
			metricsManagerLogControlPanelOntologyCreation(userId, "KO");

			throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.NO_VALID_SCHEMA,
					"The provided json schema is not valid", e);
		}

		prepareOntologyRtdbToHdb(ontology);
		if (ontologyService.existsOntology(ontology.getIdentification())) {
			throw new OntologyServiceException(
					"Ontology with identification: " + ontology.getIdentification() + " exists");
		}

		invokeCreateOntology(ontology, config);
		invokeCreateOntologyLogic(ontology, config);
	}

	private void prepareOntologyRtdbToHdb(Ontology ontology) {
		if (ontology.isRtdbToHdb()) {
			ontology.setRtdbClean(true);
		} else {
			ontology.setRtdbToHdbStorage(null);
		}

		if (ontology.isRtdbClean() && ontology.getRtdbCleanLapse().equals(RtdbCleanLapse.NEVER)) {
			ontology.setRtdbCleanLapse(RtdbCleanLapse.ONE_DAY);
		}
	}

	private void invokeCreateOntology(Ontology ontology, OntologyConfiguration config) {
		ontologyService.createOntology(ontology, config);
	}

	private void invokeCreateOntologyLogic(Ontology ontology, OntologyConfiguration config)
			throws OntologyBusinessServiceException {
		final String errorRollingBack = "Error creating the persistence infrastructure for ontology";
		try {
			ontologyLogicService.createOntology(ontology, config == null ? null : configToConfigMap(config));
		} catch (final Exception e) {
			log.error("Error creaing ontology", e);
			ontologyService.delete(ontology);
			throw new OntologyBusinessServiceException(
					OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR,
					errorRollingBack + ": " + e.getMessage(), e);
		}

	}

	@Override
	public JsonNode completeSchema(String schema, String identification, String description) throws IOException {
		final JsonNode schemaSubTree = organizeRootNodeIfExist(schema);
		((ObjectNode) schemaSubTree).put("type", "object");
		((ObjectNode) schemaSubTree).put("description", "Info " + identification);

		((ObjectNode) schemaSubTree).put("$schema", SCHEMA_DRAFT_VERSION);
		((ObjectNode) schemaSubTree).put("title", identification);

		((ObjectNode) schemaSubTree).put("additionalProperties", true);
		return schemaSubTree;
	}

	public Map<String, TableKeysHolder> getDatabaseTree(Ontology sourceEntity, Map<String, TableKeysHolder> keyMap) {
		if (keyMap == null) {
			keyMap = new HashMap<>();
		}
		final OntologyVirtual ov = ontologyService.getOntologyVirtualByOntologyId(sourceEntity);
		final TableKeysHolder tkh = virtualRepo.getTableKeysHolder(sourceEntity,
				ontologyService.getOntologyVirtualByOntologyId(sourceEntity).getDatasourceTableName());
		keyMap.put(sourceEntity.getIdentification(), tkh);
		if (!tkh.getReferendTables().isEmpty()) {
			for (final String t : tkh.getReferendTables()) {
				final List<OntologyVirtual> os = ontologyService.getOntologyVirtualByTableName(t).stream()
						.filter(ovv -> ovv.getDatasourceId().getId().equals(ov.getDatasourceId().getId())).toList();
				if (!os.isEmpty() && !keyMap.containsKey(os.get(0).getOntologyId().getIdentification())) {
					getDatabaseTree(os.get(0).getOntologyId(), keyMap);
				} else if (os.isEmpty()) {
					log.warn("Table {} found referenced by {} but not found on platform entities", t,
							sourceEntity.getIdentification());
				}

			}
		}
		return keyMap;
	}

	private void cloneOntologyVirtual(Ontology ontology, String identification, User user,
			OntologyConfiguration config) {
		final String prefix = identification.endsWith("_") ? identification : identification + "_";
		final Map<String, TableKeysHolder> tkhMap = getDatabaseTree(ontology, null);
		checkEntitesAndModifyFKs(tkhMap, prefix);
		tkhMap.entrySet().forEach(e -> {
			final Ontology o = ontologyService.getOntologyByIdentification(e.getKey());
			final OntologyVirtual ov = ontologyService.getOntologyVirtualByOntologyId(o);
			config.setDatasourceTableName(prefix + e.getKey());
			config.setDatasourceDatabase(ov.getDatasourceDatabase());
			config.setDatasource(ov.getDatasourceId().getIdentification());
			config.setDatasourceSchema(ov.getDatasourceSchema());
			config.setObjectId(ov.getObjectId());
			config.setObjectGeometry(ov.getObjectGeometry());
			if (!ontologyService.existsOntology(prefix + e.getKey())) {
				cloneOntology(o, prefix + e.getKey(), user, config);
				virtualRepo.cloneTableNoData(e.getKey(), e.getValue().getTableName(), prefix + e.getKey(),
						e.getValue().getDatabaseType());
				virtualRepo.cloneUKeys(e.getKey(), prefix + e.getKey(), e.getValue());
			}

		});
		tkhMap.entrySet().forEach(e -> {
			virtualRepo.cloneFKeys(e.getKey(), prefix + e.getKey(), e.getValue());
		});

	}

	private void checkEntitesAndModifyFKs(Map<String, TableKeysHolder> tkhMap, String prefix) {
		tkhMap.entrySet().forEach(e -> {
			final Ontology o = ontologyService.getOntologyByIdentification(e.getKey());
			final OntologyVirtual ov = ontologyService.getOntologyVirtualByOntologyId(o);
			e.getValue().getForeignKeys().forEach(fk -> {
				final OntologyVirtual fkReferencedEntity = ontologyService
						.getOntologyVirtualByTableName(fk.getReferencedTable()).stream().filter(ovv -> ovv
								.getDatasourceId().getIdentification().equals(ov.getDatasourceId().getIdentification()))
						.findFirst().orElse(null);
				if (fkReferencedEntity == null) {
					throw new RuntimeException("Entity " + fk.getReferencedTable() + " does not exist on platform.");
				} else {
					fk.setReferencedTable(prefix + fkReferencedEntity.getOntologyId().getIdentification());
				}
			});
		});
	}

	private void cloneOntology(Ontology ontology, String identification, User user, OntologyConfiguration config) {
		final Ontology clone = new Ontology();

		clone.setIdentification(identification);
		clone.setUser(user);
		clone.setDescription(ontology.getDescription());
		clone.setActive(ontology.isActive());
		clone.setPublic(ontology.isPublic());
		clone.setDataModel(ontology.getDataModel());
		clone.setDataModelVersion(ontology.getDataModelVersion());
		clone.setJsonSchema(ontology.getJsonSchema());
		clone.setMetainf(ontology.getMetainf());
		clone.setRtdbToHdbStorage(ontology.getRtdbToHdbStorage());
		clone.setRtdbDatasource(ontology.getRtdbDatasource());
		clone.setAllowsCypherFields(ontology.isAllowsCypherFields());
		clone.setAllowsCreateNotificationTopic(ontology.isAllowsCreateNotificationTopic());
		clone.setAllowsCreateTopic(ontology.isAllowsCreateTopic());
		clone.setSupportsJsonLd(ontology.isSupportsJsonLd());
		clone.setJsonLdContext(ontology.getJsonLdContext());
		clone.setContextDataEnabled(ontology.isContextDataEnabled());
		clone.setEnableDataClass(ontology.isEnableDataClass());
		clone.setAllowsCreateMqttTopic(ontology.isAllowsCreateMqttTopic());
		ontologyService.createOntology(clone, config);

		// if its Nebula clone to Nebula

		if (ontology.getRtdbDatasource().equals(RtdbDatasource.NEBULA_GRAPH)) {
			final NebulaGraphEntity entity = new NebulaGraphEntity();
			entity.setEdges(nebulaGraphBusinessService.getEdges(ontology.getIdentification()));
			entity.setTags(nebulaGraphBusinessService.getTags(ontology.getIdentification()));
			entity.setName(clone.getIdentification());
			entity.setDescription(ontology.getDescription());
			entity.setMetainf(ontology.getMetainf());
			entity.setUser(user.getUserId());
			final NebulaSpace space = nebulaGraphBusinessService.getSpace(ontology.getIdentification());
			entity.setPartitions(space.getPartitionNum());
			entity.setReplicas(space.getReplicaFactor());
			nebulaGraphBusinessService.createNebulaGraphEntity(entity, false);
		}
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void cloneOntology(String id, String identification, String userId, OntologyConfiguration config)
			throws OntologyBusinessServiceException {
		final Ontology ontology = ontologyService.getOntologyById(id, userId);
		final User user = userService.getUser(userId);

		if (ontologyService.existsOntology(identification)) {
			throw new OntologyServiceException("Ontology already exists",
					OntologyServiceException.Error.EXISTING_ONTOLOGY);
		} else if (!ontologyService.hasUserPermissionForQuery(user, ontology)) {
			throw new OntologyServiceException("The user is not authorized",
					OntologyServiceException.Error.PERMISSION_DENIED);
		}
		if (!ontologyService.isIdValid(ontology.getIdentification())) {
			throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.ILLEGAL_ARGUMENT,
					"Ontology identification is not valid");
		}

		if (!ontology.getDataModel().getId().equals("MASTER-DataModel-30")) {
			if (!ontology.getRtdbDatasource().equals(RtdbDatasource.VIRTUAL)) {
				cloneOntology(ontology, identification, user, config);
			} else {
				if (config == null) {
					config = new OntologyConfiguration();
				}
				cloneOntologyVirtual(ontology, identification, user, config);
			}

		} else {
			ontologyTimeSeriesService.cloneOntologyTimeSeries(identification, ontology, user, config);
		}

	}

	@Override
	public JsonNode organizeRootNodeIfExist(String schema) throws IOException {

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode schemaSubTree = mapper.readTree(schema);
		boolean find = Boolean.FALSE;
		for (final Iterator<Entry<String, JsonNode>> elements = schemaSubTree.fields(); elements.hasNext();) {
			final Entry<String, JsonNode> e = elements.next();
			if (e.getKey().equals("properties")) {
				e.getValue().fields();
				for (final Iterator<Entry<String, JsonNode>> properties = e.getValue().fields(); properties
						.hasNext();) {
					final Entry<String, JsonNode> prop = properties.next();
					final String field = prop.getKey();
					if (!field.equalsIgnoreCase(field) && Character.isUpperCase(field.charAt(0))) {
						((ObjectNode) schemaSubTree).set("datos", prop.getValue());
						// Add required
						final ArrayNode required = ((ObjectNode) schemaSubTree).putArray("required");
						required.add(prop.getKey());
						final String newString = "{\"type\": \"string\",\"$ref\": \"#/datos\"}";
						final JsonNode newNode = mapper.readTree(newString);
						prop.setValue(newNode);
						find = Boolean.TRUE;
						break;
					}
					if (find) {
						break;
					}
				}
			}
		}
		return schemaSubTree;
	}

	@Override
	public VirtualDatasourceInfoDTO getInfoFromDatasource(String datasource) {
		return virtualRepo.getInfo(datasource);
	}

	@Override
	public List<String> getTablesFromDatasource(String datasource) {
		return virtualRepo.getTables(datasource);
	}

	@Override
	public List<String> getDatabasesFromDatasource(String datasource) {
		if (datasource.equals(VirtualDatasourceType.PRESTO.toString())) {
			return prestoDBBasicOpsDBRepository.getCatalogs();
		}
		return virtualRepo.getDatabases(datasource);
	}

	@Override
	public List<String> getSchemasFromDatasourceDatabase(String datasource, String database) {
		if (datasource.equals(VirtualDatasourceType.PRESTO.toString())) {
			return prestoDBBasicOpsDBRepository.getSchemas(database);
		}
		return virtualRepo.getSchemasDB(datasource, database);
	}

	@Override
	public List<Map<String, Object>> getTableInformationFromDatasource(String datasource, String database,
			String schema) {
		return virtualRepo.getTableInformation(datasource, database, schema);
	}

	@Override
	public List<Map<String, Object>> getTablePKInformation(String datasource, String database, String schema) {
		return virtualRepo.getTablePKInformation(datasource, database, schema);
	}

	@Override
	public List<String> getTablesFromDatasource(String datasource, String database, String schema) {
		if (datasource.equals(VirtualDatasourceType.PRESTO.toString())) {
			return prestoDBBasicOpsDBRepository.getTables(database, schema);
		}
		return virtualRepo.getTables(datasource, database, schema);
	}

	@Override
	public List<String> getStringSupportedFieldDataTypes() {
		return virtualRepo.getStringSupportedFieldDataTypes();
	}

	@Override
	public List<String> getStringSupportedConstraintTypes() {
		return virtualRepo.getStringSupportedConstraintTypes();
	}

	@Override
	public String getInstance(String datasource, String collection) {
		final List<String> result = virtualRepo.getInstanceFromTable(datasource, "select * from " + collection);
		if (!result.isEmpty()) {
			return result.get(0);
		} else {
			return "";
		}
	}

	@Override
	public String getRelationalSchema(String datasource, String database, String schema, String collection) {
		if (datasource.equals(VirtualDatasourceType.PRESTO.toString())) {
			return prestoDBBasicOpsDBRepository.getTableMetadata(database, schema, collection);
		}
		return virtualRepo.getTableMetadata(datasource, database, schema, collection);
	}

	@Override
	public String getSqlTableDefinitionFromSchema(final String ontology, final String schema,
			final VirtualDatasourceType datasource) {
		// implements logic for EmptyBase schema and VirtualSchema
		return virtualRepo.getSqlTableDefinitionFromSchema(ontology, schema, datasource);
	}

	@Override
	public String getSQLCreateTable(CreateStatementBusiness statementBusiness, VirtualDatasourceType datasource)
			throws OntologyBusinessServiceException {
		if (datasource.equals(VirtualDatasourceType.PRESTO)) {
			return prestoDBBasicOpsDBRepository.getSQLCreateStatment(statementBusiness.toCreateStatementPresto());
		}
		return virtualRepo.getSQLCreateStatment(statementBusiness.toCreateStatement(), datasource);
	}

	/***
	 * Generate config map with all specific params for internal database
	 */
	private HashMap<String, String> configToConfigMap(OntologyConfiguration config) {
		final HashMap<String, String> cmap = new HashMap<>();
		// relational
		cmap.put("allowsCreateTable", String.valueOf(config.isAllowsCreateTable()));
		if (config.getSqlStatement() != null) {
			cmap.put("sqlStatement", config.getSqlStatement());
		}

		if (config.getEnablePartitionIndexes() != null || "false".equals(config.getEnablePartitionIndexes())) {
			cmap.put("partitions", config.getPartitions());
			cmap.put("primarykey", config.getPrimarykey());
			cmap.put("npartitions", config.getNpartitions());

		}
		if (!StringUtils.isEmpty(config.getPartitionKey())) {
			cmap.put("partitionKey", config.getPartitionKey());
		}
		if (!StringUtils.isEmpty(config.getUniqueKeys())) {
			cmap.put("uniqueKeys", config.getUniqueKeys());
		}
		// ElasticSearch
		cmap.put("allowsCustomElasticConfig", String.valueOf(config.isAllowsCustomElasticConfig()));
		cmap.put("allowsTemplateConfig", String.valueOf(config.isAllowsTemplateConfig()));

		cmap.put("allowsCustomIdConfig", String.valueOf(config.isAllowsCustomIdConfig()));
		cmap.put("allowsUpsertById", String.valueOf(config.isAllowsUpsertById()));
		if (!StringUtils.isEmpty(config.getCustomIdField())) {
			cmap.put("customIdField", config.getCustomIdField());
		}

		if (!StringUtils.isEmpty(config.getReplicas())) {
			cmap.put("replicas", config.getReplicas());
		}
		if (!StringUtils.isEmpty(config.getShards())) {
			cmap.put("shards", config.getShards());
		}
		if (!StringUtils.isEmpty(config.getPatternField())) {
			cmap.put("patternField", config.getPatternField());
		}
		if (!StringUtils.isEmpty(config.getPatternFunction())) {
			cmap.put("patternFunction", config.getPatternFunction());
		}
		if (!StringUtils.isEmpty(config.getSubstringStart())) {
			cmap.put("substringStart", config.getSubstringStart());
		}
		if (!StringUtils.isEmpty(config.getSubstringEnd())) {
			cmap.put("substringEnd", config.getSubstringEnd());
		}
		if (!StringUtils.isEmpty(config.getDatasourceDatabase())) {
			cmap.put("datasourceDatabase", config.getDatasourceDatabase());
		}
		if (!StringUtils.isEmpty(config.getDatasourceSchema())) {
			cmap.put("datasourceSchema", config.getDatasourceSchema());
		}
		if (!StringUtils.isEmpty(config.getDatasourceCatalog())) {
			cmap.put("datasourceCatalog", config.getDatasourceCatalog());
		}
		if (!StringUtils.isEmpty(config.getBucketName())) {
			cmap.put("bucketName", config.getBucketName());
		}
		if (!StringUtils.isEmpty(config.getMqttTopicName())) {
			cmap.put("mqttTopicName", config.getMqttTopicName());
		}

		return cmap;
	}

	@Override
	public HashMap<String, String> getAditionalDBConfig(Ontology ontology) {
		final HashMap<String, String> dbProperties = new HashMap<>();
		try {
			dbProperties.putAll(ontologyLogicService.getAdditionalDBConfig(ontology));
		} catch (final OntologyLogicServiceException e) {
			log.error("There was a problem getting DB config of ontology ", e);
		}
		return dbProperties;
	}

	@Override
	public void updateOntology(Ontology ontology, OntologyConfiguration config, boolean hasDocuments)
			throws OntologyBusinessServiceException {
		kafkaAuthorizationService.checkOntologyAclAfterUpdate(ontology);
		if (ontology.isAllowsCreateMqttTopic()) {
			mqttTopicService.createMqttTopic(ontology, config.getMqttTopicName());
		} else {
			mqttTopicService.deleteTopic(ontology);
		}
	}

	@Override
	public boolean existsOntology(String identificacion) {
		return ontologyService.existsOntology(identificacion);
	}

	private void metricsManagerLogControlPanelOntologyCreation(String userId, String result) {
		if (null != metricsManager) {
			metricsManager.logControlPanelOntologyCreation(userId, result);
		}
	}

	@Override
	public void deleteOntology(String id, String userId) throws JsonProcessingException {
		final Ontology o = ontologyService.getOntologyByIdForDelete(id, userId);
		final String identification = o.getIdentification();
		// IF IT'S A COSMOSDB COLLECTION AND NO RECORDS REMOVE IT
		if (RtdbDatasource.COSMOS_DB.equals(o.getRtdbDatasource())
				&& cosmosBasicOpsRepository.count(identification) == 0) {
			cosmosManageRepository.removeTable4Ontology(identification);
		}
		if (RtdbDatasource.PRESTO.equals(o.getRtdbDatasource())) {
			final String catalog = ontologyService.getOntologyPrestoByOntologyId(o).getDatasourceCatalog();
			if (catalog != null && prestoDatasourceConfigurationService.isHistoricalCatalog(catalog)) {
				prestoManageDBRepository.removeTable4Ontology(identification);
			}
		}
		if (RtdbDatasource.NEBULA_GRAPH.equals(o.getRtdbDatasource())) {
			nebulaGraphBusinessService.deleteNebulaGraphEntity(identification);
		}
		entityDeleteionService.deleteOntology(id, userId, false);
	}

	@Override
	public void uploadHistoricalFile(MultipartFile file, String ontology) throws OntologyBusinessServiceException {
		try {
			historicalMinioService.uploadMultipartFileForOntology(file, ontology);
		} catch (final HistoricalMinioException e) {
			throw new OntologyBusinessServiceException(
					OntologyBusinessServiceException.Error.PERSISTENCE_CREATION_ERROR,
					"Unable to upload file: " + e.getMessage());
		}
	}

	@Override
	public void deleteOntologyAndData(String id, String userId, boolean deleteData)
			throws OntologyBusinessServiceException, JsonProcessingException {
		final Ontology o = ontologyService.getOntologyByIdForDelete(id, userId);
		final String identification = o.getIdentification();
		if (!RtdbDatasource.PRESTO.equals(o.getRtdbDatasource())) {
			log.error("Error deleting ontology: Ontology Database Instance is not Presto - MinIO");
			throw new OntologyBusinessServiceException(OntologyBusinessServiceException.Error.ILLEGAL_ARGUMENT,
					"Ontology Database Instance is not Presto - MinIO");
		}
		final String catalog = ontologyService.getOntologyPrestoByOntologyId(o).getDatasourceCatalog();
		if (catalog != null && prestoDatasourceConfigurationService.isHistoricalCatalog(catalog)) {
			prestoManageDBRepository.removeTable4Ontology(identification, deleteData);
		}
		entityDeleteionService.deleteOntology(id, userId, false);
	}

	@Override
	public JSONArray ontologyBulkGeneration(HttpServletRequest request, String userId) {

		final JSONArray failedOntologiesArray = new JSONArray();

		final String datasourceId = request.getParameter("datasourceId");
		final String ontologies = request.getParameter("ontolgiesObject");
		final String database = request.getParameter("databaseObject");
		final String schema = request.getParameter("schemaObject");
		final JSONArray ontologiesArray = new JSONArray(request.getParameter("ontolgiesObject"));

		final String authorization = request.getParameter("authorizationsObject");
		final JSONArray authorizationsArray = new JSONArray(request.getParameter("authorizationsObject"));

		for (int i = 0; i < ontologiesArray.length(); i++) {
			final JSONObject failedOntologies = new JSONObject();
			final Ontology ontology = new Ontology();
			final JSONObject ontologyObject = ontologiesArray.getJSONObject(i);
			final JSONObject ontologySchema = ontologyObject.getJSONObject("schema");

			ontology.setIdentification(ontologyObject.getString("identification"));
			ontology.setXmlDiagram(null);
			ontology.setOntologyClass(null);
			ontology.setUser(userService.getUser(userId));
			ontology.setJsonSchema(ontologySchema.toString());
			ontology.setActive(true);
			ontology.setContextDataEnabled(false);
			ontology.setRtdbClean(false);
			ontology.setRtdbCleanLapse(null);
			ontology.setRtdbToHdb(false);
			ontology.setRtdbToHdbStorage(RtdbToHdbStorage.MONGO_GRIDFS);
			ontology.setDescription(ontologySchema.getString("description"));
			ontology.setMetainf("Meta-Inf");
			ontology.setOntologyKPI(null);
			ontology.setOntologyTimeSeries(null);
			ontology.setRtdbDatasource(RtdbDatasource.VIRTUAL);
			ontology.setAllowsCreateMqttTopic(false);
			ontology.setPartitionKey(null);

			final OntologyConfiguration ontologyConfiguration = new OntologyConfiguration();
			ontologyConfiguration.setDatasource(datasourceId);
			ontologyConfiguration.setDatasourceTableName(ontologyObject.getString("tableName"));
			ontologyConfiguration.setDatasourceDatabase(database);
			ontologyConfiguration.setDatasourceSchema(schema);
			ontologyConfiguration.setObjectId(ontologyObject.getString("associatedId"));
			try {
				ontologyService.createOntology(ontology, ontologyConfiguration);

				for (int i2 = 0; i2 < authorizationsArray.length(); i2++) {

					final JSONObject auth = authorizationsArray.getJSONObject(i2);
					final User userAuth = userService.getUser(auth.getString("users"));
					ontologyService.createUserAccess(ontology.getId(), userAuth.getId().toString(),
							auth.getString("accesstypes"), userId);
				}
			} catch (final Exception e) {

				failedOntologies.put("TableName", ontologyObject.getString("tableName"));
				failedOntologies.put("EntityName", ontologyObject.getString("identification"));
				failedOntologiesArray.put(failedOntologies);
			}

		}
		return failedOntologiesArray;
	}

	public boolean hasDocuments(Ontology ontology) {
		long count = 0;
		if (ontology.getRtdbDatasource() != RtdbDatasource.API_REST) {
			count = basicOpsRepository.findAll(ontology.getIdentification(), 1).size();
		}
		return count > 0;
	}
	
}

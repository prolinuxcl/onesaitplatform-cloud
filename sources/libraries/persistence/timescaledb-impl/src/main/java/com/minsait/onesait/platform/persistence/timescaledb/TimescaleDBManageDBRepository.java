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
package com.minsait.onesait.platform.persistence.timescaledb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.commons.model.DescribeColumnData;
import com.minsait.onesait.platform.commons.rtdbmaintainer.dto.ExportData;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.OntologyTimeSeries;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyDataType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesProperty.PropertyType;
import com.minsait.onesait.platform.config.model.OntologyTimeSeriesWindow.FrecuencyUnit;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleAggregates;
import com.minsait.onesait.platform.config.model.OntologyTimeseriesTimescaleProperties;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.ontology.OntologyTimeSeriesService;
import com.minsait.onesait.platform.persistence.exceptions.DBPersistenceException;
import com.minsait.onesait.platform.persistence.interfaces.ManageDBRepository;
import com.minsait.onesait.platform.persistence.timescaledb.config.TimescaleDBConfiguration;
import com.minsait.onesait.platform.persistence.timescaledb.util.TimescaleDBDescribeColumnRowMapper;
import com.minsait.onesait.platform.persistence.timescaledb.util.TimescaleDBPersistantException;

import lombok.extern.slf4j.Slf4j;

@Component("TimescaleDBManageDBRepository")
@Slf4j
public class TimescaleDBManageDBRepository implements ManageDBRepository {

	private static final String NOT_IMPLEMENTED_METHOD = "Not implemented method";
	private static final String BLANK = " ";
	@Autowired(required = false)
	@Qualifier(TimescaleDBConfiguration.TIMESCALEDB_TEMPLATE_JDBC_BEAN_NAME)
	private JdbcTemplate timescaleDBJdbcTemplate;

	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private OntologyTimeSeriesService ontologyTimeseriesService;

	protected ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	@Override
	public Map<String, Boolean> getStatusDatabase() {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public String createTable4Ontology(String ontology, String schema, Map<String, String> config) {
		// Get Timescale ontology properties
		final Ontology ontol = ontologyService.getOntologyByIdentification(ontology);
		final OntologyTimeSeries ontologyTimeserie = ontologyTimeseriesService.getOntologyByOntology(ontol);
		// Regular table creation
		// Extract Tags + Signals from Timeserie
		final StringBuilder query = new StringBuilder().append("CREATE TABLE " + ontology + "( ")
				.append("timestamp TIMESTAMPTZ NOT NULL");
		final StringBuilder tagQuery = new StringBuilder();
		final StringBuilder fieldQuery = new StringBuilder();
		final StringBuilder uniqueQuery = new StringBuilder().append("unique(timestamp");
		for (final OntologyTimeSeriesProperty property : ontologyTimeserie.getTimeSeriesProperties()) {

			final String name = property.getPropertyName();
			final String dataType = getTimescaleDataType(property.getPropertyDataType());

			if (property.getPropertyType() == PropertyType.TAG) {
				tagQuery.append(", ").append(name).append(" ").append(dataType).append(" NOT NULL");
				uniqueQuery.append(", ").append(name);
			} else {
				fieldQuery.append(", ").append(name).append(" ").append(dataType).append(" NULL");
			}
		}
		// Create table
		query.append(tagQuery).append(fieldQuery);
		// IF there is no aggregation, do not add UNIQUE clause
		if (ontologyTimeserie.getTimeSeriesTimescaleProperties() != null
				&& ontologyTimeserie.getTimeSeriesTimescaleProperties().getFrecuencyUnit() != FrecuencyUnit.NONE) {
			query.append(", ").append(uniqueQuery).append(")");
		}
		// close create
		query.append(");");
		try {
			timescaleDBJdbcTemplate.execute(query.toString());
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error creating table for ontology %s in TimescaleDB.Query=%s, Cause=%s, Message=%s", ontology,
					query.toString(), e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
		// Hypertable creation
		createHypertable(ontologyTimeserie);
		try {
			// SET data compression policies
			activateCompressionPolicy(ontologyTimeserie);
			// SET old data retention policies
			activateRetentionPolicy(ontologyTimeserie);
		} catch (DBPersistenceException e) {
			// Delete original table
			rollbackProcedure(ontology);
			throw e;
		}
		return null;
	}

	String getTimescaleDataType(PropertyDataType type) {
		String dataType = null;
		switch (type) {
		case INTEGER:
			dataType = "INTEGER";
			break;
		case NUMBER:
			dataType = "NUMERIC";
			break;
		case ARRAY:
			// NEED to define dataType for the array. This does not happend to
			// MongoDB, so meanwhile we set it to text array
			dataType = "TEXT []";
			break;
		case OBJECT:
			dataType = "JSONB";
			break;
		case STRING:
			dataType = "TEXT";
			break;
		case TIMESTAMP:
			dataType = "TIMESTAMPTZ";
			break;
		case BOOLEAN:
			dataType = "BOOLEAN";
			break;
		default:
			break;
		}
		return dataType;
	}

	void createHypertable(OntologyTimeSeries ontologyTimeserie) {
		final String hypertableQuery = ontologyTimeserie.getTimeSeriesTimescaleProperties().getHypertableQuery()
				.replaceAll("--(.*)[\n\r]", "").replace("\n\n", "");
		try {
			timescaleDBJdbcTemplate.execute(hypertableQuery);
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error creating HyperTable for ontology %s in TimescaleDB.Query=%s, Cause=%s, Message=%s",
					ontologyTimeserie.getOntology().getIdentification(), hypertableQuery, e.getCause(), e.getMessage());
			log.error(errorMessage);
			rollbackProcedure(ontologyTimeserie.getOntology().getIdentification());
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	void activateCompressionPolicy(OntologyTimeSeries ontologyTimeserie) {
		final String ontology = ontologyTimeserie.getOntology().getIdentification();
		if (ontologyTimeserie.getTimeSeriesTimescaleProperties().isCompressionActive()) {
			activateCompressionPolicy(ontology, ontologyTimeserie.getTimeSeriesTimescaleProperties());
		}
	}

	public void activateCompressionPolicy(String ontology,
			OntologyTimeseriesTimescaleProperties ontologyTimeseriesTimescaleProperties) {
		final String compressionInterval = ontologyTimeseriesTimescaleProperties.getCompressionAfter() + " "
				+ ontologyTimeseriesTimescaleProperties.getCompressionUnit().toString();
		final StringBuilder compressionPolicy = new StringBuilder().append("SELECT add_compression_policy('")
				.append(ontology).append("', INTERVAL '").append(compressionInterval).append("');");
		// Alter table for TimescaleDB -> Add policy but not active
		try {
			timescaleDBJdbcTemplate.execute(ontologyTimeseriesTimescaleProperties.getCompressionQuery());
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error altering table for compression policy. Ontology %s in TimescaleDB. Query=%s, Cause=%s, Message=%s",
					ontology, ontologyTimeseriesTimescaleProperties.getCompressionQuery(), e.getCause(),
					e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}

		// Activate policy
		try {
			timescaleDBJdbcTemplate.execute(compressionPolicy.toString());
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error activating compression policy. Ontology %s in TimescaleDB. Query=%s, Cause=%s, Message=%s",
					ontology, compressionPolicy.toString(), e.getCause(), e.getMessage());
			log.error(errorMessage);

			throw new DBPersistenceException(errorMessage, e);
		}
	}

	public void deactivateCompressionPolicy(String ontology) {
		final StringBuilder retentionPolicy = new StringBuilder().append("SELECT remove_compression_policy('")
				.append(ontology).append("');");
		try {
			timescaleDBJdbcTemplate.execute(retentionPolicy.toString());
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error deactivating compression policy. Ontology %s in TimescaleDB. Query=%s, Cause=%s, Message=%s",
					ontology, retentionPolicy.toString(), e.getCause(), e.getMessage());
			log.error(errorMessage);

			throw new DBPersistenceException(errorMessage, e);
		}
	}

	void activateRetentionPolicy(OntologyTimeSeries ontologyTimeserie) {
		final String ontology = ontologyTimeserie.getOntology().getIdentification();

		if (ontologyTimeserie.getTimeSeriesTimescaleProperties().isRetentionActive()) {
			activateRetentionPolicy(ontology, ontologyTimeserie.getTimeSeriesTimescaleProperties());
		}
	}

	public void activateRetentionPolicy(String ontology,
			OntologyTimeseriesTimescaleProperties ontologyTimeseriesTimescaleProperties) {
		final String retentionInterval = ontologyTimeseriesTimescaleProperties.getRetentionBefore().toString() + " "
				+ ontologyTimeseriesTimescaleProperties.getRetentionUnit();
		final StringBuilder retentionPolicy = new StringBuilder().append("SELECT add_retention_policy('")
				.append(ontology).append("', INTERVAL '").append(retentionInterval).append("');");
		try {
			timescaleDBJdbcTemplate.execute(retentionPolicy.toString());
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error activating retention policy. Ontology %s in TimescaleDB. Query=%s, Cause=%s, Message=%s",
					ontology, retentionPolicy.toString(), e.getCause(), e.getMessage());
			log.error(errorMessage);

			// Delete original table
			rollbackProcedure(ontology);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	public void deactivateRetentionPolicy(String ontology) {
		final StringBuilder retentionPolicy = new StringBuilder().append("SELECT remove_retention_policy('")
				.append(ontology).append("');");
		try {
			timescaleDBJdbcTemplate.execute(retentionPolicy.toString());
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error deactivating retention policy. Ontology %s in TimescaleDB. Query=%s, Cause=%s, Message=%s",
					ontology, retentionPolicy.toString(), e.getCause(), e.getMessage());
			log.error(errorMessage);

			throw new DBPersistenceException(errorMessage, e);
		}

	}

	@Override
	public List<String> getListOfTables() {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) {
		try {
			return timescaleDBJdbcTemplate.queryForList(
					"SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' and (table_name LIKE ? or table_name LIKE ?)",
					String.class, ontology.toLowerCase() + "%", ontology.toLowerCase() + "\\.%");
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error listing tables for ontology %s in TimescaleDB. Cause=%s, Message=%s", ontology, e.getCause(),
					e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void removeTable4Ontology(String ontology) {
		try {
			timescaleDBJdbcTemplate.execute(String.format("DROP TABLE  %s CASCADE;", ontology));
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error deleting table %s from user in TimescaleDB. Cause=%s, Message=%s", ontology, e.getCause(),
					e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void createIndex(String ontology, String attribute) {
		try {
			final String statement = String.format("CREATE INDEX ON %s (%s);", ontology, attribute);
			timescaleDBJdbcTemplate.execute(statement);
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error creating index on table %s in TimescaleDB. Cause=%s, Message=%s", ontology, e.getCause(),
					e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}

	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) {
		try {
			final String statement = String.format("CREATE INDEX %s ON %s (%s);", nameIndex, ontology, attribute);
			timescaleDBJdbcTemplate.execute(statement);
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error creating named index %s on table %s in TimescaleDB. Cause=%s, Message=%s", nameIndex,
					ontology, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void createIndex(String sentence) {
		try {
			timescaleDBJdbcTemplate.execute(sentence);
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error creating index with statement %s in TimescaleDB. Cause=%s, Message=%s", sentence,
					e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void dropIndex(String ontology, String indexName) {
		try {
			final String statement = String.format("DROP INDEX IF EXISTS %s", indexName);
			timescaleDBJdbcTemplate.execute(statement);
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error dropping named index %s on table %s in TimescaleDB. Cause=%s, Message=%s", indexName,
					ontology, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public List<String> getListIndexes(String ontology) {
		try {
			return timescaleDBJdbcTemplate.queryForList(
					"SELECT indexname FROM pg_indexes WHERE schemaname = 'public' and (table_name LIKE ? or table_name LIKE ?) ORDER BY indexname",
					String.class, ontology + "%", ontology + "\\.%");
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error listing indexes for table %s in TimescaleDB. Cause=%s, Message=%s", ontology, e.getCause(),
					e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public String getIndexes(String ontology) {
		try {
			return objectMapper.writeValueAsString(getListIndexes(ontology));
		} catch (final Exception e) {
			final String errorMessage = String.format(
					"Error listing indexes for table %s in TimescaleDB. Cause=%s, Message=%s", ontology, e.getCause(),
					e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void validateIndexes(String ontology, String schema) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public ExportData exportToJson(String ontology, long startDateMillis, String path) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public long deleteAfterExport(String ontology, String query) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public List<DescribeColumnData> describeTable(String name) {
		List<DescribeColumnData> descriptors = new ArrayList<>();

		try {
			final String sql = String.format(
					"SELECT COLUMN_NAME , UDT_NAME\n" + "FROM information_schema.COLUMNS  \n" + "WHERE TABLE_NAME =",
					name);
			descriptors = timescaleDBJdbcTemplate.query(sql, new TimescaleDBDescribeColumnRowMapper());

		} catch (final DataAccessException e) {
			final String errorMessage = String.format("Error describing table %s in TimescaleDB. Cause=%s, Message=%s",
					name, e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}

		return descriptors;
	}

	@Override
	public Map<String, String> getAdditionalDBConfig(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public String updateTable4Ontology(String identification, String jsonSchema, Map<String, String> newFields) {
		// extract new Signals and execute the alter table statement
		if (newFields != null && !newFields.isEmpty()) {
			final StringBuilder query = new StringBuilder().append("ALTER TABLE ").append(identification);
			String prefix = "";
			for (final Entry<String, String> field : newFields.entrySet()) {
				final String dataType = getTimescaleDataType(PropertyDataType.valueOf(field.getValue()));
				query.append(prefix).append(" ADD COLUMN ").append(field.getKey()).append(" ").append(dataType);
				prefix = ",";
			}
			query.append(";");
			try {
				timescaleDBJdbcTemplate.execute(query.toString());
			} catch (final Exception e) {

				final String errorMessage = String.format(
						"Error while updating timescaleDB ontology %s table. Query=%s, Cause=%s, Message=%s",
						identification, query.toString(), e.getCause(), e.getMessage());
				log.error(errorMessage);
				throw new DBPersistenceException(errorMessage, e);
			}
		}
		return "";
	}

	private void rollbackProcedure(String ontology) {
		try {
			final String sql = String.format("DROP TABLE  %s CASCADE;", ontology);
			timescaleDBJdbcTemplate.execute(sql);

		} catch (final DataAccessException e) {
			final String errorMessage = String.format(
					"Error while rolling back table creation.Table %s in TimescaleDB. Cause=%s, Message=%s", ontology,
					e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}

	}

	public void createContinuousAggregate(OntologyTimeseriesTimescaleAggregates aggregate) {
		// Create materialized view
		try {
			timescaleDBJdbcTemplate.execute(aggregate.getAggregateQuery());
		} catch (final DataAccessException e) {
			final String errorMessage = String.format(
					"Error while creating aggregate %s for timeserie %s in TimescaleDB. Cause=%s, Message=%s",
					aggregate.getName(), aggregate.getOntologyTimeSeries().getOntology().getIdentification(),
					e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new TimescaleDBPersistantException(errorMessage, e,
					TimescaleDBPersistantException.ErrorType.AGGREGATE_CREATION);
		}
		// Create execution policy
		try {
			final String tableName = aggregate.getOntologyTimeSeries().getOntology().getIdentification() + "_"
					+ aggregate.getName();
			final String startOffset = aggregate.getStartOffset() + BLANK + aggregate.getStartOffsetUnit();
			final String endOffset = aggregate.getEndOffset() + BLANK + aggregate.getEndOffsetUnit();
			final String scheduleInterval = aggregate.getSchedulerFrequency() + BLANK
					+ aggregate.getSchedulerFrequencyUnit();
			final String policyQuery = String.format(
					"SELECT add_continuous_aggregate_policy('%s',\n" + "    start_offset => INTERVAL '%s',\n"
							+ "    end_offset => INTERVAL '%s',\n" + "    schedule_interval => INTERVAL '%s');",
					tableName, startOffset, endOffset, scheduleInterval);
			timescaleDBJdbcTemplate.execute(policyQuery);
		} catch (final DataAccessException e) {
			final String errorMessage = String.format(
					"Error while creating aggregate policy %s for timeserie %s in TimescaleDB. Cause=%s, Message=%s",
					aggregate.getName(), aggregate.getOntologyTimeSeries().getOntology().getIdentification(),
					e.getCause(), e.getMessage());
			log.error(errorMessage);
			deleteContinuousAggregate(aggregate.getOntologyTimeSeries(), aggregate.getName());
			throw new TimescaleDBPersistantException(errorMessage, e,
					TimescaleDBPersistantException.ErrorType.AGGREGATE_POLICY);
		}
	}

	public void deleteContinuousAggregate(OntologyTimeSeries ontologyTimeserie, String aggregateName) {
		final String tableName = ontologyTimeserie.getOntology().getIdentification() + "_" + aggregateName;
		final String query = "DROP MATERIALIZED view " + tableName;
		try {
			timescaleDBJdbcTemplate.execute(query);
		} catch (final DataAccessException e) {
			final String errorMessage = String.format(
					"Error while removing aggregate policy %s for timeserie %s in TimescaleDB. Cause=%s, Message=%s",
					aggregateName, ontologyTimeserie.getOntology().getIdentification(), e.getCause(), e.getMessage());
			log.error(errorMessage);
			throw new DBPersistenceException(errorMessage, e);
		}
	}

	@Override
	public void createTTLIndex(String ontology, String attribute, Long seconds) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);

	}

	@Override
	public Map<String, List<String>> getListIndexes(String datatableName, String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public void dropIndex(String ontology, String ontologyVirtual, String indexName) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);

	}

	@Override
	public String getIndexesOptions(String ontology) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);
	}

	@Override
	public void createIndexWithParameter(String ontologyName, String typeIndex, String indexName, boolean unique,
			boolean background, boolean sparse, boolean ttl, String timesecondsTTL, Object checkboxValuesArray) {
		throw new DBPersistenceException(NOT_IMPLEMENTED_METHOD);

	}

}

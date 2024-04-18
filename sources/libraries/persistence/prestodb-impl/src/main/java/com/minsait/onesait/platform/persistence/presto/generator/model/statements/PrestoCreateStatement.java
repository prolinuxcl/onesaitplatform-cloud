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
package com.minsait.onesait.platform.persistence.presto.generator.model.statements;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.minsait.onesait.platform.persistence.presto.generator.PrestoSQLGenerator;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.ColumnPresto;
import com.minsait.onesait.platform.persistence.presto.generator.model.common.HistoricalOptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

@NoArgsConstructor
public class PrestoCreateStatement extends CreateTable {
	@NotNull
	@Size(min = 1)
	@Getter
	private String ontology;
	@Getter
	@Setter
	private String database;
	@Getter
	@Setter
	private String schema;
	@Getter
	@Setter
	private String type = null;
	List<ColumnPresto> columns = new ArrayList<>();
	@Getter
	@Setter
	private HistoricalOptions historicalOptions;
	@NotNull
	@Getter
	@Setter
	private PrestoSQLGenerator sqlGenerator;

	public PrestoCreateStatement(final PrestoSQLGenerator sqlGenerator) {
		this.sqlGenerator = sqlGenerator;
	}

	public PrestoCreateStatement setOntology(String ontology) {
		if (ontology != null && !ontology.trim().isEmpty()) {
			this.ontology = ontology.trim();
			this.setTable(new Table(this.ontology));
		} else {
			throw new IllegalArgumentException("Ontology in model can't be null or empty");
		}
		return this;

	}

	public PrestoCreateStatement addColumn(ColumnPresto col) {
		final List<String> colNames = new ArrayList<>();
		final List<ColumnDefinition> existentCols = getColumnDefinitions();
		if (existentCols != null) {
			for (final ColumnDefinition colDef : existentCols) {
				colNames.add(colDef.getColumnName());
			}
			if (colNames.contains(col.getColumnName())) {
				throw new IllegalArgumentException("Invalid input: duplicated column name: " + col.getColumnName());
			}
		}
		this.columns.add(col);
		return this;
	}

	public String toString(boolean enClosePathElements) {
		if (enClosePathElements) {
			this.setTable(new Table((database == null || "".equals(database) ? "" : "\"" + database + "\".")
					+ (schema == null || "".equals(schema) ? "" : "\"" + schema + "\".")
					+ (ontology == null || "".equals(ontology) ? "" : "\"" + ontology + "\"")));
		} else {
			this.setTable(new Table((database == null || "".equals(database) ? "" : database + ".")
					+ (schema == null || "".equals(schema) ? "" : schema + ".")
					+ (ontology == null || "".equals(ontology) ? "" : ontology)));
		}
		return super.toString();
	}

	public List<ColumnPresto> getColumnsPresto() {
		return columns;
	}

	public void setColumnsPresto(List<ColumnPresto> columns) {
		this.columns = columns;
	}

}

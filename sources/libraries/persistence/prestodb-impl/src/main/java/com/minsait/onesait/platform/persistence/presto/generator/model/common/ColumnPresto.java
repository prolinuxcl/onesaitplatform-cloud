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
package com.minsait.onesait.platform.persistence.presto.generator.model.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

@NoArgsConstructor
@AllArgsConstructor
public class ColumnPresto extends ColumnDefinition {

	@Getter
	@Setter
	private boolean notNull = false;
	@Getter
	@Setter
	private String colComment = null;

	public ColumnPresto(ColumnDefinition col) {
		super();
		this.setColumnName(col.getColumnName());
		this.setColDataType(col.getColDataType());
		this.setColumnSpecs(col.getColumnSpecs());
	}

	public void setColDataType(String dataType) {
		this.getColDataType().setDataType(dataType);
	}

	public void addSpecification(String spec) {
		this.getColumnSpecs().add(spec);
	}

	public String getStringColDataType() {
		return this.getColDataType().getDataType();
	}

}

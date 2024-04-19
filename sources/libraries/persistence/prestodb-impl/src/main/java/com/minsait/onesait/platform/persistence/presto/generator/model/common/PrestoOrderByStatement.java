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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PrestoOrderByStatement {
	@Size(min = 1)
	@NotNull
	private String column;
	private String order;

	public String getColumn() {
		return column;
	}

	public PrestoOrderByStatement setColumn(String column) {
		this.column = column != null ? column.trim() : null;
		return this;
	}

	public String getOrder() {
		return order;
	}

	public PrestoOrderByStatement setOrder(String order) {
		this.order = order == null || order.isEmpty() ? "ASC" : order.trim();
		return this;
	}

}
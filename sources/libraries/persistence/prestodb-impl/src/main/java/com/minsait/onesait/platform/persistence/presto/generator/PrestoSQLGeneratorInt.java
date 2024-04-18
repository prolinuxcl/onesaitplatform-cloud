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
package com.minsait.onesait.platform.persistence.presto.generator;

import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoCreateStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoDeleteStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoDropStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoInsertStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoPreparedStatement;
import com.minsait.onesait.platform.persistence.presto.generator.model.statements.PrestoSelectStatement;

public interface PrestoSQLGeneratorInt {
	PrestoPreparedStatement generate(PrestoSelectStatement selectStatement, boolean withParams);

	PrestoPreparedStatement generate(PrestoInsertStatement insert, boolean withParams);

	PrestoPreparedStatement generate(PrestoDeleteStatement deleteStatement, boolean withParams);

	PrestoPreparedStatement generate(PrestoDropStatement dropStatement);
	
	PrestoPreparedStatement generate(PrestoCreateStatement createStatement);
		
	PrestoSelectStatement buildSelect();

	PrestoInsertStatement buildInsert();

	PrestoDeleteStatement buildDelete();

	PrestoDropStatement buildDrop();
	
	PrestoCreateStatement buildCreate();
	
}

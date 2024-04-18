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
package com.minsait.onesait.platform.persistence.external.generator;

import com.minsait.onesait.platform.persistence.external.generator.model.statements.CreateStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DeleteStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.DropStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.GetIndexStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.InsertStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.PreparedStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.SelectStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpdateStatement;
import com.minsait.onesait.platform.persistence.external.generator.model.statements.UpsertStatement;

public interface SQLGeneratorInt {
	PreparedStatement generate(SelectStatement selectStatement, boolean withParams);

	PreparedStatement generate(InsertStatement insert, boolean withParams);

	PreparedStatement generate(DeleteStatement deleteStatement, boolean withParams);

	PreparedStatement generate(UpdateStatement updateStatement, boolean withParams);

	PreparedStatement generate(DropStatement dropStatement);

	PreparedStatement generate(CreateStatement createStatement);

	PreparedStatement generate(GetIndexStatement getIndexStatement);

	SelectStatement buildSelect();

	InsertStatement buildInsert();

	UpdateStatement buildUpdate();

	DeleteStatement buildDelete();

	DropStatement buildDrop();

	CreateStatement buildCreate();

	GetIndexStatement buildGetIndex();

	PreparedStatement generate(UpsertStatement updateStatement, boolean withParams);

	UpsertStatement buildUpsert();

}

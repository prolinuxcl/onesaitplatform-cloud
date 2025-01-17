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
package com.minsait.onesait.platform.config.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minsait.onesait.platform.config.model.ConsoleMenu;
import com.minsait.onesait.platform.config.model.Role;

public interface ConsoleMenuRepository extends JpaRepository<ConsoleMenu, String> {

	@Override

	<S extends ConsoleMenu> List<S> saveAll(Iterable<S> entities);

	@Override

	void flush();

	@Override

	<S extends ConsoleMenu> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override

	ConsoleMenu save(ConsoleMenu entity);

	@Override

	void delete(ConsoleMenu id);

	@Override
	List<ConsoleMenu> findAll();

	@Override

	void deleteAll();

	ConsoleMenu findByRoleType(Role roleType);

}

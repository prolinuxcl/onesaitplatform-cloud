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

import com.minsait.onesait.platform.config.model.ActionsDigitalTwinType;
import com.minsait.onesait.platform.config.model.DigitalTwinType;

public interface ActionsDigitalTwinTypeRepository extends JpaRepository<ActionsDigitalTwinType, String> {

	List<ActionsDigitalTwinType> findByNameIgnoreCase(String name);

	List<ActionsDigitalTwinType> findByDescription(String description);

	ActionsDigitalTwinType findByName(String name);

	List<ActionsDigitalTwinType> findAllByOrderByNameAsc();

	List<ActionsDigitalTwinType> findByDescriptionContaining(String description);

	List<ActionsDigitalTwinType> findByNameContaining(String name);

	List<ActionsDigitalTwinType> findByNameLikeAndDescriptionLike(String name, String description);

	List<ActionsDigitalTwinType> findByNameContainingAndDescriptionContaining(String name, String description);

	List<ActionsDigitalTwinType> findByTypeId(DigitalTwinType digitalTwinType);
}

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

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.minsait.onesait.platform.config.model.KafkaClusterInstance;

public interface KafkaClusterInstanceRepository extends JpaRepository<KafkaClusterInstance, String> {

	List<KafkaClusterInstance> findByIdentification(String identification);

	List<KafkaClusterInstance> findByIdentificationContaining(String identification);

	List<KafkaClusterInstance> findByIdentificationAndDescription(String identification, String description);

	List<KafkaClusterInstance> findByIdentificationContainingAndDescriptionContaining(String identification,
			String description);

	List<KafkaClusterInstance> findByDescription(String description);

	List<KafkaClusterInstance> findByDescriptionContaining(String description);
	
	@Modifying
	@Transactional
	void deleteByIdentification(String identification);

	@Modifying
	@Transactional
	void deleteById(String id);
}

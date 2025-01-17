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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Subcategory;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

	Subcategory findById(String id);

	List<Subcategory> findByCategory(Category category);

	Subcategory findByIdentification(String identification);

	List<Subcategory> findByDescription(String description);

	List<Subcategory> findByIdentificationContainingAndDescriptionContaining(String identification, String description);

	List<Subcategory> findByIdentificationContaining(String identification);

	List<Subcategory> findByDescriptionContaining(String description);

	List<Subcategory> findAllByOrderByIdentificationAsc();

	List<Subcategory> findByIdentificationAndDescription(String identification, String description);

	List<Subcategory> findByIdentificationLikeAndDescriptionLike(String identification, String description);

	Subcategory findByIdentificationAndCategory(String identification, Category category);

	@Query("select o.identification from Subcategory as o where o.category = :category")
	List<String> findIdentificationsByCategory(@Param("category") Category category);

}

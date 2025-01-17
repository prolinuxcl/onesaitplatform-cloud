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
package com.minsait.onesait.platform.config.services.category;

import java.util.List;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.Category.Type;

public interface CategoryService {

	List<Category> getCategoriesByIdentificationAndDescription(String identification, String description);

	List<String> getAllIdentifications();

	void createCategory(Category category);

	Category getCategoryToUpdate(String id);

	void updateCategory(Category category);

	Category getCategoryById(String id);

	Category getCategoryByIdentification(String identification);
	
	List<Category> getCategoryByIdentificationLike(String identification);
	
	List<Category> getCategoryByDescriptionLike(String description);

	List<Category> findAllCategories();

	void deleteCategory(String id);

	List<Type> getCategoryTypeList();

	List<Category> getCategoriesByTypeAndGeneralType(Type type);

	boolean isValidCategoryType(Type elementType, Type categoryType);

	List<Category> getCategoriesByType(Type type);

}

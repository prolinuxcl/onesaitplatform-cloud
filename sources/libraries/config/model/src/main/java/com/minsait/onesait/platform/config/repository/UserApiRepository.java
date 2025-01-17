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

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserApi;

public interface UserApiRepository extends JpaRepository<UserApi, String> {

	List<UserApi> findByUser(User user);

	@Query("SELECT ua FROM UserApi AS ua WHERE ua.api.id = :api")
	List<UserApi> findByApiId(@Param("api") String api);

	@Query("SELECT ua FROM UserApi AS ua WHERE ua.api.id = :api and ua.user.userId = :user")
	UserApi findByApiIdAndUser(@Param("api") String api, @Param("user") String user);

	@Query("SELECT ua FROM UserApi AS ua WHERE ua.api.user.userId = :userId")
	List<UserApi> findByOwner(@Param("userId") String userId);

	@Modifying
	@Transactional
	void deleteByIdNotIn(Collection<String> ids);
}
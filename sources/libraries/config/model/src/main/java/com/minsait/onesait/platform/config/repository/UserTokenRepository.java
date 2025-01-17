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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.UserToken;

public interface UserTokenRepository extends JpaRepository<UserToken, String> {

	@Override

	void flush();

	@Override

	<S extends UserToken> S saveAndFlush(S entity);

	@SuppressWarnings("unchecked")
	@Override

	UserToken save(UserToken entity);

	@Override
	@Transactional
	void delete(UserToken id);

	@Override
	@Transactional
	void deleteAll();

	List<UserToken> findByUser(User user);

	UserToken findByToken(String token);

	UserToken findByUserAndToken(String user, String token);

	UserToken findByUserAndToken(User user, String token);

	@Transactional
	@Modifying
	@Query("DELETE FROM UserToken u WHERE u.user.userId= :userId")
	void deleteByUser(@Param("userId") String userId);

}

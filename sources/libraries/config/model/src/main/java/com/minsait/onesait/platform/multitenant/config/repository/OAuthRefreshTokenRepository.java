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
package com.minsait.onesait.platform.multitenant.config.repository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.multitenant.config.model.OAuthRefreshToken;

@Transactional
public interface OAuthRefreshTokenRepository extends JpaRepository<OAuthRefreshToken, String> {

	public static final String OAUTH_REFRESH_TOKEN_REPOSITORY = "OauthRefreshTokenRepository";

	@Cacheable(cacheNames = OAUTH_REFRESH_TOKEN_REPOSITORY, unless = "#result == null", key = "#p0")
	OAuthRefreshToken findByTokenId(String tokenId);

	@Override
	@CachePut(cacheNames = OAUTH_REFRESH_TOKEN_REPOSITORY, unless = "#result == null", key = "#p0.tokenId")
	<S extends OAuthRefreshToken> S save(S entity);

	@Modifying
	@Transactional
	@CacheEvict(cacheNames = OAUTH_REFRESH_TOKEN_REPOSITORY,key = "#p0")
	void deleteByTokenId(String tokenId);

}

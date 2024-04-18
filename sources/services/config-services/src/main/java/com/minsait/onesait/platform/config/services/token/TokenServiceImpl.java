/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.config.services.token;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.repository.TokenRepository;
import com.minsait.onesait.platform.config.services.exceptions.TokenServiceException;

@Service

public class TokenServiceImpl implements TokenService {

	@Autowired
	private TokenRepository tokenRepository;

	@Override
	public Token generateTokenForClient(ClientPlatform clientPlatform) {
		Token token = new Token();
		if (clientPlatform.getId() != null) {
			token.setClientPlatform(clientPlatform);
			token.setTokenName(UUID.randomUUID().toString().replaceAll("-", ""));
			token.setActive(true);
			if (this.tokenRepository.findByTokenName(token.getTokenName()) == null) {
				token = this.tokenRepository.save(token);
			} else {
				throw new TokenServiceException("Token with value " + token.getTokenName() + " already exists");
			}
		}
		return token;
	}

	@Override
	public Token getToken(ClientPlatform clientPlatform) {
		return this.tokenRepository.findByClientPlatform(clientPlatform).get(0);
	}

	@Override
	public Token getTokenByToken(String token) {
		return this.tokenRepository.findByTokenName(token);
	}

	@Override
	public void deactivateToken(Token token, boolean active) {
		token.setActive(active);
		this.tokenRepository.save(token);

	}

	@Override
	public Token getTokenByID(String id) {
		return this.tokenRepository.findById(id);
	}

	@Override
	public List<Token> getTokens(ClientPlatform clientPlatform) {
		return this.tokenRepository.findByClientPlatform(clientPlatform);
	}

}

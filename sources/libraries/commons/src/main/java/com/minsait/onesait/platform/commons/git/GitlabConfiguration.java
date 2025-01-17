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
package com.minsait.onesait.platform.commons.git;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GitlabConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	private String site;
	private String user;
	private String password;
	private String privateToken;
	private String email;
	private String branch;
	private String projectURL;
	private String projectName;
	private String gitlabGroup;
	private Boolean enable;
	private String lastCommitSHA;

}

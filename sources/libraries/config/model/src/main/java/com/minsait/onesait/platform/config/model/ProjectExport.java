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
package com.minsait.onesait.platform.config.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PROJECT")
@Configurable
public class ProjectExport extends ProjectParent {

	private static final long serialVersionUID = 1L;

	public enum ProjectType {
		ENGINE, THINGS, INTELLIGENCE
	}

	@ManyToMany(cascade = { CascadeType.PERSIST }, mappedBy = "projects", fetch = FetchType.EAGER)
	@Getter
	@Setter
	@JsonIgnore
	private Set<UserExport> users = new HashSet<>();

	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	@Getter
	@Setter
	private ProjectType type;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@Getter
	@Setter
	private Set<ProjectResourceAccessExport> projectResourceAccesses = new HashSet<>();

	@OneToOne(mappedBy = "project")
	@Getter
	@Setter
	private AppExport app;

}

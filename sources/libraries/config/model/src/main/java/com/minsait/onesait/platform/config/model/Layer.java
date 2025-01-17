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
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "LAYER")
public class Layer extends OPResource implements Versionable<Layer> {

	private static final long serialVersionUID = 1L;

	@Column(name = "DESCRIPTION", length = 512, unique = false, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY", referencedColumnName = "ID", nullable = true)
	@Getter
	@Setter
	private Ontology ontology;

	@Column(name = "GEOMETRY_TYPE", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String geometryType;

	@Column(name = "GEOMETRY_FIELD", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String geometryField;

	@Column(name = "SYMBOL", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String symbol;

	@Column(name = "INNER_COLOR", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String innerColor;

	@Column(name = "OUTER_COLOR", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String outerColor;

	@Column(name = "LAYER_SIZE", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String size;

	@Column(name = "OUTER_THIN", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String outerThin;

	@Column(name = "URL", length = 1024, unique = false, nullable = true)
	@Getter
	@Setter
	private String url;

	@Column(name = "EXTERNAL_TYPE", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String externalType;

	@Column(name = "LAYER_TYPE_WMS", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String layerTypeWms;

	@Column(name = "INFO_BOX", length = 2000, unique = false, nullable = true)
	@Getter
	@Setter
	private String infoBox;

	@Column(name = "FILTERS", length = 2000, unique = false, nullable = true)
	@Getter
	@Setter
	private String filters;

	@Column(name = "QUERY_PARAMS", length = 2000, unique = false, nullable = true)
	@Getter
	@Setter
	private String queryParams;

	@Column(name = "QUERY", length = 2000, unique = false, nullable = true)
	@Getter
	@Setter
	private String query;

	@Column(name = "IS_PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "IS_VIRTUAL", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	@Getter
	@Setter
	private boolean isVirtual;

	@Column(name = "IS_HEAT_MAP", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	@Getter
	@Setter
	private boolean isHeatMap;

	@Column(name = "IS_FILTER", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@NotNull
	@Getter
	@Setter
	private boolean isFilter;

	@Column(name = "WEIGHT_FIELD", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private String weightField;

	@Column(name = "HEAT_MAP_MIN", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private Integer heatMapMin;

	@Column(name = "REFRESH_TIME", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private Integer refreshTime;

	@Column(name = "HEAT_MAP_MAX", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private Integer heatMapMax;

	@Column(name = "HEAT_MAP_RADIUS", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private Integer heatMapRadius;

	@Column(name = "EAST", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private Double east;

	@Column(name = "WEST", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private Double west;

	@Column(name = "NORTH", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private Double north;

	@Column(name = "SOUTH", length = 50, unique = false, nullable = true)
	@Getter
	@Setter
	private Double south;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "LAYER_VIEWER", joinColumns = @JoinColumn(name = "LAYER_ID"), inverseJoinColumns = @JoinColumn(name = "VIEWER_ID"))
	@Getter
	@Setter
	@JsonIgnore
	private Set<Viewer> viewers = new HashSet<>();

	@JsonSetter("ontology")
	public void setOntologyJson(String id) {
		if (!StringUtils.isEmpty(id)) {
			final Ontology o = new Ontology();
			o.setId(id);
			ontology = o;
		}
	}

	@JsonGetter("ontology")
	public String getOntologyJson() {
		if(ontology != null) {
			return ontology.getId();
		}
		return null;
	}

	@Override
	public String fileName() {
		return getIdentification() + ".yaml";
	}

	@Override
	public Versionable<Layer> runExclusions(Map<String, Set<String>> excludedIds, Set<String> excludedUsers) {
		Versionable<Layer> o = Versionable.super.runExclusions(excludedIds, excludedUsers);
		if (o != null) {
			if (!viewers.isEmpty() && !CollectionUtils.isEmpty(excludedIds.get(Viewer.class.getSimpleName()))) {
				viewers.removeIf(v -> excludedIds.get(Viewer.class.getSimpleName()).contains(v.getId()));
				o = this;
			}
			if (ontology != null && !CollectionUtils.isEmpty(excludedIds.get(Ontology.class.getSimpleName()))
					&& excludedIds.get(Ontology.class.getSimpleName()).contains(ontology.getId())) {
				o = null;
			}
		}
		return o;
	}

}

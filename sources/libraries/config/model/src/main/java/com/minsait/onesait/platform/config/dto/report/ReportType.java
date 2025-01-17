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
package com.minsait.onesait.platform.config.dto.report;

public enum ReportType {

	PDF("application/pdf", "pdf"), JRXML("application/jrxml", "jrxml"), XML("application/xml", "xml"), HTML("text/html",
			"html"), XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"), CSV("text/csv",
					"csv"), DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");

	private ReportType(String contentType, String extension) {
		this.contentType = contentType;
		this.extension = extension;
	}

	private String contentType;
	private String extension;

	public String contentType() {
		return contentType;
	}

	public String extension() {
		return extension;
	}
}

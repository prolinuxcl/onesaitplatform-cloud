/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.controlpanel.controller.openrefine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

@Controller
@RequestMapping("datacleanerui")
@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR,ROLE_DATASCIENTIST')")
public class DataCleanerUIController {
	@Autowired
	private IntegrationResourcesService resourcesService;

	@GetMapping
	public String iframe(Model model) {
		model.addAttribute("url", getDataCleanerUrl());
		return "datacleaner/ui";
	}

	private String getDataCleanerUrl() {
		return resourcesService.getUrl(Module.DATACLEANERUI, ServiceUrl.BASE);
	}
}

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
package com.minsait.onesait.platform.controlpanel.controller.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.config.model.DroolsRule;
import com.minsait.onesait.platform.config.model.DroolsRuleDomain;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.services.drools.DroolsRuleService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.project.ProjectService;
import com.minsait.onesait.platform.controlpanel.services.rules.BusinessRuleService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/rule-domains")
@Slf4j
public class DomainRuleController {

	@Autowired
	private DroolsRuleService droolsRuleService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private BusinessRuleService businessRuleService;

	private static final String DOMANI_ID = "domainId";
	private static final String ERROR_403 = "error/403";
	private static final String REDIRECT_RULEDOMAINS = "redirect:/rule-domains/";
	private static final String PATH_TO_RULES = "src/main/resources/rules/";

	@GetMapping("list")
	public String list(Model model) {
		final DroolsRuleDomain domain = droolsRuleService.getUserDomain(utils.getUserId());
		if (domain == null)
			businessRuleService.createDomain(utils.getUserId());

		model.addAttribute("domains", domains());
		return "rules/list";
	}

	@GetMapping("data")
	public @ResponseBody List<RuleDomainDTO> domains() {
		return droolsRuleService.getAllDomains(utils.getUserId()).stream()
				.map(drd -> RuleDomainDTO.builder().id(drd.getId()).name(drd.getIdentification())
						.owner(drd.getUser().getUserId()).active(drd.isActive())
						.rules(droolsRuleService.countRules(drd.getUser().getUserId())).build())
				.collect(Collectors.toList());
	}

	@GetMapping("{id}/rule")
	public String create(Model model, @PathVariable("id") String id) {
		final Set<Ontology> ontologies = new LinkedHashSet<>(ontologyService.getAllOntologies(utils.getUserId()));
		ontologies.addAll(projectService.getResourcesForUserOfType(utils.getUserId(), Ontology.class));
		model.addAttribute("ontologies", ontologies);
		model.addAttribute("types", DroolsRule.Type.values());
		DroolsRuleDTO rule = new DroolsRuleDTO();
		if (null != model.asMap().get("rule"))
			rule = (DroolsRuleDTO) model.asMap().get("rule");
		model.addAttribute("rule", rule);
		model.addAttribute(DOMANI_ID, id);

		return "rules/create";
	}

	@PostMapping("{domainId}/rule")
	public String save(Model model, @PathVariable("domainId") String domainId, DroolsRuleDTO rule,
			RedirectAttributes ra) {
		try {
			businessRuleService.save(rule, utils.getUserId());
		} catch (final Exception e) {
			utils.addRedirectException(e, ra);
			rule.setId(null);
			ra.addFlashAttribute("rule", rule);
			return REDIRECT_RULEDOMAINS + domainId + "/rule";
		}
		return REDIRECT_RULEDOMAINS + domainId + "/rules";
	}

	@PostMapping("{id}/rule/{rule}")
	public String update(Model model, @PathVariable("id") String id, @PathVariable("rule") String identification,
			DroolsRuleDTO rule, RedirectAttributes ra) {
		try {
			if (!droolsRuleService.hasUserEditPermission(identification, utils.getUserId()))
				return ERROR_403;
			businessRuleService.update(rule, utils.getUserId(), identification);
		} catch (final Exception e) {
			utils.addRedirectException(e, ra);
			ra.addFlashAttribute("rule", rule);
			return REDIRECT_RULEDOMAINS + id + "/rule/" + identification;

		}
		return REDIRECT_RULEDOMAINS + id + "/rules";
	}

	@GetMapping("{id}/rule/{rule}")
	public String create(Model model, @PathVariable("id") String id, @PathVariable("rule") String identification) {
		if (!droolsRuleService.hasUserEditPermission(identification, utils.getUserId()))
			return ERROR_403;
		final Set<Ontology> ontologies = new LinkedHashSet<>(ontologyService.getAllOntologies(utils.getUserId()));
		ontologies.addAll(projectService.getResourcesForUserOfType(utils.getUserId(), Ontology.class));
		model.addAttribute("ontologies", ontologies);
		DroolsRule rule = droolsRuleService.getRule(identification);
		DroolsRuleDTO ruleDTO = DroolsRuleDTO.builder().id(rule.getId()).active(rule.isActive())
				.decisionTable(rule.getDecisionTable() != null ? true : false).DRL(rule.getDRL())
				.identification(rule.getIdentification()).sourceOntology(rule.getSourceOntology())
				.targetOntology(rule.getTargetOntology()).type(rule.getType()).table(null).build();
		if (null != model.asMap().get("rule"))
			ruleDTO = (DroolsRuleDTO) model.asMap().get("rule");
		model.addAttribute("rule", ruleDTO);
		model.addAttribute(DOMANI_ID, id);

		return "rules/create";

	}

	@GetMapping("{id}/rules")
	public String rules(Model model, @PathVariable("id") String id) {
		final DroolsRuleDomain domain = droolsRuleService.getDomain(id);
		if (domain == null || !domain.isActive())
			return "redirect:/rule-domains/list";
		if (!droolsRuleService.hasUserPermissionOnDomain(id, utils.getUserId()))
			return ERROR_403;
		final List<DroolsRule> rules = droolsRuleService.getAllRules(domain.getUser());
		model.addAttribute("rules", rules.stream().map(RuleDTO::convert).collect(Collectors.toList()));
		model.addAttribute(DOMANI_ID, id);
		return "rules/rules";
	}

	@PostMapping("{id}/start-stop")
	public ResponseEntity<String> startStop(@PathVariable("id") String id) {
		if (droolsRuleService.hasUserPermissionOnDomain(id, utils.getUserId())) {
			businessRuleService.changeDomainState(id);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PutMapping("rule/{identification}/drl")
	public ResponseEntity<String> updateRuleDRL(@PathVariable("identification") String identification,
			@RequestBody String newDRL) {
		if (droolsRuleService.hasUserEditPermission(identification, utils.getUserId())) {
			try {
				businessRuleService.updateDRL(identification, newDRL);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} catch (final Exception e) {
				return new ResponseEntity<>("Could not update DRL " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("rule/{identification}/decisionTable")
	public ResponseEntity<String> updateRuleDecisionTable(@PathVariable("identification") String identification,
			@RequestBody MultipartFile decisionTable) {
		if (droolsRuleService.hasUserEditPermission(identification, utils.getUserId())) {
			try {
				businessRuleService.updateDecisionTable(identification, decisionTable);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} catch (final Exception e) {
				return new ResponseEntity<>("Could not update Decision Table " + e.getMessage(),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping(value = "/rule/{identification}/downloadTable", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<InputStreamResource> downloadDecisionTable(
			@PathVariable("identification") String identification) throws FileNotFoundException {
		if (droolsRuleService.hasUserEditPermission(identification, utils.getUserId())) {
			DroolsRule rule = droolsRuleService.getRule(identification);
			businessRuleService.createFolder(PATH_TO_RULES + utils.getUserId());
			File file = businessRuleService.uploadFileToFolder(rule.getDecisionTable(),
					PATH_TO_RULES + utils.getUserId(), rule.getIdentification(), rule.getExtension().name());
			final HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			respHeaders.setContentDispositionFormData("attachment", file.getName());
			respHeaders.setContentLength(file.length());
			final InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
			businessRuleService.deleteDirectory(file);
			return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PutMapping("rule/{identification}/active")
	public ResponseEntity<String> updateRule(@PathVariable("identification") String identification) {
		if (droolsRuleService.hasUserEditPermission(identification, utils.getUserId())) {
			try {
				businessRuleService.updateActive(identification);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} catch (final Exception e) {
				return new ResponseEntity<>("Could not update Rule", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@DeleteMapping("rule/{identification}")
	public ResponseEntity<String> deleteRule(@PathVariable("identification") String identification) {
		if (droolsRuleService.hasUserEditPermission(identification, utils.getUserId())) {
			try {
				businessRuleService.delete(identification);
				return new ResponseEntity<>(HttpStatus.OK);
			} catch (final Exception e) {
				return new ResponseEntity<>("Could not delete Rule", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("rule/{identification}/test")
	public ResponseEntity<String> test(@PathVariable("identification") String identification,
			@RequestBody String input) {
		try {
			final String response = businessRuleService.test(identification, input);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (final HttpClientErrorException | HttpServerErrorException e) {
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}

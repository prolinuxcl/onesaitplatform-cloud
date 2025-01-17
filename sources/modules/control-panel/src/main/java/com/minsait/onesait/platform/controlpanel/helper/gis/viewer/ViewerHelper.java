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
package com.minsait.onesait.platform.controlpanel.helper.gis.viewer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.Viewer;
import com.minsait.onesait.platform.config.repository.ViewerRepository;
import com.minsait.onesait.platform.config.services.exceptions.ViewerServiceException;
import com.minsait.onesait.platform.config.services.utils.ZipUtil;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ViewerHelper {

	@Autowired
	private ViewerRepository viewerRepo;

	@Autowired
	private ZipUtil zipUtil;

	@Value("${digitaltwin.temp.dir}")
	private String tempDir;

	private Template indexViewerTemplate;

	@PostConstruct
	public void init() {
		final Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		try {
			final TemplateLoader templateLoader = new ClassTemplateLoader(getClass(), "/viewers/templates");

			cfg.setTemplateLoader(templateLoader);
			indexViewerTemplate = cfg.getTemplate("indexViewerTemplate.ftl");
		} catch (final IOException e) {
			log.error("Error configuring the template loader.", e);
		}
	}

	public File generateProject(String id) {

		final Optional<Viewer> opt = viewerRepo.findById(id);
		if (!opt.isPresent())
			throw new ViewerServiceException("Viewer not found");
		final Viewer viewer = opt.get();

		final String projectDirectory = tempDir + File.separator + UUID.randomUUID();

		final File src = new File(projectDirectory + File.separator + viewer.getIdentification() + File.separator);
		if (!src.exists()) {
			final Boolean success = src.mkdirs();
			if (!success) {
				log.error("Creating project for Viewer falied");
				return null;
			}
		} else {
			log.error("Creating project for Viewer falied, the temporary directory don't exist: "
					+ src.getAbsolutePath());
			return null;
		}

		final Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("jsCode", viewer.getJs());

		PrintWriter outLogic = null;
		File zipFile = null;

		try {
			zipFile = File.createTempFile(viewer.getIdentification(), ".zip");

			// Create index.html
			log.info("New file is going to be generate on: " + src.getAbsolutePath());
			final File index = new File(src.getAbsolutePath());
			if (!index.isDirectory()) {
				index.mkdirs();
			}

			outLogic = new PrintWriter(index + File.separator + "index.html");
			outLogic.println(viewer.getJs().replace("\\n", System.getProperty("line.separator")));
			outLogic.flush();

		} catch (final Exception e) {
			log.error("Error generating Viewer project", e);
		} finally {

			try {
				if (null != outLogic) {
					outLogic.close();
				}
			} catch (final Exception e) {
				log.error("Error closing File object", e);
			}
		}

		final File fileProjectDirectory = new File(projectDirectory);
		try {
			zipUtil.zipDirectory(fileProjectDirectory, zipFile);
		} catch (final IOException e) {
			log.error("Zip file viewer failed", e);
		}

		// Removes the directory
		deleteDirectory(fileProjectDirectory);

		return zipFile;
	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		final File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

}

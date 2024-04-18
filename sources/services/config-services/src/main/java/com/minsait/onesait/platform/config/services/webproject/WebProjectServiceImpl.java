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
package com.minsait.onesait.platform.config.services.webproject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import com.minsait.onesait.platform.config.model.GitEditorConfig;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.WebProject;
import com.minsait.onesait.platform.config.repository.GitEditorConfigRepository;
import com.minsait.onesait.platform.config.repository.WebProjectRepository;
import com.minsait.onesait.platform.config.services.exceptions.WebProjectServiceException;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.services.webproject.NPMCommandResult.NPMCommandResultStatus;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.config.services.MultitenancyService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebProjectServiceImpl implements WebProjectService {

	private static final String ERROR_ZIPPING_FILES = "Error zipping files ";
	private static final String WTOP_ZIP = "wtop.zip";
	@Autowired
	private WebProjectRepository webProjectRepository;

	@Autowired
	private GitEditorConfigRepository gitEditorConfigRepository;

	private boolean npmInstall = false;

	private NPMCommandResultStatus npmStatus;

	@Autowired
	private UserService userService;

	@Autowired
	private WebProjectNPMHelper webProjectNPMHelper;

	@Autowired
	private MultitenancyService masterUserService;

	@Value("${digitaltwin.temp.dir:/tmp}")
	private String tmpDirectory;

	private static final String USER_UNAUTH = "The user is not authorized";
	private static final String SLASH_STRING = "/";
	private static final String ERROR_DELETING_FOLDER = "Error deleting folder: {}";
	private static final String DEFAULT_VERTICAL = "onesaitplatform";

	@Value("${onesaitplatform.webproject.baseurl:https://localhost:18080/web/}")
	private String rootWWW;

	@Value("${onesaitplatform.webproject.rootfolder.path:/usr/local/webprojects/}")
	private String rootFolder;

	@Value("${onesaitplatform.webproject.template.zip:http://localhost:18000/controlpanel/static/wtop/wtop.zip}")
	private String wtop;

	private ExecutorService exService = Executors.newSingleThreadExecutor();

	@Override
	public List<WebProjectDTO> getWebProjectsWithDescriptionAndIdentification(String userId, String identification,
			String description) {
		List<WebProject> webProjects;
		final User user = userService.getUser(userId);

		description = description == null ? "" : description;
		identification = identification == null ? "" : identification;

		if (userService.isUserAdministrator(user)) {
			webProjects = webProjectRepository.findByIdentificationContainingAndDescriptionContaining(identification,
					description);
		} else {
			webProjects = webProjectRepository.findByUserAndIdentificationContainingAndDescriptionContaining(user,
					identification, description);
		}

		return webProjects.stream().map(WebProjectDTO::convert).collect(Collectors.toList());
	}

	@Override
	public List<String> getWebProjectsIdentifications(String userId) {
		List<WebProject> webProjects;
		final List<String> identifications = new ArrayList<>();
		final User user = userService.getUser(userId);

		if (userService.isUserAdministrator(user)) {
			webProjects = webProjectRepository.findAllByOrderByIdentificationAsc();
		} else {
			webProjects = webProjectRepository.findByUserOrderByIdentificationAsc(user);
		}

		for (final WebProject webProject : webProjects) {
			identifications.add(webProject.getIdentification());
		}

		return identifications;
	}

	@Override
	public List<String> getAllIdentifications() {
		final List<WebProject> webProjects = webProjectRepository.findAll();
		final List<String> allIdentifications = new ArrayList<>();

		for (final WebProject webProject : webProjects) {
			allIdentifications.add(webProject.getIdentification());
		}

		return allIdentifications;
	}

	@Override
	public boolean webProjectExists(String identification) {
		return webProjectRepository.findByIdentification(identification) != null;
	}

	@Override
	public void createWebProject(WebProjectDTO webProject, String userId) {
		if (!webProjectExists(webProject.getIdentification())) {
			log.debug("Web Project does not exist, creating..");
			final User user = userService.getUser(userId);
			final WebProject wp = WebProjectDTO.convert(webProject, user);

			if (wp.getMainFile().isEmpty()) {
				if (webProject.getNpm()) {
					wp.setMainFile("");
				} else {
					wp.setMainFile("index.html");

				}

			}

			createFolderWebProject(wp.getIdentification(), userId);
			WebProject web = webProjectRepository.save(wp);

			if (!webProject.getGitUrl().isBlank() && !webProject.getGitToken().isBlank()) {
				GitEditorConfig gitConfig = new GitEditorConfig();
				gitConfig.setGitToken(webProject.getGitToken());
				gitConfig.setGitUrl(webProject.getGitUrl());
				gitConfig.setType("WEB_PROJECT");
				gitConfig.setResourceId(web.getId());
				gitEditorConfigRepository.save(gitConfig);

			}

		} else {
			throw new WebProjectServiceException(
					"Web Project with identification: " + webProject.getIdentification() + " already exists");
		}
	}

	@Override
	public WebProjectDTO getWebProjectById(String webProjectId, String userId) {
		final WebProject webProject = webProjectRepository.findById(webProjectId).orElse(null);
		final User user = userService.getUser(userId);
		if (webProject != null) {
			if (hasUserPermissionToEditWebProject(user, webProject)) {
				return WebProjectDTO.convert(webProject);
			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		} else {
			return null;
		}

	}

	@Override
	public void loadGitDetails(WebProjectDTO web) {
		GitEditorConfig gitConfig = gitEditorConfigRepository.findByResourceId(web.getId());
		if (gitConfig != null) {
			web.setGitToken(gitConfig.getGitToken());
			web.setGitUrl(gitConfig.getGitUrl());
		}

	}

	@Override
	public void compileNPM(WebProjectDTO web, String userId) throws IOException {
		String path = tmpDirectory + SLASH_STRING + web.getIdentification();
		log.info("compileNPM " + web.getIdentification() + " target directory: " + web.getTargetDirectory());
		NPMCommandResult npmResult = webProjectNPMHelper.executeNPMInstall(path, web.getRunCommand());
		log.info("compileNPM Status: " + npmResult.getStatus());
		if (npmResult.getStatus() == NPMCommandResultStatus.OK) {
			log.info("npmResult.getStatus() OK ");
			npmStatus = npmResult.getStatus();
			npmInstall = false;
			String route = tmpDirectory + SLASH_STRING + web.getIdentification() + SLASH_STRING
					+ web.getTargetDirectory();
			ZipUtil.pack(new File(route), new File(route + ".zip"));
			File zipNPM = new File(route + ".zip");
			log.info("zipNPM");
			uploadZip(zipNPM, userId);
			log.info("uploadZip");
			updateWebProject(web, userId);
			log.info("updateWebProject");
			deleteFolder(tmpDirectory + SLASH_STRING + web.getIdentification());
			log.info("deleteFolder");
		} else {
			log.info("npmResult.getStatus() KO ");
			npmStatus = npmResult.getStatus();
			npmInstall = false;
		}

	}

	@Override
	public String getCurrentStatus() {

		return this.webProjectNPMHelper.getCurrentStatus();
	}

	@Override
	public void resetCurrentStatus() {
		this.webProjectNPMHelper.deleteCurrentStatus();

	}

	public boolean hasUserPermissionToEditWebProject(User user, WebProject webProject) {
		if (userService.isUserAdministrator(user)) {
			return true;
		} else {
			return webProject.getUser().getUserId().equals(user.getUserId());
		}
	}

	public String getWebProjectURL(String identification) {
		final WebProject webProject = webProjectRepository.findByIdentification(identification);
		return rootWWW + webProject.getIdentification() + SLASH_STRING + webProject.getMainFile();
	}

	@Override
	public void updateWebProject(WebProjectDTO webProject, String userId) {
		final WebProject wp = webProjectRepository.findByIdentification(webProject.getIdentification());
		final User user = userService.getUser(userId);

		if (wp != null) {
			if (hasUserPermissionToEditWebProject(user, wp)) {
				if (webProjectExists(wp.getIdentification())) {
					if (StringUtils.hasText(webProject.getDescription())) {
						wp.setDescription(webProject.getDescription());
					}
					if (StringUtils.hasText(webProject.getMainFile()) || webProject.getNpm()) {
						wp.setMainFile(webProject.getMainFile());
					}
					if (StringUtils.hasText(webProject.getRunCommand())) {
						wp.setRunCommand(webProject.getRunCommand());
					}
					if (StringUtils.hasText(webProject.getTargetDirectory())) {
						wp.setTargetDirectory(webProject.getTargetDirectory());
					}
					if (webProject.getNpm() != null) {
						wp.setNpm(webProject.getNpm());
					}

					if (!webProject.getGitUrl().isBlank() || !webProject.getGitUrl().isBlank()) {
						GitEditorConfig gitConfig = gitEditorConfigRepository.findByResourceId(wp.getId());
						if (gitConfig == null) {
							gitConfig = new GitEditorConfig();
							gitConfig.setResourceId(webProject.getId());
							gitConfig.setType("WEB_PROJECT");

							gitConfig.setGitUrl(webProject.getGitUrl());
							gitConfig.setGitToken(webProject.getGitToken());
							gitEditorConfigRepository.save(gitConfig);
						} else {
							gitConfig.setGitUrl(webProject.getGitUrl());
							gitConfig.setGitToken(webProject.getGitToken());
							gitEditorConfigRepository.save(gitConfig);
						}

					}
					updateFolderWebProject(webProject.getIdentification(), userId);
					webProjectRepository.save(wp);
				} else {
					throw new WebProjectServiceException(
							"Web Project with identification:" + webProject.getIdentification() + " not exist");
				}
			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		} else {
			throw new WebProjectServiceException("Web project does not exist");
		}
	}

	@Override
	public void deleteWebProject(String webProjectId, String userId) {
		webProjectRepository.findById(webProjectId).ifPresent(wp -> {
			final User user = userService.getUser(userId);

			if (hasUserPermissionToEditWebProject(user, wp)) {
				String vertical_name = masterUserService
						.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema()).getName();
				if (vertical_name.equals(DEFAULT_VERTICAL)) {
					deleteFolder(rootFolder + wp.getIdentification() + SLASH_STRING);
				} else {
					deleteFolder(rootFolder + vertical_name + SLASH_STRING + wp.getIdentification() + SLASH_STRING);
				}
				webProjectRepository.delete(wp);
				if (gitEditorConfigRepository.findByResourceId(wp.getId()) != null) {
					gitEditorConfigRepository.deleteByResourceId(wp.getId());
				}

			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		});

	}

	@Override
	public void deleteWebProjectById(String id, String userId) {
		webProjectRepository.findById(id).ifPresent(wp -> {
			final User user = userService.getUser(userId);

			if (hasUserPermissionToEditWebProject(user, wp)) {
				String vertical_name = masterUserService
						.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema()).getName();

				if (vertical_name.equals(DEFAULT_VERTICAL)) {
					deleteFolder(rootFolder + wp.getIdentification() + SLASH_STRING);
				} else {

					deleteFolder(rootFolder + vertical_name + SLASH_STRING + wp.getIdentification() + SLASH_STRING);
				}
				webProjectRepository.delete(wp);
				if (gitEditorConfigRepository.findByResourceId(wp.getId()) != null) {
					gitEditorConfigRepository.deleteByResourceId(wp.getId());
				}
			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		});

	}

	@Override
	public void uploadZip(MultipartFile file, String userId) {
		String vertical_name = masterUserService.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema())
				.getName();
		String folder;
		if (vertical_name.equals(DEFAULT_VERTICAL)) {
			folder = rootFolder + userId + SLASH_STRING;
		} else {
			folder = rootFolder + vertical_name + SLASH_STRING + userId + SLASH_STRING;
		}

		deleteFolder(folder);
		uploadFileToFolder(file, folder);
		unzipFile(folder, file.getOriginalFilename());
	}

	@Override
	public void uploadZip(File file, String userId) {
		String vertical_name = masterUserService.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema())
				.getName();
		String folder;
		if (vertical_name.equals(DEFAULT_VERTICAL)) {
			folder = rootFolder + userId + SLASH_STRING;
		} else {
			folder = rootFolder + vertical_name + SLASH_STRING + userId + SLASH_STRING;
		}

		deleteFolder(folder);
		uploadFileToFolder(file, folder);
		unzipFile(folder, file.getName());
	}

	@Override
	public void uploadWebTemplate(String userId) {
		String vertical_name = masterUserService.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema())
				.getName();

		String folder;
		if (vertical_name.equals(DEFAULT_VERTICAL)) {
			folder = rootFolder + userId + SLASH_STRING;
		} else {
			folder = rootFolder + vertical_name + SLASH_STRING + userId + SLASH_STRING;
		}
		deleteFolder(folder);
		uploadWebTemplateToFolder(folder);
		unzipFile(folder, WTOP_ZIP);
	}

	private void uploadWebTemplateToFolder(String path) {

		final String fileName = WTOP_ZIP;

		try {
			final InputStream is = new URL(wtop).openStream();
			final File folder = new File(path);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			final String fullPath = path + fileName;
			final OutputStream os = new FileOutputStream(new File(fullPath));

			IOUtils.copy(is, os);

			is.close();
			os.close();
		} catch (final IOException e) {
			throw new WebProjectServiceException("Error uploading files " + e);
		}
		if (log.isDebugEnabled()) {
			log.debug("File: {}{} uploaded", path, fileName);
		}
	}

	private void uploadFileToFolder(MultipartFile file, String path) {

		final String fileName = file.getOriginalFilename();
		byte[] bytes;
		try {
			bytes = file.getBytes();

			final InputStream is = new ByteArrayInputStream(bytes);

			final File folder = new File(path);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			final String fullPath = path + fileName;
			final OutputStream os = new FileOutputStream(new File(fullPath));

			IOUtils.copy(is, os);

			is.close();
			os.close();
		} catch (final IOException e) {
			throw new WebProjectServiceException("Error uploading files " + e);
		}
		if (log.isDebugEnabled()) {
			log.debug("File: {}{} uploaded", path, fileName);
		}
	}

	private void uploadFileToFolder(File file, String path) {

		final String fileName = file.getName();
		byte[] bytes;
		try {
			InputStream stream = new FileInputStream(file);

			bytes = stream.readAllBytes();
			final InputStream is = new ByteArrayInputStream(bytes);
			stream.close();
			final File folder = new File(path);
			if (!folder.exists()) {
				folder.mkdirs();
			}

			final String fullPath = path + fileName;
			final OutputStream os = new FileOutputStream(new File(fullPath));

			IOUtils.copy(is, os);

			is.close();
			os.close();
		} catch (final IOException e) {
			throw new WebProjectServiceException("Error uploading files " + e);
		}

		log.debug("File: " + path + fileName + " uploaded");
	}

	@Override
	public void deleteFolder(String path) {
		final File folder = new File(path);
		final File[] files = folder.listFiles();
		if (files != null) {
			for (final File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f.getAbsolutePath());
				} else {
					deleteFile(f);
				}
			}
		}
		deleteFile(folder);
	}

	private void deleteFile(File file) {
		try {
			Files.delete(file.toPath());
		} catch (final IOException e) {
			log.debug(ERROR_DELETING_FOLDER, file.getPath());
		}
	}

	private void createFolderWebProject(String identification, String userId) {

		String vertical_name = masterUserService.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema())
				.getName();
		File file = null;
		if (vertical_name.equals(DEFAULT_VERTICAL)) {
			log.info("Main webproject root {}", rootFolder + userId + SLASH_STRING);
			file = new File(rootFolder + userId + SLASH_STRING);
		} else {
			log.info("Main webproject root {}", rootFolder + vertical_name + SLASH_STRING + userId + SLASH_STRING);
			file = new File(rootFolder + vertical_name + SLASH_STRING + userId + SLASH_STRING);
		}
		if (file.exists() && file.isDirectory()) {
			File newFile = null;
			if (vertical_name.equals(DEFAULT_VERTICAL)) {
				newFile = new File(rootFolder + identification + SLASH_STRING);
			} else {
				newFile = new File(rootFolder + vertical_name + SLASH_STRING + identification + SLASH_STRING);
			}
			log.info("New webproject root {}", newFile.getAbsolutePath());
			if (!file.renameTo(newFile)) {
				log.info("Unable to rename webproject to root {}", newFile.getAbsolutePath());
				boolean created=newFile.mkdirs();
				if(!created) {
					log.info("Unable to create new webproject root {}", newFile.getAbsolutePath());
				}
				if(!file.renameTo(newFile)) {
					log.info("Unable to create new webproject root after 2 attempts {}", newFile.getAbsolutePath());
					throw new WebProjectServiceException("Cannot create web project folder " + identification);
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("New folder for Web Project {} has been created", identification);
			}
		}
	}

	private void updateFolderWebProject(String identification, String userId) {
		String vertical_name = masterUserService.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema())
				.getName();
		File file = null;
		if (vertical_name.equals(DEFAULT_VERTICAL)) {
			log.info("Main webproject root {}", rootFolder + userId + SLASH_STRING);
			file = new File(rootFolder + userId + SLASH_STRING);
		} else {
			log.info("Main webproject root {}", rootFolder + vertical_name + SLASH_STRING + userId + SLASH_STRING);
			file = new File(rootFolder + vertical_name + SLASH_STRING + userId + SLASH_STRING);
		}
		if (file.exists() && file.isDirectory()) {
			File newFile = null;
			if (vertical_name.equals(DEFAULT_VERTICAL)) {
				deleteFolder(rootFolder + identification + SLASH_STRING);
				newFile = new File(rootFolder + identification + SLASH_STRING);
			} else {
				deleteFolder(rootFolder + vertical_name + SLASH_STRING + identification + SLASH_STRING);
				newFile = new File(rootFolder + vertical_name + SLASH_STRING + identification + SLASH_STRING);
			}
			log.info("New webproject root {}", newFile.getAbsolutePath());
			if (!file.renameTo(newFile)) {
				log.info("Unable to rename webproject to root {}", newFile.getAbsolutePath());
				boolean created=newFile.mkdirs();
				if(!created) {
					log.info("Unable to create new webproject root {}", newFile.getAbsolutePath());
				}
				if(!file.renameTo(newFile)) {
					log.info("Unable to create new webproject root after 2 attempts {}", newFile.getAbsolutePath());
					throw new WebProjectServiceException("Cannot create web project folder " + identification);
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("Folder for Web Project {} has been created", identification);
			}
		}
	}

	@Override
	public void unzipFile(String path, String fileName) {

		final File folder = new File(path + fileName);
		if (log.isDebugEnabled()) {
			log.debug("Unzipping zip file: {}", folder);
		}

		DataInputStream is = null;
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(folder))) {
			final byte[] buffer = new byte[4];
			final byte[] zipbf = new byte[] { 0x50, 0x4B, 0x03, 0x04 };

			is = new DataInputStream(new FileInputStream(folder));
			is.readFully(buffer);
			is.close();
			if (!Arrays.equals(buffer, zipbf)) {
				throw new WebProjectServiceException("Error: Invalid file");
			}

			ZipEntry ze;

			while (null != (ze = zis.getNextEntry())) {
				if (ze.isDirectory()) {
					final File f = new File(path + ze.getName());
					f.mkdirs();
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Unzipping file: {}", ze.getName());
					}
					final FileOutputStream fos = new FileOutputStream(path + ze.getName());
					IOUtils.copy(zis, fos);
					fos.close();
					zis.closeEntry();
				}
			}

		} catch (final IOException e) {
			throw new WebProjectServiceException("Error unzipping files " + e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
					if (log.isDebugEnabled()) {
						log.debug("Error: {}", e);
					}
				}
			}
		}

		if (folder.exists()) {
			deleteFile(folder);
		}

	}

	@Override
	public byte[] downloadZip(String identification, String userId) {

		final String path = rootFolder;
		String vertical_name = masterUserService.getVerticalFromSchema(MultitenancyContextHolder.getVerticalSchema())
				.getName();

		final String fileName = identification + ".zip";

		final ByteArrayOutputStream zipByte = new ByteArrayOutputStream();
		final ZipOutputStream zipOut = new ZipOutputStream(zipByte);
		File fileToZip = null;
		if (vertical_name.equals(DEFAULT_VERTICAL)) {
			fileToZip = new File(rootFolder + identification + SLASH_STRING);
		} else {
			fileToZip = new File(rootFolder + vertical_name + SLASH_STRING + identification + SLASH_STRING);
		}
		log.debug("Zipping file: " + path + fileName);

		try {
			if (fileToZip.isDirectory()) {
				final File[] fileList = fileToZip.listFiles();
				for (final File file : fileList) {
					zipFile(file, file.getName(), zipOut);
				}
			}
			zipOut.close();
			zipByte.close();
		} catch (final IOException e) {
			log.error(ERROR_ZIPPING_FILES + e);
			throw new WebProjectServiceException(ERROR_ZIPPING_FILES + e);
		}

		return zipByte.toByteArray();
	}

	private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

		try {
			if (fileToZip.isDirectory()) {
				if (fileName.endsWith("/")) {
					zipOut.putNextEntry(new ZipEntry(fileName));
					zipOut.closeEntry();
				} else {
					zipOut.putNextEntry(new ZipEntry(fileName + "/"));
					zipOut.closeEntry();
				}
				final File[] fileList = fileToZip.listFiles();
				for (final File file : fileList) {
					zipFile(file, fileName + "/" + file.getName(), zipOut);
				}
				return;
			}
			copyFilesToZip(fileToZip, fileName, zipOut);

		} catch (final IOException e) {
			log.error(ERROR_ZIPPING_FILES + e);
			throw e;
		}
	}

	private void copyFilesToZip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

		try (FileInputStream fis = new FileInputStream(fileToZip)) {
			final ZipEntry zipEntry = new ZipEntry(fileName);
			zipOut.putNextEntry(zipEntry);

			IOUtils.copy(fis, zipOut);
		} catch (final IOException e) {
			log.error(ERROR_ZIPPING_FILES + e);
			throw e;
		}
	}

	@Override
	public WebProjectDTO getWebProjectByName(String name, String userId) {
		final WebProject webProject = webProjectRepository.findByIdentification(name);
		final User user = userService.getUser(userId);
		if (webProject != null) {
			if (hasUserPermissionToEditWebProject(user, webProject)) {
				return WebProjectDTO.convert(webProject);
			} else {
				throw new WebProjectServiceException(USER_UNAUTH);
			}
		} else {
			return null;
		}
	}

	@Override
	public List<WebProjectDTO> getAllWebProjects() {
		return webProjectRepository.findAll().stream().map(WebProjectDTO::convert).collect(Collectors.toList());
	}

	@Override
	public boolean isNpmInstall() {
		return npmInstall;
	}

	@Override
	public void setNpmInstall(boolean val) {
		npmInstall = val;
	}

	@Override
	public NPMCommandResultStatus getNpmStatus() {
		return npmStatus;
	}

	@Override
	public void cloneGitAndDownload(final WebProjectDTO webProject, RestTemplate template, HttpEntity<?> httpEntity,
			String url, String urlDelete, String userId) {

		log.debug("cloneGitAndDownload");
		exService.submit(new Runnable() {

			@Override
			public void run() {
				try {
					log.debug("cloneGitAndDownload run");
					ResponseEntity<Resource> response = template.exchange(url, HttpMethod.POST, httpEntity,
							Resource.class);
					log.debug("cloneGitAndDownload run response:" + (response.getBody() != null));
					Resource resource = response.getBody();
					byte[] bytes = resource.getInputStream().readAllBytes();
					File targetFile = new File(tmpDirectory + "/" + webProject.getIdentification() + "/"
							+ webProject.getIdentification() + ".zip");
					File directory = new File(tmpDirectory + "/" + webProject.getIdentification());
					if (!directory.exists()) {
						directory.mkdir();
					}
					OutputStream outStream = new FileOutputStream(targetFile);
					outStream.write(bytes);
					outStream.close();
					// Delete tempfiles
					template.exchange(urlDelete, HttpMethod.POST, httpEntity, String.class);
					log.debug("cloneGitAndDownload zip generated");
					if (webProject.getNpm()) {
						log.debug("cloneGitAndDownload is Npm, unzipfile");
						unzipFile(tmpDirectory + "/" + webProject.getIdentification() + "/",
								webProject.getIdentification() + ".zip");
						log.debug("cloneGitAndDownload is Npm, compileNPM");
						compileNPM(webProject, userId);
						targetFile.delete();
						directory.delete();

					} else {
						log.debug("cloneGitAndDownload is not Npm, uploadZip");
						uploadZip(targetFile, userId);
						log.debug("cloneGitAndDownload is not Npm, updateWebProject");
						updateWebProject(webProject, userId);
						targetFile.delete();
						directory.delete();
					}
				} catch (Exception e) {
					if (webProject.getNpm()) {
						if (isNpmInstall()) {
							setNpmInstall(false);
						}
					}
					log.error("Error:", e);
				}
			}
		});
	}
}

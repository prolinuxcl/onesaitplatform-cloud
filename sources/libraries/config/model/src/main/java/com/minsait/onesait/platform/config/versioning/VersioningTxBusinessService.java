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
package com.minsait.onesait.platform.config.versioning;

import java.util.Map;

import javax.validation.constraints.NotNull;

import com.minsait.onesait.platform.commons.git.GitlabConfiguration;

public interface VersioningTxBusinessService {

	public void generateSnapShot(String tagName, @NotNull RestoreReport report, Map<String, String> versionableClasses,
			GitlabConfiguration configuration);

	public void createBundle(@NotNull RestoreReport report, Map<String, String> versionableClasses,
			GitlabConfiguration configuration, String directory, BundleGenerateDTO bundle);

	void createZipBundle(@NotNull RestoreReport report, Map<String, String> versionableClasses, String directory,
			BundleGenerateDTO bundle);

	public void restorePlatform(RestorePlatformDTO restoreDTO, RestoreReport report,
			Map<String, String> versionableClasses, GitlabConfiguration gitConfig);

	public void syncOriginAndDB();

	public void syncOriginAndDB(String originSHA);

	void deleteUser(String userId);

	void restoreBundle(RestoreReport report, Map<String, String> versionableClasses, String directory,
			String folderName, String userId);

}

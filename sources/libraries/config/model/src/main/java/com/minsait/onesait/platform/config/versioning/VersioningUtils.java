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
package com.minsait.onesait.platform.config.versioning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class VersioningUtils {

	public static <T extends Object> Yaml versioningYaml(Class<T> clazz) {
		final DumperOptions opts = new DumperOptions();
		opts.setDefaultFlowStyle(FlowStyle.BLOCK);
		opts.setIndent(4);
		opts.setPrettyFlow(true);
		opts.setSplitLines(true);
		opts.setAllowUnicode(true);
		final HTMLRepresenter rep = new HTMLRepresenter();
		rep.addClassTag(clazz, Tag.MAP);
		return new Yaml(new HTMLConstructor(), rep, opts);
	}

	public static void zipFolder(File srcFolder, File destZipFile) throws Exception {
		try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
				ZipOutputStream zip = new ZipOutputStream(fileWriter)) {

			addFolderToZip(srcFolder, srcFolder, zip);
		}
	}

	private static void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip) throws Exception {

		if (srcFile.isDirectory()) {
			addFolderToZip(rootPath, srcFile, zip);
		} else {
			final byte[] buf = new byte[1024];
			int len;
			try (FileInputStream in = new FileInputStream(srcFile)) {
				String name = srcFile.getPath();
				name = name.replace(rootPath.getPath(), "");
				zip.putNextEntry(new ZipEntry(name));
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
			}catch (final Exception e) {
				log.error("Could not add file {}, message:{}", srcFile.toString(),e.getMessage());
			}
		}
	}

	private static void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip) throws Exception {
		for (final File fileName : srcFolder.listFiles()) {
			addFileToZip(rootPath, fileName, zip);
		}
	}

	public static void unzipFolder(File sourceFile, File targetFile) throws IOException {
		final Path source = sourceFile.toPath();
		final Path target = targetFile.toPath();
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				boolean isDirectory = false;
				if (zipEntry.getName().endsWith(File.separator)) {
					isDirectory = true;
				}
				final Path newPath = zipSlipProtect(zipEntry, target);
				if (isDirectory) {
					Files.createDirectories(newPath);
				} else {
					if (newPath.getParent() != null) {
						if (Files.notExists(newPath.getParent())) {
							Files.createDirectories(newPath.getParent());
						}
					}
					Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
				}
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
		}
	}


	private static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
			throws IOException {
		return new File(targetDir.toString()+zipEntry.getName()).toPath();
	}




}

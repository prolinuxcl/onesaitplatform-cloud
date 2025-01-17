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
package com.minsait.onesait.platform.controlpanel.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.config.model.MarketAsset.MarketAssetPaymentMode;
import com.minsait.onesait.platform.config.model.MarketAsset.MarketAssetState;
import com.minsait.onesait.platform.config.model.MarketAsset.MarketAssetType;
import com.minsait.onesait.platform.config.model.User;

import lombok.Getter;
import lombok.Setter;

public class MarketAssetMultipart {

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private User user;

	@Getter
	@Setter
	private boolean isPublic;

	@Getter
	@Setter
	private MarketAssetState state;

	@Getter
	@Setter
	private MarketAssetType marketAssetType;

	@Getter
	@Setter
	private MarketAssetPaymentMode paymentMode;

	@Getter
	@Setter
	private String jsonDesc;

	@Getter
	@Setter
	private Date createdAt;

	@Getter
	@Setter
	private Date updatedAt;

	@Getter
	private MultipartFile image;

	public void setImage(MultipartFile image) {
		this.image = image;
	}

	public void setImage() {
		image = new MultipartFile() {
			@Override
			public void transferTo(File dest) throws IOException {
				// Method necessary as part of a MultipartFile but will not be used
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public long getSize() {
				return 0;
			}

			@Override
			public String getOriginalFilename() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return null;
			}

			@Override
			public String getContentType() {
				return null;
			}

			@Override
			public byte[] getBytes() throws IOException {
				return new byte[0];
			}
		};
	}

	@Getter
	@Setter
	private String imageType;

	@Getter
	private MultipartFile content;

	public void setContent(MultipartFile content) {
		this.content = content;
	}

	public void setContent() {
		content = new MultipartFile() {
			@Override
			public void transferTo(File dest) throws IOException {
				// Method necessary as part of a MultipartFile but will not be used
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public long getSize() {
				return 0;
			}

			@Override
			public String getOriginalFilename() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return null;
			}

			@Override
			public String getContentType() {
				return null;
			}

			@Override
			public byte[] getBytes() throws IOException {
				return new byte[0];
			}
		};
	}

	@Getter
	@Setter
	private String contentId;

}

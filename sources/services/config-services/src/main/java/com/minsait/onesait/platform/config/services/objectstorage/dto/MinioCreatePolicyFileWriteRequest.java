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
package com.minsait.onesait.platform.config.services.objectstorage.dto;

import lombok.Data;

@Data
public class MinioCreatePolicyFileWriteRequest {

	private String name;
	private String policy;
	
	public MinioCreatePolicyFileWriteRequest(String policyName, String filePath){
		this.name=policyName;
		this.policy="{\"Version\": \"2012-10-17\",\"Statement\":[{\"Effect\": \"Allow\",\"Action\": [\"s3:GetBucketLocation\",\"s3:GetObject\"],\"Resource\": [\"arn:aws:s3:::"+filePath+"\"]},"
				+ "{\"Effect\": \"Allow\",\"Action\": [\"s3:PutObject\"],\"Resource\": [\"arn:aws:s3:::"+filePath+"\"]}]}";
		
	}
}

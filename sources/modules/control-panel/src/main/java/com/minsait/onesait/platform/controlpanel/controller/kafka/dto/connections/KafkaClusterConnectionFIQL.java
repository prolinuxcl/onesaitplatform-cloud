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
package com.minsait.onesait.platform.controlpanel.controller.kafka.dto.connections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.config.model.KafkaClusterInstance;

public class KafkaClusterConnectionFIQL {
	public static KakfaClusterConnectionDTO fromKafkaClusterInstance(KafkaClusterInstance instance) throws IOException {
		KakfaClusterConnectionDTO dto = new KakfaClusterConnectionDTO();
		dto.setId(instance.getId());
		dto.setIdentification(instance.getIdentification());
		dto.setDescription(instance.getDescription());
		String brokers = "";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(instance.getKafkaConfig());
		ArrayNode array = ((ArrayNode)actualObj);
		JsonNode bootstrapServers=null;
		int index=-1;
		for(int i=0;i<array.size();i++) {
			if(array.get(i).get("bootstrap.servers")!=null) {
				 bootstrapServers=array.get(i);
				 index=i;
				 break;
			 }
		}

		if (bootstrapServers != null) {
			array.remove(index);
			brokers = bootstrapServers.get("bootstrap.servers").asText();
		}
		dto.setBrokers(brokers);
		dto.setConfig(array.toString());
		return dto;
	}

	public static List<KakfaClusterConnectionDTO> fromKafkaClusterInstanceList(List<KafkaClusterInstance> instances)
			throws IOException {
		List<KakfaClusterConnectionDTO> dtoList = new ArrayList<>();
		for (KafkaClusterInstance instance : instances) {
			dtoList.add(fromKafkaClusterInstance(instance));
		}
		return dtoList;
	}
	
	public static KafkaClusterInstance toKafkaClusterInstance(KakfaClusterConnectionDTO dto) throws IOException{
		KafkaClusterInstance res = new KafkaClusterInstance();
		res.setId(dto.getId());
		res.setIdentification(dto.getIdentification());
		res.setDescription(dto.getDescription());
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(dto.getConfig());
		ArrayNode arr = ((ArrayNode)actualObj);
		ObjectNode outerObject = mapper.createObjectNode(); //the object with the "data" array
		outerObject.putPOJO("bootstrap.servers", dto.getBrokers()); 
		arr.addPOJO(outerObject);
		//((ObjectNode)actualObj).put("bootstrap.servers", dto.getBrokers());
		res.setKafkaConfig(actualObj.toString());
		return res;
	}
}
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
package com.minsait.onesait.platform.iotbroker.processor;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyJoinMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyLogMessage;
import com.minsait.onesait.platform.comms.protocol.body.parent.SSAPBodyMessage;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformInstance;
import com.minsait.onesait.platform.config.services.device.ClientPlatformInstanceService;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Component
public class DeviceManagerDelegate implements DeviceManager {

	@Autowired
	ClientPlatformInstanceService deviceService;

	@Value("${onesaitplatform.iotbroker.devices.perclient.max:0}")
	private int maxDevicesPerClient;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public <T extends SSAPBodyMessage> boolean registerActivity(SSAPMessage<T> request, String clientPlatformIdentification, String clientPlatformInstanceIdentification, GatewayInfo info) {

		ClientPlatformInstance device = new ClientPlatformInstance();
		device.setIdentification(clientPlatformInstanceIdentification);
		device.setProtocol(info.getProtocol());

		switch (request.getMessageType()) {
		case JOIN:
			final SSAPBodyJoinMessage body = (SSAPBodyJoinMessage) request.getBody();
			device.setJsonActions(
					body.getDeviceConfiguration() != null ? body.getDeviceConfiguration().toString()
							: device.getJsonActions());
			device.setTags(body.getTags() != null ? body.getTags() : device.getTags());
			return touchDevice(device, true, info, null, null, clientPlatformIdentification);			

		case LEAVE:
			return touchDevice(device, false, info, null, null, clientPlatformIdentification);
			
		case LOG:
			final SSAPBodyLogMessage logMessage = (SSAPBodyLogMessage) request.getBody();
			final double[] location = { logMessage.getCoordinates().getX(),
					logMessage.getCoordinates().getY() };
			return touchDevice(device, true, info, logMessage.getStatus().name(), location, clientPlatformIdentification);
		default:
			return touchDevice(device, true, info, null, null, clientPlatformIdentification);
		
		}
	}

	@Scheduled(fixedDelay = 60000)
	public void updatingDevicesPeriodic() {
		updatingDevices();
	}

	@PostConstruct
	public void updatingDevicesAtStartUp() {
		updatingDevices();
	}

	//TODO this does not modify cached data. Two easy approaches, evict cache or the ReferenceSecurityImpl strategy for IoTSessions
	private void updatingDevices() {
		log.info("Start Updating all devices");
		final Calendar c = Calendar.getInstance();
		long millis = c.getTimeInMillis() - 5 * 60 * 1000l;
		c.setTimeInMillis(millis);

		// Setting connected false when 5 minutes without activity
		int n = deviceService.updateClientPlatformInstanceStatusAndDisableWhenUpdatedAtLessThanDate(false, false,
				c.getTime());
		log.info("End Updating all devices: {} disconected", n);

		// Setting disabled a true when 1 day witout activity
		millis = c.getTimeInMillis() - 24 * 60 * 60 * 1000l;
		c.setTimeInMillis(millis);
		n = deviceService.updateClientPlatformInstanceStatusAndDisableWhenUpdatedAtLessThanDate(false, true,
				c.getTime());
		log.info("End Updating all devices: {} disabled", n);

	}
	
	private boolean touchDevice(ClientPlatformInstance device, boolean connected, GatewayInfo info,
			String status, double[] location, String cpIdentification) {
		
		updateOrCreateDevice(device, true, info, status, location, cpIdentification);
		
		
		log.debug("ClientPlatformInstance updated. ClientPlatform: {}, ClientPlatformInstance: {}", 
				cpIdentification, device.getIdentification());
		
		return true; //TODO deal with possible return statuses
	}
	
	private void  updateOrCreateDevice(ClientPlatformInstance device, boolean connected, GatewayInfo info,
			String status, double[] location, String cpIdentification) {
		completeDevice(device, connected, info, status, location);
		deviceService.updateClientPlatformInstance(device, cpIdentification);
	}
	
	private void completeDevice(ClientPlatformInstance device, boolean connected, GatewayInfo info,
			String status, double[] location) {
		
		device.setStatus(status == null ? ClientPlatformInstance.StatusType.OK.name() : status);
		device.setConnected(connected);
		device.setDisabled(false);
		device.setProtocol(info.getProtocol());
		device.setUpdatedAt(new Date());
		if (location != null) {
			device.setLocation(location);
		}
	}

	@Override
	public JsonNode createDeviceLog(ClientPlatform client, String deviceId, SSAPBodyLogMessage logMessage)
			throws IOException {
		final ClientPlatformInstance device = deviceService.getByClientPlatformIdAndIdentification(client, deviceId);
		final double longitude = logMessage.getCoordinates() == null ? 0 : logMessage.getCoordinates().getX();
		final double latitude = logMessage.getCoordinates() == null ? 0 : logMessage.getCoordinates().getY();
		return createLogInstance(device, logMessage.getStatus().name(), logMessage.getLevel().name(),
				logMessage.getMessage(), logMessage.getExtraData().toString(), longitude, latitude,
				logMessage.getCommandId());

	}

	public JsonNode createLogInstance(ClientPlatformInstance device, String status, String level, String message,
			String extraOptions, double longitude, double latitude, String commandId) {
		final JsonNode root = mapper.createObjectNode();
		final JsonNode properties = mapper.createObjectNode();
		((ObjectNode) properties).put("device", device.getIdentification());
		((ObjectNode) properties).put("level", level);
		((ObjectNode) properties).put("status", status);
		((ObjectNode) properties).put("message", message);
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		((ObjectNode) properties).put("timestamp", df.format(new Date()));
		if (extraOptions != null)
			((ObjectNode) properties).put("extraOptions", extraOptions);
		if (longitude != 0 && latitude != 0) {
			final JsonNode subcoordinates = mapper.createObjectNode();
			((ObjectNode) subcoordinates).put("latitude", latitude);
			((ObjectNode) subcoordinates).put("longitude", longitude);
			final JsonNode coordinates = mapper.createObjectNode();
			((ObjectNode) coordinates).set("coordinates", subcoordinates);
			((ObjectNode) coordinates).put("type", "Point");
			((ObjectNode) properties).set("location", coordinates);
		}
		if (commandId != null) {
			((ObjectNode) properties).put("commandId", commandId);
		}
		((ObjectNode) root).set("DeviceLog", properties);
		return root;

	}

}

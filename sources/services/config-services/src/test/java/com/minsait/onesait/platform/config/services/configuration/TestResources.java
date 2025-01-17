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
package com.minsait.onesait.platform.config.services.configuration;

public class TestResources {
	public static final String globalConfig = "onesaitplatform:\n" + "  env:\n" + "    ontologies:\n"
			+ "      ignore-case-properties: false\n" + "    database:\n" + "      queries-limit: 2000\n"
			+ "    files:\n" + "      maxsize: 5000000\n" + "    iotbroker:\n"
			+ "      session-expiration: !!java.lang.Long 600000\n" + "\n" + "";
	public static final String dockerYMLSample = "version: '2'\n" + "services:\n" + "  chatbot:\n"
			+ "    image: moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/chatbot:latest\n" + "    environment:\n"
			+ "      S4C_URL: {{DOMAIN_NAME}}\n" + "    stdin_open: true\n" + "    tty: true\n" + "    links:\n"
			+ "    - apimanagerservice:apimanagerservice\n" + "    labels:\n"
			+ "      io.rancher.container.pull_image: always\n"
			+ "      io.rancher.scheduler.affinity:host_label: node={{WORKER2DEPLOY}}\n" + "  zookeeper:\n"
			+ "    image: moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/zookeeper-secured:latest\n"
			+ "    environment:\n" + "      ZOOKEEPER_CLIENT_PORT: '2182'\n" + "    stdin_open: true\n"
			+ "    network_mode: host\n" + "    tty: true\n" + "    labels:\n"
			+ "      io.rancher.scheduler.affinity:host_label: node={{WORKER2DEPLOY}}\n"
			+ "      io.rancher.container.dns: 'true'\n"
			+ "      io.rancher.container.hostname_override: container_name\n"
			+ "      io.rancher.container.pull_image: always\n" + "  schedulerdb:\n"
			+ "    image: moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/schedulerdb:latest\n"
			+ "    stdin_open: true\n" + "    volumes:\n" + "    - /datadrive/schedulerdb:/var/lib/mysql:rw\n"
			+ "    tty: true\n" + "    ports:\n" + "    - 3307:3306/tcp\n" + "    labels:\n"
			+ "      io.rancher.scheduler.affinity:host_label: node={{WORKER2DEPLOY}}\n"
			+ "      io.rancher.container.pull_image: always";
	public static final String nginxConf = "user www-data;\n" + "worker_processes 4;\n" + "pid /run/nginx.pid;\n" + "\n"
			+ "events {\n" + "	worker_connections 4000;\n" + "	use epoll;\n" + "	multi_accept on;\n" + "}\n" + "\n"
			+ "http {\n" + "	##\n" + "	# Basic Settings\n" + "	##\n" + "\n" + "	sendfile on;\n"
			+ "	tcp_nopush on;\n" + "	tcp_nodelay on;\n" + "	keepalive_timeout 65;\n"
			+ "	types_hash_max_size 2048;\n" + "	client_max_body_size 500m;\n" + "	\n"
			+ "	include /etc/nginx/mime.types;\n" + "	default_type application/octet-stream;\n" + "\n" + "	##\n"
			+ "	# Logging Settings\n" + "	##\n" + "\n" + "	access_log /var/log/nginx/access.log;\n"
			+ "	error_log /var/log/nginx/error.log;\n" + "\n" + "	##\n" + "	# Gzip Settings\n" + "	##\n" + "\n"
			+ "	gzip on;\n" + "	gzip_disable \"msie6\";\n" + "\n" + "	##\n" + "	# Virtual Host Configs\n"
			+ "	##\n" + "	\n" + "	# Importante para nombres de dominio muy largos\n"
			+ "	server_names_hash_bucket_size 128;\n" + "\n" + "	include /etc/nginx/conf.d/*.conf;\n"
			+ "	include /etc/nginx/sites-enabled/*;\n" + "\n" + "	server {\n" + "			\n"
			+ "    		listen 80;\n" + "    		server_name {{SERVER_NAME}};	  					\n"
			+ " 			\n" + "			location /controlpanel {\n"
			+ "				proxy_pass http://controlpanelservice:18000/controlpanel;\n"
			+ "				proxy_read_timeout 360s;\n" + "			\n"
			+ "				proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n"
			+ "            	proxy_set_header Host $http_host;\n"
			+ "				proxy_set_header X-Forwarded-Proto http;\n" + "            	proxy_redirect off;\n" + "\n"
			+ "				add_header 'Access-Control-Allow-Origin: *' always;                                                                                        \n"
			+ "				add_header 'Access-Control-Allow-Credentials' 'true';                                                                                      \n"
			+ "				add_header 'Access-Control-Allow-Methods' 'GET,POST';                                                                                      \n"
			+ "                add_header 'Access-Control-Allow-Headers' 'DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,X-SOFIA2-ApiKey,Authorization';		 \n"
			+ "			}\n" + "			\n" + "			location /dashboardengine/loginRest {\n"
			+ "				proxy_pass http://dashboardengineservice:18300;\n" + "			}\n" + "\n"
			+ "			location /dashboardengine/dsengine/solver { \n"
			+ "			    proxy_pass http://dashboardengineservice:18300;\n" + "			\n"
			+ "				proxy_set_header X-Real-IP $remote_addr;		\n"
			+ "				proxy_set_header Host $host;\n"
			+ "				proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" + "\n"
			+ "				proxy_http_version 1.1;\n" + "				proxy_set_header Upgrade websocket;\n"
			+ "				proxy_set_header Connection upgrade;\n" + "				proxy_read_timeout 86400;\n"
			+ "			} \n" + "			\n" + "			location /dashboardengine { \n"
			+ "			    proxy_pass http://dashboardengineservice:18300/dashboardengine;\n"
			+ "				proxy_read_timeout 360s;\n" + "			\n" + "				#WebSocket support\n"
			+ "				proxy_http_version 1.1;\n" + "				proxy_set_header Upgrade $http_upgrade;\n"
			+ "				proxy_set_header Connection \"upgrade\";\n"
			+ "				proxy_set_header X-Forwarded-For $remote_addr;\n" + "			}\n" + "			\n"
			+ "			location /devicesimulator { \n"
			+ "			    proxy_pass http://devicesimulator:19200/devicesimulator;\n"
			+ "				proxy_read_timeout 360s;\n" + "			\n" + "				#WebSocket support\n"
			+ "				proxy_http_version 1.1;\n" + "				proxy_set_header Upgrade $http_upgrade;\n"
			+ "				proxy_set_header Connection \"upgrade\";\n"
			+ "				proxy_set_header X-Forwarded-For $remote_addr;\n"
			+ "			}									\n" + "		 \n" + "		 	location /iotbroker {\n"
			+ "				proxy_pass http://iotbrokerservice:19000/iotbroker;\n"
			+ "				proxy_read_timeout 360s;\n" + "			\n" + "				#WebSocket support\n"
			+ "				proxy_http_version 1.1;\n" + "				proxy_set_header Upgrade $http_upgrade;\n"
			+ "				proxy_set_header Connection \"upgrade\";\n"
			+ "				proxy_set_header X-Forwarded-For $remote_addr;\n" + "			}\n" + "			\n"
			+ "		 	location /api-manager {\n"
			+ "				proxy_pass http://apimanagerservice:19100/api-manager;\n"
			+ "				proxy_read_timeout 360s;\n" + "			\n" + "				#WebSocket support\n"
			+ "				proxy_http_version 1.1;\n" + "				proxy_set_header Upgrade $http_upgrade;\n"
			+ "				proxy_set_header Connection \"upgrade\";\n"
			+ "				proxy_set_header X-Forwarded-For $remote_addr;\n" + "			}\n" + "			\n"
			+ "		 	location /digitaltwinbroker {	\n"
			+ "				proxy_pass http://digitaltwinbrokerservice:19300/digitaltwinbroker;\n"
			+ "				proxy_read_timeout 360s;\n" + "			\n" + "				#WebSocket support\n"
			+ "				proxy_http_version 1.1;\n" + "				proxy_set_header Upgrade $http_upgrade;\n"
			+ "				proxy_set_header Connection \"upgrade\";\n"
			+ "				proxy_set_header X-Forwarded-For $remote_addr;\n" + "			}			\n"
			+ "			\n" + "        	location /nodered/ {	\n"
			+ "                proxy_pass  http://flowengineservice:5050/;\n"
			+ "           		proxy_read_timeout 360s;\n" + "\n" + "            	# WebSocket support\n"
			+ "            	proxy_http_version 1.1;\n" + "            	proxy_set_header Upgrade $http_upgrade;\n"
			+ "            	proxy_set_header Connection \"upgrade\";\n"
			+ "            	proxy_set_header X-Forwarded-For $remote_addr;\n" + "        	}      	   			 \n"
			+ "\n" + "        	location /flowengine {\n"
			+ "                proxy_pass  http://flowengineservice:20100;\n"
			+ "           		proxy_read_timeout 360s;\n" + "\n" + "            	# WebSocket support\n"
			+ "            	proxy_http_version 1.1;\n" + "            	proxy_set_header Upgrade $http_upgrade;\n"
			+ "            	proxy_set_header Connection \"upgrade\";\n"
			+ "            	proxy_set_header X-Forwarded-For $remote_addr;\n" + "        	} \n" + "        	\n"
			+ "        	location /monitoring {\n"
			+ "                proxy_pass  http://monitoringuiservice:18100/monitoring;\n"
			+ "           		proxy_read_timeout 360s;\n" + "\n" + "            	# WebSocket support\n"
			+ "            	proxy_http_version 1.1;\n" + "            	proxy_set_header Upgrade $http_upgrade;\n"
			+ "            	proxy_set_header Connection \"upgrade\";\n"
			+ "            	proxy_set_header X-Forwarded-For $remote_addr;\n" + "        	} \n" + "        	\n"
			+ "			location /notebooks/ {\n" + "				proxy_pass http://zeppelin:8080/;\n"
			+ "				\n" + "            	# WebSocket support\n" + "            	proxy_http_version 1.1;\n"
			+ "            	proxy_set_header Upgrade websocket;\n"
			+ "            	proxy_set_header Connection \"upgrade\";\n"
			+ "            	proxy_read_timeout 86400;			\n" + "			}  \n" + "			\n"
			+ "			location /notebooks/ws {\n" + "				proxy_pass http://zeppelin:8080/ws;\n"
			+ "				\n" + "            	# WebSocket support\n" + "            	proxy_http_version 1.1;\n"
			+ "            	proxy_set_header Upgrade websocket;\n"
			+ "            	proxy_set_header Connection \"upgrade\";\n"
			+ "            	proxy_read_timeout 86400;		\n" + "			} 		\n" + "			\n"
			+ "			location /controlpanel/notebooks/app/ws {\n"
			+ "				proxy_pass http://zeppelin:8080/ws;\n" + "				\n"
			+ "            	# WebSocket support\n" + "            	proxy_http_version 1.1;\n"
			+ "            	proxy_set_header Upgrade websocket;\n"
			+ "            	proxy_set_header Connection \"upgrade\";\n"
			+ "            	proxy_read_timeout 86400;			\n"
			+ "			} 	          				    	        	        	        	        	\n"
			+ "        	\n" + "        	location /web {\n" + "                alias /usr/local/webprojects;\n"
			+ "        	}          	       	            	  	\n" + "\n" + "	}	\n" + "\n" + "}";
}

FROM fnproject/dind

# Metadata
LABEL platform.image.maintainer="Onesait Platform"
LABEL platform.image.vendor="Minsait"
LABEL platform.image.support="support@onesaitplatform.com"
LABEL platform.image.license="Apache Software License 2"

RUN apk add --no-cache libc6-compat \
    curl \
    openjdk8
    
RUN curl -LSs https://raw.githubusercontent.com/fnproject/cli/master/install | sh
	
ADD *.jar /app.jar

EXPOSE 8086

ENV FN_API_URL=https://development.onesaitplatform.com/fn \
    FN_REGISTRY=registry.onesaitplatform.com \
    SERVER_NAME=localhost \
    ACCESS_TOKEN_URL=https://${SERVER_NAME}/auth/realms/onesaitplatform/protocol/openid-connect/token \
    AUTHORIZATION_URL=https://${SERVER_NAME}/auth/realms/onesaitplatform/protocol/openid-connect/auth?scope=openid \
    CHECK_TOKEN_URL=https://${SERVER_NAME}/auth/realms/onesaitplatform/protocol/openid-connect/instrospect \
    USERINFO_URL=https://${SERVER_NAME}/auth/realms/onesaitplatform/protocol/openid-connect/userinfo \
    ADMIN_API_KEY=52bd41e41c2c4b919ace6f43560a3d03 \
    CONFIGDBURL=jdbc:mysql://configdb:3306/onesaitplatform_serverless?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false&autoReconnect=true \
    DBADDPROPS= \
    CONFIGDBUSER=root \
    CONFIGDBPASS=changeIt! \
	CONFIGDB_DRIVER=com.mysql.cj.jdbc.Driver \
	CONFIGDB_DIALECT=org.hibernate.dialect.MySQL5InnoDBDialect \
    ROOT_LOG_LEVEL=WARN \
    MINSAIT_LOG_LEVEL=INFO \
    SPRING_LOG_LEVEL=INFO
    
CMD ["java","-Dspring.application.json=$ONESAIT_PROPERTIES","-Dspring.profiles.active=docker","-jar","/app.jar"]
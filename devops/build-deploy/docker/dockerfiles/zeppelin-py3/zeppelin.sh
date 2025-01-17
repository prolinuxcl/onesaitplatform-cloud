#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Run Zeppelin 
#

USAGE="Usage: bin/zeppelin.sh [--config <conf-dir>]"

if [[ "$1" == "--config" ]]; then
  shift
  conf_dir="$1"
  if [[ ! -d "${conf_dir}" ]]; then
    echo "ERROR : ${conf_dir} is not a directory"
    echo ${USAGE}
    exit 1
  else
    export ZEPPELIN_CONF_DIR="${conf_dir}"
  fi
  shift
fi

bin=$(dirname "${BASH_SOURCE-$0}")
bin=$(cd "${bin}">/dev/null; pwd)

. "${bin}/common.sh"

if [ "$1" == "--version" ] || [ "$1" == "-v" ]; then
    getZeppelinVersion
fi

HOSTNAME=$(hostname)
ZEPPELIN_LOGFILE="${ZEPPELIN_LOG_DIR}/zeppelin-${ZEPPELIN_IDENT_STRING}-${HOSTNAME}.log"
LOG="${ZEPPELIN_LOG_DIR}/zeppelin-cli-${ZEPPELIN_IDENT_STRING}-${HOSTNAME}.out"
  
ZEPPELIN_SERVER=org.apache.zeppelin.server.ZeppelinServer
JAVA_OPTS+=" -Dzeppelin.log.file=${ZEPPELIN_LOGFILE}"

# construct classpath
if [[ -d "${ZEPPELIN_HOME}/zeppelin-interpreter/target/classes" ]]; then
  ZEPPELIN_CLASSPATH+=":${ZEPPELIN_HOME}/zeppelin-interpreter/target/classes"
fi

if [[ -d "${ZEPPELIN_HOME}/zeppelin-zengine/target/classes" ]]; then
  ZEPPELIN_CLASSPATH+=":${ZEPPELIN_HOME}/zeppelin-zengine/target/classes"
fi

if [[ -d "${ZEPPELIN_HOME}/zeppelin-server/target/classes" ]]; then
  ZEPPELIN_CLASSPATH+=":${ZEPPELIN_HOME}/zeppelin-server/target/classes"
fi

addJarInDir "${ZEPPELIN_HOME}"
addJarInDir "${ZEPPELIN_HOME}/lib"
addJarInDir "${ZEPPELIN_HOME}/lib/interpreter"
addJarInDir "${ZEPPELIN_HOME}/zeppelin-interpreter/target/lib"
addJarInDir "${ZEPPELIN_HOME}/zeppelin-zengine/target/lib"
addJarInDir "${ZEPPELIN_HOME}/zeppelin-server/target/lib"
addJarInDir "${ZEPPELIN_HOME}/zeppelin-web/target/lib"

ZEPPELIN_CLASSPATH="$CLASSPATH:$ZEPPELIN_CLASSPATH"

if [[ -n "${HADOOP_CONF_DIR}" ]] && [[ -d "${HADOOP_CONF_DIR}" ]]; then
  ZEPPELIN_CLASSPATH+=":${HADOOP_CONF_DIR}"
fi

if [[ ! -d "${ZEPPELIN_LOG_DIR}" ]]; then
  echo "Log dir doesn't exist, create ${ZEPPELIN_LOG_DIR}"
  $(mkdir -p "${ZEPPELIN_LOG_DIR}")
fi

if [[ ! -d "${ZEPPELIN_PID_DIR}" ]]; then
  echo "Pid dir doesn't exist, create ${ZEPPELIN_PID_DIR}"
  $(mkdir -p "${ZEPPELIN_PID_DIR}")
fi
#Enable Graylog logging if necessary

if [ ! -z ${GRAYLOG_ENABLED} ] && [ ${GRAYLOG_ENABLED} = true ]
then
    #Use Graylog's log4j properties file and change env vars
    mv -f /tmp/conf/log4j-graylog.properties /tmp/conf/log4j.properties
    grep -rl '${GRAYLOG_HOST}' /tmp/conf/log4j.properties | xargs sed -i 's/${GRAYLOG_HOST}/'"$GRAYLOG_HOST"'/g'
    grep -rl '${GRAYLOG_PORT}' /tmp/conf/log4j.properties | xargs sed -i 's/${GRAYLOG_PORT}/'"$GRAYLOG_PORT"'/g'
    grep -rl '${GRAYLOG_APP_NAME}' /tmp/conf/log4j.properties | xargs sed -i 's/${GRAYLOG_APP_NAME}/'"$GRAYLOG_APP_NAME"'/g'
else
    if [ -f /tmp/conf/log4j-graylog.properties ]
    then
       rm /tmp/conf/log4j-graylog.properties
    fi   
fi

myuid=$(id -u)
mygid=$(id -g)
uidentry=$(getent passwd $myuid)

if [ -z "$uidentry" ] ; then
    # assumes /etc/passwd has root-group (gid 0) ownership
    echo "$myuid:x:$myuid:$mygid:anonymous uid:/tmp:/bin/false" >> /etc/passwd
fi

cp -rf /tmp/conf /zeppelin
cp /tmp/.condarc /home/notebook/.condarc
pip config set global.target $(python -m site --user-site)

export PYTHONPATH='/zeppelin/interpreter/python/py4j-0.9.2/src:/zeppelin/interpreter/lib/python'

exec $ZEPPELIN_RUNNER $JAVA_OPTS -cp $ZEPPELIN_CLASSPATH_OVERRIDES:${ZEPPELIN_CLASSPATH} $ZEPPELIN_SERVER "$@"

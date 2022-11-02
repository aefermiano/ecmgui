#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

JAVA_LIBRARY_PATH=${SCRIPT_DIR}/native/lib
JAR_FILE=$(ls -Art target/*with-dependencies.jar | tail -n 1)

java -Djava.library.path=${JAVA_LIBRARY_PATH} -jar ${JAR_FILE}

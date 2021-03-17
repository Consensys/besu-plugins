#!/bin/bash
set -euo pipefail

LAUNCHER_VERSION=${1:?Must specify besu-plugins version}
JAR=${2:?Must specify path to jar}
POM=${3:?Must specify path to pom}

ENV_DIR=./build/tmp/cloudsmith-env
if [[ -d ${ENV_DIR} ]] ; then
    source ${ENV_DIR}/bin/activate
else
    python3 -m venv ${ENV_DIR}
    source ${ENV_DIR}/bin/activate
fi

python3 -m pip install --upgrade cloudsmith-cli

cloudsmith push maven consensys/besu-plugins "${JAR}" --pom-file="${POM}" --version "1.0.1"
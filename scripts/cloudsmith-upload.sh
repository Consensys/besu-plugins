#!/bin/bash
set -euo pipefail

PLUGINS_VERSION=${1:?Must specify plugins version}
TAR_DIST=${2:?Must specify path to tar distribution}
ZIP_DIST=${3:?Must specify path to zip distribution}

ENV_DIR=./build/tmp/cloudsmith-env
if [[ -d ${ENV_DIR} ]] ; then
    source ${ENV_DIR}/bin/activate
else
    python3 -m venv ${ENV_DIR}
    source ${ENV_DIR}/bin/activate
fi

python3 -m pip install --upgrade cloudsmith-cli

cloudsmith push raw consensys/quorum-besu-plugins $TAR_DIST --name "besu-plugins-${PLUGINS_VERSION}.tar.gz" --version "${PLUGINS_VERSION}" --summary "Quorum Besu plugins ${PLUGINS_VERSION} binary distribution" --description "Binary distribution of Quorum Besu plugins ${PLUGINS_VERSION}." --content-type 'application/tar+gzip'
cloudsmith push raw consensys/quorum-besu-plugins $ZIP_DIST --name "besu-plugins-${PLUGINS_VERSION}.zip" --version "${PLUGINS_VERSION}" --summary "Quorum Besu plugins ${PLUGINS_VERSION} binary distribution" --description "Binary distribution of Quorum Besu plugins ${PLUGINS_VERSION}." --content-type 'application/zip'
#!/bin/sh

export VERSION_NEW_CLASSI="${1}"; shift

export REMOTE=maggu2810
export REF_TAG_MVN_ARGS_CHECK_BUILD="clean install deploy -DskipTests=true -DaltDeploymentRepository=bintray::default::https://api.bintray.com/maven/maggu2810/smarthome/esh"
export COMMIT_ID="$(git rev-parse HEAD)"

exec tools/my/ref-tag.sh "${COMMIT_ID}"

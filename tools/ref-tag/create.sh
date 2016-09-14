#!/bin/bash

#
# Define some functions first
#

log() {
  echo "${@}"
}

log_err() {
  echo "${@}" 2>&1
}

cleanup() {
  if [ -n "${COMMIT_ID_WC}" ]; then
    log "restore working copy..."
    git reset --hard "${COMMIT_ID_WC}"
  fi
}

die() {
  if [ ${#} -gt 0 ]; then
    log_err "${@}"
  fi
  cleanup
  exit 1
}

get_abs() {
  local ARG_PATH="${1}"; shift

  if [ ! -e "${ARG_PATH}" ]; then
    log_err "Cannot resolve path (${ARG_PATH})"
    return 1
  fi

  if [ -d "${ARG_PATH}" ]; then
    cd "${ARG_PATH}"
    echo "${PWD}"
    cd "${OLDPWD}"
  else
    local DIRNAME="$(dirname "${ARG_PATH}")"
    local BASENAME="$(basename "${ARG_PATH}")"
    cd "${DIRNAME}"
    echo "${PWD}/${BASENAME}"
    cd "${OLDPWD}"
  fi
}

#
# Begin
#

MY_CMD="${0}"
MY_CMD_ABS="$(get_abs "${MY_CMD}")" || die "Cannot resolve path"
MY_DIRNAME_ABS="$(dirname "${MY_CMD_ABS}")"

if [ -z "${REMOTE}" ]; then
  REMOTE=origin
fi
unset COMMIT_ID_WC

#
# Parse command line arguments
#
COMMIT_ID="${1}"; shift

if [ -z "${COMMIT_ID}" ]; then
  die "commit id missing"
fi

# Clone only if necessary
#if [ ! -d "${REPO_DIR}"/.git ]; then
#  git clone -o "${REMOTE}" "git@github.com:eclipse/smarthome.git"
#fi
# Goto git clone
#cd "${REPO_DIR}"
cd "${MY_DIRNAME_ABS}"/../.. || die "Cannot enter working copy's root directory of the repository"

#
# Check working copy
#
GIT_STATUS="$(git status -s)"

if [ -n "${GIT_STATUS}" ]; then
  log "${GIT_STATUS}"
  die "Your working copy is not clean"
fi

#
# Store commit ID of current working copy
#
COMMIT_ID_WC="$(git rev-parse HEAD)"
log "To restore your working copy (if script does not finish correctly), use:"
log "git reset -q --hard ${COMMIT_ID_WC}"

#
# Fetch all from repos
#
git fetch "${REMOTE}" || die "Cannot fetch."

#
# Reset current working copy to given commit id
#
git reset -q --hard "${COMMIT_ID}" || die "Reset working copy failed."

#
# Clean working copy
#
git clean -q -x -d -f || die "Git clean failed."

#
# Parse version of the current working copy
#
VERSION_OLD="$(cat pom.xml | grep '<version>.*</version>' | head -n1 | sed 's:<version>\(.*\)</version>:\1:g' | awk '{print $1}')"
case "${VERSION_OLD}" in
  *-SNAPSHOT) log "version old: ${VERSION_OLD}"
    ;;
  *) die "version old is no snapshot"
    ;;
esac

#
# Prepare some version old variables
#
VERSION_OLD_MMR=${VERSION_OLD%-SNAPSHOT}
VERSION_OLD_QUALI="${VERSION_OLD_MMR}.qualifier"

#
# Generate new version
#
if [ -z "${VERSION_NEW_MMR}" ]; then
  VERSION_NEW_MMR="${VERSION_OLD_MMR}"
fi
if [ -n "${VERSION_NEW_QUALI}" ]; then
  case "${VERSION_NEW_QUALI}" in
    SNAPSHOT)
      VERSION_NEW="${VERSION_NEW_MMR}-${VERSION_NEW_QUALI}"
      ;;
    *)
      VERSION_NEW="${VERSION_NEW_MMR}.${VERSION_NEW_QUALI}"
      ;;
  esac
else
  VERSION_NEW_QUALI=".qualifier"
  VERSION_NEW="${VERSION_NEW_MMR}".{VERSION_NEW_QUALI}
fi

#
# Print version info
#
log "version old: ${VERSION_OLD}, mmr: ${VERSION_OLD_MMR}, quali: ${VERSION_OLD_QUALI}"
log "version new: ${VERSION_NEW}, mmr: ${VERSION_NEW_MMR}, quali: ${VERSION_NEW_QUALI}"
log "commit id: ${COMMIT_ID}"

log "Enter 'Y' to proceed"
read PROCEED
if [ x"${PROCEED}" != x"Y" ]; then
  die
fi

# Use tycho to set version of pom and manifest files
log "set new version using tycho"
mvn tycho-versions:set-version -DnewVersion="${VERSION_NEW}" || die "Tycho set-version failed."

#
# Now change some files manually
#
log "change additional files"

for FILE in \
  bundles/config/org.eclipse.smarthome.config.core.test/ConfigCoreTests.launch \
  bundles/core/org.eclipse.smarthome.core.thing.test/org.eclipse.smarthome.core.thing.test.launch \
  bundles/io/org.eclipse.smarthome.io.rest.core.test/org.eclipse.smarthome.io.rest.core.test.launch \
  docs/documentation/development/testing.md \
  products/org.eclipse.smarthome.repo/category.xml
do
  sed 's:'"${VERSION_OLD_QUALI}"':'"${VERSION_NEW_QUALI}"':g' -i "${FILE}"
done

for FILE in \
  docs/documentation/community/downloads.md \
  docs/pom.xml \
  extensions/binding/create_binding_skeleton.cmd \
  extensions/binding/create_binding_skeleton.sh
do
  sed 's:'"${VERSION_OLD}"':'"${VERSION_NEW}"':g' -i "${FILE}"
done

#for FILE in \
#  extensions/ui/org.eclipse.smarthome.ui.basic/package.json \
#  extensions/ui/org.eclipse.smarthome.ui.paper/package.json \
#  extensions/ui/org.eclipse.smarthome.ui.paper/bower.json
#do
#  sed 's:\("version"\: *"\).*\(".*\):\1'"${VERSION_NEW_MMR}"'\2:g' -i "${FILE}"
#done

#
# Check if maven could build
#
if [ -z "${REF_TAG_MVN_ARGS_CHECK_BUILD}" ]; then
  REF_TAG_MVN_ARGS_CHECK_BUILD="clean install"
fi
mvn ${REF_TAG_MVN_ARGS_CHECK_BUILD} || die "Check build using 'mvn ${REF_TAG_MVN_ARGS_CHECK_BUILD}' failed"

#
# Commit changes done in the working copy
#
git add . || die "git add failed"
git commit -s -m "[ref] set version to ${VERSION_NEW}" || die "git commit failed"

#
# Generate tag name
#
TAG_NAME=ref-"${VERSION_NEW}"

#
# Create tag
#
git tag "${TAG_NAME}" || die "create tag failed"

#
# Push tag to remote
#
git push --tags "${REMOTE}" "${TAG_NAME}" || die "push tag failed"

#
# Now, do a build with additional deploy of the artifacts
#
#mvn clean install deploy

#
# Cleanup (e.g. restore working copy)
#
cleanup

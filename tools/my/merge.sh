#!/bin/sh

REMOTES=""
REMOTES="${REMOTES} eclipse"
REMOTES="${REMOTES} kaikreuzer"
REMOTES="${REMOTES} kgoderis"
REMOTES="${REMOTES} qivicon"
REMOTES="${REMOTES} maggu2810"

git fetch --all --tags --prune
for REMOTE in ${REMOTES}; do
	git remote | grep ^${REMOTE}$ &>/dev/null
	if [ ${?} -ne 0 ]; then
		git remote add "${REMOTE}" "git@github.com:${REMOTE}/smarthome.git"
		git fetch "${REMOTE}"
	fi
done

REMOTE_BRANCHES=""
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/maggu2810/merge_drop-repo.eclipse"
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/maggu2810/merge_tycho-0.25.0"
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/maggu2810/merge_workaround-1550"
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/maggu2810/persistence-model"
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/maggu2810/ref-tag-tool"
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/maggu2810/npm-tempdirs"
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/maggu2810/servlet-port"
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/qivicon/thing-bridge-lifecycle"
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/kgoderis/cron-fix"
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/kaikreuzer/sonosaudio"

die() {
	if [ ${#} -gt 0 ]; then
		echo "${@}" 1>&2
	fi
	exit 1
}

git merge ${REMOTE_BRANCHES} || die "Merge failed"

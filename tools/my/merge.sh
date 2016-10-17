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
		git fetch --tags --prune "${REMOTE}"
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
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/qivicon/thing-bridge-lifecycle" # https://github.com/eclipse/smarthome/pull/2087
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/kgoderis/cron-fix" # https://github.com/eclipse/smarthome/pull/2270
REMOTE_BRANCHES="${REMOTE_BRANCHES} remotes/kaikreuzer/sonosaudio" # https://github.com/eclipse/smarthome/pull/2306

die() {
	if [ ${#} -gt 0 ]; then
		echo "${@}" 1>&2
	fi
	exit 1
}

git rebase eclipse/master || die "Rebase failed"
git merge ${REMOTE_BRANCHES} || die "Merge failed"

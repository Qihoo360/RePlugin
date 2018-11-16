#!/bin/bash

export RP_BASE_DIR=$(cd "$(dirname "$0")"; pwd)

export TARGET_PROJECTS=(
replugin-host-gradle
replugin-host-library
replugin-plugin-gradle
replugin-plugin-library
)

__gradle_exec(){ if [[ -x gradlew ]];then ./gradlew ${@}; else gradle ${@}; fi; }

__rp_deploy_project(){
	[[ ! -d ${1} ]] && echo ">>> INVALID ${1}!!! <<<" && return
	# execute deploying
	echo ">>> ${1} <<<" && __gradle_exec -p ${1} clean bintrayUpload
	# revert changed files
	git checkout ${1}
}

rp_deploy(){
	local current=`pwd` && cd ${RP_BASE_DIR}
	# saving all changes: git stash save "saving stash for deploying!!!"
	# deploy
	for p in ${TARGET_PROJECTS}; do __rp_deploy_project ${RP_BASE_DIR}/${p}; done
	# revert local changes: git revert --hard HEAD; git stash pop
	local current=`pwd`
}

rp_test(){
	local projects=(
		# replugin-sample/host/app
		replugin-sample/host
		# replugin-sample/plugin/plugin-demo1/app
		replugin-sample/plugin/plugin-demo1
		# replugin-sample/plugin/plugin-demo2/app
		replugin-sample/plugin/plugin-demo2
		# replugin-sample/plugin/plugin-demo3-kotlin/app
		replugin-sample/plugin/plugin-demo3-kotlin
		# replugin-sample/plugin/plugin-webview/app
		replugin-sample/plugin/plugin-webview
		# replugin-sample-extra/fresco/FrescoHost/app
		replugin-sample-extra/fresco/FrescoHost
		# replugin-sample-extra/fresco/FrescoPlugin/app
		replugin-sample-extra/fresco/FrescoPlugin
	)
	local log=${RP_BASE_DIR}/build/rp_test.log && [[ -f $log ]] && rm -f $log
	local current=`pwd`
	for p in ${projects}; do
		echo -e ">>> BUILDING ${p}"
		p=${RP_BASE_DIR}/${p} && __gradle_exec -p ${p} clean asDebug 2>/dev/null >> ${log} && echo "SUCCEED";
		ls -l ${p}/app/build/outputs/apk
	done
	cd ${current}
}

# grep --exclude-dir={.bzr,CVS,.git,.hg,.svn,.idea,build,.gradle} -inr '2\.3\.0' .
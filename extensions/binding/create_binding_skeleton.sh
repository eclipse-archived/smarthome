#!/bin/bash

bindingVersion=0.10.0-SNAPSHOT
archetypeVersion=0.10.0-SNAPSHOT

camelcaseId=$1
author="$2"

[ $# -lt 2 ] && { echo "Usage: $0 <BindingIdInCamelCase> <Author>"; exit 1; }

id=`echo $camelcaseId | tr '[:upper:]' '[:lower:]'`

# the binding
mvn archetype:generate --non-recursive \
-DinteractiveMode=false \
-DarchetypeGroupId=org.eclipse.smarthome.archetype \
-DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding \
-DarchetypeVersion=$archetypeVersion \
-DgroupId=org.eclipse.smarthome.binding \
-DartifactId=org.eclipse.smarthome.binding.$id \
-Dpackage=org.eclipse.smarthome.binding.$id \
-Dversion=$bindingVersion \
-DbindingId=$id \
-DbindingIdCamelCase=$camelcaseId \
-Dauthor="$author"

# the tests
mvn archetype:generate --non-recursive \
-DinteractiveMode=false \
-DarchetypeGroupId=org.eclipse.smarthome.archetype \
-DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding.test \
-DarchetypeVersion=$archetypeVersion \
-DgroupId=org.eclipse.smarthome.binding \
-DartifactId=org.eclipse.smarthome.binding.$id.test \
-Dpackage=org.eclipse.smarthome.binding.$id \
-Dversion=$bindingVersion \
-DbindingId=$id \
-DbindingIdCamelCase=$camelcaseId \
-Dauthor="$author"

cp ../../src/etc/NOTICE "org.eclipse.smarthome.binding.$id/"
cp ../../src/etc/NOTICE "org.eclipse.smarthome.binding.$id.test/"


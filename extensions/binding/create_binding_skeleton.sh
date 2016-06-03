#!/bin/bash

camelcaseId=$1
author="$2"

[ $# -lt 2 ] && { echo "Usage: $0 <BindingIdInCamelCase> <Author>"; exit 1; }

id=`echo $camelcaseId | tr '[:upper:]' '[:lower:]'` 

# the binding
mvn archetype:generate --non-recursive -DinteractiveMode=false -DarchetypeGroupId=org.eclipse.smarthome.archetype -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding -DarchetypeVersion=0.9.0-SNAPSHOT -DgroupId=org.eclipse.smarthome.binding -DartifactId=org.eclipse.smarthome.binding.$id -Dpackage=org.eclipse.smarthome.binding.$id -DarchetypeCatalog='file://../archetype-catalog.xml' -Dversion=0.9.0-SNAPSHOT -DbindingId=$id -DbindingIdCamelCase=$camelcaseId -Dauthor="$author"

# the tests
mvn archetype:generate --non-recursive -DinteractiveMode=false -DarchetypeGroupId=org.eclipse.smarthome.archetype -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding.test -DarchetypeVersion=0.9.0-SNAPSHOT -DgroupId=org.eclipse.smarthome.binding -DartifactId=org.eclipse.smarthome.binding.$id.test -Dpackage=org.eclipse.smarthome.binding.$id -DarchetypeCatalog='file://../archetype-catalog.xml' -Dversion=0.9.0-SNAPSHOT -DbindingId=$id -DbindingIdCamelCase=$camelcaseId -Dauthor="$author"


@echo off


SETLOCAL
SET ARGC=0

FOR %%x IN (%*) DO SET /A ARGC+=1

IF %ARGC% NEQ 2 (
	echo Usage: %0 BindingIdInCamelCase BindingIdInLowerCase
	exit /B 1
)

mvn archetype:generate -DarchetypeGroupId=org.eclipse.smarthome.archetype -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding -DarchetypeVersion=0.8.0-SNAPSHOT -DgroupId=org.eclipse.smarthome.binding -DartifactId=org.eclipse.smarthome.binding.%2 -Dpackage=org.eclipse.smarthome.binding.%2 -DarchetypeCatalog='file://../archetype-catalog.xml' -Dversion=0.8.0-SNAPSHOT -DbindingId=%2 -DbindingIdCamelCase=%1


ENDLOCAL
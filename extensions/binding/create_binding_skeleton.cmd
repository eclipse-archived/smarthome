@ECHO off


SETLOCAL
SET ARGC=0

FOR %%x IN (%*) DO SET /A ARGC+=1

IF %ARGC% NEQ 2 (
	ECHO Usage: %0 BindingIdInCamelCase Author
	EXIT /B 1
)

SET BindingIdInCamelCase=%1
SET BindingIdInLowerCase=%BindingIdInCamelCase%

CALL :LoCase BindingIdInLowerCase

call mvn archetype:generate -N -DinteractiveMode=false -DarchetypeGroupId=org.eclipse.smarthome.archetype -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding -DarchetypeVersion=0.10.0-SNAPSHOT -DgroupId=org.eclipse.smarthome.binding -DartifactId=org.eclipse.smarthome.binding.%BindingIdInLowerCase% -Dpackage=org.eclipse.smarthome.binding.%BindingIdInLowerCase% -Dversion=0.10.0-SNAPSHOT -DbindingId=%BindingIdInLowerCase% -DbindingIdCamelCase=%BindingIdInCamelCase% -Dauthor=%2

call mvn archetype:generate -N -DinteractiveMode=false -DarchetypeGroupId=org.eclipse.smarthome.archetype -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding.test -DarchetypeVersion=0.10.0-SNAPSHOT -DgroupId=org.eclipse.smarthome.binding -DartifactId=org.eclipse.smarthome.binding.%BindingIdInLowerCase%.test -Dpackage=org.eclipse.smarthome.binding.%BindingIdInLowerCase% -Dversion=0.10.0-SNAPSHOT -DbindingId=%BindingIdInLowerCase% -DbindingIdCamelCase=%BindingIdInCamelCase% -Dauthor=%2

COPY ..\..\src\etc\NOTICE org.eclipse.smarthome.binding.%BindingIdInLowerCase%\
COPY ..\..\src\etc\NOTICE org.eclipse.smarthome.binding.%BindingIdInLowerCase%.test\

SET BindingIdInLowerCase=
SET BindingIdInCamelCase=

GOTO:EOF


:LoCase
:: Subroutine to convert a variable VALUE to all lower case.
:: The argument for this subroutine is the variable NAME.
FOR %%i IN ("A=a" "B=b" "C=c" "D=d" "E=e" "F=f" "G=g" "H=h" "I=i" "J=j" "K=k" "L=l" "M=m" "N=n" "O=o" "P=p" "Q=q" "R=r" "S=s" "T=t" "U=u" "V=v" "W=w" "X=x" "Y=y" "Z=z") DO CALL SET "%1=%%%1:%%~i%%"
GOTO:EOF

ENDLOCAL

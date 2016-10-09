This place could be used to inject third party JARs to the target platform.
This mechanism works for the Eclipse IDE only because Tycho does not support the location types "Directory", "Installation", and "Features".

It allows us to add third party JARs for the development process.
Also if you need to test the Runtime with a third party bundle you can use this mechanism to add it easily to the launch configuration.

Drop the JARs to this directory, refresh the target platform, start coding / testing.
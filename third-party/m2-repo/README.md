Here you can add local artifacts that should not be fetched from an external maven repository.
The layout should fit to a standard maven repository one.

You could use for exmaple:
* no/home/some-bundle/1.2.3/some-bundle-1.2.3.jar
and access it in the pom using:
* group id: no.home
* artifact id: some-bundle
* version: 1.2.3

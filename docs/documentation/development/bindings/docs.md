---
layout: documentation
---

{% include base.html %}

# Documenting a Binding

A binding should come with some documentation in form of a ```README.md``` file (in markdown syntax) within its project folder.
If a single file is not enough, additional resources (e.g. further md-files, images, example files) can be put in a ```doc``` folder.

Neither the ```README.md``` file nor the ```doc``` folder must be added to ```build.properties```, i.e. they only exist in the source repo, but should not be packaged within the binary bundles.

It is planned to generate parts of the documentation based on the files that are available with the ```ESH-INF``` folder of the binding. As this documentation generation is not (yet) in place, the documentation currently needs to be maintained fully manually.
The Maven archetype creates a [template for the binding documentation](https://github.com/eclipse/smarthome/blob/master/tools/archetype/binding/src/main/resources/archetype-resources/README.md).

---
layout: documentation
---

{% include base.html %}

# Conventions

## Null annotations

[Null annotations](https://wiki.eclipse.org/JDT_Core/Null_Analysis) are used from the Eclipse JDT project.
Our intention with these annotations is to transfer a method's contract written in its JavaDoc into the code to be processed by tools.
We are aware that these annotations can be used for **static** checks, but **not** at runtime.

Thus for publicly exposed methods that belong to our API and are (potentially) called by external callers, we cannot omit a `null` check although a method parameter is marked to be not `null` via an annotation.
We will get a warning in the IDE for this check, but we decided to live with that.
For private methods or methods in an internal package we agreed to respect the annotations and omit an additional `null` check.

To use the annotations, every bundle should have an **optional** `Import-Package` dependency to `org.eclipse.jdt.annotation`.
Classes should be annotated by `@NonNullByDefault` and return types, parameter types, generic types etc. are annotated with `@Nullable` only.
Fields that get a static and mandatory reference injected through OSGi Declarative Services can be annotated with

```java
@NonNullByDefault({})
private MyService injectedService;
```

to skip the nullevaluation for these fields.
Fields within `ThingHandler` classes that are initialized within the `initialize()` method may also be annotated like this, because the framework ensures that `initialize()` will be called before any other method.
However please watch the scenario where the initialization of the handler fails, because then fields might not have been initialized and using them should be prepended by a `null` check.

There is **no need** for a `@NonNull` annotation because it is set as default.
Test classes do not have to be annotated (the usage of `SuppressWarnings("null")` in tests is allowed too).

The transition of existing classes could be a longer process but if you want to use nullness annotation in a class / interface you need to set the default for the whole class and annotate all types that differ from the default.

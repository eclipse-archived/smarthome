# Simple Math Transformation Service

Transforms the input by performing simple math on it.

## Example

item:

```
Number Usage  "Usage in Watt [MULTIPLY(1000):%s W]"  { someBinding:somevalue }
Number Usage  "Usage in Watt [ADD(5):%s W]"  { someBinding:somevalue }
```

Example in rules:

```
transform("MULTIPLY", "1000", "2")
transform("ADD", "1000", "2")
```

# Map Transformation Service

Transforms the input by mapping it to another string. It expects the mappings to be read from a file which is stored under the `transform` folder. 

This file should be in property syntax, i.e. simple lines with "key=value" pairs. 
The file format is documented [here](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html#load-java.io.Reader-).
To organize the various transformations one might use subfolders.

## Example

transform/binary.map:

```properties
key=value
1=ON
0=OFF
ON=1
OFF=0
```

| input | output  |
|-------|---------|
| `1`   | `ON`    |
| `OFF` | `0`     |
| `key` | `value` |

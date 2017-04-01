# Scale Transformation Service

Transform the input by matching it between limits of ranges in a scale file.  The input string must be in numerical format.

The file is expected to exist in the `transform` directory, and should follow the format given in the example below.

## Example

transform/temps.scale:

```properties
[12..23]=Temp between 12 or higher and 23 or lower
]12..23[=Temp over 12 and below 23
..23]=Any temp up to and including 23
]12..=Any temp above 12
```

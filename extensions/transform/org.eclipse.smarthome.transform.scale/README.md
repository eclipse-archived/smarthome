# Scale Transformation Service

Transform a given input by matching it between limits of ranges.
The input string must be in numerical format.

The file is expected to exist in the `transform` configuration directory and its ending has to be `.scale`.
It should follow the format given in the table below.

Range expressions always contain two parts.
The range to scale on, which is located left from the equality sign and the corresponding output string on the right of it.
A range consists of two bounds. Both are optional, the range is then open. Both bounds can be inclusive or exclusive.

| Scale Expression | Returns XYZ when the given Value is                        |
|------------------|------------------------------------------------------------|
| `[12..23]=XYZ`   | `between (or equal to) 12 and 23`                          |
| `]12..23[=XYZ`   | `between 12 and 23 (12 and 23 are excluded in this case.)` |
| `[..23]=XYZ`     | `lower than or equal to 23`                                |
| `]12..]=XYZ`     | `greater than 12`                                          |

These expressions are evaluated from top to bottom.
The first range that includes the value is selected.

## Example

The following example shows how to break down numeric UV values into fixed UV index categories.
We have an example UV sensor that sends numeric values from `0` to `100`, which we then want to scale into the [UV Index](https://en.wikipedia.org/wiki/Ultraviolet_index) range.

Example item:

```java
Number Uv_Sensor_Level "UV Level [SCALE(uvindex.scale):%s]"
```

Referenced scale file `uvindex.scale` in the `transform` folder::

```python
[..3]=1
]3..6]=2
]6..8]=3
]8..10]=4
[10..100]=5
```

Each value the item receives, will be categorized to one of the five given ranges.
Values **lower than or equal to 3** are catched with `[..3]=1`.
Greater values are catched in ranges with 2 values as criteria.
The only condition here is that the received value has to be lower than or equal to `100` in our example, since we haven't defined other cases yet.
If **none** of the configured conditions matches the given value, the response will be empty.

Please note that all ranges for values above **3** are opened with a `]`.
So the border values (3, 6, 8 and 10) are always transformed to the lower range, since the `]` excludes the given critera.
# RegEx Transformation Service

Given a source string and a regular expression, use the regular expression to yield a transformed string.  

If the regular expression is in the format `s/<regex>/result/g`, replace all occurrences of `<regex>` in the source string with `result` and return the result.

If the regular expression is in the format `s/<regex>/result/`, replace the first occurrence of `<regex>` in the source string with `result` and return the result.

If the regular expression contains a [capture group](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#cg), return the captured string.  The regular expression in this case is further restricted to isolate a complete line by adding `^` to the beginning and `$` to the end.

## Examples

With the input string `My network does not work.`:

| regular expression | output |
|--------------------|--------|
| `s/work/cast/g`    | `"My netcast does not cast."` |
| `.*(\snot).*`      | `" not"` |

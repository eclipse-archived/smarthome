# RegEx Transformation Service

Transforms a source string on basis of the regular expression pattern to a defined result.

The simplest regex is in the form `<regex>` and transforms the input string on basis of the regex patern to a result string.
A full regex is in the form `s/<regex>/<result>/g` whereat the delimiter `s` and the regex flag `g` have special meaning.

The regular expression in the format `s/<regex>/result/g`, replaces all occurrences of `<regex>` in the source string with `result`.
The regular expression in the format `s/<regex>/result/` (without `g`), replaces the first occurrence of `<regex>` in the source string with `result`.

If the regular expression contains a [capture group](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#cg), it returns the captured string.
The regular expression in this case is further restricted to isolate a complete line by adding `^` to the beginning and `$` to the end.

** Consider **
The special characters `\.[]{}()*+-?^$|` have to be escaped when they should be used as literal, as character to find.
The regex is embedded in a string so when double qoutes `"` are used in an regex they need to be escaped `\"` to keep the string intact.

There are plenty online regex evaluater which allows fast checking of an regex.

## Examples

|         Input String        |    Regular Expression    |         Output String        | Explanation              |
|---------------------------|------------------------|----------------------------|--------------------------|
| `My network does not work.` | `s/work/cast/g` | `"My netcastdoes not cast."` | Replaces all matches with regex |
| `My network does not work.` | `.*(\snot).*` | `" not"` | Returns only first match, strips of rest |
| `temp=44.0'C` | `temp=(.*?)'C)`          | `44.0` | Matches whole string, retuns captcha group (.?) |
| `48312` | `s/(.{2})(.{3})/$1.$2/g` | `48.312` | Captures 2 and 3 character, retuns first capture group adds a dot and the second capture group. This devides by 1000. |

# Exec Transformation Service

Transforms an input string with an external program.

Executes an external program and returns the output as a string.
In the given command line the placeholder `%s` is substituted with the input value.

The external program must either be in the executable search path of the server process, or an absolute path has to be used.

## Examples

### General Setup

**Item**

This will replace the visible label in the UI with the transformation you apply with the command <TransformProgram>.
  
```java
String yourItem "Some info  [EXEC(/absolute/path/to/your/<TransformProgram> %s):%s]"
```

**Rule**

```java
rule "Your Rule Name"
when
    Item YourTriggeringItem changed
then
    var formatted = transform("EXEC","/absolute/path/to/your/<TransformProgram>", YourTriggeringItem.state.toString)
    yourFormattedItem.sendCommand(formatted.toString) 
end
```

### Example with a program

Substitute the `/absolute/path/to/your/<TransformProgram>` with

```shell
/bin/date -v1d -v+1m -v-1d -v-%s
```

When the input argument for `%s` is `fri` the execution returns a string with the last weekday of the month, formated as readable text.

```
Fri 31 Mar 2017 13:58:47 IST`
```

Or replace it with

```shell
numfmt --to=iec-i --suffix=B --padding=7 %s
```

When the input argument for `%s` is 1234567 it will return the bytes formated in a better readable form

```shell
1.2MiB
```

# Further Reading

* [Manual](http://man7.org/linux/man-pages/man1/date.1.html) and [tutorial](https://linode.com/docs/tools-reference/tools/use-the-date-command-in-linux/) for date.
* [Manual](http://man7.org/linux/man-pages/man1/numfmt.1.html) and [tutorial](http://www.pixelbeat.org/docs/numfmt.html) for numfmt.



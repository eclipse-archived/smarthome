# Exec Transformation Service

Execute an external program, substituting the placeholder `%s` in the given command line with the input value, and returning the output of the external program.

The external program must either be in the executable search path of the server process, or an absolute path can be used.

## Example

**.items**

This will replace the visible label in the UI with the transformation you apply with the command <YourCommand>.
  
```java
String yourItem "Some info  [EXEC(/absolute/path/to/your/TransformProgram %s):%s]"
```

**.rules**

```java
rule "Your Rule Name"
when
    Item YourTriggeringItem changed
then
    var formatted = transform("EXEC","/absolute/path/to/your/TransformProgram", YourTriggeringItem.state.toString)
    yourFormattedItem.sendCommand(formatted.toString) 
end
```

**Example with a program**

Substitute the `/absolute/path/to/your/TransformProgram` with

```shell
/bin/date -v1d -v+1m -v-1d -v-%s
```

The execution returns a string showing the last weekday of the month.

| input | output                         |
|-------|--------------------------------|
| `fri` | `Fri 31 Mar 2017 13:58:47 IST` |

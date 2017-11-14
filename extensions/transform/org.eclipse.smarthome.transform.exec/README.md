# Exec Transformation Service

Execute an external program, substituting the placeholder `%s` in the given command line with the input value, and returning the output of the external program.

The external program must either be in the executable search path of the server process, or an absolute path can be used.

## Example

With the command line 

```
/bin/date -v1d -v+1m -v-1d -v-%s
```

return a string showing the last weekday of the month.

| input | output |
|-------|--------|
| `fri` | `Fri 31 Mar 2017 13:58:47 IST` |

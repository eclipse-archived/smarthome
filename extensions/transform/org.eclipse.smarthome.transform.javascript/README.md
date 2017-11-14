# JavaScript Transformation Service

Transform an input to an output using JavaScript. 

It expects the transformation rule to be read from a file which is stored under the `transform` folder. 
To organize the various transformations, one should use subfolders.

## Example

Let's assume we have received a string containing `foo bar baz` and we're looking for a length of the last word (`baz`).

transform/getValue.js:

```
(function(i) {
    var array = i.split(" ");
    return array[array.length - 1].length;
})(input)
```

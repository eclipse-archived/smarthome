(function() {
	"use strict";

	var
		gulp = require("gulp"),
		less = require("gulp-less"),
		uglify = require("gulp-uglify"),
		eslint = require("gulp-eslint");

	var
		sources = {
			js: "web-src/smarthome.js",
			less: "web-src/smarthome.less"
		};

	gulp.task("css", function() {
		return gulp.src(sources.less)
			.pipe(less({ compress: true }))
			.pipe(gulp.dest("web"));
	});

	gulp.task("eslint", function() {
		return gulp.src(sources.js)
			.pipe(eslint({
				configFile: "eslint.json"
			}))
			.pipe(eslint.format())
			.pipe(eslint.failAfterError());
	});

	gulp.task("js", function() {
		return gulp.src(sources.js)
			.pipe(uglify())
			.pipe(gulp.dest("web"));
	});

	gulp.task("default", [ "css", "eslint", "js" ]);
})();

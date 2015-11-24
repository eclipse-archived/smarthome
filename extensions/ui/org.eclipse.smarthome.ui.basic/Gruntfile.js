/*global module, require */

module.exports = function(grunt) {
	"use strict";

	require("load-grunt-tasks")(grunt);

	grunt.initConfig({
		pkg: grunt.file.readJSON("package.json"),

		less: {
			all: {
				options: {
					compress: true
				},
				files: {
					"web/smarthome.css": ["web-src/smarthome.less"]
				}
			}
		},

		clean: {
			all: [
				"web/smarthome.js",
				"web/smarthome.css"
			]
		},

		eslint: {
			options: {
				configFile: "eslint.json"
			},
			target: ["web-src/smarthome.js"]
		},

		uglify: {
			all: {
				files: {
					"web/smarthome.js": ["web-src/smarthome.js"]
				}
			}
		}
	});

	grunt.registerTask("default", [
		"less",
		"eslint",
		"uglify"
	]);

	grunt.registerTask("clean", [
		"clean"
	]);
};

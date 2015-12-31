'use strict';

var gulp = require('gulp'),
    sass = require('gulp-sass'),
    uglify = require('gulp-uglify'),
    concat = require('gulp-concat'),
    rename = require("gulp-rename");

var paths = {
    scripts: ['./web-src/js/app.js', './web-src/js/constants.js', './web-src/js/extensions.js', './web-src/js/main.js', './web-src/js/shared.properties.js', './web-src/js/controllers.module.js'],
    styles: ['./web-src/css/style.scss'],
    images: ['./web-src/img/*'],
    concat: [{
        'src': './web-src/js/services*.js',
        'name': 'services.js'
    }, {
        'src': './web-src/js/controllers*.js',
        'name': 'controllers.js'
    }]
};

gulp.task('default', ['build']);
gulp.task('build', ['styles', 'uglify', 'copyScripts', 'copyImgs', 'copyFonts']);

gulp.task('styles', function () {
    return gulp.src(paths.styles)
        .pipe(sass({
            outputStyle: 'compressed'
        })).on('error', sass.logError)
        .pipe(rename(function (path) {
            path.basename += '.min';
            return path;
        }))
        .pipe(gulp.dest('./web/css/'));
});

gulp.task('uglify', function () {
    return gulp.src(paths.scripts)
        .pipe(uglify())
        .pipe(rename(function (path) {
            path.basename += '.min';
            return path;
        }))
        .pipe(gulp.dest('./dist/js/'));
});

gulp.task('copyScripts', function () {
    return gulp.src('./js/*.min.js')
        .pipe(gulp.dest('./dist/js'))
});

gulp.task('copyImgs', function () {
    return gulp.src(paths.images)
        .pipe(gulp.dest('./web/img'))
});

gulp.task('copyFonts', function () {
    return gulp.src('./fonts/**/*')
        .pipe(gulp.dest('./dist/fonts'))
});

gulp.task('concat', function () {
    return paths.concat.forEach(function(obj) {
        return gulp.src(obj.src)
            .pipe(concat(obj.name))
            .pipe(rename(function (path) {
                path.basename += '.min';
                return path;
            }))
            .pipe(gulp.dest('./web/js'));
    });
});

gulp.task('watch', function () {
    gulp.watch('./stylesheets/**/*.scss', ['styles']);
    gulp.watch('js/*.js', ['uglify']);
});

'use strict';

var gulp = require('gulp'),
    del = require('del'),
    sass = require('gulp-sass'),
    uglify = require('gulp-uglify'),
    uglifySaveLicense = require('uglify-save-license'),
    size = require('gulp-size'),
    filter = require('gulp-filter'),
    ngAnnotate = require('gulp-ng-annotate'),
    rename = require("gulp-rename"),
    inject = require('gulp-inject'),
    useref = require('gulp-useref'),
    minifyHtml = require('gulp-minify-html'),
    rev = require('gulp-rev'),
    revReplace = require('gulp-rev-replace'),
    angularFilesort = require('gulp-angular-filesort'),
    wiredep = require('wiredep').stream;;

var paths = {
    scripts: './web-src/js/**/*.js',
    styles: ['./web-src/css/style.scss'],
    images: ['./web-src/img/*'],
    partials: ['./web-src/partials/*.html'],
    FontLibs: [
        './web-src/bower_components/roboto-fontface/fonts/*',
        '!./web-src/bower_components/roboto-fontface/fonts/*.svg'
    ]
};

gulp.task('default', ['build']);
gulp.task('build', ['build-main', 'copyImgs', 'copyFonts', 'copyFontLibs', 'copyPartials']);

gulp.task('clean', function () {
    return del(['./web-tmp','./web']);
})

gulp.task('styles', function () {
    return gulp.src(paths.styles)
        .pipe(sass({
            outputStyle: 'compressed'
        })).on('error', sass.logError)
        .pipe(rename(function (path) {
            path.basename += '.min';
            return path;
        }))
        .pipe(gulp.dest('./web-tmp/css/'));
});

gulp.task('inject', function () {
    var injectScripts = gulp.src(paths.scripts).pipe(angularFilesort());

    var injectOptions = {
        ignorePath: ['web-src'],
        addRootSlash: false
    };

    var wiredepConf = {
        directory: './web-src/bower_components'
    };

    return gulp.src('./web-src/index.html')
        .pipe(inject(injectScripts, injectOptions))
        .pipe(wiredep(wiredepConf))
        .pipe(gulp.dest('./web-tmp/'));
});

gulp.task('build-main', ['inject', 'styles'], function () {
    var htmlFilter = filter('*.html');
    var jsFilter = filter('**/*.js');
    var cssFilter = filter('**/*.css');
    var assets;

    return gulp.src('./web-tmp/index.html')
        .pipe(assets = useref.assets())
        .pipe(rev()) // create dynamic file name
        // concat and minify JavaScript files
        .pipe(jsFilter)
        .pipe(ngAnnotate())
        .pipe(uglify({ preserveComments: uglifySaveLicense }))
        .pipe(jsFilter.restore())
        // minify CSS
        //.pipe(cssFilter)
        //.pipe(csso(true))
        //.pipe(cssFilter.restore())
        .pipe(assets.restore())
        .pipe(useref())
        .pipe(revReplace())
        // minify HTML files
        .pipe(htmlFilter)
        .pipe(minifyHtml({
            empty: true,
            spare: true,
            quotes: true,
            conditionals: true
        }))
        .pipe(htmlFilter.restore())
        .pipe(gulp.dest('./web/'))
        .pipe(size({ title: 'web/', showFiles: true }));
});

gulp.task('copyImgs', function () {
    return gulp.src(paths.images)
        .pipe(gulp.dest('./web/img'));
});

gulp.task('copyFonts', function () {
    return gulp.src('./fonts/**/*')
        .pipe(gulp.dest('./web/fonts'));
});

gulp.task('copyPartials', function () {
    return gulp.src(paths.partials)
        .pipe(gulp.dest('./web/partials'));
});

gulp.task('copyFontLibs', function () {
    return gulp.src(paths.FontLibs)
        .pipe(gulp.dest('./web/fonts'));
});

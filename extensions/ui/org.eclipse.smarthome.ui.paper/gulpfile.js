'use strict';

var angularFilesort = require('gulp-angular-filesort'),
    browserSync = require('browser-sync'),
    concat = require('gulp-concat'),
    del = require('del'),
    gulp = require('gulp'),
    ngAnnotate = require('gulp-ng-annotate'),
    proxyMiddleware = require('http-proxy-middleware'),
    rename = require("gulp-rename"),
    uglify = require('gulp-uglify');

var paths = {
    scripts: [
        './web-src/js/app.js',
        './web-src/js/constants.js',
        './web-src/js/extensions.js',
        './web-src/js/main.js',
        './web-src/js/shared.properties.js',
        './web-src/js/controllers.module.js',
        './web-src/bower_components/angular/angular.js'
    ],
    static: [
        './web-src/css/*.css',
        './web-src/img/*',
        './web-src/index.html'
    ],
    concat: [{
        'src': './web-src/js/services*.js',
        'name': 'services.js'
    }, {
        'src': './web-src/js/controllers*.js',
        'name': 'controllers.js'
    }, {
        'src': [
            // @TODO Figure out why including angular in concat breaks stuff
            //'./web-src/bower_components/angular/angular.min.js',
            './web-src/bower_components/angular-route/angular-route.min.js',
            './web-src/bower_components/angular-resource/angular-resource.min.js',
            './web-src/bower_components/angular-animate/angular-animate.min.js',
            './web-src/bower_components/angular-aria/angular-aria.min.js',
            './web-src/bower_components/angular-aria/angular-aria.min.js',
            './web-src/bower_components/angular-material/angular-material.min.js',
            './web-src/bower_components/angular-messages/angular-messages.min.js'
        ],
        'name': 'angular-bundle.js'
    }],
    partials: ['./web-src/partials/*.html'],
    JSLibs: [
        './web-src/bower_components/jquery/dist/jquery.min.js',
        './web-src/bower_components/masonry/dist/masonry.pkgd.min.js',
        './web-src/bower_components/sprintf/dist/sprintf.min.js',
        './web-src/bower_components/bootstrap/dist/js/bootstrap.min.js',
        './web-src/bower_components/tinycolor/tinycolor.js',
    ],
    CSSLibs: [
        './web-src/bower_components/bootstrap/dist/css/bootstrap.min.css',
        './web-src/bower_components/angular-material/angular-material.min.css',
        './web-src/bower_components/roboto-fontface/css/roboto-fontface.css'
    ],
    FontLibs: [
        './web-src/bower_components/roboto-fontface/fonts/*',
        '!./web-src/bower_components/roboto-fontface/fonts/*.svg'
    ]
};

gulp.task('default', ['build']);
gulp.task('build', ['uglify', 'concat', 'copyCSSLibs', 'copyFontLibs', 'copyJSLibs', 'copyStatic', 'copyPartials']);

gulp.task('uglify', function () {
    return gulp.src(paths.scripts)
        .pipe(ngAnnotate())
        .pipe(uglify())
        .pipe(rename(function (path) {
            path.basename += '.min';
            return path;
        }))
        .pipe(gulp.dest('./web/js/'));
});

gulp.task('copyStatic', function () {
    return gulp.src(paths.static, {base: 'web-src'})
        .pipe(gulp.dest('./web'));
});

gulp.task('copyPartials', function () {
    return gulp.src(paths.partials)
        .pipe(gulp.dest('./web/partials'));
});

gulp.task('copyJSLibs', function () {
    return gulp.src(paths.JSLibs)
        .pipe(gulp.dest('./web/js'));
});

gulp.task('copyCSSLibs', function () {
    return gulp.src(paths.CSSLibs)
        .pipe(gulp.dest('./web/css'));
});

gulp.task('copyFontLibs', function () {
    return gulp.src(paths.FontLibs)
        .pipe(gulp.dest('./web/fonts'));
});

gulp.task('concat', function () {
    return paths.concat.forEach(function (obj) {
        return gulp.src(obj.src)
            .pipe(angularFilesort())
            .pipe(concat(obj.name))
            .pipe(rename(function (path) {
                path.basename += '.min';
                return path;
            }))
            .pipe(gulp.dest('./web/js'));
    });
});

gulp.task('clean', function () {
    return del([
        './web/'
      ]);
});

// Gulp Serve
function browserSyncInit(baseDir) {
    var server = {
        baseDir: baseDir
    };

    server.middleware = proxyMiddleware(['/rest'], {target: 'http://localhost:8080'});

    browserSync.instance = browserSync.init({
        startPath: '/',
        server: server,
        browser: 'default'
    });
}

gulp.task('serve', ['build'], function () {
    browserSyncInit(['./web-src', './web']);
});

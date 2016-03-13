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
            './node_modules/angular-route/angular-route.min.js',
            './node_modules/angular-resource/angular-resource.min.js',
            './node_modules/angular-animate/angular-animate.min.js',
            './node_modules/angular-aria/angular-aria.min.js',
            './node_modules/angular-material/angular-material.min.js',
            './node_modules/angular-messages/angular-messages.min.js'
        ],
        'name': 'angular-bundle.js'
    }],
    partials: ['./web-src/partials/*.html'],
    JSLibs: [
        './node_modules/angular/angular.min.js',
        './node_modules/jquery/dist/jquery.min.js',
        './node_modules/masonry-layout/dist/masonry.pkgd.min.js',
        './node_modules/sprintf-js/dist/sprintf.min.js',
        './node_modules/bootstrap/dist/js/bootstrap.min.js',
        './node_modules/tinycolor2/tinycolor.js',
    ],
    CSSLibs: [
        './node_modules/bootstrap/dist/css/bootstrap.min.css',
        './node_modules/angular-material/angular-material.min.css',
    ],
    FontLibs: [
        './node_modules/roboto-fontface/fonts/*.woff',
        './node_modules/material-design-icons/iconfont/MaterialIcons-Regular.woff'
    ]
};

gulp.task('default', ['build']);
gulp.task('build', ['uglify', 'concat', 'copyCSSLibs', 'copyFontLibs', 'copyJSLibs', 'copyStatic', 'copyPartials']);

gulp.task('uglify', function () {
    return gulp.src(paths.scripts)
        .pipe(ngAnnotate())
        .pipe(uglify())
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

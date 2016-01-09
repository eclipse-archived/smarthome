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
    wiredep = require('wiredep').stream,
    browserSync = require('browser-sync'),
    browserSyncSpa = require('browser-sync-spa'),
    proxyMiddleware = require('http-proxy-middleware');

var paths = {
    indexFile: './web-src/index.html',
    indexTmpFile: './web-tmp/index.html',
    scripts: './web-src/js/**/*.js',
    styles: ['./web-src/css/style.scss'],
    images: ['./web-src/img/*'],
    partials: ['./web-src/partials/*.html'],
    FontLibs: [
        './web-src/bower_components/roboto-fontface/fonts/*',
        '!./web-src/bower_components/roboto-fontface/fonts/*.svg'
    ]
};

gulp.task('watch', ['inject'], function () {
    gulp.watch([paths.indexFile, 'bower.json'], ['inject']);

    gulp.watch(paths.styles, function(event) {
        if(event.type === 'changed') {
            gulp.start('styles');
        } else {
            gulp.start('inject');
        }
    });

    gulp.watch(paths.scripts, function(event) {
        if(event.type === 'changed') {
            gulp.start('scripts');
        } else {
            gulp.start('inject');
        }
    });

    gulp.watch(paths.partials, function(event) {
        browserSync.reload(event.path);
    });
    gulp.watch(paths.indexTmpFile, function(event) {
        browserSync.reload(event.path);
    });
});


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

browserSync.use(browserSyncSpa({
    selector: '[ng-app]'// Only needed for angular apps
}));

gulp.task('scripts', function () {
    return gulp.src(paths.scripts)
        .pipe(browserSync.reload({ stream: true }))
        .pipe(size())
});

gulp.task('styles', function () {
    return gulp.src(paths.styles)
        .pipe(sass({
            outputStyle: 'compressed'
        })).on('error', sass.logError)
        .pipe(rename(function (path) {
            path.basename += '.min';
            return path;
        }))
        .pipe(gulp.dest('./web-tmp/css/'))
        .pipe(browserSync.reload({ stream: true }));
});

gulp.task('inject', ['scripts', 'styles'], function () {
    var injectScripts = gulp.src(paths.scripts).pipe(angularFilesort());

    var injectOptions = {
        ignorePath: ['web-src'],
        addRootSlash: false
    };

    var wiredepConf = {
        directory: './web-src/bower_components'
    };

    return gulp.src(paths.indexFile)
        .pipe(inject(injectScripts, injectOptions))
        .pipe(wiredep(wiredepConf))
        .pipe(gulp.dest('./web-tmp/'));
});

gulp.task('build-main', ['inject'], function () {
    var htmlFilter = filter('*.html');
    var jsFilter = filter('**/*.js');
    var cssFilter = filter('**/*.css');
    var assets;

    return gulp.src(paths.indexTmpFile)
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

// These are the task you run from the CLI or via script

gulp.task('default', ['build']);

gulp.task('build', ['build-main', 'copyImgs', 'copyFonts', 'copyFontLibs', 'copyPartials']);

gulp.task('serve', ['watch'], function () {
    browserSyncInit(['./web-tmp', './web-src']);
});

gulp.task('serve:dist', ['build'], function () {
    browserSyncInit('./web');
});

gulp.task('clean', function () {
    return del(['./web-tmp','./web']);
});

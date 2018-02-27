(function() {
    'use strict';

    angular.module('PaperUI.bindings').config(function($routeProvider) {
        $routeProvider.when('/configuration/bindings', {
            templateUrl : 'partials/bindings/configuration.bindings.html',
            title : 'Configuration'
        }).when('/configuration/bindings/:bindingId', {
            templateUrl : 'partials/bindings/configuration.binding.html',
            controller : 'BindingDetailController',
            title : 'Configuration'
        })
    });

})()
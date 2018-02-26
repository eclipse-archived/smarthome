(function () {
  'use strict';

  angular.module('PaperUI.bindings').config([ '$routeProvider', function ($routeProvider) {
    $routeProvider.when('/configuration/bindings', {
      templateUrl: 'partials/bindings/configuration.bindings.html',
      controller: 'BindingController',
      title: 'Configuration'
    }).when('/configuration/bindings/:bindingId', {
      templateUrl: 'partials/bindings/configuration.binding.html',
      controller: 'BindingDetailController',
      title: 'Configuration'
    })
  } ]);

})()
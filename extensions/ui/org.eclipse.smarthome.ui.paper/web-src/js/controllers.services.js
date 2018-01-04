angular.module('PaperUI.controllers.configuration', [ 'PaperUI.constants', 'PaperUI.controllers.firmware', 'PaperUI.controllers.configurableServiceDialog' ]) //
.controller('ServicesController', function($scope, $mdDialog, $location, serviceConfigService, toastService) {
    $scope.setSubtitle([ 'Services' ]);
    $scope.setHeaderText('Shows all configurable services.');
    $scope.tabs = [];

    $scope.navigateTo = function(path) {
        $location.path('/configuration/services/' + path);
    }

    $scope.refresh = function() {
        serviceConfigService.getAll(function(services) {
            // $scope.services = services;
            var arrOfIndex = [];
            var index = 0;
            angular.forEach(services, function(value) {
                if (arrOfIndex[value.category] === undefined) {
                    arrOfIndex[value.category] = index++;
                }
                if ($scope.tabs[arrOfIndex[value.category]] === undefined) {
                    $scope.tabs[arrOfIndex[value.category]] = [];
                    $scope.tabs[arrOfIndex[value.category]].category = value.category;
                }
                $scope.tabs[arrOfIndex[value.category]].push(value);
            });
        });
    };

    $scope.configure = function(serviceId, configDescriptionURI, event) {
        $mdDialog.show({
            controller : 'ConfigurableServiceDialogController',
            templateUrl : 'partials/dialog.configureservice.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                serviceId : serviceId,
                configDescriptionURI : configDescriptionURI,
                multiple : false
            }
        });
    }

    $scope.refresh();
}).controller('MultiServicesController', function($scope, $mdDialog, $location, $routeParams, serviceConfigService, toastService) {
    $scope.setSubtitle([ 'Services' ]);
    $scope.setHeaderText('Shows all multiple configurable services.');
    $scope.servicePID = $routeParams.servicePID;
    $scope.serviceContexts = [];

    $scope.navigateTo = function(path) {
        $location.path('/configuration/services/' + path);
    }

    $scope.configure = function(serviceId, event) {
        $mdDialog.show({
            controller : 'ConfigurableServiceDialogController',
            templateUrl : 'partials/dialog.configureservice.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                serviceId : serviceId,
                configDescriptionURI : $scope.serviceConfigDescriptionURI,
                multiple : true
            }
        }).then(function() {
            $scope.refresh();
        });
    };

    $scope.deleteConfig = function(serviceContext, event) {
        $mdDialog.show({
            controller : 'ServiceConfigRemoveController',
            templateUrl : 'partials/dialog.remove.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                serviceContext : serviceContext
            }
        }).then(function() {
            $scope.refresh();
        });
    }

    serviceConfigService.getById({
        id : $scope.servicePID
    }, function(service) {
        $scope.serviceLabel = service.label;
        $scope.setSubtitle([ 'Services', service.label ]);
        $scope.serviceConfigDescriptionURI = service.configDescriptionURI;
    });

    $scope.refresh = function() {
        serviceConfigService.getContexts({
            id : $scope.servicePID
        }, function(serviceContexts) {
            $scope.serviceContexts = serviceContexts;
        });
    }

    $scope.refresh();
}).controller('ServiceConfigRemoveController', function($scope, $mdDialog, $filter, $location, toastService, serviceConfigService, serviceContext) {
    $scope.serviceContext = serviceContext;
    $scope.remove = function() {
        serviceConfigService.deleteConfig({
            id : serviceContext.id
        }, function() {
            toastService.showDefaultToast('Service config removed.');
        });
        $mdDialog.hide();
    }

    $scope.close = function() {
        $mdDialog.cancel();
    }
});

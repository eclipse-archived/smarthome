var configurableServiceDialogController = angular.module('PaperUI.controllers.configurableServiceDialog', [ 'PaperUI.services', 'PaperUI.services.rest', 'ngMaterial', 'PaperUI.directive.parameterDescription' ]);

configurableServiceDialogController.controller('ConfigurableServiceDialogController', function($scope, $mdDialog, configService, serviceConfigService, configDescriptionService, toastService, serviceId, configDescriptionURI) {

    var loadService = function(serviceId) {
        serviceConfigService.getById({
            id : serviceId
        }).$promise.then(function(service) {
            $scope.service = service;
        });
    }

    var createEmtpyConfig = function() {
        $scope.serviceId = '';
        $scope.configuration = {
            '' : ''
        };
        $scope.configArray = [];
    }

    var loadServiceConfiguration = function(serviceId, configDescriptionURI) {
        serviceConfigService.getConfigById({
            id : serviceId
        }).$promise.then(function(config) {
            if (config) {
                $scope.configuration = configService.convertValues(config);
                $scope.configArray = configService.getConfigAsArray($scope.configuration);
            }
        }).then(applyDefaults(configDescriptionURI));
    }

    var applyDefaults = function(configDescriptionURI) {
        if (!configDescriptionURI) {
            return;
        }

        configDescriptionService.getByUri({
            uri : configDescriptionURI
        }).$promise.then(function(configDescription) {
            if (configDescription) {
                $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
                $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
            }
        });
    }

    $scope.service = null;
    $scope.parameters = [];
    $scope.configuration = {};

    $scope.expertMode = serviceId ? false : true;
    $scope.newConfig = serviceId ? false : true;

    if (serviceId) {
        loadService(serviceId);
        loadServiceConfiguration(serviceId, configDescriptionURI);
    } else {
        createEmptyConfig();
    }

    $scope.close = function() {
        $mdDialog.hide();
    }

    $scope.addParameter = function() {
        $scope.configArray.push({
            name : '',
            value : undefined
        });
    }

    $scope.save = function() {
        var configuration = {};
        if ($scope.expertMode) {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
        var configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters, true);
        serviceConfigService.updateConfig({
            id : (serviceId ? serviceId : $scope.serviceId)
        }, configuration, function() {
            toastService.showDefaultToast('Service config updated.');
        });
        $mdDialog.hide();
    }

    $scope.$watch('expertMode', function() {
        if ($scope.expertMode) {
            $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);
        } else {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
    });

});
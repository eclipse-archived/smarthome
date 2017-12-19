angular.module('PaperUI.controllers.configuration.bindings', [ 'ngRoute', 'PaperUI.directive.searchField' ]).config([ '$routeProvider', function($routeProvider) {
    // Configure the routes for this controller
    $routeProvider.when('/configuration/bindings', {
        templateUrl : 'partials/configuration.bindings.html',
        controller : 'BindingController',
        title : 'Configuration'
    }).when('/configuration/bindings/:bindingId', {
        templateUrl : 'partials/configuration.binding.html',
        controller : 'BindingDetailController',
        title : 'Configuration'
    });
} ]).controller('BindingController', function($scope, $location, $mdDialog, bindingRepository, extensionService) {
    /**
     * This is the main binding controller to display all bindings
     */
    $scope.navigateTo = function(path) {
        $location.path(path);
    }

    $scope.bindings = [];
    $scope.extensionServiceAvailable = false;

    extensionService.isAvailable(function(available) {
        $scope.extensionServiceAvailable = available;
    })

    $scope.setSubtitle([ 'Bindings' ]);
    $scope.setHeaderText('Shows all installed bindings.');
    $scope.refresh = function() {
        bindingRepository.getAll(function(bindings) {
            $scope.bindings = bindings;
        }, true);
    };

    $scope.configure = function(binding, event) {
        event.stopPropagation();
        $mdDialog.show({
            controller : 'ConfigureBindingDialogController',
            templateUrl : 'partials/dialog.configurebinding.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                binding : binding
            }
        });
    }

    $scope.isConfigurable = function(binding) {
        return binding.configDescriptionURI ? true : false;
    }

    $scope.refresh();

}).controller('BindingDetailController', function($scope, $location, $mdExpansionPanel, $mdDialog, thingTypeRepository, bindingRepository, extensionService) {
    var bindingId = $scope.path[3];
    $scope.filter = {
        text : ''
    };

    $scope.navigateTo = function(path) {
        $location.path(path);
    }

    $scope.filterItems = function(lookupFields, searchText) {
        return function(item) {
            if (searchText && searchText.length > 0) {
                for (var i = 0; i < lookupFields.length; i++) {
                    if (item[lookupFields[i]] && item[lookupFields[i]].toUpperCase().indexOf(searchText.toUpperCase()) != -1) {
                        return true;
                    }
                }
                return false
            }
            return true;
        }
    }

    $scope.clearFilter = function(event) {
        if (!event || event.keyCode === 27) {
            $scope.filter.text = '';
        }
    }

    $scope.binding = undefined;
    bindingRepository.getOne(function(binding) {
        return binding.id === bindingId;
    }, function(binding) {
        $scope.setSubtitle([ 'Bindings', binding.name ]);
        $scope.setHeaderText('Shows detailed binding information.');

        $scope.binding = binding;
        $scope.binding.thingTypes = [];
        thingTypeRepository.getAll(function(thingTypes) {
            angular.forEach(thingTypes, function(thingType) {
                if (thingType.UID.split(':')[0] === binding.id) {
                    $scope.binding.thingTypes.push(thingType);
                }
            });
        });
    });

    $scope.configure = function(event) {
        $mdDialog.show({
            controller : 'ConfigureBindingDialogController',
            templateUrl : 'partials/dialog.configurebinding.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                binding : $scope.binding
            }
        });
    }

    $scope.isConfigurable = function() {
        return $scope.binding.configDescriptionURI ? true : false;
    }
}).controller('ConfigureBindingDialogController', function($scope, $mdDialog, bindingRepository, bindingService, configService, configDescriptionService, toastService, binding) {

    $scope.binding = binding;
    $scope.parameters = [];
    $scope.config = {};

    if (binding.configDescriptionURI) {
        $scope.expertMode = false;
        configDescriptionService.getByUri({
            uri : binding.configDescriptionURI
        }, function(configDescription) {
            if (configDescription) {
                $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
                $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
                $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);
            }
        });
    }
    if (binding) {
        bindingService.getConfigById({
            id : binding.id
        }).$promise.then(function(config) {
            $scope.configuration = configService.convertValues(config);
            $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
            $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);

        }, function(failed) {
            $scope.configuration = {};
            $scope.configArray = configService.getConfigAsArray($scope.configuration);
        });
    } else {
        $scope.newConfig = true;
        $scope.serviceId = '';
        $scope.configuration = {
            '' : ''
        };
        $scope.configArray = [];
        $scope.expertMode = true;
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
        if ($scope.expertMode) {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
        var configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters, true);
        bindingService.updateConfig({
            id : binding.id
        }, configuration, function() {
            $mdDialog.hide();
            toastService.showDefaultToast('Binding config updated.');
        });
    }
    $scope.$watch('expertMode', function() {
        if ($scope.expertMode) {
            $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);
        } else {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
    });
});
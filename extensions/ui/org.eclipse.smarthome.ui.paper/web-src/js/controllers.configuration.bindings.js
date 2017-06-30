angular.module('PaperUI.controllers.configuration.bindings', [ 'ngRoute' ]).config([ '$routeProvider', function($routeProvider) {
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
} ]).controller('BindingController', function($scope, $location, bindingRepository) {
    /**
     * This is the main binding controller to display all bindings
     */
    $scope.navigateTo = function(path) {
        $location.path(path);
    }

    $scope.setSubtitle([ 'Bindings' ]);
    $scope.setHeaderText('Shows all installed bindings.');
    $scope.refresh = function() {
        bindingRepository.getAll(true);
    };

    $scope.configure = function(bindingId, configDescriptionURI, event) {
        $mdDialog.show({
            controller : 'ConfigureBindingDialogController',
            templateUrl : 'partials/dialog.configurebinding.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                bindingId : bindingId,
                configDescriptionURI : configDescriptionURI
            }
        });
    }
    $scope.refresh();

}).controller('BindingDetailController', function($scope, $location, $mdExpansionPanel, thingTypeRepository, bindingRepository) {
    /**
     * This is the binding controller to display the binding detail
     */
    $scope.navigateTo = function(path) {
        $location.path(path);
    }

    // Function to allow filtering of thing-types linked to the search box
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

    var bindingId = $scope.path[3];

    $scope.searchText = "";
    $scope.binding = undefined;
    bindingRepository.getOne(function(binding) {
        return binding.id === bindingId;
    }, function(binding) {
        $scope.setSubtitle([ 'Bindings', binding.name ]);
        $scope.setHeaderText('Shows detailed binding information.');

        $scope.binding = binding;
        $scope.binding.thingTypes = [];
        thingTypeRepository.getAll(function(thingTypes) {
            $.each(thingTypes, function(index, thingType) {
                if (thingType.UID.split(':')[0] === binding.id) {
                    $scope.binding.thingTypes.push(thingType);
                }
            });
        });
    });

    $mdExpansionPanel().waitFor('bindingOverview').then(function(panel) {
        panel.expand();
    });
});
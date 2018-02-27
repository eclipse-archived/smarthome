(function() {
    'use strict';

    angular.module('PaperUI.bindings') //
    .directive('bindingsList', [ '$location', 'extensionService', '$mdDialog', 'bindingRepository', bindingsList ]);

    function bindingsList($location, extensionService, $mdDialog, bindingRepository) {
        return {
            restrict : 'E',
            scope : {},
            controllerAs : '$ctrl',
            templateUrl : 'partials/bindings/directive.bindings-list.html',
            controller : controller
        }

        function controller() {
            this.bindings = [];
            this.extensionServiceAvailable = false;

            this.navigateTo = navigateTo;
            this.refresh = refresh;
            this.configure = configure;
            this.isConfigurable = isConfigurable;

            extensionService.isAvailable(function(available) {
                this.extensionServiceAvailable = available;
            }.bind(this))

            this.refresh();

            // $scope.setSubtitle([ 'Bindings' ]);
            // $scope.setHeaderText('Shows all installed bindings.');

            function navigateTo(path) {
                $location.path(path);
            }

            function refresh() {
                bindingRepository.getAll(function(bindings) {
                    this.bindings = bindings;
                }.bind(this), true);
            }

            function configure(binding, event) {
                event.stopPropagation();
                $mdDialog.show({
                    controller : 'ConfigureBindingDialogController',
                    templateUrl : 'partials/bindings/dialog.configurebinding.html',
                    targetEvent : event,
                    hasBackdrop : true,
                    locals : {
                        binding : binding
                    }
                });
            }

            function isConfigurable(binding) {
                return binding.configDescriptionURI ? true : false;
            }
        }
    }

})()
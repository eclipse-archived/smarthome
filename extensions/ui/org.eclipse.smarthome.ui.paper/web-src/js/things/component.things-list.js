;
(function() {
    'use strict';

    angular.module('PaperUI.things').component('thingsList', {
        templateUrl : 'partials/things/component.things-list.html',
        controller : ThingsListController

    });

    ThingsListController.$inject = [ '$timeout', '$location', '$mdDialog', 'thingRepository', 'thingTypeRepository', 'bindingRepository', 'thingService', 'toastService' ];

    function ThingsListController() {
        var ctrl = this;

        $scope.setSubtitle([ 'Things' ]);
        $scope.setHeaderText('Shows all configured Things.');
        this.bindings = []; // used for the things filter
        this.thingTypes = [];
        this.things;

        this.refresh = refresh;
        this.navigateTo = navigateTo;
        this.getThingTypeLabel = getThingTypeLabel;
        this.remove = remove;
        this.clearAll = clearAll;

        this.$onInit = refresh;
        this.$onChanges = refreshBindings;

        function navigateTo(path) {
            if (path.startsWith("/")) {
                $location.path(path);
            } else {
                $location.path('configuration/things/' + path);
            }
        }

        function refresh() {
            return refreshThingTypes().then(function() {
                thingRepository.getAll(function(things) {
                    angular.forEach(things, function(thing) {
                        thing.bindingType = thing.thingTypeUID.split(':')[0];
                    })
                    ctrl.things = things;
                });
            });
        }

        function remove(thing, event) {
            event.stopImmediatePropagation();
            $mdDialog.show({
                controller : 'RemoveThingDialogController',
                templateUrl : 'partials/things/dialog.removething.html',
                targetEvent : event,
                hasBackdrop : true,
                locals : {
                    thing : thing
                }
            }).then(function() {
                ctrl.refresh();
            });
        }

        function getThingTypeLabel(key) {
            var thingType = ctrl.thingTypes[key]
            return thingType ? thingType.label : '';
        }
        ;

        function clearAll() {
            ctrl.searchText = "";
            ctrl.$broadcast("ClearFilters");
        }

        function refreshThingTypes() {
            return thingTypeRepository.getAll(function(thingTypes) {
                angular.forEach(thingTypes, function(thingType) {
                    ctrl.thingTypes[thingType.UID] = thingType;
                });
            });
        }

        function refreshBindings() {
            bindingRepository.getAll(function(bindings) {
                var filteredBindings = new Set();
                angular.forEach(ctrl.things, function(thing) {
                    var binding = bindings.filter(function(binding) {
                        return binding.id === thing.bindingType
                    })
                    filteredBindings.add(binding[0])
                })
                ctrl.bindings = Array.from(filteredBindings)
            }, true);
        }
    }

})()
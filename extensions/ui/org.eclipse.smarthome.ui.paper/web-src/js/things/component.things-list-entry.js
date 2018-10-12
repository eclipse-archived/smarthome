;
(function() {
    'use strict';

    angular.module('PaperUI.things').component('thingEntry', {
        bindings : {
            thing : '<'
        },
        templateUrl : 'partials/things/component.things-list-entry.html',
        controller : ThingEntryController

    });

    ThingEntryController.$inject = [ '$location', '$timeout', '$mdDialog', 'thingTypeRepository', 'eventService' ];

    function ThingEntryController($location, $timeout, $mdDialog, thingTypeRepository, eventService) {
        var ctrl = this;
        this.thingTypes = [];

        this.getThingTypeLabel = getThingTypeLabel;
        this.navigateTo = navigateTo;
        this.remove = remove;

        this.$onInit = activate;
        this.$onDestroy = dispose;

        function activate() {
            eventService.onEvent('smarthome/things/' + ctrl.thing.UID + '/statuschanged', function(topic, statusInfo) {
                $timeout(function() {
                    ctrl.thing.statusInfo = statusInfo[0];
                }, 0);
            });

            return refreshThingTypes();
        }

        function dispose() {
            eventService.removeListener('smarthome/things/' + ctrl.thing.UID + '/statuschanged');
        }

        function navigateTo(path) {
            if (path.startsWith("/")) {
                $location.path(path);
            } else {
                $location.path('configuration/things/' + path);
            }
        }

        function getThingTypeLabel() {
            var thingType = ctrl.thingTypes[ctrl.thing.thingTypeUID]
            return thingType ? thingType.label : '';
        }

        function refreshThingTypes() {
            return thingTypeRepository.getAll(function(thingTypes) {
                angular.forEach(thingTypes, function(thingType) {
                    ctrl.thingTypes[thingType.UID] = thingType;
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
            });
        }
    }

})()
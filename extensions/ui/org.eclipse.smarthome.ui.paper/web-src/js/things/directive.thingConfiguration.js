;
(function() {
    'use strict';

    angular.module('PaperUI.things').directive('thingConfiguration', ThingConfiguration);

    function ThingConfiguration() {
        return {
            restrict : 'E',
            scope : {},
            bindToController : {
                thing : '=',
                isEditing : '=?',
                form : '=?'
            },
            controller : ThingConfigurationController,
            controllerAs : '$ctrl',
            templateUrl : 'partials/things/directive.thingConfiguration.html'
        }
    }

    ThingConfigurationController.$inject = [ '$q', 'thingTypeService', 'thingRepository' ];

    function ThingConfigurationController($q, thingTypeService, thingRepository) {
        var ctrl = this;

        this.bridges = [];
        this.needsBridge = false;
        this.hasBridge = hasBridge;
        this.createBridge = createBridge;

        this.$onInit = activate;

        function activate() {
            return $q(function() {
                if (ctrl.thing.thingTypeUID) {
                    getThingType(ctrl.thing.thingTypeUID);
                }
            });
        }

        function hasBridge() {
            return ctrl.bridges && ctrl.bridges.length > 0;
        }

        function createBridge() {

        }

        function refreshBridges(supportedBridgeTypeUIDs) {
            thingRepository.getAll(function(things) {
                ctrl.bridges = things.filter(function(thing) {
                    return supportedBridgeTypeUIDs.includes(thing.thingTypeUID)
                })
            });
        }

        function getThingType(thingTypeUID) {
            thingTypeService.getByUid({
                thingTypeUID : thingTypeUID
            }).$promise.then(function(thingType) {
                if (thingType.supportedBridgeTypeUIDs && thingType.supportedBridgeTypeUIDs.length > 0) {
                    ctrl.needsBridge = true;
                    refreshBridges(thingType.supportedBridgeTypeUIDs);
                } else {
                    ctrl.needsBridge = false;
                }
            });
        }
    }

})();

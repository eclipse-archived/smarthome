;
(function() {
    'use strict';

    angular.module('PaperUI.things').component('thingStatus', {
        bindings : {
            statusInfo : '<'
        },
        templateUrl : 'partials/things/component.thing.statusInfo.html',
        controller : ThingStatusInfoController

    });

    function ThingStatusInfoController() {
        var ctrl = this;

        this.$onChanges = onChanges;

        function onChanges(changes) {
            if (changes.statusInfo) {
                console.log(changes.statusInfo);
            }
        }

    }

})()

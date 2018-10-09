;
(function() {
    'use strict';

    angular.module('PaperUI.things').component('thingStatus', {
        bindings : {
            statusInfo : '<'
        },
        templateUrl : 'partials/things/component.thing.statusInfo.html'
    });
})()

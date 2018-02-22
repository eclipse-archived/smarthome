;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('itemCommandOptions', {
        bindings : {
            itemName : '<',
            channel : '<'
        },
        templateUrl : 'partials/control/component.control.commandOptions.html',
        controller : CommandOptionsController
    });

    CommandOptionsController.$inject = [ 'itemService' ];

    function CommandOptionsController(itemService) {
        var ctrl = this;
        this.commandOptions = [];

        this.sendCommand = sendCommand;

        this.$onInit = activate;

        function activate() {
            if (ctrl.channel.channelType) {
                ctrl.commandOptions = ctrl.channel.channelType.commandOptions;
            }
        }

        function sendCommand(command) {
            itemService.sendCommand({
                itemName : ctrl.itemName
            }, command);
        }
    }
})()

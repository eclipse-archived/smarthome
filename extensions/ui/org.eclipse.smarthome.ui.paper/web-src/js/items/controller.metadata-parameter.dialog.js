;
(function() {
    'use strict';

    angular.module('PaperUI.items').controller('ConfigurableMetadataDialogController', ConfigurableMetadataDialogController);

    // ConfigurableMetadataDialogController.$inject([ 'configDescriptionService' ]);

    function ConfigurableMetadataDialogController($mdDialog, configDescriptionService, configService, metadata, configDescription) {
        var ctrl = this;
        this.metadata = metadata;
        this.configDescription = configDescription;

        this.parameterGroups = [];
        this.configuration = metadata.config;
        this.configArray = [];
        this.expertMode = false;

        this.cancel = cancel;
        this.close = close;
        this.addParameter = addParameter;

        activate();

        function activate() {
            ctrl.parameterGroups = configService.getRenderingModel(ctrl.configDescription.parameters, ctrl.configDescription.parameterGroups);
            ctrl.configuration = configService.setConfigDefaults(ctrl.configuration, ctrl.parameterGroups);
        }

        function cancel() {
            $mdDialog.hide();
        }

        function addParameter() {
            ctrl.configArray.push({
                name : '',
                value : undefined
            });
        }

        function close() {
            if (ctrl.expertMode) {
                ctrl.configuration = configService.getConfigAsObject(ctrl.configArray, ctrl.parameterGroups);
            }
            ctrl.metadata.config = configService.setConfigDefaults(ctrl.configuration, ctrl.parameterGroups, true);
            $mdDialog.hide();
        }
    }

})();
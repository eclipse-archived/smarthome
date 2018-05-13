;
(function() {
    'use strict';

    angular.module('PaperUI.items').controller('ConfigurableMetadataDialogController', ConfigurableMetadataDialogController);

    // ConfigurableMetadataDialogController.$inject([ 'configDescriptionService' ]);

    function ConfigurableMetadataDialogController($mdDialog, configDescriptionService, metadata, configDescription) {
        var ctrl = this;
        this.metadata = metadata;
        this.configDescription = configDescription;

        this.parameters = [];
        this.configuration = {};
        this.configArray = [];
        this.expertMode = false;

        this.close = close;
        this.save = save;
        this.addParameter = addParameter;

        function close() {
            $mdDialog.hide();
        }

        function addParameter() {
            ctrl.configArray.push({
                name : '',
                value : undefined
            });
        }

        function save() {
            var configuration = {};
            if ($scope.expertMode) {
                $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
            }
            var configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters, true);
            serviceConfigService.updateConfig({
                id : (serviceId ? serviceId : $scope.serviceId)
            }, configuration, function() {
                toastService.showDefaultToast('Service config updated.');
            });
            $mdDialog.hide();
        }
    }

})();
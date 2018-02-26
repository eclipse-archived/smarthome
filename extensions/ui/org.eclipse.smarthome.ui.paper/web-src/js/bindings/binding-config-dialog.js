(function () {
  'use strict';

  angular //
  .module('PaperUI.bindings') //
  .controller('ConfigureBindingDialogController')

  function ConfigureBindingDialogController ($scope, $mdDialog, bindingRepository, bindingService, configService, configDescriptionService, toastService, binding) {

    $scope.binding = binding;
    $scope.parameters = [];
    $scope.config = {};

    if (binding.configDescriptionURI) {
      $scope.expertMode = false;
      configDescriptionService.getByUri({
        uri: binding.configDescriptionURI
      }, function (configDescription) {
        if (configDescription) {
          $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
          $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
          $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);
        }
      });
    }
    if (binding) {
      bindingService.getConfigById({
        id: binding.id
      }).$promise.then(function (config) {
        $scope.configuration = configService.convertValues(config);
        $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
        $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);

      }, function (failed) {
        $scope.configuration = {};
        $scope.configArray = configService.getConfigAsArray($scope.configuration);
      });
    } else {
      $scope.newConfig = true;
      $scope.serviceId = '';
      $scope.configuration = {
        '': ''
      };
      $scope.configArray = [];
      $scope.expertMode = true;
    }
    $scope.close = function () {
      $mdDialog.hide();
    }
    $scope.addParameter = function () {
      $scope.configArray.push({
        name: '',
        value: undefined
      });
    }
    $scope.save = function () {
      if ($scope.expertMode) {
        $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
      }
      var configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters, true);
      bindingService.updateConfig({
        id: binding.id
      }, configuration, function () {
        $mdDialog.hide();
        toastService.showDefaultToast('Binding config updated.');
      });
    }
    $scope.$watch('expertMode', function () {
      if ($scope.expertMode) {
        $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);
      } else {
        $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
      }
    });
  }
})()

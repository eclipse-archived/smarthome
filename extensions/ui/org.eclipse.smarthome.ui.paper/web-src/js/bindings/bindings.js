(function () {
  'use strict';

  angular //
  .module('PaperUI.bindings') //
  .controller('BindingController', BindingController)

  function BindingController ($scope, $location, $mdDialog, bindingRepository, extensionService) {
    /**
     * This is the main binding controller to display all bindings
     */
    $scope.navigateTo = function (path) {
      $location.path(path);
    }

    $scope.bindings = [];
    $scope.extensionServiceAvailable = false;

    extensionService.isAvailable(function (available) {
      $scope.extensionServiceAvailable = available;
    })

    $scope.setSubtitle([ 'Bindings' ]);
    $scope.setHeaderText('Shows all installed bindings.');
    $scope.refresh = function () {
      bindingRepository.getAll(function (bindings) {
        $scope.bindings = bindings;
      }, true);
    };

    $scope.configure = function (binding, event) {
      event.stopPropagation();
      $mdDialog.show({
        controller: 'ConfigureBindingDialogController',
        templateUrl: 'partials/bindings/dialog.configurebinding.html',
        targetEvent: event,
        hasBackdrop: true,
        locals: {
          binding: binding
        }
      });
    }

    $scope.isConfigurable = function (binding) {
      return binding.configDescriptionURI ? true : false;
    }

    $scope.refresh();

  }
})()

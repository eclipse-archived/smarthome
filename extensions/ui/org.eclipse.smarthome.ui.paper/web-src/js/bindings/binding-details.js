(function () {
  'use strict';

  angular //
  .module('PaperUI.bindings') //
  .controller('BindingDetailController', BindingDetailController)

  function BindingDetailController ($scope, $location, $mdExpansionPanel, $mdDialog, thingTypeRepository, bindingRepository, extensionService) {
    var bindingId = $scope.path[3];
    $scope.filter = {
      text: ''
    };

    $scope.navigateTo = function (path) {
      $location.path(path);
    }

    $scope.filterItems = function (lookupFields, searchText) {
      return function (item) {
        if (searchText && searchText.length > 0) {
          for (var i = 0; i < lookupFields.length; i++) {
            if (item[lookupFields[i]] && item[lookupFields[i]].toUpperCase().indexOf(searchText.toUpperCase()) != -1) {
              return true;
            }
          }
          return false
        }
        return true;
      }
    }

    $scope.clearFilter = function (event) {
      if (!event || event.keyCode === 27) {
        $scope.filter.text = '';
      }
    }

    $scope.binding = undefined;
    bindingRepository.getOne(function (binding) {
      return binding.id === bindingId;
    }, function (binding) {
      $scope.setSubtitle([ 'Bindings', binding.name ]);
      $scope.setHeaderText('Shows detailed binding information.');

      $scope.binding = binding;
      $scope.binding.thingTypes = [];
      thingTypeRepository.getAll(function (thingTypes) {
        angular.forEach(thingTypes, function (thingType) {
          if (thingType.UID.split(':')[0] === binding.id) {
            $scope.binding.thingTypes.push(thingType);
          }
        });
      });
    });

    $scope.configure = function (event) {
      $mdDialog.show({
        controller: 'ConfigureBindingDialogController',
        templateUrl: 'partials/bindings/dialog.configurebinding.html',
        targetEvent: event,
        hasBackdrop: true,
        locals: {
          binding: $scope.binding
        }
      });
    }

    $scope.isConfigurable = function () {
      return $scope.binding.configDescriptionURI ? true : false;
    }
  }
})()

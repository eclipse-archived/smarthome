angular.module('PaperUI.controllers.extension', [ 'PaperUI.constants' ]).controller('ExtensionPageController', function($scope, extensionService, bindingRepository, thingTypeRepository, eventService, toastService, $filter) {
    $scope.navigateTo = function(path) {
        $location.path('extensions/' + path);
    };
    $scope.extensionTypes = [];
    $scope.refresh = function() {
        extensionService.getAllTypes(function(extensionTypes) {
            $scope.extensionTypes = [];
            angular.forEach(extensionTypes, function(extensionType) {
                $scope.extensionTypes.push({
                    typeId : extensionType.id,
                    label : extensionType.label,
                    extensions : [],
                    inProgress : false
                });
            });
            extensionService.getAll(function(extensions) {
                angular.forEach(extensions, function(extension) {
                    var extensionType = $scope.getType(extension.type);
                    if (extensionType !== undefined) {
                        extensionType.extensions.push(extension);
                    }
                });
                angular.forEach($scope.extensionTypes, function(extensionType) {
                    extensionType.extensions = $filter('orderBy')(extensionType.extensions, "label")
                });
            });
        });
    }

    $scope.getType = function(extensionTypeId) {
        var result;
        angular.forEach($scope.extensionTypes, function(extensionType) {
            if (extensionType.typeId === extensionTypeId) {
                result = extensionType;
            }
        });
        return result;
    };
    $scope.getExtension = function(extensionId) {
        var result;
        angular.forEach($scope.extensionTypes, function(extensionType) {
            angular.forEach(extensionType.extensions, function(extension) {
                if (extension.id === extensionId) {
                    result = extension;
                }
            });
        });
        return result;
    };
    $scope.refresh();
    $scope.install = function(extensionId) {
        var extension = $scope.getExtension(extensionId);
        extension.inProgress = true;
        extensionService.install({
            id : extensionId
        });
        bindingRepository.setDirty(true);
        thingTypeRepository.setDirty(true);
    };
    $scope.uninstall = function(extensionId) {
        var extension = $scope.getExtension(extensionId);
        extension.inProgress = true;
        extensionService.uninstall({
            id : extensionId
        });
        bindingRepository.setDirty(true);
        thingTypeRepository.setDirty(true);
    };
    eventService.onEvent('smarthome/extensions/*', function(topic, extensionId) {
        var extension = $scope.getExtension(extensionId);
        if (extension) {
            extension.inProgress = false;
            if (topic.indexOf("uninstalled") > -1) {
                extension.installed = false;
                toastService.showDefaultToast('Extension ' + extension.label + ' uninstalled.');
            } else if (topic.indexOf("installed") > -1) {
                extension.installed = true;
                toastService.showDefaultToast('Extension ' + extension.label + ' installed.');
            } else {
                toastService.showDefaultToast('Install or uninstall of extension ' + extension.label + ' failed.');
            }
        }
    });
});

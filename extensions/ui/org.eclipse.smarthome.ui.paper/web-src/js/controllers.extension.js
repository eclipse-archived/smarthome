angular.module('PaperUI.controllers.extension', [ 'PaperUI.constants' ]).controller('ExtensionPageController', function($scope, extensionService, bindingRepository, thingTypeRepository, eventService, toastService, $filter, $window, $timeout, $location, templateRepository) {
    $scope.navigateTo = function(path) {
        $location.path('extensions/' + path);
    };
    $scope.extensionTypes = [];
    var view = window.localStorage.getItem('paperui.extension.view')
    $scope.showCards = view ? view.toUpperCase() == 'LIST' ? false : true : false;
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

    $scope.changeView = function(showCards) {
        if (showCards) {
            window.localStorage.setItem('paperui.extension.view', 'card');
        } else {
            window.localStorage.setItem('paperui.extension.view', 'list');
        }
        $scope.showCards = showCards;
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
    $scope.openExternalLink = function(link) {
        if (link) {
            $window.open(link, '_blank');
        }
    }

    $scope.masonry = function(showCards) {
        if (showCards) {
            $timeout(function() {
                var itemContainer = '#extensions-' + ($scope.selectedIndex ? $scope.selectedIndex : 0);
                new Masonry(itemContainer, {});
            }, 100, true);
        }
    }
    $scope.$on('ngRepeatFinished', function(ngRepeatFinishedEvent) {
        $scope.masonry(true);
    });

    eventService.onEvent('smarthome/extensions/*', function(topic, extensionObject) {
        var id = extensionObject;
        if (extensionObject && Array.isArray(extensionObject)) {
            id = extensionObject[0]
        }
        var extension = $scope.getExtension(id);
        if (extension) {
            extension.inProgress = false;
            if (topic.indexOf("uninstalled") > -1) {
                extension.installed = false;
                toastService.showDefaultToast('Extension ' + extension.label + ' uninstalled.');
            } else if (topic.indexOf("installed") > -1) {
                extension.installed = true;
                toastService.showDefaultToast('Extension ' + extension.label + ' installed.');
                if (extension.type == "ruletemplate") {
                    $scope.$broadcast("RuleExtensionInstalled", extension.id);
                }
            } else {
                var msg = Array.isArray(extensionObject) ? extensionObject[1] : 'Install or uninstall of extension ' + extension.label + ' failed.';
                toastService.showDefaultToast(msg);
                $scope.$broadcast("RuleExtensionFailed");
            }
        }
    });
});

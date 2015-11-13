angular.module('PaperUI.controllers.extension', ['PaperUI.constants']).controller('ExtensionPageController', function($scope, 
        extensionService, eventService, toastService) {
	$scope.navigateTo = function(path) {
		$location.path('extensions/' + path);
    };
    $scope.extensionTypes = [];
    $scope.extensionTypeLabels = {};
    $scope.refresh = function() {
        extensionService.getAll(function(extensions) {
            $scope.extensionTypes = [];
        	angular.forEach(extensions, function(extension) {
        	    var extensionType = $scope.getType(extension.type);
        	    if(extensionType) {
        	        extensionType.extensions.push(extension);
        	    } else {
        	        extensionType = {typeId: extension.type, extensions: [extension], inProgress: false};
        	        $scope.extensionTypes.push(extensionType);
        	    }
        	});
        });
        extensionService.getAllTypes(function(extensionTypes) {
            $scope.extensionTypeLabels = {};
            angular.forEach(extensionTypes, function(extensioType) {
                $scope.extensionTypeLabels[extensioType.id] = extensioType.label;
            });
        });
    }
    
    $scope.getType = function(extensionTypeId) {
        var result;
        angular.forEach($scope.extensionTypes, function(extensionType) {
            if(extensionType.typeId === extensionTypeId) {
                result = extensionType;
            }
        });
        return result;
    };
    $scope.getExtension = function(extensionId) {
        var result;
        angular.forEach($scope.extensionTypes, function(extensionType) {
            angular.forEach(extensionType.extensions, function(extension) {
                if(extension.id === extensionId) {
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
        extensionService.install({id: extensionId});
    };
    $scope.uninstall = function(extensionId) {
        var extension = $scope.getExtension(extensionId);
        extension.inProgress = true;
        extensionService.uninstall({id: extensionId});
    };
    eventService.onEvent('smarthome/extensions/*', function(topic, extensionId) {
        var extension = $scope.getExtension(extensionId);
        if(extension) {
            extension.inProgress = false;
            if(topic.indexOf("uninstalled") > -1) {
                extension.installed = false;
                toastService.showDefaultToast('Extension ' + extension.label + ' uninstalled.');
            } else if(topic.indexOf("installed") > -1) {
                extension.installed = true;
                toastService.showDefaultToast('Extension ' + extension.label + ' installed.');
            } else {
                toastService.showDefaultToast('Install or uninstall of extension ' + extension.label + ' failed.');
            }
        }
    });
});
'use strict';

angular.module('PaperUI.controllers.rules').controller('addModuleDialogController', function($rootScope, $scope, $mdDialog, moduleTypeService, sharedProperties, $filter, configService, module, ruleID, type) {

    var objectFilter = $filter('filter');

    $scope.moduleData = moduleTypeService.getByType({
        mtype : type
    });
    $scope.id = module.id;
    $scope.type = type;
    $scope.description = '';
    $scope.module = '';
    $scope.step = 1;
    $scope.editMode = false;
    $scope.configuration = {};

    function setConfigurations() {

        $scope.moduleData.$promise.then(function(data) {
            var params = filterByUid(data, $scope.module);
            var res = configService.getRenderingModel(params[0].configDescriptions);
            angular.forEach(res, function(value) {
                sharedProperties.updateParams(value);
            });
        });

        var index = sharedProperties.searchArray(sharedProperties.getModuleArray(type), $scope.id);
        if (index != -1) {
            $scope.configuration = sharedProperties.getModuleArray(type)[index].configuration;
            $scope.configArray = configService.getConfigAsArray($scope.configuration);
        }
    }

    if ($scope.id) {
        $scope.editMode = true;
        $scope.module = module.type;
        $scope.name = module.label;
        $scope.description = module.description;
        setConfigurations();
        $scope.step = 2;
    }

    $scope.parameters = sharedProperties.getParams();

    $scope.close = function() {
        sharedProperties.resetParams();
        $mdDialog.hide();
    };

    $scope.saveModule = function() {
        var tempModule = filterByUid($scope.moduleData, $scope.module);
        if (tempModule != null && tempModule.length > 0) {
            tempModule[0].label = $scope.name;
            var obj = {
                id : $scope.id,
                label : $scope.name,
                description : $scope.description,
                type : tempModule[0].uid,
                configuration : $scope.configuration
            };
            sharedProperties.updateModule($scope.type, obj);
        }
        sharedProperties.resetParams();
        $mdDialog.hide();
    };

    $scope.deleteModule = function(opt) {
        sharedProperties.removeFromArray(opt, $scope.id);
        sharedProperties.resetParams();
        $mdDialog.hide();
    };

    $scope.secondStep = function() {
        var tempModule = filterByUid($scope.moduleData, $scope.module);
        if (tempModule != null && tempModule.length > 0) {
            $scope.name = tempModule[0].label;
            $scope.description = tempModule[0].description;
            setConfigurations();
            $scope.step = 2;
        }

    };

    function filterByUid(data, uid) {
        return objectFilter(data, {
            uid : uid
        });
    }

});
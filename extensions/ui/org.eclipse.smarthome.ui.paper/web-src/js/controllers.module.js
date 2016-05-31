'use strict';

angular.module('PaperUI.controllers.rules').controller('addModuleDialogController', function($rootScope, $scope, $mdDialog, moduleTypeService, sharedProperties, $filter, configService, module, ruleID, type) {

    var objectFilter = $filter('filter');
    $scope.moduleData;
    moduleTypeService.getAll().$promise.then(function(data) {
        $scope.moduleData = objectFilter(data, {
            visibility : 'VISIBLE'
        });
        if ($scope.id) {
            setConfigurations();
        }
    });
    $scope.id = module.id;
    $scope.type = type;
    $scope.description = '';
    $scope.module = '';
    $scope.step = 1;
    $scope.editMode = false;
    $scope.configuration = {};
    var originalConfiguration = {};
    $scope.items = [];

    $scope.selectChip = function(chip, textAreaName) {
        var textArea = $("textarea[name=" + textAreaName + "]")[0];
        var textBefore = textArea.value.substring(0, textArea.selectionStart);
        var textAfter = textArea.value.substring(textArea.selectionStart, textArea.value.length);
        $scope.configuration[textAreaName] = textBefore + chip.name + textAfter;
    }

    function setConfigurations() {
        if ($scope.moduleData) {
            var params = filterByUid($scope.moduleData, $scope.module);
            var res = configService.getRenderingModel(params[0].configDescriptions);
            var hasScript = false;
            angular.forEach(res, function(value) {
                sharedProperties.updateParams(value);
                hasScript = $.grep(value.parameters, function(parameter) {
                    return parameter.context == 'script';
                }).length > 0;
            });

            var index = sharedProperties.searchArray(sharedProperties.getModuleArray(type), $scope.id);
            if (index != -1) {
                $scope.configuration = configService.convertValues(sharedProperties.getModuleArray(type)[index].configuration);

                angular.copy($scope.configuration, originalConfiguration);
                $scope.configArray = configService.getConfigAsArray($scope.configuration);
                if (hasScript && type != 'trigger') {
                    var triggers = sharedProperties.getModuleArray('trigger');
                    angular.forEach(triggers, function(trigger, i) {
                        var moduleType = filterByUid($scope.moduleData, trigger.type);
                        $scope.items = $scope.items.concat(moduleType[0].outputs);
                    });
                    if (type == 'action') {
                        var actions = sharedProperties.getModuleArray('action');
                        for (var i = 0; i < sharedProperties.searchArray(actions, $scope.id); i++) {
                            var moduleType = filterByUid($scope.moduleData, actions[i].type);
                            $scope.items = $scope.items.concat(moduleType[0].outputs);
                        }
                    }
                }
            }
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
        var index = sharedProperties.searchArray(sharedProperties.getModuleArray(type), $scope.id);
        if (index != -1) {
            sharedProperties.getModuleArray(type)[index].configuration = originalConfiguration;
        }
        $mdDialog.hide();
    };

    $scope.saveModule = function() {
        var tempModule = filterByUid($scope.moduleData, $scope.module);
        if (tempModule != null && tempModule.length > 0) {
            tempModule[0].label = $scope.name;
            $scope.configuration = configService.replaceEmptyValues($scope.configuration);
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

}).directive('mdChips', function() {
    return {
        restrict : 'E',
        require : 'mdChips',
        link : function(scope, element, attributes, ctrl) {
            setTimeout(deferListeners, 500);
            function deferListeners() {
                var chipContents = element[0].getElementsByClassName('md-chip-content');
                for (var i = 0; i < chipContents.length; i++) {
                    chipContents[i].addEventListener("blur", function() {
                        ctrl.$scope.$apply();
                    });
                }
            }
        }
    }
});
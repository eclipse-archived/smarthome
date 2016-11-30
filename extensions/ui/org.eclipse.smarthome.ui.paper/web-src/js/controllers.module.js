'use strict';

angular.module('PaperUI.controllers.rules').controller('addModuleDialogController', function($rootScope, $scope, $mdDialog, moduleTypeService, sharedProperties, $filter, configService, module, ruleID, type) {

    var objectFilter = $filter('filter');
    $scope.moduleData;
    $scope.triggerData;

    getModulesByType(type, type == 'trigger' ? setConfigurations : null);

    function getModulesByType(mtype, callback) {
        moduleTypeService.getByType({
            mtype : mtype
        }).$promise.then(function(data) {
            var modules = objectFilter(data, {
                visibility : 'VISIBLE'
            });
            if (mtype != 'trigger' || type == 'trigger') {
                $scope.moduleData = modules;
            }
            if (callback) {
                $scope.triggerData = modules;
                if ($scope.module) {
                    callback();
                }
            } else {
                getModulesByType('trigger', setConfigurations);
            }

        });
    }
    $scope.id = module.id;
    $scope.type = type;
    $scope.description = '';
    $scope.module = '';
    $scope.step = 1;
    $scope.editMode = false;
    $scope.configuration = {};
    $scope.parameters = [];
    var originalConfiguration = {};
    $scope.items = [];

    $scope.selectChip = function(chip, textAreaName) {
        var textArea = $("textarea[name=" + textAreaName + "]")[0];
        var textBefore = textArea.value.substring(0, textArea.selectionStart);
        var textAfter = textArea.value.substring(textArea.selectionStart, textArea.value.length);
        $scope.configuration[textAreaName] = textBefore + chip.name + textAfter;
    }

    var setConfigurations = function() {
        if ($scope.moduleData) {
            var params = filterByUid($scope.moduleData, $scope.module);
            if (params && params.length > 0) {
                $scope.parameters = configService.getRenderingModel(params[0].configDescriptions);
            }
            var hasScript = false;
            angular.forEach($scope.parameters, function(value) {

                sharedProperties.updateParams(value);
                hasScript = $.grep(value.parameters, function(parameter) {
                    return parameter.context == 'script';
                }).length > 0;
            });

            var index = sharedProperties.searchArray(sharedProperties.getModuleArray(type), $scope.id);
            if (index != -1) {
                $scope.configuration = configService.convertValues(sharedProperties.getModuleArray(type)[index].configuration);
                angular.copy($scope.configuration, originalConfiguration);
            }
            $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
            if (hasScript && type != 'trigger') {
                var triggers = sharedProperties.getModuleArray('trigger');
                angular.forEach(triggers, function(trigger, i) {
                    var moduleType = filterByUid($scope.triggerData, trigger.type);
                    if (moduleType && moduleType.length > 0) {
                        $scope.items = $scope.items.concat(moduleType[0].outputs);
                    }
                });
                if (type == 'action') {
                    var actions = sharedProperties.getModuleArray('action');
                    for (var i = 0; i < sharedProperties.searchArray(actions, $scope.id); i++) {
                        var moduleType = filterByUid($scope.moduleData, actions[i].type);
                        if (moduleType[0] && moduleType[0].outputs && moduleType[0].outputs.length > 0) {
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
            var configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters, true);
            configuration = configService.replaceEmptyValues(configuration);
            var obj = {
                id : $scope.id,
                label : $scope.name,
                description : $scope.description,
                type : tempModule[0].uid,
                configuration : configuration
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
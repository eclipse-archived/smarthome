'use strict';

angular.module('PaperUI.controllers.rules', [ 'PaperUI.controllers.extension' ]).controller('RulesPageController', function($scope, $location, $mdDialog, toastService, $timeout, templateRepository) {
    $scope.navigateTo = function(path) {
        $location.path('rules/' + path);
    };
    $scope.inProgress = false;

    $scope.openDialog = function(ctrl, url, params) {
        $mdDialog.show({
            controller : ctrl,
            templateUrl : url,
            targetEvent : params.event,
            hasBackdrop : true,
            locals : params
        });
    };

    $scope.getRuleJSON = function(sharedProperties, uid, name, desc) {
        var rule = {
            tags : [],
            conditions : sharedProperties.getModuleJSON('condition'),
            description : desc,
            name : name,
            triggers : sharedProperties.getModuleJSON('trigger'),
            configDescriptions : [],
            actions : sharedProperties.getModuleJSON('action')
        };

        if (uid) {
            rule.uid = uid;
        }

        return rule;
    }
    $scope.installRuleExtenstion = function(id, installed) {
        if (!installed) {
            $scope.install(id);
            $timeout(function() {
                $scope.inProgress = false;
            }, 30000);
            $scope.inProgress = true;
        } else {
            fetchTemplate(id);
        }
    }
    $scope.$on("RuleExtensionFailed", function() {
        $scope.inProgress = false;
    })

    $scope.$on("RuleExtensionInstalled", function(event, id) {
        fetchTemplate(id);
    })

    function fetchTemplate(id) {
        templateRepository.getOne(function(template) {
            return template.uid == id;
        }, function(template) {
            if (template) {
                $location.path("rules/template/" + template.uid);
            } else {
                toastService.showDefaultToast('Rule template could not be found.');
                $scope.inProgress = false;
            }
        });
    }
}).controller('RulesController', function($scope, $timeout, ruleRepository, ruleService, toastService, extensionService, sharedProperties) {
    $scope.setHeaderText('Shows all rules.');
    $scope.ruleOptions = [ 'New rule', 'Rule from template' ];
    $scope.refresh = function(force) {
        ruleRepository.getAll(null, force);
        extensionService.getAll(function(extensions) {
            var hasRuleExtensions = $.grep(extensions, function(extension) {
                return extension.type == "ruletemplate";
            }).length > 0;
            if (hasRuleExtensions) {
                $scope.ruleOptions.push('Rule from catalog');
            }
        });
    };

    $scope.configure = function(rule) {
        $scope.navigateTo('configure/' + rule.uid);
    };

    $scope.remove = function(rule, e) {
        e.stopImmediatePropagation();
        $scope.openDialog('RuleRemoveController', 'partials/dialog.remove.html', {
            event : e,
            rule : rule
        });
    };

    $scope.refresh(false);

    $scope.removePart = function(opt, id) {
        sharedProperties.removeFromArray(opt, id);
    };
    $scope.toggleEnabled = function(rule, e) {
        e.stopImmediatePropagation();
        ruleService.setEnabled({
            ruleUID : rule.uid
        }, (!rule.enabled).toString(), function() {
            if (!rule.enabled) {
                toastService.showDefaultToast('Rule disabled.');
            } else {
                toastService.showDefaultToast('Rule enabled.');
            }
        });
    };

    $scope.ruleOptionSelected = function(event, value) {
        if (value == 0) {
            $scope.navigateTo('new');
        } else if (value == 1) {
            $scope.openDialog('TemplateDialogController', 'partials/dialog.ruletemplate.html', {
                event : event
            });
        } else {
            $scope.navigateTo('catalog');
        }
    };
    $scope.runRule = function(ruleUID, e) {
        e.stopImmediatePropagation();
        ruleService.runRule({
            ruleUID : ruleUID
        }, function(response) {
            toastService.showDefaultToast('Rule executed.');
        });
    }
}).controller('NewRuleController', function($scope, itemRepository, ruleService, ruleRepository, toastService, $mdDialog, sharedProperties, moduleTypeService) {
    $scope.setSubtitle([ 'New Rule' ]);
    itemRepository.getAll();
    sharedProperties.reset();
    $scope.editing = false;
    var ruleUID = $scope.path[3];

    if ($scope.path[3]) {
        ruleRepository.getOne(function(rule) {
            return rule.uid === ruleUID;
        }, function(data) {
            $scope.name = data.name;
            $scope.description = data.description;
            $scope.status = data.status;
            setModuleArrays(data);
        });
        $scope.setTitle('Edit ' + ruleUID);
        $scope.setSubtitle([]);
        $scope.editing = true;
    } else if (sharedProperties.getParams().length > 0 && sharedProperties.getParams()[0]) {
        $scope.name = sharedProperties.getParams()[0].label;
        $scope.description = sharedProperties.getParams()[0].description;
        setModuleArrays(sharedProperties.getParams()[0]);
    }

    function setModuleArrays(data) {
        moduleTypeService.getByType({
            mtype : 'trigger'
        }).$promise.then(function(moduleData) {
            sharedProperties.setModuleTypes(moduleData);
            sharedProperties.addArray('trigger', data.triggers);
            sharedProperties.addArray('action', data.actions);
            sharedProperties.addArray('condition', data.conditions);
        });
    }

    $scope.saveUserRule = function() {

        var rule = $scope.getRuleJSON(sharedProperties, null, $scope.name, $scope.description);
        ruleService.add(rule).$promise.then(function() {
            toastService.showDefaultToast('Rule added.');
            $scope.navigateTo('');
        });

    };

    $scope.updateUserRule = function() {

        var rule = $scope.getRuleJSON(sharedProperties, $scope.path[3], $scope.name, $scope.description);
        ruleService.update({
            ruleUID : $scope.path[3]
        }, rule).$promise.then(function() {
            toastService.showDefaultToast('Rule updated.');
            $scope.navigateTo('');
        });
    };

    $scope.openNewModuleDialog = function(event, type) {
        $scope.openDialog('addModuleDialogController', 'partials/dialog.addmodule.html', {
            event : event,
            module : {},
            ruleID : $scope.path[3] || '',
            type : type
        });
    };

    $scope.openUpdateModuleDialog = function(event, type, module) {
        $scope.openDialog('addModuleDialogController', 'partials/dialog.addmodule.html', {
            event : event,
            module : module,
            ruleID : $scope.path[3] || '',
            type : type
        });
    };

    $scope.aTriggers = sharedProperties.getTriggersArray();
    $scope.aActions = sharedProperties.getActionsArray();
    $scope.aConditions = sharedProperties.getConditionsArray();

    $scope.sortableOptions = {
        handle : '.draggable',
        update : function(e, ui) {
        },
        axis : 'y'
    };

}).controller('RuleConfigureController', function($scope, ruleRepository, ruleService, toastService) {
    $scope.setSubtitle([ 'Configure' ]);
    var ruleUID = $scope.path[3];

    ruleRepository.getOne(function(rule) {
        return rule.uid === ruleUID;
    }, function(rule) {
        $scope.setSubtitle([ 'Configure ' + rule.name ]);
    });

    ruleService.getModuleConfigParameter({
        ruleUID : ruleUID
    }, function(data) {
        $scope.script = data.content;
    });

    $scope.save = function() {
        ruleService.setModuleConfigParameter({
            ruleUID : ruleUID
        }, $scope.script, function() {
            toastService.showDefaultToast('Rule updated successfully.');
            $scope.navigateTo('');
        });
    };
}).controller('RuleTemplateController', function($scope, $location, templateRepository, ruleService, configService, toastService) {
    $scope.setTitle("Configure rule");
    var templateUID = $scope.path[3];
    if (templateUID) {
        templateRepository.getOne(function(template) {
            return template.uid == templateUID;
        }, function(template) {
            if (template) {
                $scope.parameters = configService.getRenderingModel(template.configDescriptions);
                $scope.name = template.label;
                $scope.description = template.description;
                $scope.configuration = configService.setConfigDefaults({}, $scope.parameters);
                $scope.templateUID = template.uid;
            }
        });
    }

    $scope.saveRule = function() {
        $scope.configuration = configService.replaceEmptyValues($scope.configuration);
        var rule = {
            templateUID : $scope.templateUID,
            name : $scope.name,
            description : $scope.description,
            configuration : $scope.configuration
        };
        ruleService.add(rule).$promise.then(function() {
            toastService.showDefaultToast('Rule added.');
            $location.path('rules/');
        });

    };
}).controller('TemplateDialogController', function($scope, $mdDialog, toastService, templateRepository, $location) {
    templateRepository.getAll(function(templates) {
        $scope.templateData = templates;
    });
    $scope.openConfig = function(templateUID) {
        $mdDialog.hide();
        $location.path('rules/template/' + templateUID);
    };
    $scope.close = function() {
        $mdDialog.hide();
    };
}).controller('RuleRemoveController', function($scope, $mdDialog, toastService, ruleService, rule) {
    $scope.rule = rule;
    $scope.remove = function(ruleUID) {
        ruleService.remove({
            ruleUID : ruleUID
        }, function() {
            $mdDialog.hide();
            toastService.showDefaultToast('Rule removed.');
        }, function() {
            $mdDialog.hide();
        });
    }
    $scope.close = function() {
        $mdDialog.hide();
    }
}).directive('dragdrop', function() {
    return {
        restrict : 'AE',
        replace : true,
        template : '<span class="draggable md-icon-reorder"></span>',
        link : function(scope, elem, attrs) {

            var touchHandler = function(event) {
                var touch = event.changedTouches[0];
                var simulatedEvent = document.createEvent("MouseEvent");
                simulatedEvent.initMouseEvent({
                    touchstart : "mousedown",
                    touchmove : "mousemove",
                    touchend : "mouseup"
                }[event.type], true, true, window, 1, touch.screenX, touch.screenY, touch.clientX, touch.clientY, false, false, false, false, 0, null);

                touch.target.dispatchEvent(simulatedEvent);
                event.preventDefault();
            };
            elem[0].addEventListener("touchstart", touchHandler, true);
            elem[0].addEventListener("touchmove", touchHandler, true);
            elem[0].addEventListener("touchend", touchHandler, true);
            elem[0].addEventListener("touchcancel", touchHandler, true);
        }
    };
}).directive('scriptarea', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, elem, attrs, ngModel) {
            elem.ready(function() {
                setTimeout(function() {
                    elem[0].style.cssText = 'height:auto;';
                    elem[0].style.cssText = 'height:' + elem[0].scrollHeight + 'px';
                }, 500);
            });
            var localAttrs = attrs;
            var element = elem;
            var resizeHandler = function(event) {
                elem[0].style.cssText = 'height:auto;';
                if (elem[0].value.length < 1) {
                    elem[0].style.cssText = 'height:35px';
                } else {
                    elem[0].style.cssText = 'height:' + elem[0].scrollHeight + 'px';
                }
                validateAtrribute();
            };
            elem[0].addEventListener("keydown", resizeHandler, true);
            elem[0].addEventListener("input", resizeHandler, true);
            elem[0].addEventListener("cut", resizeHandler);
            elem[0].addEventListener("blur", function() {
                validateAtrribute();
            });
            function validateAtrribute() {
                var modelArr = document.getElementsByName(attrs.name);
                if (modelArr && modelArr.length > 0) {
                    var modelValue = modelArr[0].value;
                    if (modelValue && (modelValue.length < localAttrs.ngMinlength || modelValue.length > localAttrs.ngMaxlength)) {
                        element.addClass('border-invalid');
                    } else if ((modelValue === undefined || modelValue == "") && localAttrs.ngRequired) {
                        element.addClass('border-invalid');
                    } else {
                        element.removeClass('border-invalid');
                    }
                }
            }
        }
    }
});

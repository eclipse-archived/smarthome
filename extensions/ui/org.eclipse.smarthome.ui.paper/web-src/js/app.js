angular.module('PaperUI', [ 'PaperUI.controllers', 'PaperUI.controllers.control', 'PaperUI.controllers.setup', 'PaperUI.controllers.configuration', 'PaperUI.controllers.extension', 'PaperUI.controllers.rules', 'PaperUI.services', 'PaperUI.services.rest', 'PaperUI.services.repositories', 'PaperUI.extensions', 'ngRoute', 'ngResource', 'ngMaterial', 'ngMessages', 'ngSanitize', 'ui.sortable' ]).config([ '$routeProvider', '$httpProvider', 'globalConfig', '$mdDateLocaleProvider', function($routeProvider, httpProvider, globalConfig, $mdDateLocaleProvider) {
    $routeProvider.when('/control', {
        templateUrl : 'partials/control.html',
        controller : 'ControlPageController',
        title : 'Control',
        simpleHeader : true
    }).when('/setup', {
        redirectTo : '/inbox/search'
    }).when('/inbox', {
        redirectTo : '/inbox/search'
    }).when('/inbox/setup', {
        redirectTo : '/inbox/setup/bindings'
    }).when('/inbox/search', {
        templateUrl : 'partials/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/inbox/setup/bindings', {
        templateUrl : 'partials/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/inbox/setup/search/:bindingId', {
        templateUrl : 'partials/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/inbox/setup/thing-types/:bindingId', {
        templateUrl : 'partials/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/inbox/setup/add/:thingTypeUID', {
        templateUrl : 'partials/setup.html',
        controller : 'SetupWizardController',
        title : 'Inbox'
    }).when('/configuration', {
        redirectTo : '/configuration/bindings'
    }).when('/configuration/bindings', {
        templateUrl : 'partials/configuration.html',
        controller : 'ConfigurationPageController',
        title : 'Configuration'
    }).when('/configuration/services', {
        templateUrl : 'partials/configuration.html',
        controller : 'ConfigurationPageController',
        title : 'Configuration'
    }).when('/configuration/things', {
        templateUrl : 'partials/configuration.html',
        controller : 'ConfigurationPageController',
        title : 'Configuration'
    }).when('/configuration/items', {
        templateUrl : 'partials/configuration.html',
        controller : 'ConfigurationPageController',
        title : 'Configuration'
    }).when('/configuration/item/edit/:itemName', {
        templateUrl : 'partials/item.config.html',
        controller : 'ItemSetupController'
    }).when('/configuration/item/create', {
        templateUrl : 'partials/item.config.html',
        controller : 'ItemSetupController',
        title : 'Create item'
    }).when('/configuration/things/view/:thingUID', {
        templateUrl : 'partials/configuration.html',
        controller : 'ConfigurationPageController',
        title : 'Configuration'
    }).when('/configuration/things/edit/:thingUID', {
        templateUrl : 'partials/configuration.html',
        controller : 'ConfigurationPageController',
        title : 'Configuration'
    }).when('/configuration/system', {
        templateUrl : 'partials/system.configuration.html',
        controller : 'ConfigurationPageController',
        title : 'Configuration'
    }).when('/extensions', {
        templateUrl : 'partials/extensions.html',
        controller : 'ExtensionPageController',
        title : 'Extensions'
    }).when('/rules', {
        templateUrl : 'partials/rules.html',
        controller : 'RulesPageController',
        title : 'Rules'
    }).when('/rules/new', {
        templateUrl : 'partials/rules.html',
        controller : 'RulesPageController',
        title : 'Rules'
    }).when('/rules/configure/:ruleUID', {
        templateUrl : 'partials/rules.html',
        controller : 'RulesPageController',
        title : 'Rules'
    }).when('/preferences', {
        templateUrl : 'partials/preferences.html',
        controller : 'PreferencesPageController',
        title : 'Preferences'
    });
    if (globalConfig.defaultRoute) {
        $routeProvider.otherwise({
            redirectTo : globalConfig.defaultRoute
        });
    } else {
        $routeProvider.otherwise({
            redirectTo : '/control'
        });
    }
    if (window.localStorage.getItem('paperui.language') == 'de') {
        $mdDateLocaleProvider.shortMonths = [ 'Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez' ];
    } else {
        $mdDateLocaleProvider.shortMonths = [ 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec' ];
    }
    $mdDateLocaleProvider.formatDate = function(date) {
        if (!date) {
            return null;
        }
        return (date.getDate() + '.' + (date.getMonth() + 1) + '.' + date.getFullYear());
    };

    $mdDateLocaleProvider.parseDate = function(date) {
        if (!date) {
            return null;
        }
        var dateParts = date.split(/[\s\/,.:-]+/);
        if (dateParts.length > 2) {
            return new Date(dateParts[1] + '.' + dateParts[0] + '.' + dateParts[2]);
        }
    };
} ]).directive('editableitemstate', function() {
    return function($scope, $element) {
        $element.context.addEventListener('focusout', function(e) {
            $scope.sendCommand($($element).html());
        });
    };
}).directive('onFinishRender', function($timeout) {
    return {
        restrict : 'A',
        link : function(scope, element, attr) {
            if (scope.$last === true) {
                $timeout(function() {
                    scope.$emit('ngRepeatFinished');
                });
            }
        }
    }
}).directive('isrequired', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, element, attrs, ngModel) {

            scope.$watch(attrs.ngModel, function(value) {
                if ((value === undefined || value === "") && attrs.isrequired == "true") {
                    element.addClass('border-invalid');
                } else {
                    element.removeClass('border-invalid');
                }
            });
        }
    };
}).directive('customFocus', function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs, ngModel) {

            if (element[0] && element[0].childNodes && element[0].childNodes.length > 1 && element[0].children[1].childNodes && element[0].childNodes[1].childNodes.length > 0) {
                element[0].childNodes[1].childNodes[0].addEventListener('focus', function() {
                    scope.focus = true;
                    scope.initial = false;
                    scope.$apply();
                });
                element[0].childNodes[1].childNodes[0].addEventListener('blur', function() {
                    scope.focus = false;
                    scope.$apply();
                });
            }

        }
    };

}).directive('colorSelect', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, element, attrs, ngModel) {

            element[0].addEventListener('click', function() {
                if (!scope.configuration[scope.parameter.name]) {
                    scope.configuration[scope.parameter.name] = "#ffffff";
                }
            });

        }
    };
}).directive('colorRemove', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, element, attrs, ngModel) {

            element[0].addEventListener('click', function() {
                scope.configuration[scope.parameter.name] = undefined;
                scope.$apply();
            });
        }
    };
}).directive('dayOfWeek', function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs, ngModel) {
            if (element[0] && element[0].children && element[0].children.length > 1) {
                if (!scope.configuration[scope.parameter.name]) {
                    scope.configuration[scope.parameter.name] = attrs.multi == "true" ? [] : "";
                    if (attrs.ngRequired == "true") {
                        $(element[0]).addClass('border-invalid');
                    }
                }
                for (var nodeIndex = 0; nodeIndex < element[0].children.length; nodeIndex++) {
                    if (scope.configuration[scope.parameter.name].indexOf(element[0].children[nodeIndex].value) != -1) {
                        $(element[0].children[nodeIndex]).addClass('dow-selected');
                    }
                    element[0].children[nodeIndex].addEventListener('click', function(event) {
                        $(element[0]).removeClass('border-invalid');
                        if (attrs.multi == "true") {
                            var index = scope.configuration[scope.parameter.name].indexOf(event.target.value)
                            if (index == -1) {
                                scope.configuration[scope.parameter.name].push(event.target.value);
                                $(event.target).addClass('dow-selected');
                            } else {
                                scope.configuration[scope.parameter.name].splice(index, 1);
                                $(event.target).removeClass('dow-selected');
                                if (attrs.ngRequired && scope.configuration[scope.parameter.name].length == 0) {
                                    $(element[0]).addClass('border-invalid');
                                }
                            }
                        } else {
                            if (scope.configuration[scope.parameter.name] == "" || scope.configuration[scope.parameter.name] != event.target.value) {
                                if (scope.configuration[scope.parameter.name] != event.target.value) {
                                    $(element[0].children).removeClass('dow-selected');
                                }
                                scope.configuration[scope.parameter.name] = event.target.value;
                                $(event.target).addClass('dow-selected');
                            } else {
                                scope.configuration[scope.parameter.name] = "";
                                $(event.target).removeClass('dow-selected');
                                if (attrs.ngRequired == "true") {
                                    $(element[0]).addClass('border-invalid');
                                }
                            }
                        }
                    });
                }
            }
        }
    };
}).directive('copyclipboard', function(toastService) {
    return {
        restrict : 'A',
        link : function(scope, element, attrs) {
            element[0].addEventListener('click', function() {
                var input = document.createElement("input");
                input.value = attrs.copyclipboard;
                var body = document.getElementsByTagName('body')[0];
                body.appendChild(input);
                input.select();
                var isCopied = document.execCommand('copy');
                if (isCopied) {
                    toastService.showDefaultToast('Text copied to clipboard');
                } else {
                    toastService.showDefaultToast('Could not copy to clipboard');
                }
                body.removeChild(input);
            });
        }
    };
}).directive('longPress', function($timeout) {
    return {
        restrict : 'A',
        link : function($scope, elem, $attrs) {
            var timeoutHandler;
            var longClicked = false;
            elem[0].addEventListener('mousedown', function(evt) {
                timeoutHandler = $timeout(function() {
                    longClicked = true;
                    if ($attrs.onLongPress) {
                        $scope.$apply(function() {
                            $scope.$eval($attrs.onLongPress, {
                                $event : evt
                            });
                        });
                    }
                }, 400)
            });

            elem[0].addEventListener('mouseup', function(evt) {
                $timeout.cancel(timeoutHandler);
                if (!longClicked && $attrs.onClick) {
                    $scope.$apply(function() {
                        $scope.$eval($attrs.onClick, {
                            $event : evt
                        });
                    });
                }
                longClicked = false;
            });
        }
    };
}).run([ '$location', '$rootScope', 'globalConfig', function($location, $rootScope, globalConfig) {
    var original = $location.path;
    $rootScope.$on('$routeChangeSuccess', function(event, current, previous) {
        if (current.hasOwnProperty('$$route')) {
            $rootScope.title = current.$$route.title;
            $rootScope.simpleHeader = current.$$route.simpleHeader;
        }
        $rootScope.path = $location.path().split('/');
        $rootScope.section = $rootScope.path[1];
        $rootScope.page = $rootScope.path[2];
    });
    $rootScope.asArray = function(object) {
        return $.isArray(object) ? object : object ? [ object ] : [];
    }
    $rootScope.itemUpdates = {};
    $rootScope.data = [];
    $rootScope.navigateToRoot = function() {
        $location.path('');
    }
    $rootScope.$location = $location;
    var advancedMode = localStorage.getItem('paperui.advancedMode');
    if (advancedMode !== 'true' && advancedMode !== 'false') {
        $rootScope.advancedMode = globalConfig.advancedDefault;
    } else {
        $rootScope.advancedMode = advancedMode === 'true';
    }

} ]);
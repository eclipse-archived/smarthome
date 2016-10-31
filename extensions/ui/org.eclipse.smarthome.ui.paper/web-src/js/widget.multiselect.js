angular.module('PaperUI').directive('multiSelect', function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs) {
            scope.filterText = "";
            if (!scope.parameter.options) {
                scope.parameter.options = [];
            }
            
            if (scope.configuration[scope.parameter.name])
                for (var i = 0; i < scope.configuration[scope.parameter.name].length; i++) {
                    var inParam = $.grep(scope.parameter.options, function(option) {
                        return option.value == scope.configuration[scope.parameter.name][i];
                    }).length > 0;
                    if (!inParam) {
                        scope.parameter.options.push({
                            value : scope.configuration[scope.parameter.name][i],
                            label : scope.configuration[scope.parameter.name][i]
                        });
                    }
                }
            $(document).bind('click', function(e) {
                var $clicked = $(e.target);
                if (!$clicked.parents().hasClass("dropdown")) {
                    element.find("dd ul").hide();
                }
            });
            element.find('dd ul li a').on('click', function(e) {
                element.find("dd ul").hide();
            });
            scope.openDropdown = function($event) {
                $event.stopImmediatePropagation();
                element.find("dd ul").slideToggle('fast');
            }

            scope.addItemToList = function($event) {

                if (scope.filterText) {
                    scope.parameter.options.push({
                        value : scope.filterText,
                        label : scope.filterText
                    });
                }
                scope.updateInConfig(scope.filterText);
                scope.filterText = "";
                element.find("dd ul").slideDown('fast');
                $event.stopImmediatePropagation();
            }

            scope.onEnterPress = function($event) {

                if (!scope.parameter.limitToOptions && $event.keyCode == 13) {
                    scope.addItemToList($event);
                }
            }

            scope.searchInConfig = function(optionValue) {
                if (scope.configuration && scope.configuration[scope.parameter.name]) {
                    if (scope.configuration[scope.parameter.name].indexOf(optionValue) !== -1) {
                        return true;
                    }
                }
                return false;
            }

            scope.updateInConfig = function(optionValue) {
                if (scope.configuration && !scope.configuration[scope.parameter.name]) {
                    scope.configuration[scope.parameter.name] = [];
                }
                if (!this.searchInConfig(optionValue)) {
                    scope.configuration[scope.parameter.name].push(optionValue);
                } else {
                    var index = scope.configuration[scope.parameter.name].indexOf(optionValue);
                    if (index != -1) {
                        scope.configuration[scope.parameter.name].splice(index, 1);
                    }
                }
            }
        }
    };

});
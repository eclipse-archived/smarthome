angular.module('PaperUI').directive('multiSelect', function() {
    return {
        restrict : 'A',
        link : function(scope, element, attrs) {
            scope.filterText = "";
            scope.parameter.optionList = [];
            var originalList = [];
            if (scope.configuration[scope.parameter.name])
                for (var i = 0; i < scope.configuration[scope.parameter.name].length; i++) {
                    var inParam = $.grep(scope.parameter.options, function(option) {
                        return option.value == scope.configuration[scope.parameter.name][i];
                    }).length > 0;
                    if (!inParam) {
                        scope.parameter.optionList.push({
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
                var visible = element.find("dd ul").is(":visible");
                angular.element(document).find("dd ul").hide();
                if (!visible)
                    element.find("dd ul").slideDown('fast');
                else {
                    element.find("dd ul").slideUp('fast');
                }
            }

            scope.addItemToList = function($event) {
                var inParam = $.grep(scope.parameter.optionList, function(option) {
                    return option.value == scope.filterText;
                }).length > 0;
                if (!inParam) {
                    if (scope.filterText) {
                        scope.parameter.optionList.push({
                            value : scope.filterText,
                            label : scope.filterText
                        });
                    }
                    scope.updateInConfig(scope.filterText);
                    scope.filterText = "";
                    element.find("dd ul").slideDown('fast');
                }
                $event.stopImmediatePropagation();
            }

            scope.onEnterPress = function($event) {
                if (((scope.parameter.options.length == 0) || (scope.parameter.options.length > 0 && !scope.parameter.limitToOptions)) && $event.keyCode == 13) {
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
                if (optionValue && !this.searchInConfig(optionValue)) {
                    scope.configuration[scope.parameter.name].push(optionValue);
                } else {
                    var index = scope.configuration[scope.parameter.name].indexOf(optionValue);
                    if (index != -1) {
                        scope.configuration[scope.parameter.name].splice(index, 1);
                    }
                }
            }

            scope.$watch('parameter.options', function() {
                if (!('$promise' in scope.parameter.options)) {
                    addOptionToParam();
                } else {
                    scope.parameter.options.$promise.then(function() {
                        addOptionToParam();
                    });
                }
            });

            scope.$watch('filterText', function() {
                if (scope.parameter.optionList && scope.parameter.optionList.length > 0) {
                    originalList = originalList.length == 0 ? scope.parameter.optionList : originalList;
                    var filteredOptions = $.grep(originalList, function(option) {
                        var optionValue = (option.label + "").toLowerCase();
                        return optionValue.indexOf(("" + scope.filterText).toLowerCase()) != -1;
                    });
                    scope.parameter.optionList = filteredOptions && filteredOptions.length > 0 ? filteredOptions : originalList;
                }
            });

            scope.getPlaceHolderText = function(configuration, parameter) {
                if (configuration[parameter.name] && configuration[parameter.name].length > 0) {
                    if (parameter.context == "thing" || parameter.context == "item") {
                        return configuration[parameter.name].length == 1 ? '1 option selected' : configuration[parameter.name].length + ' options selected';
                    } else {
                        return configuration[parameter.name].toString();
                    }
                }
                return parameter.options.length == 0 || (parameter.options.length > 0 && !parameter.limitToOptions) ? 'Add or search' : 'Search';
            }

            function addOptionToParam() {
                for (var i = 0; i < scope.parameter.options.length; i++) {
                    var value = scope.parameter.context == 'item' ? scope.parameter.options[i].name : scope.parameter.context == 'thing' ? scope.parameter.options[i].UID : scope.parameter.options[i].value;
                    var index = searchInOptionList(scope.parameter, value);
                    if (index == -1) {
                        index = scope.parameter.optionList.length;
                    }
                    scope.parameter.optionList[index] = {
                        value : value,
                        label : scope.parameter.options[i].label
                    };
                }
            }

            function searchInOptionList(parameter, searchItem) {

                for (var i = 0; i < parameter.optionList.length; i++) {
                    if (parameter.optionList[i].value == searchItem)
                        return i;
                }
                return -1;
            }
        }
    };

});
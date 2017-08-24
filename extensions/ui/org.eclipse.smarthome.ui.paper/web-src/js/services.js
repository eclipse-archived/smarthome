angular.module('PaperUI.services', [ 'PaperUI.services.repositories', 'PaperUI.constants' ]).config(function($httpProvider) {
    var language = localStorage.getItem('paperui.language');
    if (language) {
        $httpProvider.defaults.headers.common['Accept-Language'] = language;
    }
    $httpProvider.interceptors.push(function($q, $injector) {
        return {
            'responseError' : function(rejection) {
                var showError = rejection.data.showError;
                if (showError !== false) {
                    var errorText = "";
                    if (rejection.data && rejection.data.customMessage) {
                        errorText = rejection.data.customMessage
                    } else {
                        errorText = rejection.statusText;
                    }
                    $injector.get('toastService').showErrorToast('ERROR: ' + rejection.status + ' - ' + errorText);
                }
                return $q.reject(rejection);
            }
        };
    });
}).factory('eventService', function($resource, $log, restConfig) {

    var callbacks = [];
    var eventSrc;

    var initializeEventService = function() {

        eventSrc = new EventSource(restConfig.eventPath)
        $log.debug('Initializing event service.')

        eventSrc.addEventListener('error', function(event) {
            if (eventSrc.readyState === 2) { // CLOSED
                $log.debug('Event connection broken. Trying to reconnect in 5 seconds.');
                setTimeout(initializeEventService, 5000);
            }
        });
        eventSrc.addEventListener('message', function(event) {
            var data = JSON.parse(event.data);
            $log.debug('Event received: ' + data.topic + ' - ' + data.payload);
            $.each(callbacks, function(index, element) {
                var match = data.topic.match(element.topic);
                if (match != null && match == data.topic) {
                    element.callback(data.topic, JSON.parse(data.payload));
                }
            });
        });
    }
    if (typeof (EventSource) !== "undefined") {
        initializeEventService();
    }

    return new function() {
        this.onEvent = function(topic, callback) {
            var topicRegex = topic.replace('/', '\/').replace('*', '.*');
            callbacks.push({
                topic : topicRegex,
                callback : callback
            });
        }
    };
}).factory('toastService', function($mdToast, $rootScope) {
    return new function() {
        var self = this;
        this.showToast = function(id, text, actionText, actionUrl) {
            var toast = $mdToast.simple().content(text);
            if (actionText) {
                toast.action(actionText);
                toast.hideDelay(6000);
            } else {
                toast.hideDelay(3000);
            }
            toast.position('bottom right');
            $mdToast.show(toast).then(function(value) {
                if (value == "ok") {
                    $rootScope.$location.path(actionUrl);
                }
            });
        }
        this.showDefaultToast = function(text, actionText, actionUrl) {
            self.showToast('default', text, actionText, actionUrl);
        }
        this.showErrorToast = function(text, actionText, actionUrl) {
            self.showToast('error', text, actionText, actionUrl);
        }
        this.showSuccessToast = function(text, actionText, actionUrl) {
            self.showToast('success', text, actionText, actionUrl);
        }
    };
}).factory('configService', function(itemService, thingRepository, ruleRepository, $filter, itemRepository) {

    var applyParameterContext = function(parameter) {
        if (!parameter.context) {
            return false;
        }

        var context = parameter.context.toUpperCase();
        switch (context) {
            case 'ITEM':
            case 'CHANNEL':
            case 'THING':
            case 'RULE':
                if (parameter.multiple) {
                    parameter.element = 'multiSelect';
                    parameter.limitToOptions = true;
                } else {
                    parameter.element = 'select';
                }
                break;
            case 'DATE':
                if (parameter.type.toUpperCase() === 'TEXT') {
                    parameter.element = 'date';
                } else {
                    parameter.element = 'input';
                    parameter.context = "";
                }
                break;
            case 'TIME':
                parameter.element = 'input';
                if (parameter.type.toUpperCase() === 'TEXT') {
                    parameter.inputType = 'time';
                } else {
                    parameter.context = "";
                }
                break;
            case 'COLOR':
                parameter.element = 'color';
                parameter.input = 'TEXT';
                parameter.inputType = 'color';
                break;
            case 'SCRIPT':
                parameter.element = 'textarea';
                parameter.inputType = 'text';
                parameter.label = parameter.label && parameter.label.length > 0 ? parameter.label : 'Script';
                break;
            case 'DAYOFWEEK':
                parameter.element = 'dayofweek';
                parameter.inputType = 'text';
                break;
            case 'PASSWORD':
                parameter.element = 'input';
                parameter.inputType = 'password';
                break;
            default:
                return false;
        }

        if (context === "RULE") {
            parameter.options = parameter.options ? parameter.options : [];
            ruleRepository.getAll(function(rules) {
                angular.forEach(rules, function(rule) {
                    rule.value = rule.uid;
                    rule.label = rule.name;
                    parameter.options.push(rule);
                });
            });
        }

        return true;
    }

    var applyParameterType = function(parameter) {
        var type = parameter.type ? parameter.type.toUpperCase() : "TEXT";
        switch (type) {
            case 'TEXT':
            case 'INTEGER':
            case 'DECIMAL':
                parameter.inputType = type === 'TEXT' ? 'text' : 'number';
                parameter.options = parameter.options && parameter.options.length > 0 ? parameter.options : [];
                if (parameter.multiple) {
                    parameter.element = 'multiSelect';
                } else if (parameter.options.length > 0) {
                    if (!parameter.limitToOptions) {
                        parameter.element = "multiSelect";
                    } else {
                        parameter.element = "select";
                    }
                } else {
                    parameter.element = 'input';
                }
                break;
            case 'BOOLEAN':
                parameter.element = 'switch';
                break;
            default:
                parameter.element = 'input';
                parameter.inputType = 'text';
        }

        if (type === 'TEXT') {
            insertEmptyOption(parameter);
        }

        if (type === 'INTEGER') {
            adjustNumberValue(parameter, parseInt);
            if (!parameter.pattern) {
                // force the input of integers if not stated otherwise.
                parameter.pattern = '-?\\d+'
            }
        }
        if (type === 'DECIMAL') {
            adjustNumberValue(parameter, parseFloat);
        }
    }

    var adjustNumberValue = function(parameter, parseNumberFunction) {
        angular.forEach(parameter.options, function(option) {
            option.value = parseNumberFunction(option.value);
        })
        if (parameter.defaultValue) {
            parameter.defaultValue = parseNumberFunction(parameter.defaultValue);
        }
    }

    return {
        getRenderingModel : function(configParameters, configGroups) {
            if (!configParameters || configParameters.length == 0) {
                return [];
            }

            if (!configGroups) {
                configGroups = [];
            }
            configGroups.push({
                "name" : "_default",
                "label" : "Others"
            });

            var groupNameIndexMap = {};
            angular.forEach(configGroups, function(configGroup, index) {
                groupNameIndexMap[configGroup.name] = index;
            })

            var groupsList = [];
            angular.forEach(configParameters, function(parameter) {
                parameter.locale = window.localStorage.getItem('paperui.language');
                parameter.filterText = '';
                var contextApplied = applyParameterContext(parameter);
                if (!contextApplied) {
                    applyParameterType(parameter);
                }

                var group = $filter('filter')(configGroups, function(configGroup) {
                    // default the group name if the parameter group name is unknown.
                    var groupName = groupNameIndexMap[parameter.groupName] >= 0 ? parameter.groupName : '_default';
                    return configGroup.name === groupName;
                });
                var groupIndex = groupNameIndexMap[group[0].name];
                if (!groupsList[groupIndex]) {
                    // initialise the resulting group
                    groupsList[groupIndex] = {
                        parameters : []
                    }
                }
                groupsList[groupIndex].groupName = group[0].name;
                groupsList[groupIndex].groupLabel = group[0].label;
                groupsList[groupIndex].advanced = group[0].advanced;
                groupsList[groupIndex].parameters.push(parameter);
            });

            var renderingGroups = [];
            renderingGroups.hasAdvanced = false;
            angular.forEach(groupsList, function(group) {
                if (group.advanced) {
                    angular.forEach(group.parameters, function(parameter) {
                        parameter.advanced = true;
                    });
                }
                group.advParam = $filter('filter')(group.parameters, function(parameter) {
                    return parameter.advanced;
                }).length;

                if (group.advParam > 0) {
                    renderingGroups.hasAdvanced = true;
                }
                renderingGroups.push(group);
            });
            renderingGroups = this.getItemConfigs(renderingGroups)
            return this.getChannelsConfig(renderingGroups);
        },
        getChannelsConfig : function(configParams) {
            var self = this, hasOneItem;
            var configParameters = configParams;
            for (var i = 0; !hasOneItem && i < configParameters.length; i++) {
                var parameterItems = $.grep(configParameters[i].parameters, function(value) {
                    return value.context && (value.context.toUpperCase() == "THING" || value.context.toUpperCase() == "CHANNEL");
                });
                if (parameterItems.length > 0) {
                    hasOneItem = true;
                }
                if (hasOneItem) {
                    thingRepository.getAll(function(things) {
                        for (var g_i = 0; g_i < configParameters.length; g_i++) {
                            for (var i = 0; i < configParameters[g_i].parameters.length; i++) {
                                if (configParameters[g_i].parameters[i].context) {
                                    if (configParameters[g_i].parameters[i].context.toUpperCase() === "THING") {
                                        configParameters[g_i].parameters[i].options = self.filterByAttributes(things, configParameters[g_i].parameters[i].filterCriteria);
                                    } else if (configParameters[g_i].parameters[i].context.toUpperCase() === "CHANNEL") {
                                        configParameters[g_i].parameters[i].options = getChannelsFromThings(things, configParameters[g_i].parameters[i].filterCriteria);
                                    }
                                }
                            }
                        }
                    });
                }
                function getChannelsFromThings(arr, filter) {
                    var channels = [];
                    for (var i = 0; i < arr.length; i++) {
                        var filteredChannels = self.filterByAttributes(arr[i].channels, filter);
                        for (var j = 0; j < filteredChannels.length; j++) {
                            filteredChannels[j].label = arr[i].label;
                            filteredChannels[j].value = filteredChannels[j].uid;
                        }
                        channels = channels.concat(filteredChannels);
                    }
                    return channels;
                }
            }
            return configParameters;
        },
        getItemConfigs : function(configParams) {
            var self = this, hasOneItem = false;
            var configParameters = configParams;
            for (var i = 0; !hasOneItem && i < configParameters.length; i++) {
                var parameterItems = $.grep(configParameters[i].parameters, function(value) {
                    return value.context && value.context.toUpperCase() == "ITEM";
                });
                if (parameterItems.length > 0) {
                    hasOneItem = true;
                }
            }
            if (hasOneItem) {
                itemRepository.getAll(function(items) {
                    for (var g_i = 0; g_i < configParameters.length; g_i++) {
                        for (var i = 0; i < configParameters[g_i].parameters.length; i++) {
                            if (configParameters[g_i].parameters[i].context && configParameters[g_i].parameters[i].context.toUpperCase() === "ITEM") {
                                var filteredItems = self.filterByAttributes(items, configParameters[g_i].parameters[i].filterCriteria);
                                configParameters[g_i].parameters[i].options = $filter('orderBy')(filteredItems, "label");
                            }
                        }
                    }
                });
            }
            return configParameters;
        },
        filterByAttributes : function(arr, filters) {
            if (!filters || filters.length == 0) {
                return arr;
            }
            return $.grep(arr, function(element, i) {
                return $.grep(filters, function(filter) {
                    if (arr[i].hasOwnProperty(filter.name) && filter.value != "" && filter.value != null) {
                        var filterValues = filter.value.split(',');
                        return $.grep(filterValues, function(filterValue) {
                            if (Array.isArray(arr[i][filter.name])) {
                                return $.grep(arr[i][filter.name], function(arrValue) {
                                    return arrValue.toUpperCase().indexOf(filterValue.toUpperCase()) != -1;
                                }).length > 0;
                            } else {
                                return arr[i][filter.name].toUpperCase().indexOf(filterValue.toUpperCase()) != -1;
                            }
                        }).length > 0
                    } else {
                        return false;
                    }
                }).length == filters.length;
            });
        },

        getConfigAsArray : function(config, paramGroups) {
            var configArray = [];
            var self = this;
            angular.forEach(config, function(value, name) {
                var value = config[name];
                if (paramGroups) {
                    var param = self.getParameter(paramGroups, name);
                    var date = Date.parse(value);
                    if (param !== null && param.context && !isNaN(date)) {
                        if (param.context.toUpperCase() === 'TIME') {
                            value = (value.getHours() < 10 ? '0' : '') + value.getHours() + ':' + (value.getMinutes() < 10 ? '0' : '') + value.getMinutes();
                        } else if (param.context.toUpperCase() === 'DATE') {
                            value = (value.getFullYear() + '-' + (value.getMonth() + 1 < 10 ? '0' : '') + (value.getMonth() + 1) + '-' + value.getDate());
                        }
                    }
                }
                configArray.push({
                    name : name,
                    value : value
                });
            });
            return configArray;
        },
        getConfigAsObject : function(configArray, paramGroups, sending) {
            var config = {};

            for (var i = 0; configArray && i < configArray.length; i++) {
                var configEntry = configArray[i];
                var param = this.getParameter(paramGroups, configEntry.name);
                if (param !== null && param.type.toUpperCase() == "BOOLEAN") {
                    configEntry.value = String(configEntry.value).toUpperCase() == "TRUE";
                } else if (param !== null && param.context) {
                    if (param.context.toUpperCase() === 'TIME') {
                        var time = configEntry.value ? configEntry.value.split(/[\s\/,.:-]+/) : [];
                        if (time.length > 1) {
                            configEntry.value = new Date(1970, 0, 1, time[0], time[1]);
                        }
                    } else if (param.context.toUpperCase() === 'DATE') {
                        var dateParts = configEntry.value ? configEntry.value.split(/[\s\/,.:-]+/) : [];
                        if (dateParts.length > 2) {
                            configEntry.value = new Date(dateParts[1] + '/' + dateParts[2] + '/' + dateParts[0]);
                        } else {
                            configEntry.value = null;
                        }
                    }
                }
                config[configEntry.name] = configEntry.value;
            }
            return config;
        },
        getParameter : function(paramGroups, itemName) {
            for (var i = 0; i < paramGroups.length; i++) {
                for (var j = 0; paramGroups[i].parameters && j < paramGroups[i].parameters.length; j++) {
                    if (paramGroups[i].parameters[j].name == itemName) {
                        return paramGroups[i].parameters[j]
                    }
                }
            }
            return null;
        },
        setDefaults : function(thing, thingType) {
            if (thingType && thingType.configParameters) {
                $.each(thingType.configParameters, function(i, parameter) {
                    if (parameter.defaultValue !== 'null') {
                        if (parameter.type === 'TEXT') {
                            thing.configuration[parameter.name] = parameter.defaultValue
                        } else if (parameter.type === 'BOOLEAN') {
                            var value = thing.configuration[parameter.name] != null && thing.configuration[parameter.name] != "" ? thing.configuration[parameter.name] : parameter.defaultValue != null ? parameter.defaultValue : "";
                            if (String(value).length > 0) {
                                thing.configuration[parameter.name] = String(value).toUpperCase() == "TRUE";
                            }
                        } else if (parameter.type === 'INTEGER') {
                            thing.configuration[parameter.name] = parameter.defaultValue != null && parameter.defaultValue !== "" ? parseInt(parameter.defaultValue) : "";
                        } else if (parameter.type === 'DECIMAL') {
                            thing.configuration[parameter.name] = parameter.defaultValue != null && parameter.defaultValue !== "" ? parseFloat(parameter.defaultValue) : "";
                        } else {
                            thing.configuration[parameter.name] = parameter.defaultValue;
                        }
                    } else {
                        thing.configuration[parameter.name] = null;
                    }
                });
            }
        },
        setConfigDefaults : function(originalConfiguration, groups, sending) {
            var configuration = {};
            angular.copy(originalConfiguration, configuration);
            for (var i = 0; i < groups.length; i++) {
                $.each(groups[i].parameters, function(i, parameter) {
                    var hasValue = configuration[parameter.name] != null && String(configuration[parameter.name]).length > 0;
                    if (parameter.context && (parameter.context.toUpperCase() === 'DATE' || parameter.context.toUpperCase() === 'TIME')) {
                        var date = hasValue ? configuration[parameter.name] : parameter.defaultValue ? parameter.defaultValue : null;
                        if (date) {
                            if (typeof sending !== "undefined" && sending) {
                                if (parameter.context.toUpperCase() === 'DATE') {
                                    configuration[parameter.name] = date instanceof Date ? (date.getFullYear() + '-' + (date.getMonth() + 1 < 10 ? '0' : '') + (date.getMonth() + 1) + '-' + date.getDate()) : date;
                                } else {
                                    configuration[parameter.name] = date instanceof Date ? (date.getHours() < 10 ? '0' : '') + date.getHours() + ':' + (date.getMinutes() < 10 ? '0' : '') + date.getMinutes() : date;
                                }
                            } else {
                                if (parameter.context.toUpperCase() === 'TIME') {
                                    var time = date.split(/[\s\/,.:-]+/);
                                    if (time.length > 1) {
                                        configuration[parameter.name] = new Date(1970, 0, 1, time[0], time[1]);
                                    }
                                } else {
                                    var dateParts = date.split(/[\s\/,.:-]+/);
                                    if (dateParts.length > 2) {
                                        configuration[parameter.name] = new Date(dateParts[1] + '/' + dateParts[2] + '/' + dateParts[0]);
                                    } else {
                                        configuration[parameter.name] = null;
                                    }
                                }
                            }
                        }

                    } else if (!hasValue && parameter.context && (parameter.context.toUpperCase() === 'COLOR' && !sending)) {
                        // configuration[parameter.name] = "#ffffff";
                    } else if (!hasValue && parameter.type === 'TEXT') {
                        configuration[parameter.name] = parameter.defaultValue;
                    } else if (parameter.type === 'BOOLEAN') {
                        var value = hasValue ? configuration[parameter.name] : parameter.defaultValue;
                        if (String(value).length > 0) {
                            configuration[parameter.name] = String(value).toUpperCase() == "TRUE";
                        }
                    } else if (!hasValue && parameter.type === 'INTEGER') {
                        configuration[parameter.name] = parameter.defaultValue != null && parameter.defaultValue !== "" ? parseInt(parameter.defaultValue) : null;
                    } else if (!hasValue && parameter.type === 'DECIMAL') {
                        configuration[parameter.name] = parameter.defaultValue != null && parameter.defaultValue !== "" ? parseFloat(parameter.defaultValue) : null;
                    } else if (!hasValue) {
                        configuration[parameter.name] = parameter.defaultValue;
                    }
                    if (!parameter.limitToOptions && parameter.filterText && parameter.filterText.length > 0) {
                        if (Array.isArray(configuration[parameter.name])) {
                            configuration[parameter.name].push(parameter.filterText);
                        } else {
                            configuration[parameter.name] = parameter.filterText
                        }
                    }
                });
            }
            return this.replaceEmptyValues(configuration);
        },
        convertValues : function(configurations, parameters) {
            angular.forEach(configurations, function(value, name) {
                if (value && typeof (value) !== "boolean") {
                    var parsedValue = Number(value);
                    if (isNaN(parsedValue)) {
                        configurations[name] = value;
                    } else {
                        configurations[name] = parsedValue;
                    }
                }
            });
            return configurations;
        },
        replaceEmptyValues : function(configurations) {
            angular.forEach(configurations, function(value, name) {
                if (configurations[name] === undefined || configurations[name] == null || configurations[name] === '') {
                    configurations[name] = null;
                }
            });
            return configurations;
        }
    };
}).factory('thingConfigService', function() {
    return {
        getThingChannels : function(thing, thingType, channelTypes, advanced) {
            var thingChannels = [];
            var includedChannels = [];
            if (thingType && thingType.channelGroups && thingType.channelGroups.length > 0) {
                angular.forEach(thingType.channelGroups, function(channelGroup) {
                    var group = {};
                    group.name = channelGroup.label;
                    group.description = channelGroup.description;
                    group.channels = this.matchGroup(thing.channels, channelGroup.id);
                    includedChannels = includedChannels.concat(group.channels);
                    group.channels = advanced ? group.channels : this.filterAdvance(thingType, channelTypes, group.channels, false);
                    thingChannels.push(group);
                }, this)

                var group = {
                    "name" : "Others",
                    "description" : "Other channels",
                    "channels" : []
                };
                angular.forEach(thing.channels, function(channel) {
                    if (includedChannels.indexOf(channel) == -1) {
                        group.channels.push(channel);
                    }
                })
                if (group.channels && group.channels.length > 0) {
                    thingChannels.push(group);
                }
            } else {
                var group = {};
                group.channels = advanced ? thing.channels : this.filterAdvance(thingType, channelTypes, thing.channels, advanced);
                thingChannels.push(group);
            }

            thingChannels = this.addTypeToChannels(thingChannels, channelTypes);
            return thingChannels;
        },

        filterAdvance : function(thingType, channelTypes, channels, advanced) {
            return channels.filter(function(channel) {
                var channelType = this.getChannelTypeByUID(thingType, channelTypes, channel.channelTypeUID);
                return channelType ? advanced == channelType.advanced : true;
            }, this);
        },
        getChannelTypeByUID : function(thingType, channelTypes, channelUID) {
            if (thingType) {
                if (thingType.channels && thingType.channels.length > 0) {
                    var result = thingType.channels.filter(function(channel) {
                        return channel.typeUID === channelUID;
                    })
                    if (result.length > 0) {
                        return result[0];
                    }
                }
                if (thingType.channelGroups && thingType.channelGroups.length > 0) {
                    angular.forEach(thingType.channelGroups, function(channelGroup) {
                        if (channelGroup && channelGroup.channels) {
                            var result = channelGroup.channels.filter(function(channel) {
                                return channel.typeUID === channelUID;
                            })
                            if (result.length > 0) {
                                return result[0];
                            }
                        }
                    })
                }
            }
            if (channelTypes) {
                return this.getChannelFromChannelTypes(channelTypes, channelUID);
            }
        },
        getChannelFromChannelTypes : function(channelTypes, channelUID) {
            var result = channelTypes.filter(function(channelType) {
                return channelType.UID === channelUID;
            })
            return result.length > 0 ? result[0] : null;
        },
        matchGroup : function(arr, id) {
            var matched = [];
            for (var i = 0; i < arr.length; i++) {
                if (arr[i].id) {
                    var sub = arr[i].id.split("#");
                    if (sub[0] && sub[0] == id) {
                        matched.push(arr[i]);
                    }
                }
            }
            return matched;
        },
        addTypeToChannels : function(groups, channelTypes) {
            angular.forEach(groups, function(group) {
                angular.forEach(group.channels, function(channel) {
                    channel.channelType = this.getChannelFromChannelTypes(channelTypes, channel.channelTypeUID);
                }, this)
            }, this)
            return groups;
        }
    }
}).provider("dateTime", function dateTimeProvider() {
    var months, daysOfWeek, shortChars;
    if (window.localStorage.getItem('paperui.language') == 'de') {
        months = [ 'Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember' ];
        daysOfWeek = [ 'Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag' ];
        shortChars = 2;
    } else {
        months = [ 'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December' ];
        daysOfWeek = [ 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday' ];
        shortChars = 3;
    }
    return {
        getMonths : function(shortNames) {
            if (shortNames) {
                var shortMonths = [];
                for (var i = 0; i < months.length; i++) {
                    shortMonths.push(months[i].substr(0, 3));
                }
                return shortMonths;
            }
            return months;
        },
        $get : function() {
            return {
                getMonths : function(shortNames) {
                    if (shortNames) {
                        var shortMonths = [];
                        for (var i = 0; i < months.length; i++) {
                            shortMonths.push(months[i].substr(0, 3));
                        }
                        return shortMonths;
                    }
                    return months;
                },
                getDaysOfWeek : function(shortNames) {
                    if (shortNames) {
                        var shortDaysOfWeek = [];
                        for (var i = 0; i < daysOfWeek.length; i++) {
                            shortDaysOfWeek.push(daysOfWeek[i].substr(0, shortChars));
                        }
                        return shortDaysOfWeek;
                    }
                    return daysOfWeek;
                }
            }
        }
    }
});
function insertEmptyOption(parameter) {
    if (!parameter.required && ((parameter.options && parameter.options.length > 0) || parameter.context)) {
        parameter.options.splice(0, 0, {
            label : '',
            value : null
        })
    }
}
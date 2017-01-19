angular.module('PaperUI.services', [ 'PaperUI.constants' ]).config(function($httpProvider) {
    var language = localStorage.getItem('paperui.language');
    if (language) {
        $httpProvider.defaults.headers.common['Accept-Language'] = language;
    }
    $httpProvider.interceptors.push(function($q, $injector) {
        return {
            'responseError' : function(rejection) {
                var showError = rejection.showError;
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
    return {
        getRenderingModel : function(configParameters, configGroups) {
            var parameters = [];
            var indexArray = [];
            if (!configGroups) {
                configGroups = [];
            }
            configGroups.push({
                "name" : "_default",
                "label" : "Others"
            });

            for (var j = 0; j < configGroups.length; j++) {
                indexArray[configGroups[j].name] = j;
            }
            if (!configParameters || configParameters.length == 0) {
                return parameters;
            }
            var groupsList = [];
            for (var j = 0; j < configGroups.length; j++) {
                groupsList[j] = {};
                groupsList[j].parameters = [];
            }
            var thingList;
            for (var i = 0; i < configParameters.length; i++) {
                var parameter = configParameters[i];

                var group = [];
                if (!parameter.groupName) {
                    parameter.groupName = "_default";
                }
                group = $filter('filter')(configGroups, {
                    name : parameter.groupName
                }, true);
                if (group.length == 0) {
                    group = $filter('filter')(configGroups, {
                        name : "_default"
                    }, true);
                }
                parameter.locale = window.localStorage.getItem('paperui.language');
                parameter.filterText = "";
                if (parameter.context) {
                    if (parameter.context.toUpperCase() === 'ITEM') {
                        if (parameter.multiple) {
                            parameter.element = 'multiSelect';
                            parameter.limitToOptions = true;
                        } else {
                            parameter.element = 'select';
                        }
                    } else if (parameter.context.toUpperCase() === 'CHANNEL') {
                        if (parameter.multiple) {
                            parameter.element = 'multiSelect';
                            parameter.limitToOptions = true;
                        } else {
                            parameter.element = 'select';
                        }
                        parameter.context = 'channel';
                    } else if (parameter.context.toUpperCase() === "RULE") {
                        if (parameter.multiple) {
                            parameter.element = 'multiSelect';
                            parameter.limitToOptions = true;
                        } else {
                            parameter.element = 'select';
                        }
                        function encloseParameter(parameter) {
                            var param = parameter;
                            ruleRepository.getAll(function(rules) {
                                for (var j_r = 0; j_r < rules.length; j_r++) {
                                    rules[j_r].value = rules[j_r].uid;
                                    rules[j_r].label = rules[j_r].name;
                                }
                                param.options = rules;
                            });
                        }
                        encloseParameter(parameter);
                        parameter.context = 'rule';
                    } else if (parameter.context.toUpperCase() === 'DATE') {
                        if (parameter.type.toUpperCase() === 'TEXT') {
                            parameter.element = 'date';
                        } else {
                            parameter.element = 'input';
                            parameter.context = "";
                        }
                    } else if (parameter.context.toUpperCase() === 'THING') {
                        if (parameter.multiple) {
                            parameter.element = 'multiSelect';
                            parameter.limitToOptions = true;
                        } else {
                            parameter.element = 'select';
                        }
                    } else if (parameter.context.toUpperCase() === 'TIME') {
                        parameter.element = 'input';
                        if (parameter.type.toUpperCase() === 'TEXT') {
                            parameter.inputType = parameter.context;
                        } else {
                            parameter.context = "";
                        }
                    } else if (parameter.context.toUpperCase() === 'COLOR') {
                        parameter.element = 'color';
                        parameter.input = "TEXT";
                        parameter.inputType = parameter.context;
                    } else if (parameter.context.toUpperCase() === 'SCRIPT') {
                        parameter.element = 'textarea';
                        parameter.inputType = 'text';
                        parameter.label = parameter.label && parameter.label.length > 0 ? parameter.label : 'Script';
                    } else if (parameter.context.toUpperCase() === 'DAYOFWEEK') {
                        parameter.element = 'dayofweek';
                        parameter.inputType = 'text';
                    } else if (parameter.context.toUpperCase() === 'PASSWORD') {
                        parameter.element = 'input';
                        parameter.inputType = 'password';
                    } else {
                        parameter.element = 'input';
                        parameter.inputType = 'text';
                    }
                } else if (parameter.type.toUpperCase() === 'TEXT') {
                    parameter.options = parameter.options && parameter.options.length > 0 ? parameter.options : [];
                    insertEmptyOption(parameter);
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
                        parameter.inputType = 'text';
                    }
                } else if (parameter.type.toUpperCase() === 'BOOLEAN') {
                    parameter.element = 'switch';
                } else if (parameter.type.toUpperCase() === 'INTEGER' || parameter.type.toUpperCase() === 'DECIMAL') {
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
                    parameter.inputType = 'number';
                    if (parameter.options) {
                        for (var k = 0; k < parameter.options.length; k++) {
                            parameter.options[k].value = parseInt(parameter.options[k].value);
                        }
                    }
                    if (parameter.defaultValue) {
                        parameter.defaultValue = parseInt(parameter.defaultValue);
                    }
                } else {
                    parameter.element = 'input';
                    parameter.inputType = 'text';
                }
                groupsList[indexArray[group[0].name]].groupName = group[0].name;
                groupsList[indexArray[group[0].name]].groupLabel = group[0].label;
                groupsList[indexArray[group[0].name]].parameters.push(parameter);
            }
            parameters.hasAdvanced = false;
            for (var j = 0; j < groupsList.length; j++) {
                if (groupsList[j].groupName) {
                    groupsList[j].advParam = $.grep(groupsList[j].parameters, function(parameter) {
                        return parameter.advanced;
                    }).length;
                    if (groupsList[j].advParam > 0) {
                        parameters.hasAdvanced = true;
                    }
                    parameters.push(groupsList[j]);
                }

            }
            parameters = this.getItemConfigs(parameters)
            return this.getChannelsConfig(parameters);
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
                            value = (value.getFullYear() + '-' + (value.getMonth() < 10 ? '0' : '') + (value.getMonth() + 1) + '-' + value.getDate());
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
                        } else if (parameter.type === 'INTEGER' || parameter.type === 'DECIMAL') {
                            thing.configuration[parameter.name] = parameter.defaultValue != null && parameter.defaultValue !== "" ? parseInt(parameter.defaultValue) : "";
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
                                    configuration[parameter.name] = date instanceof Date ? (date.getFullYear() + '-' + (date.getMonth() < 10 ? '0' : '') + (date.getMonth() + 1) + '-' + date.getDate()) : date;
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
                    } else if (!hasValue && (parameter.type === 'INTEGER' || parameter.type === 'DECIMAL')) {
                        configuration[parameter.name] = parameter.defaultValue != null && parameter.defaultValue !== "" ? parseInt(parameter.defaultValue) : null;
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
                for (var i = 0; i < thingType.channelGroups.length; i++) {
                    var group = {};
                    group.name = thingType.channelGroups[i].label;
                    group.description = thingType.channelGroups[i].description;
                    group.channels = this.matchGroup(thing.channels, thingType.channelGroups[i].id);
                    includedChannels = includedChannels.concat(group.channels);
                    group.channels = advanced ? group.channels : this.filterAdvance(thingType, channelTypes, group.channels, false);
                    thingChannels.push(group);
                }
                var group = {
                    "name" : "Others",
                    "description" : "Other channels",
                    "channels" : []
                };
                for (var i = 0; i < thing.channels.length; i++) {
                    if (includedChannels.indexOf(thing.channels[i]) == -1) {
                        group.channels.push(thing.channels[i]);
                    }
                }
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
            var self = this;
            self.thingType = thingType, self.channelTypes = channelTypes, self.channels = channels;
            return $.grep(channels, function(channel, i) {
                var channelType = self.getChannelTypeById(self.thingType, self.channelTypes, channel.id);
                return channelType ? advanced == channelType.advanced : true;
            });
        },
        getChannelTypeById : function(thingType, channelTypes, channelId) {
            if (thingType) {
                var cid_part = channelId.split('#', 2)
                if (cid_part.length == 1) {
                    var c, c_i, c_l;
                    for (c_i = 0, c_l = thingType.channels.length; c_i < c_l; ++c_i) {
                        c = thingType.channels[c_i];
                        if (c.id == channelId) {
                            return c;
                        }
                    }
                } else if (cid_part.length == 2) {
                    var cg, cg_i, cg_l;
                    var c, c_i, c_l;
                    for (cg_i = 0, cg_l = thingType.channelGroups.length; cg_i < cg_l; ++cg_i) {
                        cg = thingType.channelGroups[cg_i];
                        if (cg.id == cid_part[0]) {
                            for (c_i = 0, c_l = cg.channels.length; c_i < c_l; ++c_i) {
                                c = cg.channels[c_i];
                                if (c.id == cid_part[1]) {
                                    return c;
                                }
                            }
                        }
                    }
                } else {
                    return;
                }
            }
            if (channelTypes) {
                var c = {}, c_i, c_l;
                for (c_i = 0, c_l = channelTypes.length; c_i < c_l; ++c_i) {
                    c = channelTypes[c_i];
                    c.advanced = false;
                    var id = c.UID.split(':', 2);
                    if (id[1] == channelId) {
                        return c;
                    }
                }
            }
            return;
        },
        getChannelFromChannelTypes : function(channelTypes, channelUID) {
            if (channelTypes) {
                var c = {}, c_i, c_l;
                for (c_i = 0, c_l = channelTypes.length; c_i < c_l; ++c_i) {
                    c = channelTypes[c_i];
                    if (c.UID == channelUID) {
                        return c;
                    }
                }
            }
            return;
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
            for (var g_i = 0; g_i < groups.length; g_i++) {
                for (var c_i = 0; c_i < groups[g_i].channels.length; c_i++) {
                    groups[g_i].channels[c_i].channelType = this.getChannelFromChannelTypes(channelTypes, groups[g_i].channels[c_i].channelTypeUID);
                }
            }
            return groups;
        }
    }
}).factory('util', function(dateTime) {
    return {
        hasProperties : function(object) {
            if (typeof jQuery !== 'undefined') {
                return !jQuery.isEmptyObject(object);
            } else {
                if (object) {
                    return Object.keys(object).length > 0;
                }
                return false;
            }
        },
        timePrint : function(pattern, date) {
            var months = dateTime.getMonths(true);
            if (pattern) {
                var exp = '%1$T';
                while (pattern.toUpperCase().indexOf(exp) != -1) {
                    var index = pattern.toUpperCase().indexOf(exp);
                    var str = "";
                    if (pattern.length > (index + exp.length)) {
                        switch (pattern[index + exp.length]) {
                            case 'H':
                                str = formatNumber(date.getHours());
                                break;
                            case 'l':
                                var hours = (date.getHours() % 12 || 12);
                                var ampm = date.getHours() < 12 ? "AM" : "PM";
                                str = hours + " " + ampm;
                                break;
                            case 'I':
                                var hours = (date.getHours() % 12 || 12);
                                var ampm = date.getHours() < 12 ? "AM" : "PM";
                                str = formatNumber(hours) + " " + formatNumber(ampm);
                                break;
                            case 'M':
                                str = formatNumber(date.getMinutes());
                                break;
                            case 'S':
                                str = formatNumber(date.getSeconds());
                                break;
                            case 'p':
                                str = date.getHours() < 12 ? "AM" : "PM";
                                break;
                            case 'R':
                                str = formatNumber(date.getHours()) + ":" + formatNumber(date.getMinutes());
                                break;
                            case 'T':
                                str = (formatNumber(date.getHours()) + ":" + formatNumber(date.getMinutes()) + ":" + formatNumber(date.getSeconds()));
                                break;
                            case 'r':
                                var hours = (date.getHours() % 12 || 12);
                                var ampm = date.getHours() < 12 ? "AM" : "PM";
                                str = formatNumber(hours) + ":" + formatNumber(date.getMinutes()) + ":" + formatNumber(date.getSeconds()) + " " + ampm;
                                break;
                            case 'D':
                                str = formatNumber(date.getMonth()) + "/" + formatNumber(date.getDate()) + "/" + formatNumber(date.getFullYear());
                                break;
                            case 'F':
                                str = formatNumber(date.getFullYear()) + "-" + formatNumber(date.getMonth()) + "-" + formatNumber(date.getDate());
                                break;
                            case 'c':
                                str = date;
                                break;
                            case 'B':
                                var fullMonths = dateTime.getMonths(false);
                                if (fullMonths.length > 0) {
                                    str = fullMonths[date.getMonth()];
                                }
                                break;
                            case 'h':
                            case 'b':
                                var shortMonths = dateTime.getMonths(true);
                                if (shortMonths.length > 0) {
                                    str = shortMonths[date.getMonth()];
                                }
                                break;
                            case 'A':
                                var longDays = dateTime.getDaysOfWeek(false);
                                if (longDays.length > 0) {
                                    str = longDays[date.getDay()];
                                }
                                break;
                            case 'a':
                                var shortDays = dateTime.getDaysOfWeek(true);
                                if (shortDays.length > 0) {
                                    str = shortDays[date.getDay()];
                                }
                                break;
                            case 'C':
                                str = formatNumber(parseInt(date.getFullYear() / 100));
                                break;
                            case 'Y':
                                str = date.getFullYear();
                                break;
                            case 'y':
                                str = formatNumber(parseInt(date.getFullYear() % 100));
                                break;
                            case 'm':
                                str = formatNumber(date.getMonth() + 1);
                                break;
                            case 'd':
                                str = formatNumber(date.getDate());
                                break;
                            case 'e':
                                str = formatNumber(date.getDate());
                                break;
                        }
                        pattern = pattern.substr(0, index) + str + pattern.substr(index + exp.length + 1, pattern.length);
                    }
                }
                return pattern;
            } else {
                return "";
            }
            function formatNumber(number) {
                return (number < 10 ? "0" : "") + number;
            }
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
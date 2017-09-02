angular.module('PaperUI.controllers.configuration', [ 'PaperUI.constants', 'PaperUI.controllers.firmware', 'PaperUI.controllers.configurableServiceDialog' ]) //
.controller('ConfigurationPageController', function($scope, $location, thingTypeRepository) {
    $scope.navigateTo = function(path) {
        $location.path('configuration/' + path);
    }
    $scope.thingTypes = [];
    function getThingTypes() {
        thingTypeRepository.getAll(function(thingTypes) {
            $.each(thingTypes, function(i, thingType) {
                $scope.thingTypes[thingType.UID] = thingType;
            });
        });
    }
    $scope.getThingTypeLabel = function(key) {
        if ($scope.thingTypes && Object.keys($scope.thingTypes).length != 0) {
            if ($scope.thingTypes[key]) {
                return $scope.thingTypes[key].label;
            } else {
                return '';
            }
        } else {
            thingTypeRepository.setDirty(false);
        }
    };
    getThingTypes();
}).controller('ServicesController', function($scope, $mdDialog, serviceConfigService, toastService) {
    $scope.setSubtitle([ 'Services' ]);
    $scope.setHeaderText('Shows all configurable services.');
    $scope.tabs = [];
    $scope.refresh = function() {
        serviceConfigService.getAll(function(services) {
            // $scope.services = services;
            var arrOfIndex = [];
            var index = 0;
            angular.forEach(services, function(value) {
                if (arrOfIndex[value.category] === undefined) {
                    arrOfIndex[value.category] = index++;
                }
                if ($scope.tabs[arrOfIndex[value.category]] === undefined) {
                    $scope.tabs[arrOfIndex[value.category]] = [];
                    $scope.tabs[arrOfIndex[value.category]].category = value.category;
                }
                $scope.tabs[arrOfIndex[value.category]].push(value);
            });
        });
    };

    $scope.configure = function(serviceId, configDescriptionURI, event) {
        $mdDialog.show({
            controller : 'ConfigurableServiceDialogController',
            templateUrl : 'partials/dialog.configureservice.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                serviceId : serviceId,
                configDescriptionURI : configDescriptionURI
            }
        });
    }

    $scope.refresh();
}).controller('ThingController', function($scope, $timeout, $mdDialog, thingRepository, bindingRepository, thingService, toastService) {
    $scope.setSubtitle([ 'Things' ]);
    $scope.setHeaderText('Shows all configured Things.');
    $scope.newThingUID = window.localStorage.getItem('thingUID');
    window.localStorage.removeItem('thingUID');
    $scope.things;
    $scope.refresh = function() {
        bindingRepository.getAll(true);
        thingRepository.getAll(function(things) {
            for (var i = 0; i < things.length; i++) {
                things[i].bindingType = things[i].thingTypeUID.split(':')[0];
            }
            $scope.things = things;
            refreshBindings();
        });
    }
    $scope.remove = function(thing, event) {
        event.stopImmediatePropagation();
        $mdDialog.show({
            controller : 'RemoveThingDialogController',
            templateUrl : 'partials/dialog.removething.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                thing : thing
            }
        }).then(function() {
            $scope.refresh();
        });
    }
    $scope.clearAll = function() {
        $scope.searchText = "";
        $scope.$broadcast("ClearFilters");
    }
    $scope.$watch("things", function() {
        refreshBindings();
    })
    function refreshBindings() {
        $scope.bindings = [];
        if ($scope.data && $scope.data.bindings && $scope.data.bindings.length > 0) {
            var arr = [];
            if ($scope.things) {
                for (var i = 0; i < $scope.data.bindings.length; i++) {
                    var a = $.grep($scope.things, function(result) {
                        return result.bindingType == $scope.data.bindings[i].id;
                    });
                    if (a.length > 0) {
                        $scope.bindings.push($scope.data.bindings[i]);
                    }

                }
            }
        }
    }
    $scope.refresh();
}).controller('ViewThingController', function($scope, $mdDialog, toastService, thingTypeService, thingRepository, thingService, linkService, channelTypeService, configService, thingConfigService, util, itemRepository) {
    $scope.setSubtitle([ 'Things' ]);

    var thingUID = $scope.path[4];
    $scope.thingTypeUID = null;
    $scope.advancedMode;
    $scope.thing;
    $scope.thingType;
    $scope.thingChannels = [];
    $scope.showAdvanced = false;
    $scope.channelTypes;
    $scope.items;

    channelTypeService.getAll().$promise.then(function(channels) {
        $scope.channelTypes = channels;
        $scope.refreshChannels(false);
    });
    itemRepository.getAll(function(items) {
        $scope.items = items;
    });

    $scope.remove = function(thing, event) {
        event.stopImmediatePropagation();
        $mdDialog.show({
            controller : 'RemoveThingDialogController',
            templateUrl : 'partials/dialog.removething.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                thing : thing
            }
        }).then(function() {
            $scope.navigateTo('things');
        });
    }

    $scope.enableChannel = function(thingUID, channelID, event, longPress) {
        var channel = $scope.getChannelById(channelID);
        event.stopImmediatePropagation();
        if ($scope.advancedMode) {
            if (channel.linkedItems.length > 0) {
                $scope.getLinkedItems(channel, event);
            } else {
                $scope.linkChannel(channelID, event, longPress);
            }
        } else if (channel.linkedItems.length == 0) {
            linkService.link({
                itemName : $scope.thing.UID.replace(/[^a-zA-Z0-9_]/g, "_") + '_' + channelID.replace(/[^a-zA-Z0-9_]/g, "_"),
                channelUID : $scope.thing.UID + ':' + channelID
            }, function(newItem) {
                $scope.getThing(true);
                toastService.showDefaultToast('Channel linked');
            });
        }
    };

    $scope.disableChannel = function(thingUID, channelID, itemName, event) {
        var channel = $scope.getChannelById(channelID);
        event.stopImmediatePropagation();
        var linkedItem = channel.linkedItems[0];
        if ($scope.advancedMode) {
            $scope.unlinkChannel(channelID, itemName, event);
        } else {
            linkService.unlink({
                itemName : $scope.thing.UID.replace(/[^a-zA-Z0-9_]/g, "_") + '_' + channelID.replace(/[^a-zA-Z0-9_]/g, "_"),
                channelUID : $scope.thing.UID + ':' + channelID
            }, function() {
                $scope.getThing(true);
                toastService.showDefaultToast('Channel unlinked');
            });
        }
    };

    $scope.linkChannel = function(channelID, event, preSelect) {
        var channel = $scope.getChannelById(channelID);
        var channelType = $scope.getChannelTypeByUID(channel.channelTypeUID);
        var params = {
            linkedItems : channel.linkedItems.length > 0 ? channel.linkedItems : '',
            acceptedItemType : channel.itemType,
            category : channelType.category ? channelType.category : '',
            suggestedName : getItemNameSuggestion(channelID, channelType.label),
            suggestedLabel : channelType.label,
            suggestedCategory : channelType.category ? channelType.category : '',
            preSelectCreate : preSelect
        }
        $mdDialog.show({
            controller : 'LinkChannelDialogController',
            templateUrl : 'partials/dialog.linkchannel.html',
            targetEvent : event,
            hasBackdrop : true,
            params : params
        }).then(function(newItem) {
            if (newItem) {
                linkService.link({
                    itemName : newItem.itemName,
                    channelUID : $scope.thing.UID + ':' + channelID
                }, function() {
                    $scope.getThing(true);
                    var item = $.grep($scope.items, function(item) {
                        return item.name == newItem.itemName;
                    });
                    channel.items = channel.items ? channel.items : [];
                    if (item.length > 0) {
                        channel.items.push(item[0]);
                    } else {
                        channel.items.push({
                            name : newItem.itemName,
                            label : newItem.label
                        });
                    }
                    toastService.showDefaultToast('Channel linked');
                });
            }
        });
    }

    function getItemNameSuggestion(channelID, label) {
        var itemName = getInCamelCase($scope.thing.label);
        if (channelID) {
            var id = channelID.split('#');
            if (id.length > 1 && id[0].length > 0) {
                itemName += ('_' + getInCamelCase(id[0]));
            }
            itemName += ('_' + getInCamelCase(label));
        }
        return itemName;
    }

    function getInCamelCase(str) {
        var camelStr = "";
        if (str) {
            var arr = str.split(/[^a-zA-Z0-9_]/g);
            for (var i = 0; i < arr.length; i++) {
                if (arr[i] && arr[i].length > 0) {
                    camelStr += (arr[i][0].toUpperCase() + (arr[i].length > 1 ? arr[i].substring(1, arr[i].length) : ''));
                }
            }
        }
        return camelStr;
    }

    $scope.unlinkChannel = function(channelID, itemName, event) {
        var channel = $scope.getChannelById(channelID);
        $mdDialog.show({
            controller : 'UnlinkChannelDialogController',
            templateUrl : 'partials/dialog.unlinkchannel.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                itemName : itemName
            }
        }).then(function() {
            if (itemName) {
                linkService.unlink({
                    itemName : itemName,
                    channelUID : $scope.thing.UID + ':' + channelID
                }, function() {
                    $scope.getThing(true);
                    var item = $.grep(channel.items, function(item) {
                        return item.name == itemName;
                    });
                    if (item.length > 0) {
                        channel.items.splice(channel.items.indexOf(item[0]), 1);
                    }
                    toastService.showDefaultToast('Channel unlinked');
                });
            }
        });
    }
    $scope.getChannelById = function(channelId) {
        if (!$scope.thing) {
            return;
        }
        return $.grep($scope.thing.channels, function(channel, i) {
            return channelId == channel.id;
        })[0];
    }

    $scope.getChannelTypeByUID = function(channelUID) {
        return thingConfigService.getChannelTypeByUID($scope.thingType, $scope.channelTypes, channelUID);
    };

    $scope.getChannelFromChannelTypes = function(channelUID) {
        if (!$scope.channelTypes) {
            return;
        }
        return thingConfigService.getChannelFromChannelTypes($scope.channelTypes, channelUID);
    };

    var getChannels = function(advanced) {

        if (!$scope.thingType || !$scope.thing || !$scope.channelTypes) {
            return;
        }
        $scope.isAdvanced = checkAdvance($scope.thing.channels);
        return thingConfigService.getThingChannels($scope.thing, $scope.thingType, $scope.channelTypes, advanced);
    };
    $scope.refreshChannels = function(showAdvanced) {
        $scope.thingChannels = getChannels(showAdvanced);
    }

    function checkAdvance(channels) {
        if (channels) {
            for (var i = 0, len = channels.length; i < len; i++) {
                var channel = channels[i];
                var channelType = $scope.getChannelTypeByUID(channel.channelTypeUID);
                if (channelType && channelType.advanced) {
                    return true;
                }
            }
        }
    }

    $scope.getThing = function(refresh) {
        thingRepository.getOne(function(thing) {
            return thing.UID === thingUID;
        }, function(thing) {
            angular.forEach(thing.channels, function(value, i) {
                value.showItems = $scope.thing ? $scope.thing.channels[i].showItems : false;
                value.items = $scope.thing ? $scope.thing.channels[i].items : null;
            });
            $scope.thing = thing;
            checkThingProperties(thing);
            $scope.thingTypeUID = thing.thingTypeUID;
            getThingType();
            $scope.setSubtitle([ 'Things', thing.label ]);
        }, refresh);
    }

    function checkThingProperties(thing) {
        if (thing.properties) {
            var hasFirmwareVersion = thing.properties['firmwareVersion'];
            if ((Object.keys(thing.properties).length > 0 && !hasFirmwareVersion) || (Object.keys(thing.properties).length > 1 && hasFirmwareVersion)) {
                $scope.thing.hasProperties = true;
            } else {
                $scope.thing.hasProperties = false;
            }
        } else {
            $scope.thing.hasProperties = false;
        }
    }
    $scope.getThing(true);

    function getThingType() {
        thingTypeService.getByUid({
            thingTypeUID : $scope.thingTypeUID
        }, function(thingType) {
            $scope.thingType = thingType;
            if (thingType) {
                $scope.thingTypeChannels = thingType.channels && thingType.channels.length > 0 ? thingType.channels : thingType.channelGroups;
                $scope.setHeaderText(thingType.description);
            }
            $scope.refreshChannels($scope.showAdvanced);
        });
    }

    $scope.configChannel = function(channel, thing, event) {

        var channelType = this.getChannelFromChannelTypes(channel.channelTypeUID);

        $mdDialog.show({
            controller : 'ChannelConfigController',
            templateUrl : 'partials/dialog.channelconfig.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                channelType : channelType,
                channelUID : channel.uid,
                thing : thing
            }
        });
    };

    $scope.getLinkedItems = function(channel) {
        channel.showItems = !channel.showItems;
        if (channel.showItems && channel.items === null || channel.items === undefined) {
            channel.items = $.grep($scope.items, function(item) {
                return $.grep(channel.linkedItems, function(linkedItemName) {
                    return linkedItemName == item.name;
                }).length > 0;
            });
        }
    }

    $scope.showDescription = function(channel, channelType) {
        var description = channel.description ? channel.description : channel.channelType ? channel.channelType.description : null;
        if (description) {
            popup = $mdDialog.alert({
                title : channel.label ? channel.label : channel.channelType ? channel.channelType.label : channel.id,
                textContent : description,
                ok : 'Close'
            });
            $mdDialog.show(popup);
        }
    }

    $scope.$watch('thing.channels', function() {
        $scope.refreshChannels($scope.showAdvanced);
    });
}).controller('RemoveThingDialogController', function($scope, $mdDialog, toastService, thingService, thing) {
    $scope.thing = thing;
    if (thing.statusInfo) {
        $scope.isRemoving = thing.statusInfo.status === 'REMOVING';
    }
    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.remove = function(thingUID) {
        var forceRemove = $scope.isRemoving ? true : false;
        thingService.remove({
            thingUID : thing.UID,
            force : forceRemove
        }, function() {
            if (forceRemove) {
                toastService.showDefaultToast('Thing removed (forced).');
            } else {
                toastService.showDefaultToast('Thing removal initiated.');
            }
            $mdDialog.hide();
        }, function() {
            $mdDialog.hide();
        });
    }
}).controller('LinkChannelDialogController', function($rootScope, $scope, $mdDialog, $filter, toastService, itemRepository, itemService, sharedProperties, params) {
    $scope.itemName;
    $scope.linkedItems = params.linkedItems;
    $scope.acceptedItemType = [ params.acceptedItemType ];
    $scope.advancedMode = $rootScope.advancedMode;
    if (params.acceptedItemType == "Color") {
        $scope.acceptedItemType.push("Switch");
        $scope.acceptedItemType.push("Dimmer");
    } else if (params.acceptedItemType == "Dimmer") {
        $scope.acceptedItemType.push("Switch");
    }
    $scope.category = params.category;
    $scope.itemFormVisible = false;
    $scope.itemsList = [];
    itemRepository.getAll(function(items) {
        $scope.items = items;
        $scope.itemsList = $.grep($scope.items, function(item) {
            return $scope.acceptedItemType.indexOf(item.type) != -1;
        });
        $scope.itemsList = $.grep($scope.itemsList, function(item) {
            return $scope.linkedItems.indexOf(item.name) == -1;
        });
        $scope.itemsList.push({
            name : "_createNew",
            type : $scope.acceptedItemType
        });
        $scope.itemsList = $filter('orderBy')($scope.itemsList, "name");
    });
    $scope.checkCreateOption = function() {
        if ($scope.itemName == "_createNew") {
            $scope.itemFormVisible = true;
            sharedProperties.resetParams();
            sharedProperties.updateParams({
                linking : true,
                acceptedItemType : $scope.acceptedItemType,
                suggestedName : params.suggestedName,
                suggestedLabel : params.suggestedLabel,
                suggestedCategory : params.suggestedCategory
            });
        } else {
            $scope.itemFormVisible = false;
        }
    }
    $scope.createAndLink = function() {
        $scope.$broadcast("ItemLinkedClicked");
    }
    $scope.close = function() {
        $mdDialog.cancel();
        sharedProperties.resetParams();
    }
    $scope.link = function(itemName, label) {
        $mdDialog.hide({
            itemName : itemName,
            label : label
        });
    }
    $scope.$on('ItemCreated', function(event, args) {
        event.preventDefault();
        if (args.status) {
            $scope.link(args.itemName, args.label);
        } else {
            toastService.showDefaultToast('Some error occurred');
            $scope.close();
        }
    });

    if (params.preSelectCreate) {
        $scope.itemName = "_createNew";
        $scope.checkCreateOption();
    }
}).controller('UnlinkChannelDialogController', function($scope, $mdDialog, toastService, linkService, itemName) {
    $scope.itemName = itemName;
    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.unlink = function() {
        $mdDialog.hide();
    }
}).controller('EditThingController', function($scope, $mdDialog, toastService, thingTypeService, thingRepository, configService, configDescriptionService, thingService) {
    $scope.setSubtitle([ 'Things' ]);
    $scope.setHeaderText('Click the \'Save\' button to apply the changes.');

    var thingUID = $scope.path[4];
    $scope.thingTypeUID = null;

    $scope.thing = {};
    $scope.groups = [];
    $scope.thingType;
    $scope.isEditing = true;
    var originalThing = {};

    $scope.update = function(thing) {
        thing.configuration = configService.setConfigDefaults(thing.configuration, $scope.parameters, true);
        if (JSON.stringify(originalThing.configuration) !== JSON.stringify(thing.configuration)) {
            thing.configuration = configService.replaceEmptyValues(thing.configuration);
            thingService.updateConfig({
                thingUID : thing.UID
            }, thing.configuration);
        }
        originalThing.configuration = thing.configuration;
        originalThing.channels = thing.channels;
        if (JSON.stringify(originalThing) !== JSON.stringify(thing)) {
            thingService.update({
                thingUID : thing.UID
            }, thing);
        }
        toastService.showDefaultToast('Thing updated');
        $scope.navigateTo('things/view/' + thing.UID);
    };

    $scope.needsBridge = false;
    $scope.bridges = [];
    $scope.getBridges = function() {
        $scope.bridges = [];
        thingRepository.getAll(function(things) {
            for (var i = 0; i < things.length; i++) {
                var thing = things[i];
                for (var j = 0; j < $scope.thingType.supportedBridgeTypeUIDs.length; j++) {
                    var supportedBridgeTypeUID = $scope.thingType.supportedBridgeTypeUIDs[j];
                    if (thing.thingTypeUID === supportedBridgeTypeUID) {
                        $scope.bridges.push(thing);
                    }
                }
            }
        });
    };
    $scope.getThingType = function() {
        thingTypeService.getByUid({
            thingTypeUID : $scope.thingTypeUID
        }, function(thingType) {
            $scope.thingType = thingType;
            $scope.needsBridge = $scope.thingType.supportedBridgeTypeUIDs && $scope.thingType.supportedBridgeTypeUIDs.length > 0;
            if ($scope.needsBridge) {
                $scope.getBridges();
            }
        });
    };
    $scope.getThing = function(refresh) {
        // Get the thing
        thingRepository.getOne(function(thing) {
            return thing.UID === thingUID;
        }, function(thing) {
            $scope.thing = thing;
            angular.copy(thing, originalThing);
            $scope.thingTypeUID = thing.thingTypeUID;

            // Get the thing type
            $scope.getThingType();
            $scope.setSubtitle([ 'Things', 'Edit', thing.label ]);

            // Now get the configuration information for this thing
            configDescriptionService.getByUri({
                uri : "thing:" + thing.UID
            }, function(configDescription) {
                if (configDescription) {
                    $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
                    $scope.configuration = configService.setConfigDefaults($scope.thing.configuration, $scope.parameters)
                }
            });

        }, refresh);
    }
    $scope.$watch('configuration', function() {
        if ($scope.configuration) {
            $scope.thing.configuration = $scope.configuration;
        }
    });
    $scope.getThing(true);
}).controller('ChannelConfigController', function($scope, $mdDialog, toastService, thingRepository, thingService, configService, channelType, channelUID, thing) {
    $scope.parameters = configService.getRenderingModel(channelType.parameters, channelType.parameterGroups);
    $scope.thing = thing;
    $scope.channel = $.grep(thing.channels, function(channel) {
        return channel.uid == channelUID;
    });
    $scope.configuration = $scope.channel[0].configuration;

    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.save = function() {
        var updated = false;
        for (var i = 0; !updated && i < $scope.thing.channels.length; i++) {
            if ($scope.thing.channels[i].uid == $scope.channel.uid) {
                $scope.thing.channels[i].configuration = $scope.configuration;
                updated = true;
            }
        }
        thingService.update({
            thingUID : thing.UID
        }, $scope.thing, function() {
            $mdDialog.hide();
            toastService.showDefaultToast('Channel updated');
        });
    }
});

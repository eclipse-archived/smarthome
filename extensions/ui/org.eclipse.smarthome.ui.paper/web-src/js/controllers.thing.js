angular.module('PaperUI.controllers.configuration.things', [ 'ngRoute', 'ngInputModified' ]).config([ '$routeProvider', function($routeProvider) {
    // Configure the routes for this controller
    $routeProvider.when('/configuration/things/:thingUID', {
        templateUrl : 'partials/configuration.thing.html',
        controller : 'ThingConfigurationController',
        title : 'Configuration'
    })
} ])

.controller('ThingConfigurationController', function($scope, $location, $mdDialog, toastService, thingTypeService, thingRepository, thingService, linkService, channelTypeService, configService, thingConfigService, configDescriptionService, util, itemRepository, thingTypeRepository) {
    $scope.navigateTo = function(path) {
        $location.path('configuration/' + path);
    }

    $scope.thingConfigForm = {modified:false};

    $scope.setSubtitle([ 'Things' ]);

    var thingUID = $scope.path[3];
    $scope.thingTypeUID = null;
    $scope.advancedMode;
    $scope.thing;
    $scope.thingType;
    $scope.thingChannels = [];
    $scope.showAdvanced = false;
    $scope.channelTypes;
    $scope.items;
    $scope.isEditing = true;

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
            category : channelType.category ? channelType.category : "",
            suggestedName : getItemNameSuggestion(channelID, channelType.label),
            suggestedLabel : channel.channelType.label,
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
        var id = channelID.split('#');
        if (id.length > 1 && id[0].length > 0) {
            itemName += ('_' + getInCamelCase(id[0]));
        }
        itemName += ('_' + getInCamelCase(label));
        return itemName;
    }

    function getInCamelCase(str) {
        var arr = str.split(/[^a-zA-Z0-9_]/g);
        var camelStr = "";
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] && arr[i].length > 0) {
                camelStr += (arr[i][0].toUpperCase() + (arr[i].length > 1 ? arr[i].substring(1, arr[i].length) : ''));
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
            $scope.thingTypeUID = thing.thingTypeUID;
            getThingType();
            $scope.setSubtitle(['Things', thing.label]);
            
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
       
    $scope.getThing(true);

    function getThingType() {
        thingTypeService.getByUid({
            thingTypeUID : $scope.thingTypeUID
        }, function(thingType) {
            $scope.thingType = thingType;
            $scope.needsBridge = $scope.thingType.supportedBridgeTypeUIDs && $scope.thingType.supportedBridgeTypeUIDs.length > 0;
            if ($scope.needsBridge) {
                $scope.getBridges();
            }
            if (thingType) {
                $scope.thingTypeChannels = thingType.channels && thingType.channels.length > 0 ? thingType.channels : thingType.channelGroups;
                $scope.setHeaderText(thingType.description);
            }
            $scope.refreshChannels($scope.showAdvanced);
        });
    }

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

    $scope.hasProperties = function(properties) {
        return util.hasProperties(properties);
    }

    $scope.showDescription = function(channel, channelType) {
        var description = channel.description ? channel.description : channel.channelType ? channel.channelType.description : null;
        if (description) {
            popup = $mdDialog.alert({
                title : channel.label ? channel.label : channel.channelType ? channel.channelType.label : channel.id,
                textContent : description,
                ok : 'Close'
            });
            $mdDialog.show(popup).finally(function() {
             popup = undefined;
            });
        }
    }

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
    
    $scope.$watch('configuration', function() {
        if ($scope.configuration) {
            $scope.thing.configuration = $scope.configuration;
        }
    });
    
    $scope.$watch('thing.channels', function() {
        $scope.refreshChannels($scope.showAdvanced);
    });
})

angular.module('PaperUI.controllers.configuration', [ 'PaperUI.constants' ]).controller('ConfigurationPageController', function($scope, $location, thingTypeRepository) {
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
}).controller('BindingController', function($scope, $mdDialog, bindingRepository) {
    $scope.setSubtitle([ 'Bindings' ]);
    $scope.setHeaderText('Shows all installed bindings.');
    $scope.refresh = function() {
        bindingRepository.getAll(true);
    };
    $scope.openBindingInfoDialog = function(bindingId, event) {
        $mdDialog.show({
            controller : 'BindingInfoDialogController',
            templateUrl : 'partials/dialog.bindinginfo.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                bindingId : bindingId
            }
        });
    }
    $scope.configure = function(bindingId, configDescriptionURI, event) {
        $mdDialog.show({
            controller : 'ConfigureBindingDialogController',
            templateUrl : 'partials/dialog.configurebinding.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                bindingId : bindingId,
                configDescriptionURI : configDescriptionURI
            }
        });
    }
    bindingRepository.getAll();
}).controller('BindingInfoDialogController', function($scope, $mdDialog, thingTypeRepository, bindingRepository, bindingId) {
    $scope.binding = undefined;
    bindingRepository.getOne(function(binding) {
        return binding.id === bindingId;
    }, function(binding) {
        $scope.binding = binding;
        $scope.binding.thingTypes = [];
        thingTypeRepository.getAll(function(thingTypes) {
            $.each(thingTypes, function(index, thingType) {
                if (thingType.UID.split(':')[0] === binding.id) {
                    $scope.binding.thingTypes.push(thingType);
                }
            });
        });
    });
    $scope.close = function() {
        $mdDialog.hide();
    }
}).controller('ConfigureBindingDialogController', function($scope, $mdDialog, bindingRepository, bindingService, configService, configDescriptionService, toastService, bindingId, configDescriptionURI) {

    $scope.binding = null;
    $scope.parameters = [];
    $scope.config = {};

    if (configDescriptionURI) {
        $scope.expertMode = false;
        configDescriptionService.getByUri({
            uri : configDescriptionURI
        }, function(configDescription) {
            if (configDescription) {
                $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
            }
        });
    }
    if (bindingId) {
        bindingRepository.getOne(function(binding) {
            return binding.id === bindingId;
        }, function(binding) {
            $scope.binding = binding;
        });
        bindingService.getConfigById({
            id : bindingId
        }).$promise.then(function(config) {
            $scope.configuration = configService.convertValues(config);
            $scope.configArray = configService.getConfigAsArray($scope.configuration);
        }, function(failed) {
            $scope.configuration = {};
            $scope.configArray = configService.getConfigAsArray($scope.configuration);
        });
    } else {
        $scope.newConfig = true;
        $scope.serviceId = '';
        $scope.configuration = {
            '' : ''
        };
        $scope.configArray = [];
        $scope.expertMode = true;
    }
    $scope.close = function() {
        $mdDialog.hide();
    }
    $scope.addParameter = function() {
        $scope.configArray.push({
            name : '',
            value : undefined
        });
    }
    $scope.save = function() {
        if ($scope.expertMode) {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
        $scope.configuration = configService.replaceEmptyValues($scope.configuration);
        bindingService.updateConfig({
            id : bindingId
        }, $scope.configuration, function() {
            $mdDialog.hide();
            toastService.showDefaultToast('Binding config updated.');
        });
    }
    $scope.$watch('expertMode', function() {
        if ($scope.expertMode) {
            $scope.configArray = configService.getConfigAsArray($scope.configuration);
        } else {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
    });
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
    $scope.add = function(serviceId, event) {
        $mdDialog.show({
            controller : 'ConfigureServiceDialogController',
            templateUrl : 'partials/dialog.configureservice.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                serviceId : undefined,
                configDescriptionURI : undefined
            }
        }).then(function() {
            $scope.refresh();
        });
    }
    $scope.configure = function(serviceId, configDescriptionURI, event) {
        $mdDialog.show({
            controller : 'ConfigureServiceDialogController',
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
}).controller('ConfigureServiceDialogController', function($scope, $mdDialog, configService, serviceConfigService, configDescriptionService, toastService, serviceId, configDescriptionURI) {

    $scope.service = null;
    $scope.parameters = [];
    $scope.config = {};

    if (configDescriptionURI) {
        $scope.expertMode = false;
        configDescriptionService.getByUri({
            uri : configDescriptionURI
        }, function(configDescription) {
            if (configDescription) {
                $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
                if (!jQuery.isEmptyObject($scope.configuration)) {
                    $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
                }
            }
        });
    }
    if (serviceId) {
        serviceConfigService.getById({
            id : serviceId
        }, function(service) {
            $scope.service = service;
        });
        serviceConfigService.getConfigById({
            id : serviceId
        }).$promise.then(function(config) {
            if (config) {
                $scope.configuration = configService.convertValues(config);
                $scope.configArray = configService.getConfigAsArray($scope.configuration);
                if ($scope.parameters && $scope.parameters.length > 0) {
                    $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
                }
            }
        });
    } else {
        $scope.newConfig = true;
        $scope.serviceId = '';
        $scope.configuration = {
            '' : ''
        };
        $scope.configArray = [];
        $scope.expertMode = true;
    }
    $scope.close = function() {
        $mdDialog.hide();
    }
    $scope.addParameter = function() {
        $scope.configArray.push({
            name : '',
            value : undefined
        });
    }
    $scope.save = function() {
        if ($scope.expertMode) {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
        $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
        serviceConfigService.updateConfig({
            id : (serviceId ? serviceId : $scope.serviceId)
        }, $scope.configuration, function() {
            $mdDialog.hide();
            toastService.showDefaultToast('Service config updated.');
        });
    }
    $scope.$watch('expertMode', function() {
        if ($scope.expertMode) {
            $scope.configArray = configService.getConfigAsArray($scope.configuration);
        } else {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
    });
}).controller('AddGroupDialogController', function($scope, $mdDialog) {
    $scope.binding = undefined;

    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.add = function(label) {
        $mdDialog.hide(label);
    }
}).controller('ThingController', function($scope, $timeout, $mdDialog, thingRepository, thingService, toastService) {
    $scope.setSubtitle([ 'Things' ]);
    $scope.setHeaderText('Shows all configured Things.');
    $scope.refresh = function() {
        thingRepository.getAll(true);
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
    $scope.refresh();
}).controller('ViewThingController', function($scope, $mdDialog, toastService, thingTypeRepository, thingRepository, thingService, linkService, channelTypeService, configService, thingConfigService, util) {

    var thingUID = $scope.path[4];
    $scope.thingTypeUID = null;

    $scope.thing;
    $scope.thingType;
    $scope.thingChannels = [];
    $scope.showAdvanced = false;
    $scope.channelTypes;
    channelTypeService.getAll().$promise.then(function(channels) {
        $scope.channelTypes = channels;
        $scope.refreshChannels(false);
    });
    $scope.edit = function(thing, event) {
        $mdDialog.show({
            controller : 'EditThingDialogController',
            templateUrl : 'partials/dialog.editthing.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                thing : thing
            }
        });
    };
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

    $scope.enableChannel = function(thingUID, channelID, event) {
        var channel = $scope.getChannelById(channelID);
        if ($scope.advancedMode) {
            $scope.linkChannel(channelID, event);
        } else {
            linkService.link({
                itemName : $scope.thing.UID.replace(/[^a-zA-Z0-9_]/g, "_") + '_' + channelID.replace(/[^a-zA-Z0-9_]/g, "_"),
                channelUID : $scope.thing.UID + ':' + channelID
            }, function() {
                $scope.getThing(true);
                toastService.showDefaultToast('Channel linked');
            });
        }
    };

    $scope.disableChannel = function(thingUID, channelID, event) {
        var channel = $scope.getChannelById(channelID);
        var linkedItem = channel.linkedItems[0];
        if ($scope.advancedMode) {
            $scope.unlinkChannel(channelID, event);
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

    $scope.linkChannel = function(channelID, event) {
        var channel = $scope.getChannelById(channelID);
        var channelType = $scope.getChannelTypeById(channelID);
        $mdDialog.show({
            controller : 'LinkChannelDialogController',
            templateUrl : 'partials/dialog.linkchannel.html',
            targetEvent : event,
            hasBackdrop : true,
            linkedItem : channel.linkedItems.length > 0 ? channel.linkedItems[0] : '',
            acceptedItemType : channel.itemType + 'Item',
            category : channelType.category ? channelType.category : ""
        }).then(function(itemName) {
            linkService.link({
                itemName : itemName,
                channelUID : $scope.thing.UID + ':' + channelID
            }, function() {
                $scope.getThing(true);
                toastService.showDefaultToast('Channel linked');
            });
        });
    }

    $scope.unlinkChannel = function(channelID) {
        var channel = $scope.getChannelById(channelID);
        $mdDialog.show({
            controller : 'UnlinkChannelDialogController',
            templateUrl : 'partials/dialog.unlinkchannel.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                itemName : channel.linkedItems[0]
            }
        }).then(function(itemName) {
            linkService.unlink({
                itemName : channel.linkedItems[0],
                channelUID : $scope.thing.UID + ':' + channelID
            }, function() {
                $scope.getThing(true);
                toastService.showDefaultToast('Channel unlinked');
            });
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

    $scope.getChannelTypeById = function(channelId) {
        return thingConfigService.getChannelTypeById($scope.thingType, $scope.channelTypes, channelId);
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
                var channelType = $scope.getChannelTypeById(channel.id);
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
            $scope.thing = thing;
            $scope.thingTypeUID = thing.thingTypeUID;
            getThingType();
            if (thing.item) {
                $scope.setTitle(thing.label);
            } else {
                $scope.setTitle(thing.UID);
            }
        }, refresh);
    }
    $scope.getThing(true);

    function getThingType() {
        thingTypeRepository.getOne(function(thingType) {
            return thingType.UID === $scope.thingTypeUID;
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
                channel : channel,
                thing : thing
            }
        });
    };

    $scope.hasProperties = function(properties) {
        return util.hasProperties(properties);
    }
}).controller('RemoveThingDialogController', function($scope, $mdDialog, toastService, thingService, thing) {
    $scope.thing = thing;
    $scope.isRemoving = thing.statusInfo.status === 'REMOVING';
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
        });
    }
}).controller('LinkChannelDialogController', function($scope, $mdDialog, $filter, toastService, itemRepository, itemService, linkedItem, acceptedItemType, category) {
    $scope.itemName = linkedItem;
    $scope.acceptedItemType = acceptedItemType;
    $scope.category = category;
    $scope.itemFormVisible = false;
    itemRepository.getAll(function(items) {
        $scope.items = items;
        $scope.items = $filter('filter')($scope.items, {
            type : $scope.acceptedItemType
        });
        $scope.items = $filter('orderBy')($scope.items, "name");
        $scope.items.push({
            name : "_createNew",
            type : $scope.acceptedItemType
        });
    });
    $scope.checkCreateOption = function() {
        if ($scope.itemName == "_createNew") {
            $scope.itemFormVisible = true;
        } else {
            $scope.itemFormVisible = false;
        }
    }
    $scope.createAndLink = function() {
        var item = {
            name : $scope.newItemName,
            label : $scope.itemLabel,
            type : $scope.acceptedItemType,
            category : $scope.category
        };
        itemService.create({
            itemName : $scope.newItemName
        }, item).$promise.then(function() {
            toastService.showDefaultToast("Item created");
            itemRepository.setDirty(true);
            $mdDialog.hide($scope.newItemName);
        });
    }
    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.link = function(itemName) {
        $mdDialog.hide(itemName);
    }
}).controller('UnlinkChannelDialogController', function($scope, $mdDialog, toastService, linkService, itemName) {
    $scope.itemName = itemName;
    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.unlink = function() {
        $mdDialog.hide();
    }
}).controller('EditThingController', function($scope, $mdDialog, toastService, thingTypeRepository, thingRepository, configService, thingService) {
    $scope.setHeaderText('Click the \'Save\' button to apply the changes.');

    var thingUID = $scope.path[4];
    $scope.thingTypeUID = null;

    $scope.thing;
    $scope.groups = [];
    $scope.thingType;
    $scope.isEditing = true;
    var originalThing = {};

    $scope.update = function(thing) {
        if (!thing.item) {
            thing.item = {};
        }
        if (JSON.stringify(originalThing.configuration) !== JSON.stringify(thing.configuration)) {
            thing.configuration = configService.replaceEmptyValues(thing.configuration);
            thingService.updateConfig({
                thingUID : thing.UID
            }, thing.configuration, function() {
                thingRepository.update(thing);
            });
        }
        var dict = {};
        var update = false;
        if (originalThing.label !== thing.label) {
            dict.label = thing.label;
            update = true;
        }
        if (originalThing.bridgeUID !== thing.bridgeUID) {
            dict.bridgeUID = thing.bridgeUID
            update = true;
        }
        if (update) {
            thingService.update({
                thingUID : thing.UID
            }, dict);
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
        thingTypeRepository.getOne(function(thingType) {
            return thingType.UID === $scope.thingTypeUID;
        }, function(thingType) {
            $scope.thingType = thingType;
            $scope.parameters = configService.getRenderingModel(thingType.configParameters, thingType.parameterGroups);
            $scope.needsBridge = $scope.thingType.supportedBridgeTypeUIDs && $scope.thingType.supportedBridgeTypeUIDs.length > 0;
            if ($scope.needsBridge) {
                $scope.getBridges();
            }
        });
    };
    $scope.getThing = function(refresh) {
        thingRepository.getOne(function(thing) {
            return thing.UID === thingUID;
        }, function(thing) {
            $scope.thing = thing;
            angular.copy(thing, originalThing);
            $scope.thingTypeUID = thing.thingTypeUID;
            $scope.getThingType();
            if (thing.item) {
                $scope.setTitle('Edit ' + thing.label);
            } else {
                $scope.setTitle('Edit ' + thing.UID);
            }
        }, refresh);
    }
    $scope.getThing(false);
}).controller('ChannelConfigController', function($scope, $mdDialog, toastService, thingRepository, thingService, configService, channelType, channel, thing) {
    $scope.parameters = configService.getRenderingModel(channelType.parameters, channelType.parameterGroups);
    $scope.configuration = channel.configuration;
    $scope.channel = channel;
    $scope.thing = thing;
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
            thingRepository.update($scope.thing);
            $mdDialog.hide();
            toastService.showDefaultToast('Channel updated');
        });
    }
});

angular.module('PaperUI.controllers.configuration', []).controller('ConfigurationPageController', function($scope, $location, thingTypeRepository) {
    $scope.navigateTo = function(path) {
        $location.path('configuration/' + path);
    }
    $scope.thingTypes = [];
    thingTypeRepository.getAll(function(thingTypes) {
        $.each(thingTypes, function(i, thingType) {
            $scope.thingTypes[thingType.UID] = thingType;
        });
    });
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
            $scope.configuration = config;
            $scope.configArray = configService.getConfigAsArray(config);
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
            $scope.configuration = configService.getConfigAsObject($scope.configArray);
        }
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
            $scope.configuration = configService.getConfigAsObject($scope.configArray);
        }
    });
}).controller('ServicesController', function($scope, $mdDialog, serviceConfigService, toastService) {
    $scope.setSubtitle([ 'Services' ]);
    $scope.setHeaderText('Shows all configurable services.');
    $scope.refresh = function() {
        serviceConfigService.getAll(function(services) {
            $scope.services = services;
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
    $scope.remove = function(serviceId, event) {
        var confirm = $mdDialog.confirm().title('Remove configuration').content('Would you like to remove the service configurarion for the service ' + serviceId + '?').ariaLabel('Remove Service Configuration').ok('Remove').cancel('Cancel').targetEvent(event);
        $mdDialog.show(confirm).then(function() {
            serviceConfigService.deleteConfig({
                id : serviceId
            }, function() {
                toastService.showDefaultToast('Service config deleted.');
                $scope.refresh();
            });
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
                $scope.configuration = config;
                $scope.configArray = configService.getConfigAsArray(config);
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
            $scope.configuration = configService.getConfigAsObject($scope.configArray);
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
            $scope.configuration = configService.getConfigAsObject($scope.configArray);
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
}).controller('ThingController', function($scope, $timeout, $mdDialog, thingRepository, thingSetupService, toastService) {
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
}).controller('ViewThingController', function($scope, $mdDialog, toastService, thingTypeRepository, thingRepository, thingSetupService, linkService, channelTypeService) {

    var thingUID = $scope.path[4];
    $scope.thingTypeUID = null;

    $scope.thing;
    $scope.thingType;
    $scope.thingChannels = [];
    $scope.showAdvanced = false;
    $scope.channelTypes;
    channelTypeService.getAll().$promise.then(function(channels) {
        $scope.channelTypes = channels;
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
        if ($scope.advancedMode) {
            $scope.linkChannel(channelID, event);
        } else {
            thingSetupService.enableChannel({
                channelUID : thingUID + ':' + channelID
            }, function() {
                $scope.getThing(true);
                toastService.showDefaultToast('Channel enabled');
            });
        }
    };

    $scope.disableChannel = function(thingUID, channelID, event) {
        if ($scope.advancedMode) {
            $scope.unlinkChannel(channelID, event);
        } else {
            thingSetupService.disableChannel({
                channelUID : thingUID + ':' + channelID
            }, function() {
                $scope.getThing(true);
                toastService.showDefaultToast('Channel disabled');
            });
        }
    };

    $scope.linkChannel = function(channelID, event) {
        var channel = $scope.getChannelById(channelID);
        $mdDialog.show({
            controller : 'LinkChannelDialogController',
            templateUrl : 'partials/dialog.linkchannel.html',
            targetEvent : event,
            hasBackdrop : true,
            linkedItem : channel.linkedItems.length > 0 ? channel.linkedItems[0] : '',
            acceptedItemType : channel.itemType + 'Item'
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
        var linkedItem = channel.linkedItems[0];
        linkService.unlink({
            itemName : linkedItem,
            channelUID : $scope.thing.UID + ':' + channelID
        }, function() {
            $scope.getThing(true);
            toastService.showDefaultToast('Channel unlinked');
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
        if ($scope.thingType) {
            var cid_part = channelId.split('#', 2)
            if (cid_part.length == 1) {
                var c, c_i, c_l;
                for (c_i = 0, c_l = $scope.thingType.channels.length; c_i < c_l; ++c_i) {
                    c = $scope.thingType.channels[c_i];
                    if (c.id == channelId) {
                        return c;
                    }
                }
            } else if (cid_part.length == 2) {
                var cg, cg_i, cg_l;
                var c, c_i, c_l;
                for (cg_i = 0, cg_l = $scope.thingType.channelGroups.length; cg_i < cg_l; ++cg_i) {
                    cg = $scope.thingType.channelGroups[cg_i];
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
        if ($scope.channelTypes) {
            var c = {}, c_i, c_l;
            for (c_i = 0, c_l = $scope.channelTypes.length; c_i < c_l; ++c_i) {
                c = $scope.channelTypes[c_i];
                c.advanced = false;
                var id = c.UID.split(':', 2)
                if (id[1] == channelId) {
                    return c;
                }
            }
        }
        return;
    };

    var getChannels = function(advanced) {

        if (!$scope.thingType || !$scope.thing) {
            return;
        }
        var thingChannels = [];
        var includedChannels = [];
        $scope.isAdvanced = checkAdvance($scope.thing.channels);
        if ($scope.thingType.channelGroups && $scope.thingType.channelGroups.length > 0) {
            for (var i = 0; i < $scope.thingType.channelGroups.length; i++) {
                var group = {};
                group.name = $scope.thingType.channelGroups[i].label;
                group.description = $scope.thingType.channelGroups[i].description;
                group.channels = matchGroup($scope.thing.channels, $scope.thingType.channelGroups[i].id);
                group.channels = advanced ? group.channels : filterAdvance(group.channels, false);
                includedChannels = includedChannels.concat(group.channels);
                thingChannels.push(group);
            }
            var group = {
                "name" : "Others",
                "description" : "Other channels",
                "channels" : []
            };
            for (var i = 0; i < $scope.thing.channels.length; i++) {
                if (includedChannels.indexOf($scope.thing.channels[i]) == -1) {
                    group.channels.push($scope.thing.channels[i]);
                }
            }
            if (group.channels && group.channels.length > 0) {
                thingChannels.push(group);
            }
        } else {
            var group = {};
            group.channels = advanced ? $scope.thing.channels : filterAdvance($scope.thing.channels, advanced);
            thingChannels.push(group);
        }

        return thingChannels;
    };
    $scope.refreshChannels = function(showAdvanced) {
        $scope.thingChannels = getChannels(showAdvanced);
    };

    function checkAdvance(channels) {
        angular.forEach(channels, function(value) {
            if (value.advanced) {
                return true;
            }
        });
        return false;
    }

    function filterAdvance(channels, advanced) {
        return $.grep(channels, function(channel, i) {
            var channelType = $scope.getChannelTypeById(channel.id);
            return channelType ? advanced == channelType.advanced : true;
        });
    }
    function matchGroup(arr, id) {
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
    }
    $scope.getThing = function(refresh) {
        thingRepository.getOne(function(thing) {
            return thing.UID === thingUID;
        }, function(thing) {
            $scope.thing = thing;
            $scope.refreshChannels(false);
            $scope.thingTypeUID = thing.thingTypeUID;
            if (thing.item) {
                $scope.setTitle(thing.label);
            } else {
                $scope.setTitle(thing.UID);
            }
        }, refresh);
    }
    $scope.getThing(true);

    thingTypeRepository.getOne(function(thingType) {
        return thingType.UID === $scope.thingTypeUID;
    }, function(thingType) {
        $scope.thingType = thingType;
        if (thingType) {
            $scope.thingTypeChannels = thingType.channels && thingType.channels.length > 0 ? thingType.channels : thingType.channelGroups;
            $scope.setHeaderText(thingType.description);
        }
        $scope.refreshChannels(false);
    });
}).controller('RemoveThingDialogController', function($scope, $mdDialog, toastService, thingSetupService, thing) {
    $scope.thing = thing;
    $scope.isRemoving = thing.statusInfo.status === 'REMOVING';
    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.remove = function(thingUID) {
        var forceRemove = $scope.isRemoving ? true : false;
        thingSetupService.remove({
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
}).controller('LinkChannelDialogController', function($scope, $mdDialog, toastService, itemRepository, linkedItem, acceptedItemType) {
    itemRepository.getAll(function(items) {
        $scope.items = items;
    });
    $scope.itemName = linkedItem;
    $scope.acceptedItemType = acceptedItemType;
    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.link = function(itemName) {
        $mdDialog.hide(itemName);
    }
}).controller('EditThingController', function($scope, $mdDialog, toastService, thingTypeRepository, thingRepository, thingSetupService, configService, thingService) {

    $scope.setHeaderText('Click the \'Save\' button to apply the changes.');

    var thingUID = $scope.path[4];
    $scope.thingTypeUID = null;

    $scope.thing;
    $scope.groups = [];
    $scope.thingType;
    var originalThing = {};

    $scope.update = function(thing) {
        if (!thing.item) {
            thing.item = {};
        }
        if (JSON.stringify(originalThing.configuration) !== JSON.stringify(thing.configuration)) {
            thingService.updateConfig({
                thingUID : thing.UID
            }, thing.configuration, function() {
                thingRepository.update(thing);
            });
        }

        if (originalThing.label !== thing.label) {
            thingService.update({
                thingUID : thing.UID
            }, {
                label : thing.label,
                configuration : {}
            });
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
});
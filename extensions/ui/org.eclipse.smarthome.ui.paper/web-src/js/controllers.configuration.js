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
                $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
                $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);
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
            $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
            $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);

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
        var configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters, true);
        bindingService.updateConfig({
            id : bindingId
        }, configuration, function() {
            $mdDialog.hide();
            toastService.showDefaultToast('Binding config updated.');
        });
    }
    $scope.$watch('expertMode', function() {
        if ($scope.expertMode) {
            $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);
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
        var configuration = {};
        if ($scope.expertMode) {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
        var configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters, true);
        serviceConfigService.updateConfig({
            id : (serviceId ? serviceId : $scope.serviceId)
        }, configuration, function() {
            toastService.showDefaultToast('Service config updated.');
        });
        $mdDialog.hide();
    }
    $scope.$watch('expertMode', function() {
        if ($scope.expertMode) {
            $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);
        } else {
            $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
        }
    });
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

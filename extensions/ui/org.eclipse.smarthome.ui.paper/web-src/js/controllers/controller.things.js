angular.module('PaperUI.controllers.things') //
.controller('ThingController', function($scope, $timeout, $location, $mdDialog, thingRepository, thingTypeRepository, bindingRepository, thingService, toastService) {
    $scope.navigateTo = function(path) {
        if (path.startsWith("/")) {
            $location.path(path);
        } else {
            $location.path('configuration/things/' + path);
        }
    }

    $scope.setSubtitle([ 'Things' ]);
    $scope.setHeaderText('Shows all configured Things.');
    $scope.bindings = []; // used for the things filter
    $scope.thingTypes = [];
    $scope.things;

    $scope.refresh = function() {
        refreshThingTypes().then(function() {
            thingRepository.getAll(function(things) {
                angular.forEach(things, function(thing) {
                    thing.bindingType = thing.thingTypeUID.split(':')[0];
                })
                $scope.things = things;
            })
        })
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

    $scope.getThingTypeLabel = function(key) {
        var thingType = $scope.thingTypes[key]
        return thingType ? thingType.label : '';
    };

    $scope.clearAll = function() {
        $scope.searchText = "";
        $scope.$broadcast("ClearFilters");
    }

    $scope.$watch("things", function(newThings, oldThings) {
        refreshBindings();
    })

    function refreshThingTypes() {
        return thingTypeRepository.getAll(function(thingTypes) {
            angular.forEach(thingTypes, function(thingType) {
                $scope.thingTypes[thingType.UID] = thingType;
            });
        });
    }

    function refreshBindings() {
        bindingRepository.getAll(function(bindings) {
            var filteredBindings = new Set();
            angular.forEach($scope.things, function(thing) {
                var binding = bindings.filter(function(binding) {
                    return binding.id === thing.bindingType
                })
                filteredBindings.add(binding[0])
            })
            $scope.bindings = Array.from(filteredBindings)
        }, true);
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
}).controller('ChannelConfigController', function($scope, $mdDialog, toastService, thingService, configService, channelType, channel, thing) {
    $scope.parameters = configService.getRenderingModel(channelType.parameters, channelType.parameterGroups);
    $scope.configuration = {}
    angular.copy(channel.configuration, $scope.configuration)

    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.save = function() {
        channel.configuration = $scope.configuration
        thingService.update({
            thingUID : thing.UID
        }, thing, function() {
            $mdDialog.hide();
            toastService.showDefaultToast('Channel updated');
        });
    }
})
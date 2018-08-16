angular.module('PaperUI.control') //
.controller('ControlPageController', function($scope, $routeParams, $location, $timeout, $filter, itemRepository, thingTypeRepository, util, thingRepository, channelTypeRepository) {
    $scope.tabs = [];
    $scope.selectedTabIndex;

    var selectedTabName = $routeParams.tab;

    $scope.navigateTo = function(path) {
        $location.path(path);
    }

    $scope.refresh = function() {
        itemRepository.getAll(function() {
            channelTypeRepository.getAll(function() {
                thingTypeRepository.getAll(function() {
                    renderTabs();
                })
            })
        });
    }

    $scope.onSelectedTab = function(tab) {
        var index = $scope.tabs.indexOf(tab);
        masonry(index);
        $location.path('/control').search('tab', tab.name.toLowerCase());
    }

    function renderTabs() {
        thingRepository.getAll(function(things) {
            $scope.tabs = getTabs(things);
            if (selectedTabName) {
                var selectedTab = $scope.tabs.find(function(tab) {
                    return tab.name === selectedTabName.toUpperCase();
                });
                $scope.selectedTabIndex = selectedTab ? $scope.tabs.indexOf(selectedTab) : 0;
                masonry($scope.selectedTabIndex);
            }
        })
    }

    function getTabs(things) {
        if (!things) {
            return [];
        }

        var locations = new Set();
        angular.forEach(things, function(thing) {
            var location = thing.location ? thing.location.toUpperCase() : 'OTHER'
            thing.location = location
            locations.add(location)
        })

        var renderedTabs = Array.from(locations)
        renderedTabs = renderedTabs.sort(function(a, b) {
            if (a === 'OTHER') {
                return 1;
            }
            if (b === 'OTHER') {
                return -1;
            }

            return a < b ? -1 : a > b ? 1 : 0
        })

        return renderedTabs.map(function(location) {
            return {
                name : location,
                hasThings : false
            }
        });
    }

    function masonry(index) {
        $timeout(function() {
            new Masonry('#items-' + index, {});
        }, 100, false);
    }

    $scope.refresh();

}).controller('ControlController', function($scope, $timeout, $filter, itemService, util, $attrs, thingRepository, channelTypeRepository, thingTypeRepository, thingConfigService, imageService) {
    $scope.things = [];
    var renderedThings = []

    var renderItems = function() {
        thingRepository.getAll(function(things) {
            var thingsForTab = things.filter(function(thing) {
                var thingLocation = thing.location ? thing.location.toUpperCase() : 'OTHER'
                return thingLocation === $scope.tab.name;
            })
            channelTypeRepository.getAll(function(channelTypes) {
                angular.forEach(thingsForTab, function(thing) {
                    thingTypeRepository.getOne(function(thingType) {
                        return thingType.UID === thing.thingTypeUID
                    }, function(thingType) {
                        var renderedThing = renderThing(thing, thingType, channelTypes);
                        if (renderedThing) {
                            renderedThings.push(renderedThing);
                            renderedThings = renderedThings.sort(function(a, b) {
                                return a.label < b.label ? -1 : a.label > b.label ? 1 : 0
                            })
                            $scope.tab.hasThings = renderedThings.length > 0;
                            $scope.things = renderedThings;
                        }
                    }, false)
                })
            }, false)
        }, false)
    }

    function renderThing(thing, thingType, channelTypes) {
        thing.thingChannels = thingConfigService.getThingChannels(thing, thingType, channelTypes, true);
        angular.forEach(thing.thingChannels, function(thingChannel) {
            thingChannel.channels = thingChannel.channels.filter(function(channel) {
                return channel.linkedItems.length > 0;
            });
        });

        var hasChannels = false;
        angular.forEach(thing.thingChannels, function(channelGroup) {
            angular.forEach(channelGroup.channels, function(channel) {
                channel.items = getItems(channel.linkedItems)
                hasChannels = true;
            })
        })

        if (hasChannels) {
            return thing;
        }
    }

    var getItems = function(itemNames) {
        var items = $scope.data.items.filter(function(item) {
            return itemNames.indexOf(item.name) >= 0
        })
        angular.forEach(items, function(item) {

            if ((item.type && (item.type.indexOf("Number") === 0 || item.type == "Rollershutter")) || (item.groupType && item.groupType.indexOf("Number") === 0)) {
                var state = '' + item.state;
                if (state.indexOf(' ') > 0) {
                    item.unit = state.substring(state.indexOf(' ') + 1);
                    state = state.substring(0, state.indexOf(' '));
                }
                var parsedValue = Number(state);
                if (!isNaN(parsedValue)) {
                    item.state = parsedValue;
                }
            }
            if (item.type && item.type == "Image") {
                imageService.getItemState(item.name).then(function(state) {
                    item.state = state;
                    item.imageLoaded = true;
                });
                item.imageLoaded = false;
            }
            item.stateText = util.getItemStateText(item);

            item.readOnly = isReadOnly(item);
        })

        return items;
    }

    var isReadOnly = function(item) {
        return item.stateDescription ? item.stateDescription.readOnly : false;
    }

    renderItems();

});
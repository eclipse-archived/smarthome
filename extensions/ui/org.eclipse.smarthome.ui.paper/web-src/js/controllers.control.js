angular.module('PaperUI.controllers.control', []) //
.controller('ControlPageController', function($scope, $routeParams, $location, $timeout, $filter, itemRepository, thingTypeRepository, util, thingRepository, channelTypeRepository) {
    $scope.tabs = [];

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

    function renderTabs() {
        thingRepository.getAll(function(things) {
            $scope.tabs = getTabs(things);
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
                thingCount : 1
            }
        });
    }

    $scope.refresh();

}).controller('ControlController', function($scope, $timeout, $filter, itemService, util, $attrs, thingRepository, channelTypeRepository, thingTypeRepository, thingConfigService, imageService) {

    var tabName = $scope.$parent.tab.name
    var tabIndex = $scope.$parent.$index

    $scope.things = [];
    var renderedThings = []

    var renderItems = function() {
        var redraw;
        thingRepository.getAll(function(things) {
            var thingsForTab = things.filter(function(thing) {
                var thingLocation = thing.location ? thing.location.toUpperCase() : 'OTHER'
                return thingLocation === tabName;
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
                            $timeout.cancel(redraw)
                            redraw = $timeout(function() {
                                $scope.things = renderedThings;
                                masonry()
                            }, 0, true)
                        }
                    }, false)
                })
            }, false)
        }, false)
    }

    var masonry = function() {
        $timeout(function() {
            new Masonry('#items-' + tabIndex, {});
        }, 100, false);
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

            if (item.type && (item.type == "Number" || item.groupType == "Number" || item.type == "Rollershutter")) {
                var parsedValue = Number(item.state);
                if (isNaN(parsedValue)) {
                    item.state = null;
                } else {
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

    $scope.getItemName = function(itemName) {
        return itemName.replace(/_/g, ' ');
    }

    $scope.getMinText = function(item) {
        if (!item.stateDescription || isNaN(item.stateDescription.minimum)) {
            return '';
        } else if (!item.stateDescription.pattern) {
            return '' + item.stateDescription.minimum;
        } else {
            return sprintf(item.stateDescription.pattern, item.stateDescription.minimum);
        }
    }

    $scope.getMaxText = function(item) {
        if (!item.stateDescription || isNaN(item.stateDescription.maximum)) {
            return '';
        } else if (!item.stateDescription.pattern) {
            return '' + item.stateDescription.maximum;
        } else {
            return sprintf(item.stateDescription.pattern, item.stateDescription.maximum);
        }
    }

    var categories = {
        'Alarm' : {},
        'Battery' : {},
        'Blinds' : {},
        'ColorLight' : {
            label : 'Color',
            icon : 'wb_incandescent'
        },
        'Contact' : {},
        'DimmableLight' : {
            label : 'Brightness',
            icon : 'wb_incandescent',
            showSwitch : true
        },
        'CarbonDioxide' : {
            label : 'CO2'
        },
        'Door' : {},
        'Energy' : {},
        'Fan' : {},
        'Fire' : {},
        'Flow' : {},
        'GarageDoor' : {},
        'Gas' : {},
        'Humidity' : {},
        'Light' : {},
        'Motion' : {},
        'MoveControl' : {},
        'Player' : {},
        'PowerOutlet' : {},
        'Pressure' : {
        // icon: 'home-icon-measure_pressure_bar'
        },
        'Rain' : {},
        'Recorder' : {},
        'Smoke' : {},
        'SoundVolume' : {
            label : 'Volume',
            icon : 'volume_up'
        },
        'Switch' : {},
        'Temperature' : {
            label : 'Temperature'
        },
        'Water' : {},
        'Wind' : {},
        'Window' : {},
        'Zoom' : {},
    }

    $scope.getLabel = function(item, defaultLabel) {
        if (item.name) {
            return item.label;
        }

        if (item.category) {
            var category = categories[item.category];
            if (category) {
                return category.label ? category.label : item.category;
            } else {
                return defaultLabel;
            }
        }

        return defaultLabel;
    }

    $scope.getIcon = function(itemCategory, fallbackIcon) {
        var defaultIcon = fallbackIcon ? fallbackIcon : 'radio_button_unchecked';

        if (itemCategory && categories[itemCategory] && categories[itemCategory].icon) {
            return categories[itemCategory].icon
        }

        return defaultIcon;
    }
    $scope.showSwitch = function(itemCategory) {
        if (itemCategory) {
            var category = categories[itemCategory];
            if (category) {
                return category.showSwitch;
            }
        }
        return false;
    }

    /**
     * Check if the item has a configured option list. Returns true if there are options, otherwise false.
     * 
     * @param item
     *            the current item
     */
    $scope.isOptionList = function(item) {
        return (item.stateDescription != null && item.stateDescription.options.length > 0)
    }

    renderItems()

}).controller('ItemController', function($rootScope, $scope, itemService, util) {
    $scope.editMode = false;
    $scope.sendCommand = function(command, updateState) {
        $rootScope.itemUpdates[$scope.item.name] = new Date().getTime();
        itemService.sendCommand({
            itemName : $scope.item.name
        }, command);
        if (updateState) {
            $scope.item.state = command;
        }
        $scope.item.stateText = util.getItemStateText($scope.item);
    };
    $scope.editState = function() {
        $scope.editMode = true;
    };
    $scope.updateState = function() {
        $scope.sendCommand($scope.item.state, false);
        $scope.editMode = false;
    };
}).controller('DefaultItemController', function($scope, itemService) {
    $scope.longEditMode = $scope.shortEditMode = false;
    $scope.optionListChanged = function() {
        $scope.sendCommand($scope.item.state, false);
    };
    $scope.editState = function(shortField) {
        if (shortField) {
            $scope.shortEditMode = true;
        } else {
            $scope.longEditMode = true;
        }
    };
    $scope.updateState = function(shortField) {
        $scope.sendCommand($scope.item.state, false);
        if (shortField) {
            $scope.shortEditMode = false;
        } else {
            $scope.longEditMode = false;
        }
    };

}).controller('ImageItemController', function($scope, itemService) {

    $scope.refreshCameraImage = function() {
        itemService.sendCommand({
            itemName : $scope.item.name
        }, "REFRESH");
    };

}).controller('SwitchItemController', function($scope, $timeout, itemService) {
    if ($scope.item.state === 'UNDEF' || $scope.item.state === 'NULL') {
        $scope.item.state = '-';
    }
    $scope.setOn = function(state) {
        $scope.sendCommand(state);
    }
}).controller('DimmerItemController', function($scope, $timeout, itemService) {
    if ($scope.item.state === 'UNDEF' || $scope.item.state === 'NULL') {
        $scope.item.state = '-';
    }
    $scope.on = parseInt($scope.item.state) > 0 ? 'ON' : 'OFF';

    $scope.setOn = function(on) {
        $scope.on = on === 'ON' ? 'ON' : 'OFF';

        $scope.sendCommand(on);

        var brightness = parseInt($scope.item.state);
        if (on === 'ON' && brightness === 0) {
            $scope.item.state = 100;
        }
        if (on === 'OFF' && brightness > 0) {
            $scope.item.state = 0;
        }
    }
    $scope.pending = false;
    $scope.setBrightness = function(brightness) {
        // send updates every 300 ms only
        if (!$scope.pending) {
            $timeout(function() {
                var command = $scope.item.state === 0 ? '0' : $scope.item.state;
                $scope.sendCommand(command);
                $scope.pending = false;
            }, 300);
            $scope.pending = true;
        }
    }
    $scope.$watch('item.state', function() {
        var brightness = parseInt($scope.item.state);
        if (brightness > 0 && $scope.on === 'OFF') {
            $scope.on = 'ON';
        }
        if (brightness === 0 && $scope.on === 'ON') {
            $scope.on = 'OFF';
        }
    });
}).controller('ColorItemController', function($scope, $timeout, $element, itemService) {

    if ($scope.item.state === 'UNDEF' || $scope.item.state === 'NULL') {
        $scope.item.state = '-';
    }
    function getStateAsObject(state) {
        var stateParts = state.split(",");
        if (stateParts.length == 3) {
            return {
                h : parseInt(stateParts[0]),
                s : parseInt(stateParts[1]),
                b : parseInt(stateParts[2])
            }
        } else {
            return {
                h : 0,
                s : 0,
                b : 0
            }
        }
    }

    function toState(stateObject) {
        return Math.ceil(stateObject.h) + ',' + Math.ceil(stateObject.s) + ',' + Math.ceil(stateObject.b);
    }

    $scope.pending = false;

    $scope.setBrightness = function(brightness) {
        $scope.brightness = brightness;
        if (!$scope.pending) {
            $timeout(function() {
                var command = isNaN($scope.brightness) ? '0' : $scope.brightness;
                $scope.sendCommand(command);
                $scope.pending = false;
            }, 300);
            $scope.pending = true;
        }
    }

    $scope.setHue = function(hue) {
        $scope.hue = hue;
        setColor();
    }

    $scope.setSaturation = function(saturation) {
        $scope.saturation = saturation;
        setColor();
    }

    function setColor() {
        // send updates every 300 ms only
        if (!$scope.pending) {
            $timeout(function() {
                var stateObject = getStateAsObject($scope.item.state);
                stateObject.b = isNaN($scope.brightness) ? '0' : $scope.brightness;
                stateObject.s = isNaN($scope.saturation) ? '0' : $scope.saturation;
                stateObject.h = isNaN($scope.hue) ? '0' : $scope.hue;
                if ($scope.item.state == "UNDEF" || $scope.item.state === 'NULL' || $scope.item.state === '-') {
                    stateObject.b = 100;
                    stateObject.s = 100;
                    $scope.brightness = stateObject.b;
                    $scope.saturation = stateObject.s;
                }
                $scope.item.state = toState(stateObject);
                $scope.sendCommand($scope.item.state);
                $scope.pending = false;
            }, 300);
            $scope.pending = true;
        }
    }

    $scope.getHexColor = function(hue) {
        var hsv = tinycolor({
            h : hue,
            s : 1,
            v : 1
        }).toHsv();
        return tinycolor(hsv).toHexString();
    }

    var setStates = function() {
        var stateObject = getStateAsObject($scope.item.state);
        var hue = stateObject.h;
        var brightness = stateObject.b;
        var saturation = stateObject.s;

        $scope.hue = hue ? hue : 0;
        $scope.brightness = brightness ? brightness : 0;
        $scope.saturation = saturation ? saturation : 0;
    }

    setStates();

    $scope.$watch('item.state', function() {
        setStates();
    });

    $scope.$watch('hue', function() {
        var hexColor = $scope.getHexColor($scope.hue);
        $($element).find('.hue .md-thumb').css('background-color', hexColor);
    });

    var hexColor = $scope.getHexColor();
    $($element).find('.hue .md-thumb').css('background-color', hexColor);
}).controller('RollershutterItemController', function($scope) {
    if ($scope.item.state === 'UNDEF' || $scope.item.state === 'NULL') {
        $scope.item.state = '-';
    }
}).controller('PlayerItemController', function($scope, $timeout) {

    var isInterrupted, time;
    $scope.onPrevDown = function() {
        isInterrupted = false;
        time = new Date().getTime();
        $timeout(function() {
            if (!isInterrupted) {
                $scope.sendCommand('REWIND');
            }
        }, 300);
    }

    $scope.onPrevUp = function() {
        var newTime = new Date().getTime();
        if (time + 300 > newTime) {
            isInterrupted = true;
            $scope.sendCommand('PREVIOUS');
        } else {
            $timeout(function() {
                $scope.sendCommand('PLAY');
            });
        }
    }

    $scope.onNextDown = function() {
        isInterrupted = false;
        time = new Date().getTime();
        $timeout(function() {
            if (!isInterrupted) {
                $scope.sendCommand('FASTFORWARD');
            }
        }, 300);
    }

    $scope.onNextUp = function() {
        var newTime = new Date().getTime();
        if (time + 300 > newTime) {
            isInterrupted = true;
            $scope.sendCommand('NEXT');
        } else {
            $timeout(function() {
                $scope.sendCommand('PLAY');
            });
        }
    }

}).controller('LocationItemController', function($scope, $sce) {
    $scope.init = function() {
        if ($scope.item.state !== 'UNDEF' && $scope.item.state !== 'NULL') {
            var latitude = parseFloat($scope.item.state.split(',')[0]);
            var longitude = parseFloat($scope.item.state.split(',')[1]);
            var bbox = (longitude - 0.01) + ',' + (latitude - 0.01) + ',' + (longitude + 0.01) + ',' + (latitude + 0.01);
            var marker = latitude + ',' + longitude;
            $scope.formattedState = latitude + '°N ' + longitude + '°E';
            $scope.url = $sce.trustAsResourceUrl('https://www.openstreetmap.org/export/embed.html?bbox=' + bbox + '&marker=' + marker);
        } else {
            $scope.formattedState = '- °N - °E';
        }
    };
    $scope.$watch('item.state', function() {
        $scope.init();
    });
    $scope.init();
}).directive('itemStateDropdown', function() {
    return {
        restrict : 'A',
        templateUrl : "partials/item.state.dropdown.html"
    };
})

angular.module('PaperUI.controllers.control', []).controller('ControlPageController', function($scope, $routeParams, $location, $timeout, itemRepository) {
    $scope.items = [];
    $scope.selectedIndex = 0;
    $scope.tabs = [];

    $scope.next = function() {
        var newIndex = $scope.selectedIndex + 1;
        if (newIndex > ($scope.tabs.length - 1)) {
            newIndex = 0;
        }
        $scope.selectedIndex = newIndex;
    }
    $scope.prev = function() {
        var newIndex = $scope.selectedIndex - 1;
        if (newIndex < 0) {
            newIndex = $scope.tabs.length - 1;
        }
        $scope.selectedIndex = newIndex;
    }

    $scope.refresh = function() {
        itemRepository.getAll(function(items) {
            $scope.tabs = [];
            $scope.items['All'] = items;
            for (var int = 0; int < items.length; int++) {
                var item = items[int];
                if (item.type === 'GroupItem') {
                    if (item.tags.indexOf("home-group") > -1) {
                        $scope.tabs.push({
                            name : item.name,
                            label : item.label
                        });
                    }
                }
            }
        }, true);
    }

    $scope.getItem = function(itemName) {
        for (var int = 0; int < $scope.data.items.length; int++) {
            var item = $scope.data.items[int];
            if (item.name === itemName) {
                return item;
            }
        }
        return null;
    }

    $scope.masonry = function() {
        if ($scope.data.items) {
            $timeout(function() {
                var itemContainer = '#items-' + $scope.selectedIndex;
                new Masonry(itemContainer, {});
            }, 100, true);
        }
    }
    $scope.$on('ngRepeatFinished', function(ngRepeatFinishedEvent) {
        $scope.masonry();
    });
    $scope.refresh();
}).controller('ControlController', function($scope, $timeout, itemService) {
    $scope.getItemName = function(itemName) {
        return itemName.replace(/_/g, ' ');
    }

    $scope.getStateText = function(item) {
        if (item.state === 'NULL' || item.state === 'UNDEF') {
            return '-';
        }
        var state = item.type === 'NumberItem' ? parseFloat(item.state) : item.state;

        if (!item.stateDescription || !item.stateDescription.pattern) {
            return state;
        } else {
            return sprintf(item.stateDescription.pattern, state);
        }
    }

    $scope.getMinText = function(item) {
        if (!item.stateDescription || !item.stateDescription.minimum) {
            return '';
        } else if (!item.stateDescription.pattern) {
            return item.stateDescription.minimum;
        } else {
            return sprintf(item.stateDescription.pattern, item.stateDescription.minimum);
        }
    }

    $scope.getMaxText = function(item) {
        if (!item.stateDescription || !item.stateDescription.maximum) {
            return '';
        } else if (!item.stateDescription.pattern) {
            return item.stateDescription.maximum;
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
            icon : 'wb_incandescent'
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
            icon : 'volume_up',
            hideSwitch : true
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

    $scope.getLabel = function(itemCategory, label, defaultLabel) {
        if (label) {
            return label;
        }

        if (itemCategory) {
            var category = categories[itemCategory];
            if (category) {
                return category.label ? category.label : itemCategory;
            } else {
                return defaultLabel;
            }
        } else {
            return defaultLabel;
        }
    }
    $scope.getIcon = function(itemCategory, fallbackIcon) {
        var defaultIcon = fallbackIcon ? fallbackIcon : 'radio_button_unchecked';
        if (itemCategory) {
            var category = categories[itemCategory];
            if (category) {
                return category.icon ? category.icon : defaultIcon;
            } else {
                return defaultIcon;
            }
        } else {
            return defaultIcon;
        }
    }
    $scope.isHideSwitch = function(itemCategory) {
        if (itemCategory) {
            var category = categories[itemCategory];
            if (category) {
                return category.hideSwitch;
            }
        }
        return false;
    }
    $scope.isReadOnly = function(item) {
        return item.stateDescription ? item.stateDescription.readOnly : false;
    }

    /**
     * Check if the item has a configured option list. Returns true if there are
     * options, otherwise false.
     * 
     * @param item
     *            the current item
     */
    $scope.isOptionList = function(item) {
        return (item.stateDescription != null && item.stateDescription.options.length > 0)
    }
}).controller('ItemController', function($rootScope, $scope, itemService) {
    $scope.editMode = false;
    $scope.sendCommand = function(command, updateState) {
        $rootScope.itemUpdates[$scope.item.name] = new Date().getTime();
        itemService.sendCommand({
            itemName : $scope.item.name
        }, command);
        if (updateState) {
            $scope.item.state = command;
        }
    };
    $scope.editState = function() {
        $scope.editMode = true;
    };
    $scope.updateState = function() {
        $scope.sendCommand($scope.item.state, false);
        $scope.editMode = false;
    };
}).controller('DefaultItemController', function($scope, itemService) {

    $scope.optionListChanged = function() {
        $scope.sendCommand($scope.item.state, false);
    };

}).controller('ImageItemController', function($scope, itemService) {

    $scope.refreshCameraImage = function() {
        itemService.sendCommand({
            itemName : $scope.item.name
        }, "REFRESH");
    };

}).controller('SwitchItemController', function($scope, $timeout, itemService) {
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
        // send updates every 300 ms only
        if (!$scope.pending) {
            $timeout(function() {
                var stateObject = getStateAsObject($scope.item.state);
                stateObject.b = $scope.brightness === 0 ? '0' : $scope.brightness;
                stateObject.s = $scope.saturation === 0 ? '0' : $scope.saturation;
                stateObject.h = $scope.hue === 0 ? '0' : $scope.hue;
                $scope.item.state = toState(stateObject);
                $scope.sendCommand($scope.item.state);
                $scope.pending = false;
            }, 300);
            $scope.pending = true;
        }
    }

    $scope.setHue = function(hue) {
        // send updates every 300 ms only
        if (!$scope.pending) {
            $timeout(function() {
                var stateObject = getStateAsObject($scope.item.state);
                stateObject.h = $scope.hue === 0 ? '0' : $scope.hue;
                stateObject.b = $scope.brightness === 0 ? '0' : $scope.brightness;
                stateObject.s = $scope.saturation === 0 ? '0' : $scope.saturation;
                if ($scope.item.state == "UNDEF" || $scope.item.state === 'NULL') {
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

    $scope.setSaturation = function(saturation) {
        // send updates every 300 ms only
        if (!$scope.pending) {
            $timeout(function() {
                var stateObject = getStateAsObject($scope.item.state);
                stateObject.s = $scope.saturation === 0 ? '0' : $scope.saturation;
                stateObject.b = $scope.brightness === 0 ? '0' : $scope.brightness;
                stateObject.h = $scope.hue === 0 ? '0' : $scope.hue;
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
}).controller('NumberItemController', function($scope) {
    $scope.shouldRenderSlider = function(item) {
        if (item.stateDescription) {
            var stateDescription = item.stateDescription;
            if (stateDescription.readOnly) {
                return false;
            } else {
                if (isNaN(stateDescription.minimum) || isNaN(stateDescription.maximum)) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }
}).controller('RollershutterItemController', function($scope) {
    if ($scope.item.state === 'UNDEF' || $scope.item.state === 'NULL') {
        $scope.item.state = '-';
    }
}).controller('PlayerItemController', function($scope) {

}).controller('LocationItemController', function($scope, $sce) {
    $scope.init = function() {
        if ($scope.item.state !== 'UNDEF' && $scope.item.state !== 'NULL') {
            var latitude = parseFloat($scope.item.state.split(',')[0]);
            var longitude = parseFloat($scope.item.state.split(',')[1]);
            var bbox = (longitude - 0.01) + ',' + (latitude - 0.01) + ',' + (longitude + 0.01) + ',' + (latitude + 0.01);
            var marker = latitude + ',' + longitude;
            $scope.formattedState = latitude + '°N ' + longitude + '°E';
            $scope.url = $sce.trustAsResourceUrl('http://www.openstreetmap.org/export/embed.html?bbox=' + bbox + '&marker=' + marker);
        } else {
            $scope.formattedState = '- °N - °E';
        }
    };
    $scope.$watch('item.state', function() {
        $scope.init();
    });
    $scope.init();
});

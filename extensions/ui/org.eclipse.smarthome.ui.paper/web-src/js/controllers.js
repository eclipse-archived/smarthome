angular.module('PaperUI.controllers', [ 'PaperUI.constants' ]).controller('BodyController', function($rootScope, $scope, $http, $location, eventService, toastService, discoveryResultRepository, thingTypeRepository, bindingRepository, restConfig, util) {
    $scope.scrollTop = 0;
    $(window).scroll(function() {
        $scope.$apply(function(scope) {
            $scope.scrollTop = $('body').scrollTop();
        });
    });
    $scope.isBigTitle = function() {
        return $scope.scrollTop < 80 && !$rootScope.simpleHeader;
    }
    $scope.setTitle = function(title) {
        $rootScope.title = title;
    }
    $scope.subtitles = [];
    $scope.setSubtitle = function(args) {
        $scope.subtitles = [];
        $.each(args, function(i, subtitle) {
            $scope.subtitles.push(subtitle);
        })
    }
    $scope.setHeaderText = function(headerText) {
        $scope.headerText = headerText;
    }
    $rootScope.$on('$routeChangeStart', function() {
        $scope.subtitles = [];
        $scope.headerText = null;
    });
    $scope.generateUUID = function() {
        var d = new Date().getTime();
        var uuid = 'xxxxxxxx'.replace(/[x]/g, function(c) {
            var r = (d + Math.random() * 16) % 16 | 0;
            d = Math.floor(d / 16);
            return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });
        return uuid;
    };

    var numberOfInboxEntries = -1, prevAudioUrl = '';
    eventService.onEvent('smarthome/inbox/*/added', function(topic, discoveryResult) {
        toastService.showDefaultToast('New Inbox Entry: ' + discoveryResult.label, 'Show Inbox', 'inbox/search');
    });
    eventService.onEvent('smarthome/webaudio/playurl', function(topic, audioUrl) {
        if (prevAudioUrl !== audioUrl) {
            var context;
            window.AudioContext = window.AudioContext || window.webkitAudioContext;
            if (typeof (window.AudioContext) != "undefined") {
                try {
                    context = new AudioContext();
                    var audioBuffer = null;
                    $http({
                        url : audioUrl,
                        method : 'GET',
                        responseType : 'arraybuffer'
                    }).then(function(response) {
                        context.decodeAudioData(response.data, function(buffer) {
                            audioBuffer = buffer;
                            var source = context.createBufferSource();
                            source.buffer = buffer;
                            source.connect(context.destination);
                            source.onended = function() {
                                context.close();
                            }
                            source.start(0);
                        });
                    });
                } catch (e) {
                    if (context) {
                        context.close();
                    }
                }
            } else {
                angular.element("#audioSink").attr('src', audioUrl);
            }
            prevAudioUrl = audioUrl;
        }
    });
    eventService.onEvent('smarthome/items/*/state', function(topic, stateObject) {

        var itemName = topic.split('/')[2];
        var state = stateObject.value;

        console.log('Item ' + itemName + ' updated: ' + state);

        if ($rootScope.itemUpdates[itemName] + 500 > new Date().getTime()) {
            console.log('Ignoring update for ' + itemName + ', because update was probably triggered through UI.');
            return;
        }

        var changeStateRecursively = function(item) {
            var updateState = true;
            if (item.name === itemName) {
                // ignore ON and OFF update for Dimmer
                if (item.type === 'Dimmer') {
                    if (state === 'ON' || state == 'OFF') {
                        updateState = false;
                    }
                }
                if (item.type === "Number" || item.groupType === "Number") {
                    var parsedValue = Number(state);
                    if (!isNaN(parsedValue)) {
                        state = parsedValue;
                    }
                }
                if (item.type === "Rollershutter") {
                    if (stateObject.type == "Percent" || stateObject.type == "Decimal") {
                        state = parseInt(stateObject.value);
                    }
                }
                if (updateState) {
                    $scope.$apply(function(scope) {
                        item.state = state;
                        item.stateText = util.getItemStateText(item);
                    });
                } else {
                    console.log('Ignoring state ' + state + ' for ' + itemName)
                }
            }
            if (item.members) {
                $.each(item.members, function(i, memberItem) {
                    changeStateRecursively(memberItem);
                });
            }
        }
        var index = getItemIndex(itemName);
        if (index !== -1) {
            changeStateRecursively($rootScope.data.items[index]);
        }
    });

    eventService.onEvent('smarthome/items/*/statechanged', function(topic, stateObject) {
        var itemName = topic.split('/')[2];
        if (itemName && (stateObject.type == "Percent" || stateObject.type == "Decimal")) {
            var index = getItemIndex(itemName);
            if (index !== -1) {
                $scope.$apply(function(scope) {
                    $rootScope.data.items[index].state = parseFloat(stateObject.value);
                    $rootScope.data.items[index].stateText = util.getItemStateText($rootScope.data.items[index]);
                });
            }
        }
    });

    function getItemIndex(itemName) {
        if ($rootScope.data.items) {
            for (var it = 0; it < $rootScope.data.items.length; it++) {
                if ($rootScope.data.items[it].name == itemName) {
                    return it;
                }
            }
        }
        return -1;
    }

    $scope.getNumberOfNewDiscoveryResults = function() {
        var numberOfNewDiscoveryResults = 0;
        if (!$scope.data.discoveryResults) {
            return numberOfNewDiscoveryResults;
        }
        for (var i = 0; i < $scope.data.discoveryResults.length; i++) {
            var discoveryResult = $scope.data.discoveryResults[i];
            if (discoveryResult.flag === 'NEW') {
                numberOfNewDiscoveryResults++;
            }
        }
        return numberOfNewDiscoveryResults;
    }

    $http.get(restConfig.restPath + "/links/auto").then(function(response) {
        if (response.data !== undefined) {
            $rootScope.advancedMode = !response.data;
            window.localStorage.setItem('paperui.advancedMode', !response.data);
        }
    });

    discoveryResultRepository.getAll();
    bindingRepository.getAll();
}).controller('PreferencesPageController', function($rootScope, $scope, $window, $location, toastService) {
    $scope.setHeaderText('Edit user preferences.');

    var localStorage = window.localStorage;
    var language = localStorage.getItem('paperui.language');

    $scope.language = language ? language : 'english';
    $scope.save = function() {
        localStorage.setItem('paperui.language', $scope.language);
        toastService.showSuccessToast('Preferences saved successfully.');
        setTimeout(function() {
            $window.location.reload();
        }, 1500);
    }

    $scope.getSelected = function(property) {
        return $('select#' + property + ' option:selected').val();
    }
}).controller('NavController', function($scope, $location, $http, restConfig, moduleConfig) {
    $scope.opened = null;
    $scope.extensionEnabled;
    $scope.ruleEnabled;
    $scope.open = function(viewLocation) {
        $scope.opened = viewLocation;
    }
    $scope.isActive = function(viewLocation) {
        var active = (viewLocation === $location.path().split('/')[1]);
        return active || $scope.opened === viewLocation;
    }
    $scope.isSubActive = function(viewLocation) {
        var active = (viewLocation === $location.path().split('/')[2]);
        return active;
    }
    $scope.isHidden = function(module) {
        return (moduleConfig[module].hasOwnProperty('visible') ? moduleConfig[module].visible : moduleConfig[module]) === false;
    }
    $scope.getLabel = function(property) {
        var object = moduleConfig && moduleConfig[property] ? moduleConfig[property] : '';
        if (object && object.hasOwnProperty('label') && object['label']) {
            return object['label'];
        }
        return 'Extensions';
    }
    $scope.$on('$routeChangeSuccess', function() {
        $('body').removeClass('sml-open');
        $('.mask').remove();
        $scope.opened = null;
    });
    $http.get(restConfig.restPath).then(function(response) {
        $scope.extensionEnabled = false;
        $scope.ruleEnabled = false;
        if (response.data && response.data.links) {
            for (var i = 0; i < response.data.links.length; i++) {
                if (response.data.links[i].type === 'extensions') {
                    $scope.extensionEnabled = true;
                } else if (response.data.links[i].type === 'rules') {
                    $scope.ruleEnabled = true;
                }
            }
        }
    });
});
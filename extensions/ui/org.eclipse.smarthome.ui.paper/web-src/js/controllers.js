angular.module('PaperUI.controllers', [ 'PaperUI.constants' ]).controller('BodyController', function($rootScope, $scope, eventService, toastService, discoveryResultRepository, thingTypeRepository, bindingRepository) {
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

    var numberOfInboxEntries = -1;
    eventService.onEvent('smarthome/inbox/*/added', function(topic, discoveryResult) {
        toastService.showDefaultToast('New Inbox Entry: ' + discoveryResult.label, 'Show Inbox', 'inbox/setup');
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
                if (item.type === 'DimmerItem') {
                    if (state === 'ON' || state == 'OFF') {
                        updateState = false;
                    }
                }

                if (updateState) {
                    $scope.$apply(function(scope) {
                        item.state = state;
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

        if ($rootScope.data.items) {
            $.each($rootScope.data.items, function(i, item) {
                changeStateRecursively(item);
            });
        }
    });

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

    discoveryResultRepository.getAll();
    thingTypeRepository.getAll();
    bindingRepository.getAll();
}).controller('PreferencesPageController', function($rootScope, $scope, toastService) {
    $scope.setHeaderText('Edit user preferences.');

    var localStorage = window.localStorage;
    var language = localStorage.getItem('paperui.language');

    $scope.language = language ? language : 'english';
    $scope.advancedMode = $rootScope.advancedMode;
    $scope.save = function() {
        $rootScope.advancedMode = $scope.advancedMode;
        localStorage.setItem('paperui.language', $scope.language);
        localStorage.setItem('paperui.advancedMode', $rootScope.advancedMode);
        toastService.showSuccessToast('Preferences saved successfully. Please reload the page.');
    }

    $scope.getSelected = function(property) {
        return $('select#' + property + ' option:selected').val();
    }
}).controller('NavController', function($scope, $location, moduleConfig) {
    $scope.opened = null;
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
        return moduleConfig[module] === false;
    }
    $scope.$on('$routeChangeSuccess', function() {
        $('body').removeClass('sml-open');
        $('.mask').remove();
        $scope.opened = null;
    });
});
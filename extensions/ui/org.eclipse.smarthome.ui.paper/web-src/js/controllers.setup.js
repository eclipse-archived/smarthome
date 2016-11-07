angular.module('PaperUI.controllers.setup', []).controller('SetupPageController', function($scope, $location, thingTypeRepository, bindingRepository) {
    $scope.navigateTo = function(path) {
        $location.path('inbox/' + path);
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
                return "Unkown device";
            }
        } else {
            thingTypeRepository.setDirty(false);
        }
    };
    getThingTypes();
}).controller('InboxController', function($scope, $timeout, $mdDialog, $q, inboxService, discoveryResultRepository, thingTypeRepository, thingService, toastService) {
    $scope.setHeaderText('Shows a list of found things in your home.')

    $scope.showScanDialog = function(event) {
        $mdDialog.show({
            controller : 'ScanDialogController',
            templateUrl : 'partials/dialog.scan.html',
            targetEvent : event,
        });
    }

    $scope.refresh = function() {
        discoveryResultRepository.getAll(true);
    };
}).controller('InboxEntryController', function($scope, $mdDialog, $q, inboxService, discoveryResultRepository, thingTypeRepository, thingService, toastService, thingRepository) {
    $scope.approve = function(thingUID, thingTypeUID, event) {
        $mdDialog.show({
            controller : 'ApproveInboxEntryDialogController',
            templateUrl : 'partials/dialog.approveinboxentry.html',
            targetEvent : event,
            locals : {
                discoveryResult : discoveryResultRepository.find(function(discoveryResult) {
                    return discoveryResult.thingUID === thingUID;
                })
            }
        }).then(function(result) {
            inboxService.approve({
                'thingUID' : thingUID,
                'enableChannels' : !$scope.advancedMode
            }, result.label).$promise.then(function() {
                thingRepository.setDirty(true);
                toastService.showDefaultToast('Thing added.', 'Show Thing', 'configuration/things/view/' + thingUID);
                var thingType = thingTypeRepository.find(function(thingType) {
                    return thingTypeUID === thingType.UID;
                });

                if (thingType && thingType.bridge) {
                    $scope.navigateTo('setup/search/' + thingUID.split(':')[0]);
                } else {
                    discoveryResultRepository.getAll(true);
                }
            });
        });
    };
    $scope.ignore = function(thingUID) {
        inboxService.ignore({
            'thingUID' : thingUID
        }, function() {
            $scope.refresh();
        });
    };
    $scope.unignore = function(thingUID) {
        inboxService.unignore({
            'thingUID' : thingUID
        }, function() {
            $scope.refresh();
        });
    };
    $scope.remove = function(thingUID, event) {
        var discoveryResult = discoveryResultRepository.find(function(discoveryResult) {
            return discoveryResult.thingUID === thingUID;
        });
        var confirm = $mdDialog.confirm().title('Remove ' + discoveryResult.label).content('Would you like to remove the discovery result from the inbox?').ariaLabel('Remove Discovery Result').ok('Remove').cancel('Cancel').targetEvent(event);
        $mdDialog.show(confirm).then(function() {
            inboxService.remove({
                'thingUID' : thingUID
            }, function() {
                $scope.refresh();
                toastService.showSuccessToast('Inbox entry removed');
            });
        });
    };
}).controller('ScanDialogController', function($scope, $rootScope, $timeout, $mdDialog, discoveryService, bindingRepository) {
    $scope.supportedBindings = [];
    $scope.activeScans = [];

    $scope.scan = function(bindingId) {
        $scope.activeScans.push(bindingId);
        discoveryService.scan({
            'bindingId' : bindingId
        }, function(response) {
            var timeout = parseInt(response.timeout);
            timeout = (!isNaN(timeout) ? timeout : 3) * 1000;
            setTimeout(function() {
                $scope.$apply(function() {
                    $scope.activeScans.splice($scope.activeScans.indexOf(bindingId), 1)
                });
            }, timeout);
        });

    };

    bindingRepository.getAll();

    $scope.getBindingById = function(bindingId) {
        for (var i = 0; i < $rootScope.data.bindings.length; i++) {
            var binding = $rootScope.data.bindings[i];
            if (binding.id === bindingId) {
                return binding;
            }
        }
        return {};
    }

    discoveryService.getAll(function(response) {
        $scope.supportedBindings = response;
    });

    $scope.close = function() {
        $mdDialog.hide();
    }
}).controller('ApproveInboxEntryDialogController', function($scope, $mdDialog, discoveryResult, thingTypeRepository) {
    $scope.discoveryResult = discoveryResult;
    $scope.label = discoveryResult.label;
    $scope.thingType = null;
    $scope.thingTypeUID = discoveryResult.thingTypeUID;
    thingTypeRepository.getOne(function(thingType) {
        return thingType.UID === $scope.thingTypeUID;
    }, function(thingType) {
        $scope.thingType = thingType;
    });

    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.approve = function(label) {
        $mdDialog.hide({
            label : label
        });
    }
}).controller('ManualSetupConfigureController', function($scope, $routeParams, $mdDialog, $location, toastService, bindingRepository, thingTypeService, thingService, thingRepository, configService, linkService) {

    var thingTypeUID = $routeParams.thingTypeUID;

    function generateUUID() {
        var d = new Date().getTime();
        var uuid = 'xxxxxxxx'.replace(/[x]/g, function(c) {
            var r = (d + Math.random() * 16) % 16 | 0;
            d = Math.floor(d / 16);
            return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });
        return uuid;
    }
    ;

    $scope.thingType = null;
    $scope.thing = {
        UID : null,
        configuration : {},
        item : {
            label : null,
            groupNames : []
        }
    };
    $scope.thingID = null;

    $scope.addThing = function(thing) {
        thing.thingTypeUID = thingTypeUID;
        thing.UID = thing.thingTypeUID + ":" + thing.ID;
        thing.configuration = configService.setConfigDefaults(thing.configuration, $scope.parameters, true);
        thingService.add(thing, function() {
            toastService.showDefaultToast('Thing added.', 'Show Thing', 'configuration/things/view/' + thing.UID);
            window.localStorage.setItem('thingUID', thing.UID);
            $location.path('configuration/things');
        });
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

    thingTypeService.getByUid({
        thingTypeUID : thingTypeUID
    }, function(thingType) {
        $scope.setTitle('Configure ' + thingType.label);
        $scope.setHeaderText(thingType.description);
        $scope.thingType = thingType;
        $scope.parameters = configService.getRenderingModel(thingType.configParameters, thingType.parameterGroups);
        $scope.thing.ID = generateUUID();
        $scope.thing.item.label = thingType.label;
        $scope.thing.label = thingType.label;
        $scope.needsBridge = $scope.thingType.supportedBridgeTypeUIDs && $scope.thingType.supportedBridgeTypeUIDs.length > 0;
        if ($scope.needsBridge) {
            $scope.getBridges();
        }
        configService.setDefaults($scope.thing, $scope.thingType)
    });
}).controller('SetupWizardController', function($scope, discoveryResultRepository) {
    $scope.showIgnored = false;
    $scope.toggleShowIgnored = function() {
        $scope.showIgnored = !$scope.showIgnored;
    }
    $scope.refresh = function() {
        discoveryResultRepository.getAll(true);
    };
    $scope.refresh();
    $scope.filter = function(discoveryResult) {
        return $scope.showIgnored || discoveryResult.flag === 'NEW';
    }
    $scope.areEntriesIgnored = function(discoveryResults) {
        return $.grep(discoveryResults, function(discoveryResult) {
            return discoveryResult.flag === 'IGNORED';
        }).length > 0;
    }
}).controller('SetupWizardBindingsController', function($scope, bindingRepository, discoveryService) {
    $scope.setSubtitle([ 'Choose Binding' ]);
    $scope.setHeaderText('Choose a Binding for which you want to add new things.');
    bindingRepository.getAll();
    $scope.selectBinding = function(bindingId) {
        discoveryService.getAll(function(supportedBindings) {
            if (supportedBindings.indexOf(bindingId) >= 0) {
                $scope.navigateTo('setup/search/' + bindingId);
            } else {
                $scope.navigateTo('setup/thing-types/' + bindingId);
            }
        });
    }
}).controller('SetupWizardSearchBindingController', function($scope, discoveryResultRepository, discoveryService, thingTypeRepository, bindingRepository) {
    $scope.showIgnored = false;
    $scope.toggleShowIgnored = function() {
        $scope.showIgnored = !$scope.showIgnored;
    }
    $scope.bindingId = $scope.path[4];
    var binding = bindingRepository.find(function(binding) {
        return binding.id === $scope.bindingId;
    });
    $scope.setSubtitle([ binding ? binding.name : '', 'Search' ]);
    $scope.setHeaderText('Searching for new things for the ' + (binding ? binding.name : '') + '.');

    $scope.discoverySupported = true;
    discoveryService.getAll(function(supportedBindings) {
        if (supportedBindings.indexOf($scope.bindingId) >= 0) {
            $scope.discoverySupported = true;
            $scope.scan($scope.bindingId);
        }
    });

    $scope.scanning = false;
    $scope.filter = function(discoveryResult) {
        return ($scope.showIgnored || discoveryResult.flag === 'NEW') && discoveryResult.thingUID.split(':')[0] === $scope.bindingId;
    }
    $scope.scan = function(bindingId) {
        $scope.scanning = true;
        discoveryService.scan({
            'bindingId' : bindingId
        }, function(response) {
            var timeout = parseInt(response.timeout);
            timeout = (!isNaN(timeout) ? timeout : 10) * 1000;
            setTimeout(function() {
                $scope.$apply(function() {
                    $scope.scanning = false;
                });
            }, timeout);
        });

    };

    $scope.refresh = function() {
        discoveryResultRepository.getAll(true);
    };
    $scope.refresh();
}).controller('SetupWizardThingTypesController', function($scope, bindingRepository) {
    $scope.bindingId = $scope.path[4];
    var binding = bindingRepository.find(function(binding) {
        return binding.id === $scope.bindingId;
    });
    $scope.setSubtitle([ binding ? binding.name : '', 'Choose Thing' ]);
    $scope.setHeaderText('Choose a Thing from the ' + (binding ? binding.name : '') + ' which you want to add.');

    $scope.selectThingType = function(thingTypeUID) {
        $scope.navigateTo('setup/add/' + thingTypeUID);
    }
    $scope.filter = function(thingType) {
        return (thingType.UID.split(':')[0] === $scope.bindingId) && (thingType.listed);
    }
});
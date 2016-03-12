var Repository = function($q, $rootScope, remoteService, dataType, staticData) {
    var self = this;
    var cacheEnabled = true;
    var dirty = false;
    var initialFetch = false;

    this.setDirty = function() {
        this.dirty = true;
    }
    this.getAll = function(callback, refresh) {
        if (typeof callback === 'boolean') {
            refresh = true;
            callback = null;
        }
        var deferred = $q.defer();
        deferred.promise.then(function(res) {
            if (callback && res !== 'No update') {
                return callback(res);
            } else {
                return;
            }
        }, function(res) {
            return;
        }, function(res) {
            if (callback) {
                return callback(res);
            } else {
                return;
            }
        });
        if (cacheEnabled && staticData && self.initialFetch && !refresh && !self.dirty) {
            deferred.resolve($rootScope.data[dataType]);
        } else {
            remoteService.getAll(function(data) {
                if ((!cacheEnabled || (data.length != $rootScope.data[dataType].length) || self.dirty || refresh)) {
                    self.initialFetch = true;
                    $rootScope.data[dataType] = data;
                    self.dirty = false;
                    deferred.resolve(data);
                } else {
                    // set initial data
                    if (!self.initialFetch) {
                        self.initialFetch = true;
                        $rootScope.data[dataType] = data;
                        self.dirty = false;
                    }
                    deferred.resolve('No update');
                }
            });
            if (cacheEnabled && self.initialFetch) {
                deferred.notify($rootScope.data[dataType]);
            }
        }
        return deferred.promise;
    };
    this.getOne = function(condition, callback, refresh) {
        var element = self.find(condition);
        if (element != null && !this.dirty && !refresh) {
            callback(element);
        } else {
            self.getAll(null, true).then(function(res) {
                if (callback) {
                    callback(self.find(condition));
                    return;
                } else {
                    return;
                }
            }, function(res) {
                callback(null);
                return;
            }, function(res) {
                return;
            });
        }
    };
    this.find = function(condition) {
        for (var i = 0; i < $rootScope.data[dataType].length; i++) {
            var element = $rootScope.data[dataType][i];
            if (condition(element)) {
                return element;
            }
        }
        return null;
    };
    this.add = function(element) {
        $rootScope.data[dataType].push(element);
    };
    this.remove = function(element) {
        $rootScope.data[dataType].splice($rootScope.data[dataType].indexOf(element), 1);
    };
    this.update = function(element) {
        var index = $rootScope.data[dataType].indexOf(element);
        $rootScope.data[dataType][index] = element;
    };
}

angular.module('PaperUI.services.repositories', []).factory('bindingRepository', function($q, $rootScope, bindingService) {
    $rootScope.data.bindings = [];
    return new Repository($q, $rootScope, bindingService, 'bindings', true);
}).factory('thingTypeRepository', function($q, $rootScope, thingTypeService) {
    $rootScope.data.thingTypes = [];
    return new Repository($q, $rootScope, thingTypeService, 'thingTypes', true);
}).factory('discoveryResultRepository', function($q, $rootScope, inboxService, eventService) {
    var repository = new Repository($q, $rootScope, inboxService, 'discoveryResults')
    $rootScope.data.discoveryResults = [];
    eventService.onEvent('smarthome/inbox/*', function(topic, discoveryResult) {
        if (topic.indexOf("added") > -1) {
            repository.add(discoveryResult);
        }
        if (topic.indexOf("removed") > -1) {
            repository.remove(discoveryResult);
        }
    });
    return repository;
}).factory('thingRepository', function($q, $rootScope, thingSetupService, eventService) {
    var repository = new Repository($q, $rootScope, thingSetupService, 'things')
    $rootScope.data.things = [];

    var itemNameToThingUID = function(itemName) {
        return itemName.replace(/_/g, ':')
    }
    var updateInRepository = function(thingUID, mustExist, action) {
        var existing = repository.find(function(thing) {
            return thing.UID === thingUID;
        });
        if ((existing && mustExist) || (!existing && !mustExist)) {
            $rootScope.$apply(function(scope) {
                action(existing)
            });
        }
    }

    eventService.onEvent('smarthome/things/*/status', function(topic, statusInfo) {
        updateInRepository(topic.split('/')[2], true, function(existingThing) {
            existingThing.statusInfo = statusInfo;
        });
    });
    eventService.onEvent('smarthome/things/*/added', function(topic, thing) {
        updateInRepository(topic.split('/')[2], false, function(existingThing) {
            repository.add(thing);
        });
    });
    eventService.onEvent('smarthome/things/*/removed', function(topic, thing) {
        updateInRepository(topic.split('/')[2], true, function(existingThing) {
            repository.remove(existingThing);
        });
    });
    eventService.onEvent('smarthome/items/*/added', function(topic, item) {
        updateInRepository(itemNameToThingUID(topic.split('/')[2]), true, function(existingThing) {
            existingThing.item = item
        });
    });
    eventService.onEvent('smarthome/items/*/updated', function(topic, itemUpdate) {
        updateInRepository(itemNameToThingUID(topic.split('/')[2]), true, function(existingThing) {
            existingThing.item = itemUpdate[0]
        });
    });

    return repository;
}).factory('itemRepository', function($q, $rootScope, itemService) {
    var repository = new Repository($q, $rootScope, itemService, 'items')
    $rootScope.data.items = [];
    return repository;
}).factory('ruleRepository', function($q, $rootScope, ruleService) {
    var repository = new Repository($q, $rootScope, ruleService, 'rules')
    $rootScope.data.rules = [];
    return repository;
});
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
        if ($rootScope.data[dataType].indexOf(element) !== -1) {
            $rootScope.data[dataType].splice($rootScope.data[dataType].indexOf(element), 1);
        }
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
}).factory('thingRepository', function($q, $rootScope, thingService, eventService) {
    var repository = new Repository($q, $rootScope, thingService, 'things')
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
    eventService.onEvent('smarthome/things/*/updated', function(topic, thing) {
        updateInRepository(topic.split('/')[2], true, function(existingThing) {
            if (thing.length > 0) {
                existingThing.label = thing[0].label;
                existingThing.configuration = existingThing.configuration;
                var updatedArr = [];
                if (thing[0].channels) {
                    angular.forEach(thing[0].channels, function(newChannel) {
                        var channel = $.grep(existingThing.channels, function(existingChannel) {
                            return existingChannel.uid == newChannel.uid;
                        });
                        if (channel.length == 0) {
                            channel[0] = newChannel;
                            channel[0].linkedItems = [];

                        } else {
                            channel[0].configuration = newChannel.configuration;
                            channel[0].itemType = newChannel.itemType;
                        }
                        updatedArr.push(channel[0]);
                    });
                    existingThing.channels = updatedArr;
                }
            }
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

    eventService.onEvent('smarthome/links/*/added', function(topic, link) {
        var channelItem = link.channelUID.split(':'), thingUID;
        if (channelItem.length > 2) {
            thingUID = channelItem[0] + ":" + channelItem[1] + ":" + channelItem[2];
        }
        if (thingUID) {
            updateInRepository(thingUID, true, function(existingThing) {
                var channel = $.grep(existingThing.channels, function(channel) {
                    return channel.uid == link.channelUID;
                });
                if (channel.length > 0) {
                    channel[0].linkedItems = channel[0].linkedItems ? channel[0].linkedItems : [];
                    channel[0].linkedItems.push(link.itemName);
                }
            });
        }
    });

    eventService.onEvent('smarthome/links/*/removed', function(topic, link) {
        var channelItem = link.channelUID.split(':'), thingUID;
        if (channelItem.length > 2) {
            thingUID = channelItem[0] + ":" + channelItem[1] + ":" + channelItem[2];
        }
        if (thingUID) {
            updateInRepository(thingUID, true, function(existingThing) {
                var channel = $.grep(existingThing.channels, function(channel) {
                    return channel.uid == link.channelUID;
                });
                if (channel.length > 0) {
                    channel[0].linkedItems = [];
                }
            });
        }
    });

    return repository;
}).factory('itemRepository', function($q, $rootScope, itemService) {
    var repository = new Repository($q, $rootScope, itemService, 'items')
    $rootScope.data.items = [];
    return repository;
}).factory('ruleRepository', function($q, $rootScope, ruleService, eventService) {
    var repository = new Repository($q, $rootScope, ruleService, 'rules', true)
    $rootScope.data.rules = [];

    eventService.onEvent('smarthome/rules/*/updated', function(topic, ruleUpdate) {

        var existing = repository.find(function(rule) {
            return rule.uid === ruleUpdate[0].uid;
        });
        $rootScope.$apply(function() {
            if (existing) {
                existing.name = ruleUpdate[0].name;
                existing.description = ruleUpdate[0].description;
                existing.triggers = ruleUpdate[0].triggers;
                existing.actions = ruleUpdate[0].actions;
                existing.conditions = ruleUpdate[0].conditions;
            }
        });
    });

    eventService.onEvent('smarthome/rules/*/added', function(topic, rule) {
        $rootScope.$apply(function() {
            repository.add(rule);
        });
    });

    eventService.onEvent('smarthome/rules/*/removed', function(topic, removedRule) {
        var existing = repository.find(function(rule) {
            return rule.uid === removedRule.uid;
        });
        $rootScope.$apply(function() {
            repository.remove(existing);
        });
    });

    eventService.onEvent('smarthome/rules/*/state', function(topic, rule) {
        var existing = repository.find(function(rule) {
            return rule.uid === topic.split('/')[2];
        });
        $rootScope.$apply(function() {
            existing.status = {};
            existing.status.status = rule.status;
            existing.status.statusDetail = rule.statusDetail;
            if (rule.status.toUpperCase() === "DISABLED") {
                existing.enabled = false;
            } else {
                existing.enabled = true;
            }
        });
    });

    return repository;
});
function Repository($q, $rootScope, remoteService, dataType, staticData, getOneFunction, idParameterName, elmentId) {
    var self = this;

    this.cacheEnabled = true;
    this.dirty = false;
    this.initialFetch = false;
    this.staticData = staticData
    this.singleElements = getOneFunction ? {} : null;

    return {
        add : add,
        remove : remove,
        update : update,
        getAll : getAll,
        getOne : getOne,
        find : find,
        findByIndex : findByIndex,
        setDirty : setDirty
    };

    function setDirty() {
        self.dirty = true;
    }

    function getAll(callback, refresh) {
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
        if (self.cacheEnabled && self.staticData && self.initialFetch && !refresh && !self.dirty) {
            deferred.resolve($rootScope.data[dataType]);
        } else {
            remoteService.getAll(function(data) {
                if ((!self.cacheEnabled || (data.length != $rootScope.data[dataType].length) || self.dirty || refresh)) {
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
            if (self.cacheEnabled && self.initialFetch) {
                deferred.notify($rootScope.data[dataType]);
            }
        }
        return deferred.promise;
    }

    function getOne(condition, callback, refresh) {
        var element = find(condition);
        if (element != null && !self.dirty && !refresh) {
            resolveSingleElement(callback, element)
        } else {
            getAll(null, true).then(function(res) {
                if (callback) {
                    resolveSingleElement(callback, find(condition));
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
    }

    function resolveSingleElement(callback, element) {
        if (!element) {
            callback(undefined);
        } else if (getOneFunction && self.singleElements[element.UID]) {
            callback(self.singleElements[element.UID]);
        } else if (getOneFunction) {
            var parameter = {};
            parameter[idParameterName] = element[elmentId];
            getOneFunction(parameter, function(singleElement) {
                self.singleElements[element.UID] = singleElement;
                callback(singleElement)
            })
        } else {
            callback(element);
        }
    }

    function find(condition) {
        for (var i = 0; i < $rootScope.data[dataType].length; i++) {
            var element = $rootScope.data[dataType][i];
            if (condition(element)) {
                return element;
            }
        }
        return null;
    }

    function findByIndex(condition) {
        for (var i = 0; i < $rootScope.data[dataType].length; i++) {
            var element = $rootScope.data[dataType][i];
            if (condition(element)) {
                return i;
            }
        }
        return -1;
    }

    function add(element) {
        $rootScope.data[dataType].push(element);
    }

    function remove(element, index) {
        if (typeof (index) === 'undefined' && $rootScope.data[dataType].indexOf(element) !== -1) {
            $rootScope.data[dataType].splice($rootScope.data[dataType].indexOf(element), 1);
        } else if (typeof (index) !== 'undefined' && index !== -1) {
            $rootScope.data[dataType].splice(index, 1);
        }
    }

    function update(element) {
        var index = $rootScope.data[dataType].indexOf(element);
        $rootScope.data[dataType][index] = element;
    }
}

angular.module('PaperUI.services.repositories', [ 'PaperUI.services.rest' ]).factory('bindingRepository', function($q, $rootScope, bindingService) {
    $rootScope.data.bindings = [];
    return new Repository($q, $rootScope, bindingService, 'bindings', true);
}).factory('thingTypeRepository', function($q, $rootScope, thingTypeService) {
    $rootScope.data.thingTypes = [];
    return new Repository($q, $rootScope, thingTypeService, 'thingTypes', true, thingTypeService.getByUid, 'thingTypeUID', 'UID');
}).factory('channelTypeRepository', function($q, $rootScope, channelTypeService) {
    $rootScope.data.channelTypes = [];
    return new Repository($q, $rootScope, channelTypeService, 'channelTypes', true);
}).factory('discoveryResultRepository', function($q, $rootScope, inboxService, eventService) {
    var repository = new Repository($q, $rootScope, inboxService, 'discoveryResults')
    $rootScope.data.discoveryResults = [];
    eventService.onEvent('smarthome/inbox/*', function(topic, discoveryResult) {
        var index = repository.findByIndex(function(result) {
            return discoveryResult.thingUID == result.thingUID;
        });
        var command = topic.substring(topic.lastIndexOf('/') + 1);
        if (command === "added" && index == -1) {
            repository.add(discoveryResult);
        }
        if (command === "removed" && index != -1) {
            repository.remove(undefined, index);
        }
        if (command === "updated" && index != -1) {
            repository.remove(undefined, index);
            repository.add(discoveryResult);
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
        var newThing = thing[0];
        updateInRepository(topic.split('/')[2], true, function(existingThing) {
            if (newThing) {
                existingThing.label = newThing.label;
                existingThing.configuration = newThing.configuration;
                var updatedArr = [];
                if (newThing.channels) {
                    angular.forEach(newThing.channels, function(newChannel) {
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
}).factory('itemRepository', function($q, $rootScope, itemService, eventService) {
    var repository = new Repository($q, $rootScope, itemService, 'items')
    $rootScope.data.items = [];
    eventService.onEvent('smarthome/items/*/updated', function(topic, itemUpdate) {
        if (topic.split('/').length > 2) {
            var index = repository.findByIndex(function(item) {
                return item.name == topic.split('/')[2]
            });
            if (index !== -1) {
                $rootScope.$apply(function() {
                    $rootScope.data.items[index] = itemUpdate[0];
                });
            }
        }
    });
    eventService.onEvent('smarthome/items/*/added', function(topic, itemAdded) {
        if (topic.split('/').length > 2) {
            var index = repository.findByIndex(function(item) {
                return item.name === itemAdded.name
            });
            if (index === -1 && $rootScope.data.items) {
                // the event only sent the ItemDTO w/o state description
                // load the full item from the backend again:
                repository.getOne(function condition(item) {
                    return item.name === itemAdded.name
                }, function callback(item) {
                    $rootScope.data.items.push(item);
                }, true);
            }
        }
    });
    eventService.onEvent('smarthome/items/*/removed', function(topic, itemRemoved) {
        if (topic.split('/').length > 2) {
            var index = repository.findByIndex(function(item) {
                return item.name == itemRemoved.name
            });
            if (index !== -1) {
                $rootScope.$apply(function() {
                    $rootScope.data.items.splice(index, 1);
                });
            }
        }
    });
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
}).factory('templateRepository', function($q, $rootScope, templateService) {
    var repository = new Repository($q, $rootScope, templateService, 'templates')
    $rootScope.data.templates = [];
    return repository;
});
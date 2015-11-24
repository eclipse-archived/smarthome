angular.module('PaperUI.services.rest', ['PaperUI.constants'])
.config(function($httpProvider){
    var accessToken = function getAccessToken() { return $('#authentication').data('access-token') }();
    if (accessToken != '{{ACCESS_TOKEN}}') {
        var authorizationHeader = function getAuthorizationHeader() { return 'Bearer ' + accessToken }();
        $httpProvider.defaults.headers.common['Authorization'] = authorizationHeader;       
    }
})
.factory('itemService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/items', {}, {
        getAll : {
            method : 'GET',
            isArray: true,
            url: restConfig.restPath + '/items?recursive=true'
        },
        getByName : {
            method : 'GET',
            params : {
                bindingId : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName'
        },
        remove : {
            method : 'DELETE',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName'
        },
        create : {
            method : 'PUT',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        updateState : {
            method : 'PUT',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName/state',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        sendCommand : {
            method : 'POST',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        addMember : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                memberItemName : '@memberItemName'
            },
            url : restConfig.restPath + '/items/:itemName/members/:memberItemName'
        },
        removeMember : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                memberItemName : '@memberItemName'
            },
            url : restConfig.restPath + '/items/:itemName/members/:memberItemName'
        },
        addTag : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                tag : '@tag'
            },
            url : restConfig.restPath + '/items/:itemName/tags/:tag'
        },
        removeTag : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                tag : '@tag'
            },
            url : restConfig.restPath + '/items/:itemName/tags/:tag'
        }
    });
}).factory('bindingService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/bindings', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
    });
}).factory('inboxService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/inbox', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        approve : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID/approve',
        	headers : {
                'Content-Type' : 'text/plain'
            }
        },
        ignore : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID/ignore'
        },
        unignore : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID/unignore'
        },
        remove : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID'
        }
    })
}).factory('discoveryService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/discovery', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        scan : {
            method : 'POST',
            params : {
                bindingId : '@bindingId'
            },
            url : restConfig.restPath + '/discovery/bindings/:bindingId/scan'
        }
    });
}).factory('thingTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/thing-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                bindingId : '@thingTypeUID'
            },
            url : restConfig.restPath + '/thing-types/:thingTypeUID'
        }
    });
}).factory('linkService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/links', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        link : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                channelUID : '@channelUID'
            },
            url : restConfig.restPath + '/links/:itemName/:channelUID'
        },
        unlink : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                channelUID : '@channelUID'
            },
            url : restConfig.restPath + '/links/:itemName/:channelUID'
        }
    });
}).factory('thingService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/things', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                bindingId : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID'
        },
        remove : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID'
        },
        add : {
            method : 'POST',
            url : restConfig.restPath + '/things',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        update : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        link : {
            method : 'POST',
            params : {
                thingUID : '@thingUID',
                channelId : '@channelId'    
            },
            url : restConfig.restPath + '/things/:thingUID/channels/:channelId/link',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        unlink : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID',
                channelId : '@channelId'    
            },
            url : restConfig.restPath + '/things/:thingUID/channels/:channelId/link',
        }
    });
}).factory('thingSetupService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/setup/things', {}, {
        add : {
            method : 'POST',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        update : {
            method : 'PUT',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        getAll: {
        	method : 'GET',
            isArray : true
        },
        remove : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/setup/things/:thingUID'
        },
        enableChannel : {
            method : 'PUT',
            params : {
                channelUID : '@channelUID'
            },
            url : restConfig.restPath + '/setup/things/channels/:channelUID'
        },
        disableChannel : {
            method : 'DELETE',
            params : {
                channelUID : '@channelUID'
            },
            url : restConfig.restPath + '/setup/things/channels/:channelUID'
        },
        setLabel : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            headers : {
                'Content-Type' : 'text/plain'
            },
            url : restConfig.restPath + '/setup/labels/:thingUID'
        },
        setGroups : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            headers : {
                'Content-Type' : 'application/json'
            },
            url : restConfig.restPath + '/setup/things/:thingUID/groups'
        }
    });
}).factory('groupSetupService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/setup/groups', {}, {
    	add : {
            method : 'POST',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        remove : {
            method : 'DELETE',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/setup/groups/:itemName'
        },
        getAll: {
        	method : 'GET',
            isArray : true
        },
    });
});
angular.module('PaperUI.services.rest', [ 'PaperUI.constants' ]).config(function($httpProvider) {
    var accessToken = function getAccessToken() {
        return $('#authentication').data('access-token')
    }();
    if (accessToken != '{{ACCESS_TOKEN}}') {
        var authorizationHeader = function getAuthorizationHeader() {
            return 'Bearer ' + accessToken
        }();
        $httpProvider.defaults.headers.common['Authorization'] = authorizationHeader;
    }
}).factory('itemService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/items', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/items?recursive=false'
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
            url : restConfig.restPath + '/items/:itemName'
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
        getConfigById : {
            method : 'GET',
            params : {
                id : '@id'
            },
            interceptor : {
                response : function(response) {
                    return response.data;
                }
            },
            url : restConfig.restPath + '/bindings/:id/config'
        },
        updateConfig : {
            method : 'PUT',
            headers : {
                'Content-Type' : 'application/json'
            },
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/bindings/:id/config'
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
            transformResponse : function(data) {
                return {
                    timeout : angular.fromJson(data)
                }
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
                thingTypeUID : '@thingTypeUID'
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
        updateConfig : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID/config',
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
}).factory('serviceConfigService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/services', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getById : {
            method : 'GET',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/services/:id'
        },
        getConfigById : {
            method : 'GET',
            params : {
                id : '@id'
            },
            interceptor : {
                response : function(response) {
                    return response.data;
                }
            },
            url : restConfig.restPath + '/services/:id/config'
        },
        updateConfig : {
            method : 'PUT',
            headers : {
                'Content-Type' : 'application/json'
            },
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/services/:id/config'
        },
        deleteConfig : {
            method : 'DELETE',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/services/:id/config'
        },
    });
}).factory('configDescriptionService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/config-descriptions', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUri : {
            method : 'GET',
            params : {
                uri : '@uri'
            },
            transformResponse : function(response, headerGetter, status) {
                var response = angular.fromJson(response);
                if (status == 404) {
                    response.showError = false;
                }
                return response;
            },
            url : restConfig.restPath + '/config-descriptions/:uri'
        },
    });
}).factory('extensionService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/extensions', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUri : {
            method : 'GET',
            params : {
                uri : '@id'
            },
            url : restConfig.restPath + '/extensions/:id'
        },
        getAllTypes : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/extensions/types'
        },
        install : {
            method : 'POST',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/extensions/:id/install'
        },
        uninstall : {
            method : 'POST',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/extensions/:id/uninstall'
        }
    });
}).factory('ruleService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/rules', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID'
        },
        add : {
            method : 'POST',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        remove : {
            method : 'DELETE',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID'
        },
        getModuleConfigParameter : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            transformResponse : function(data, headersGetter, status) {
                return {
                    content : data
                };
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script'
        },
        setModuleConfigParameter : {
            method : 'PUT',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        update : {
            method : 'PUT',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        setEnabled : {
            method : 'POST',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/enable',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        getRuleTemplates : {
            method : 'GET',
            url : restConfig.restPath + '/templates',
            isArray : true
        }
    });
}).factory('moduleTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/module-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByType : {
            method : 'GET',
            params : {
                mtype : '@mtype'
            },
            url : restConfig.restPath + '/module-types?type=:mtype',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID'
        },
        getModuleConfigByUid : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID',
                moduleCategory : '@moduleCategory',
                id : '@id'

            },
            url : restConfig.restPath + '/rules/:ruleUID/:moduleCategory/:id/config'
        },
        add : {
            method : 'POST',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        remove : {
            method : 'DELETE',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID'
        },
        getModuleConfigParameter : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            transformResponse : function(data, headersGetter, status) {
                return {
                    content : data
                };
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script'
        },
        setModuleConfigParameter : {
            method : 'PUT',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script',
            headers : {
                'Content-Type' : 'text/plain'
            }
        }
    });
}).factory('channelTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/channel-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUri : {
            method : 'GET',
            params : {
                channelTypeUID : '@channelTypeUID'
            },
            url : restConfig.restPath + '/channel-types/:channelTypeUID'
        },
    });
});
describe('module PaperUI.controllers.configuration', function() {
    beforeEach(function() {
        module('PaperUI');
    });
    var restConfig;
    describe('tests for ConfigurationPageController', function() {
        var configurationPageController, scope;
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            configurationPageController = $controller('ConfigurationPageController', {
                '$scope' : scope
            });
        }));
        it('should require ConfigurationPageController', function() {
            expect(configurationPageController).toBeDefined();
        });
        it('should get thingType label', function() {
            scope.thingTypes = [ {
                label : '1'
            } ]
            var label = scope.getThingTypeLabel(0);
            expect(label).toEqual('1');
        });
    });
    describe('tests for BindingController', function() {
        var bindingController, scope, mdDialog;
        beforeEach(inject(function($injector, $rootScope, $controller, $mdDialog) {
            scope = $rootScope.$new();
            mdDialog = $mdDialog
            $controller('BodyController', {
                '$scope' : scope
            });
            bindingController = $controller('BindingController', {
                '$scope' : scope
            });
        }));
        it('should require BindingController', function() {
            expect(bindingController).toBeDefined();
        });
        it('should open binding info dialog', function() {
            spyOn(mdDialog, 'show');
            scope.openBindingInfoDialog(0);
            expect(mdDialog.show).toHaveBeenCalled();
        });
        it('should open binding configuration dialog', function() {
            spyOn(mdDialog, 'show');
            scope.configure(0);
            expect(mdDialog.show).toHaveBeenCalled();
        });
    });

    describe('tests for BindingInfoDialogController', function() {
        var bindingInfoDialogController, scope, mdDialog;
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            var bindingRepository = $injector.get('bindingRepository');
            var thingTypeRepository = $injector.get('thingTypeRepository');
            restConfig = $injector.get('restConfig');
            $rootScope.data.bindings = [ {
                id : 'B'
            } ];

            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.when('GET', restConfig.restPath + "/thing-types").respond([ {
                UID : 'B:T'
            } ]);
            bindingInfoDialogController = $controller('BindingInfoDialogController', {
                '$scope' : scope,
                'bindingId' : 'B',
                'bindingRepository' : bindingRepository,
                'thingTypeRepository' : thingTypeRepository
            });
            $httpBackend.flush();
        }));
        it('should require BindingInfoDialogController', function() {
            expect(bindingInfoDialogController).toBeDefined();
        });
        it('should get ThingTypes of Binding', function() {
            expect(scope.binding.thingTypes[0].UID).toEqual('B:T');
        });

    });

    describe('tests for ConfigureBindingDialogController', function() {
        var configureBindingDialogController, scope, bindingService;
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            var bindingRepository = $injector.get('bindingRepository');
            $rootScope.data.bindings = [ {
                id : 'B'
            } ];
            configureBindingDialogController = $controller('ConfigureBindingDialogController', {
                '$scope' : scope,
                'bindingId' : 'B',
                'configDescriptionURI' : 'CDURI',
                'bindingRepository' : bindingRepository
            });
            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.when('GET', restConfig.restPath + "/config-descriptions/CDURI").respond({
                parameters : [ {
                    type : 'input',
                    name : 'PNAME'
                } ]
            });
            $httpBackend.when('GET', restConfig.restPath + "/bindings/B/config").respond({
                PNAME : '1'
            });
            $httpBackend.flush();
            bindingService = $injector.get('bindingService');
        }));
        it('should require ConfigureBindingDialogController', function() {
            expect(configureBindingDialogController).toBeDefined();
        });
        it('should get binding parameters', function() {
            expect(scope.parameters.length).toEqual(1);
        });
        it('should get binding configuration', function() {
            expect(scope.configuration.PNAME).toEqual(1);
        });
        it('should add binding parameter', function() {
            scope.configArray = [];
            scope.addParameter();
            expect(scope.configArray.length).toEqual(1);
        });
        it('should save binding configuration', function() {
            spyOn(bindingService, 'updateConfig');
            scope.save();
            expect(bindingService.updateConfig).toHaveBeenCalled();
        });
    });

    describe('tests for ServicesController', function() {
        var ServicesController, scope, injector;
        beforeEach(inject(function($injector, $rootScope, $controller, $mdDialog) {
            scope = $rootScope.$new();
            mdDialog = $mdDialog;
            injector = $injector;
            $controller('BodyController', {
                '$scope' : scope
            });
            ServicesController = $controller('ServicesController', {
                '$scope' : scope
            });
        }));
        it('should require ServicesController', function() {
            expect(ServicesController).toBeDefined();
        });
        it('should refresh services', function() {
            var serviceConfigService = injector.get('serviceConfigService');
            spyOn(serviceConfigService, 'getAll');
            scope.refresh();
            expect(serviceConfigService.getAll).toHaveBeenCalled();
        });
        it('should open configure dialog', function() {
            spyOn(mdDialog, 'show');
            scope.configure(0);
            expect(mdDialog.show).toHaveBeenCalled();
        });
    });

    describe('tests for ConfigureServiceDialogController', function() {
        var ConfigureServiceDialogController, scope, serviceConfigService;
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            ConfigureServiceDialogController = $controller('ConfigureServiceDialogController', {
                '$scope' : scope,
                'serviceId' : 'B',
                'configDescriptionURI' : 'CDURI'
            });
            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.when('GET', restConfig.restPath + "/config-descriptions/CDURI").respond({
                parameters : [ {
                    type : 'input',
                    name : 'PNAME'
                } ]
            });
            $httpBackend.when('GET', restConfig.restPath + "/services/B").respond({
                label : '1'
            });
            $httpBackend.when('GET', restConfig.restPath + "/services/B/config").respond({
                SNAME : '2'
            });
            $httpBackend.flush();
            serviceConfigService = $injector.get('serviceConfigService');
        }));
        it('should require ConfigureServiceDialogController', function() {
            expect(ConfigureServiceDialogController).toBeDefined();
        });
        it('should get service', function() {
            expect(scope.service.label).toEqual('1');
        });
        it('should get service configuration', function() {
            expect(scope.configuration.SNAME).toEqual(2);
        });

        it('should get service parameters', function() {
            expect(scope.parameters.length).toEqual(1);
        });

        it('should add service parameter', function() {
            scope.configArray = [];
            scope.addParameter();
            expect(scope.configArray.length).toEqual(1);
        });
        it('should save service configuration', function() {
            spyOn(serviceConfigService, 'updateConfig');
            scope.save();
            expect(serviceConfigService.updateConfig).toHaveBeenCalled();
        });

    });

    describe('tests for ThingController', function() {
        var ThingController, scope, injector, deferred;
        beforeEach(inject(function($injector, $rootScope, $controller, $mdDialog, $q) {
            scope = $rootScope.$new();
            mdDialog = $mdDialog;
            $controller('BodyController', {
                '$scope' : scope
            });
            ThingController = $controller('ThingController', {
                '$scope' : scope
            });
            deferred = $q.defer();
        }));
        it('should require ThingController', function() {
            expect(ThingController).toBeDefined();
        });
        it('should open thing remove dialog', function() {
            spyOn(mdDialog, 'show').and.returnValue(deferred.promise);
            ;
            var event = {
                stopImmediatePropagation : function() {
                }
            };
            scope.remove(0, event);
            expect(mdDialog.show).toHaveBeenCalled();
        });
    });

    describe('tests for ViewThingController', function() {
        var ViewThingController, scope, injector, deferred, channelTypeService, itemRepository;
        beforeEach(inject(function($injector, $rootScope, $controller, $mdDialog, $q) {
            scope = $rootScope.$new();
            scope.path = [];
            scope.path[4] = 'TID';
            mdDialog = $mdDialog;
            channelTypeService = $injector.get('channelTypeService');
            itemRepository = $injector.get('itemRepository');
            spyOn(channelTypeService, 'getAll').and.callThrough();
            spyOn(itemRepository, 'getAll');
            ViewThingController = $controller('ViewThingController', {
                '$scope' : scope
            });
            deferred = $q.defer();
            injector = $injector;
        }));
        it('should require ViewThingController', function() {
            expect(ViewThingController).toBeDefined();
        });
        it('should get channelTypes and items', function() {
            expect(channelTypeService.getAll).toHaveBeenCalled();
            expect(itemRepository.getAll).toHaveBeenCalled();
        });
        it('should open remove thing dialog', function() {
            spyOn(mdDialog, 'show').and.returnValue(deferred.promise);
            var event = {
                stopImmediatePropagation : function() {
                }
            };
            scope.remove(0, event);
            expect(mdDialog.show).toHaveBeenCalled();
        });
        it('should link channel advance mode', function() {
            spyOn(mdDialog, 'show').and.returnValue(deferred.promise);
            var event = {
                stopImmediatePropagation : function() {
                }
            };
            scope.thing = {};
            scope.thing.channels = [ {
                id : "T",
                linkedItems : []
            } ];
            scope.channelTypes = [ {
                UID : 'C:T',
                category : ''
            } ];
            scope.advancedMode = true;
            scope.enableChannel(0, 'T', event);
            expect(mdDialog.show).toHaveBeenCalled();
        });
        it('should link channel simple mode', function() {
            var linkService = injector.get("linkService");
            var event = {
                stopImmediatePropagation : function() {
                }
            };
            scope.advancedMode = false;
            scope.thing = {
                UID : "C:T"
            };
            scope.thing.channels = [ {
                id : "T",
                linkedItems : []
            } ];
            spyOn(linkService, 'link');
            scope.enableChannel(0, 'T', event);
            expect(linkService.link).toHaveBeenCalled();
        });
        it('should unlink channel advance mode', function() {
            spyOn(mdDialog, 'show').and.returnValue(deferred.promise);
            var event = {
                stopImmediatePropagation : function() {
                }
            };
            scope.thing = {};
            scope.thing.channels = [ {
                id : "T",
                linkedItems : []
            } ];
            scope.channelTypes = [ {
                UID : 'C:T',
                category : ''
            } ];
            scope.advancedMode = true;
            scope.disableChannel(0, 'T', '', event);
            expect(mdDialog.show).toHaveBeenCalled();
        });
        it('should unlink channel simple mode', function() {
            var linkService = injector.get("linkService");
            var event = {
                stopImmediatePropagation : function() {
                }
            };
            scope.advancedMode = false;
            scope.thing = {
                UID : "C:T"
            };
            scope.thing.channels = [ {
                id : "T",
                linkedItems : []
            } ];
            spyOn(linkService, 'unlink');
            scope.disableChannel(0, 'T', '', event);
            expect(linkService.unlink).toHaveBeenCalled();
        });
    });
    describe('tests for RemoveThingDialogController', function() {
        var RemoveThingDialogController, scope, mdDialog, thingService;
        beforeEach(inject(function($injector, $rootScope, $controller, $mdDialog) {
            scope = $rootScope.$new();
            RemoveThingDialogController = $controller('RemoveThingDialogController', {
                '$scope' : scope,
                'thing' : {}
            });
            mdDialog = $mdDialog;
            thingService = $injector.get('thingService');
        }));
        it('should require ConfigurationPageController', function() {
            expect(RemoveThingDialogController).toBeDefined();
        });
        it('should require close dialog', function() {
            spyOn(mdDialog, 'cancel');
            scope.close();
            expect(mdDialog.cancel).toHaveBeenCalled();
        });
        it('should remove thing', function() {
            spyOn(thingService, 'remove');
            scope.remove(0);
            expect(thingService.remove).toHaveBeenCalled();
        });
    });
    describe('tests for LinkChannelDialogController', function() {
        var LinkChannelDialogController, scope, itemService, deferred;
        beforeEach(inject(function($injector, $rootScope, $controller, $mdDialog,$q) {
            scope = $rootScope.$new();
            $rootScope.data.items = [ {
                type : 'T'
            } ];
            var itemRepository = $injector.get('itemRepository');
            spyOn(itemRepository, 'getAll').and.callFake(function(callback) {
                return callback([ {
                    type : 'T'
                } ]);
            });
            LinkChannelDialogController = $controller('LinkChannelDialogController', {
                '$scope' : scope,
                'linkedItems' : [],
                'acceptedItemType' : 'T',
                'category' : ''
            });
            mdDialog = $mdDialog;
            deferred = $q.defer();
            itemService = $injector.get('itemService');
        }));
        it('should require LinkChannelDialogController', function() {
            expect(LinkChannelDialogController).toBeDefined();
        });
        it('should fetch items', function() {
            expect(scope.items.length).toEqual(1);
            expect(scope.itemsList.length).toEqual(2);
        });
        it('should toggle items form', function() {
            scope.checkCreateOption();
            expect(scope.itemFormVisible).toBeFalsy();
            scope.itemName = "_createNew";
            scope.checkCreateOption();
            expect(scope.itemFormVisible).toBeTruthy();
        });
        it('should create item and link', function() {
            spyOn(itemService, 'create').and.returnValue(deferred.promise).and.callThrough();
            spyOn(itemService.create({}).$promise, 'then').and.returnValue(deferred.promise);
            scope.newItemName="N";
            scope.createAndLink();
            expect(itemService.create).toHaveBeenCalled();
        });
    });
});
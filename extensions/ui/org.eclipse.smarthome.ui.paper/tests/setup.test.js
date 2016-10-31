describe('module PaperUI.controllers.setup', function() {
    beforeEach(function() {
        module('PaperUI');
    });
    describe('tests for SetupPageController', function() {
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            $httpBackend = $injector.get('$httpBackend');
            thingTypeService = $injector.get('thingTypeService');
            restConfig = $injector.get('restConfig');
            setupPageController = $controller('SetupPageController', {
                '$scope' : scope
            });
        }));
        it('should require SetupPageController', function() {
            expect(setupPageController).toBeDefined();
        });
        it('should define thingTypes', function() {
            expect(scope.thingTypes).toBeDefined();
        });
        it('should get data from thingTypeService', function() {
            expect(thingTypeService).toBeDefined();
            var success = false;
            var thingTypes = [ {
                name : 'thing1'
            } ];
            $httpBackend.when('GET', restConfig.restPath + "/thing-types").respond(thingTypes);
            thingTypeService.getAll(function(data) {
                success = true;
            });
            $httpBackend.flush();
            expect(success).toBeTruthy();
        });
    });

    describe('tests for InboxController', function() {
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            $controller('BodyController', {
                '$scope' : scope
            });
            inboxController = $controller('InboxController', {
                '$scope' : scope
            });
            discoveryResultRepository = $injector.get('discoveryResultRepository');
            spyOn(discoveryResultRepository, "getAll");
        }));
        it('should require InboxController', function() {
            expect(inboxController).toBeDefined();
        });
        it('should refresh discovery results', function() {
            scope.refresh();
            expect(discoveryResultRepository.getAll).toHaveBeenCalled();
        });
    });

    describe('tests for InboxEntryController', function() {

        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            mdDialog = $injector.get('$mdDialog');
            inboxService = $injector.get('inboxService');
            inboxEntryController = $controller('InboxEntryController', {
                '$scope' : scope
            });
            $rootScope.data.discoveryResults = [ {
                label : "LABEL",
                thingUID : "A:B"
            } ];
            restConfig = $injector.get('restConfig');
            $rootScope.$apply();
        }));

        beforeEach(inject(function($q, $rootScope) {
            rootScope = $rootScope;
            deferred = $q.defer();
            spyOn(mdDialog, 'show').and.returnValue(deferred.promise);
            $rootScope.$apply();
            spyOn(mdDialog.show(), 'then').and.returnValue(deferred.promise);
        }));

        it('should require InboxEntryController', function() {
            expect(inboxEntryController).toBeDefined();
        });
        it('should call function approve', function() {
            rootScope.$apply();
            spyOn(scope, "approve").and.callThrough();
            scope.approve(0, 0);
            expect(scope.approve).toHaveBeenCalled();
            expect(mdDialog.show().then).toHaveBeenCalled();
        });
        it('should call function ignore', function() {
            spyOn(inboxService, "ignore");
            scope.ignore(1);
            expect(inboxService.ignore).toHaveBeenCalled();
        });
        it('should call function unignore', function() {
            spyOn(inboxService, "unignore");
            scope.unignore(1);
            expect(inboxService.unignore).toHaveBeenCalled();
        });
        it('should call function remove', function() {
            rootScope.$apply();
            scope.remove("A:B");
            expect(mdDialog.show).toHaveBeenCalled();
            expect(mdDialog.show().then).toHaveBeenCalled();
        });
    });

    describe('tests for ScanDialogController', function() {
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            rootScope = $rootScope;
            $httpBackend = $injector.get('$httpBackend');
            scanDialogController = $controller('ScanDialogController', {
                '$scope' : scope
            });
            discoveryService = $injector.get('discoveryService');
            rootScope.data.bindings = [ {
                id : 1,
                name : 'binding'
            } ];
            spyOn(discoveryService, "scan");

        }));
        it('should require ScanDialogController', function() {
            expect(scanDialogController).toBeDefined();
            $httpBackend.when('GET', restConfig.restPath + "/bindings").respond();
            $httpBackend.when('GET', restConfig.restPath + "/discovery").respond([ 1, 2, 3 ]);
            $httpBackend.flush();
            expect(scope.supportedBindings.length).toEqual(3);
        });
        it('should add bindingId to scans', function() {
            scope.scan(1);
            expect(scope.activeScans).toEqual([ 1 ]);
        });
        it('should call discoveryService.scan', function() {
            scope.scan(1);
            expect(discoveryService.scan).toHaveBeenCalled();
        });
        it('should get binding by Id', function() {
            var binding = scope.getBindingById(1);
            expect(binding.name).toEqual('binding');
        });
        it('should return empty object', function() {
            var binding = scope.getBindingById(2);
            expect(binding).toEqual({});
        });
    });

    describe('tests for ApproveInboxEntryDialogController', function() {
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            rootScope = $rootScope;
            $httpBackend = $injector.get('$httpBackend');
            mdDialog = $injector.get('$mdDialog');
            thingTypeRepository = $injector.get('thingTypeRepository');
            spyOn(thingTypeRepository, "getOne").and.callThrough();
            rootScope.data.thingTypes = [ {
                UID : "A:B",
                label : 'LABEL'
            } ];
            approveInboxEntryDialogController = $controller('ApproveInboxEntryDialogController', {
                '$scope' : scope,
                'discoveryResult' : {
                    label : 'LABEL',
                    thingTypeUID : "A:B"
                }
            });
            discoveryService = $injector.get('discoveryService');
        }));
        it('should require ApproveInboxEntryDialogController', function() {
            expect(approveInboxEntryDialogController).toBeDefined();
        });
        it('should require parameters to be passed', function() {
            expect(scope.discoveryResult).toBeDefined();
            expect(scope.label).toEqual('LABEL');
            expect(scope.thingTypeUID).toEqual('A:B');
        });
        it('should get thingType', function() {
            expect(thingTypeRepository.getOne).toHaveBeenCalled();
            expect(scope.thingType.label).toEqual("LABEL");
        });
        it('should call approve', function() {
            spyOn(mdDialog, "hide").and.callThrough();
            scope.approve();
            expect(mdDialog.hide).toHaveBeenCalled();
        });
    });

    describe('tests for ManualSetupConfigureController', function() {
        var scope;
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            rootScope = $rootScope;
            $httpBackend = $injector.get('$httpBackend');
            thingService = $injector.get('thingService');
            thingTypeRepository = $injector.get('thingTypeRepository');
            thingRepository = $injector.get('thingRepository');          
            var thingTypes = {
                UID : "A:B",
                label : 'LABEL',
                supportedBridgeTypeUIDs : [ "A:B", 2, 3 ]
            };
            $httpBackend.when('GET', /rest\/thing-types.*/).respond(thingTypes);
            $controller('BodyController', {
                '$scope' : scope
            });
            manualSetupConfigureController = $controller('ManualSetupConfigureController', {
                $scope : scope,
                $routeParams : {
                    thingTypeUID : "A:B"
                }
            });
        }));
        it('should require ManualSetupConfigureController', function() {
            expect(manualSetupConfigureController).toBeDefined();
        });
        it('should require bridges', function() {
            var things = [ {
                thingTypeUID : "A:B",
                label : "THING"
            } ];
            $httpBackend.when('GET', restConfig.restPath + "/things").respond(things);
            $httpBackend.whenGET(/^((?!rest\/things).)*$/).respond(200, '');           
            $httpBackend.flush();
            expect(scope.needsBridge).toBeTruthy();
            expect(scope.bridges.length).toBe(1);
        });
        it('should add thing', function() {
            spyOn(thingService, "add");
            scope.parameters=[];
            scope.addThing({id:1});
            expect(thingService.add).toHaveBeenCalled();
        });
    });

});
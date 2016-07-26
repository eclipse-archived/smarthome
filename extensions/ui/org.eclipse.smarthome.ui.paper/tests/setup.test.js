describe('module PaperUI.controllers.setup', function() {
    var restConfig;
    describe('tests for SetupPageController', function() {
        var setupPageController, thingTypeService,scope;
        beforeEach(function() {
            module('PaperUI');
        });
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
        it('thingTypes should be defined', function() {
            expect(scope.thingTypes).toBeDefined();
        });
        it('thingTypeService should GET data', function() {
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
        var inboxController,scope;
        beforeEach(function() {
            module('PaperUI');
        });
        beforeEach(inject(function($injector, $rootScope, $controller) {
           scope=$rootScope.$new();
           $controller('BodyController', {
               '$scope' : scope
           });
           inboxController= $controller('InboxController', {
               '$scope' : scope
           });
        }));
        it('should require InboxController', function() {       
            expect(inboxController).toBeDefined();
        });
    });
    
    describe('tests for InboxEntryController', function() {
        var inboxEntryController,scope,inboxService;
        beforeEach(function() {
            module('PaperUI');
        });
        beforeEach(inject(function($injector, $rootScope, $controller,$mdDialog) {
           scope=$rootScope.$new();
           inboxService=$injector.get('inboxService');
           inboxEntryController= $controller('InboxEntryController', {
               '$scope' : scope
           });
           restConfig = $injector.get('restConfig');
        }));
        it('should require InboxEntryController', function() {       
            expect(inboxEntryController).toBeDefined();
        });
        it('should call function approve', function() {       
            spyOn(scope, "approve");
            scope.approve(0,0);
            expect(scope.approve).toHaveBeenCalled();
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
    });

});
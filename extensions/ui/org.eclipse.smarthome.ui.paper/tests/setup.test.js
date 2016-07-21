describe('module PaperUI.controllers.setup', function() {
    var scope, restConfig, controller;

    describe('tests for SetupPageController', function() {
        var setupPageController, thingTypeService;
        beforeEach(function() {
            module('PaperUI');
        });
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            rootScope = $rootScope;
            controller = $controller
            $httpBackend = $injector.get('$httpBackend');
            thingTypeService = $injector.get('thingTypeService');
            restConfig = $injector.get('restConfig');
            setupPageController = controller('SetupPageController', {
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
        var inboxController;
        beforeEach(inject(function($injector, $rootScope, $controller) {
            inboxController = controller('SetupPageController', {
                '$scope' : scope
            });
        }));
        it('should require InboxController', function() {       
            expect(inboxController).toBeDefined();
        });
        it('refresh should get discovery results', function() {
            var inboxController = controller('SetupPageController', {
                '$scope' : scope
            });
            expect(inboxController).toBeDefined();
        });
    });
});
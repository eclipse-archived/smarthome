describe('module PaperUI.controllers.configuration.bindings', function() {
    beforeEach(function() {
        module('PaperUI');
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
    });

    describe('tests for ConfigureBindingDialogController', function() {
        var configureBindingDialogController, scope, bindingService;
        var restConfig;
        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            var bindingRepository = $injector.get('bindingRepository');
            restConfig = $injector.get('restConfig');
            $rootScope.data.bindings = [ {
                id : 'B'
            } ];
            configureBindingDialogController = $controller('ConfigureBindingDialogController', {
                '$scope' : scope,
                'binding' : {
                    'id' : 'B',
                    'configDescriptionURI' : 'CDURI'
                },
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
});
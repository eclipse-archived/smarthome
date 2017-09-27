describe('module PaperUI.controllers.configuration', function() {
    beforeEach(function() {
        module('PaperUI');
    });
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
});
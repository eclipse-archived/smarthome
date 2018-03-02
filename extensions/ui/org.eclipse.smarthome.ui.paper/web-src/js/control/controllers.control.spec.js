describe('module PaperUI.controllers.control', function() {
    beforeEach(function() {
        module('PaperUI.controllers.control');
    })

    describe('DimmerItemController', function() {
        var dimmerItemController;
        var scope;
        var timeout;

        beforeEach(inject(function($rootScope, $injector, $controller) {
            timeout = $injector.get('$timeout');

            scope = $rootScope.$new();

            // in real life item is available through the parent scope
            scope.item = {
                state : 0
            }
            // in real life sendCommand is defined on the parent scope
            scope.sendCommand = jasmine.createSpy('sendCommand spy')

            dimmerItemController = $controller('DimmerItemController', {
                '$scope' : scope,
                '$timeout' : timeout
            });
        }))

        it('should be present', function() {
            expect(dimmerItemController).toBeDefined();
        })

        it('should initialize $scope.state.switchState', function() {
            expect(scope.state.switchState).toBe(false);
        })

        it('should trigger sendCommand(\'ON\') when switch toggels true', function() {
            scope.setSwitch(true);
            timeout.flush(); // immediately execute the delayed sendCommand timeout
            expect(scope.sendCommand).toHaveBeenCalledWith('ON');
        })

        it('should trigger sendCommand(\'OFF\') when switch toggels false', function() {
            scope.setSwitch(false);
            timeout.flush(); // immediately execute the delayed sendCommand timeout
            expect(scope.sendCommand).toHaveBeenCalledWith('OFF');
        })

        it('should trigger sendCommand(80) when brightness sets to 80', function() {
            scope.setBrightness(80)
            timeout.flush(); // immediately execute the delayed sendCommand timeout
            expect(scope.sendCommand).toHaveBeenCalledWith(80);
        })

        it('should set switchState true for brightness > 0', function() {
            scope.setBrightness(1);
            expect(scope.state.switchState).toBe(true);
        })

        it('should set switchState false for brightness == 0', function() {
            scope.setBrightness(0);
            expect(scope.state.switchState).toBe(false);
        })
    })
})

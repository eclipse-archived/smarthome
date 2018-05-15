angular.module('PaperUI.things') //
.directive('thingConfiguration', function() {

    var controller = function($scope, thingTypeService, thingRepository) {
        $scope.$watch('thing', function(thing) {
            if (thing.thingTypeUID) {
                getThingType(thing.thingTypeUID);
            }
        })

        $scope.bridges = [];

        $scope.needsBridge = false;

        $scope.hasBridge = function() {
            return $scope.bridges && $scope.bridges.length > 0;
        }

        var refreshBridges = function(supportedBridgeTypeUIDs) {
            thingRepository.getAll(function(things) {
                $scope.bridges = things.filter(function(thing) {
                    return supportedBridgeTypeUIDs.includes(thing.thingTypeUID)
                })
            });
        }

        var getThingType = function(thingTypeUID) {
            thingTypeService.getByUid({
                thingTypeUID : thingTypeUID
            }, function(thingType) {
                if (thingType.supportedBridgeTypeUIDs && thingType.supportedBridgeTypeUIDs.length > 0) {
                    $scope.needsBridge = true;
                    refreshBridges(thingType.supportedBridgeTypeUIDs);
                } else {
                    $scope.needsBridge = false;
                }
            });
        }
    }

    return {
        restrict : 'E',
        scope : {
            thing : '=',
            isEditing : '=?',
            form : '=?'
        },
        controller : controller,
        templateUrl : 'partials/things/directive.thingConfiguration.html'
    }
});
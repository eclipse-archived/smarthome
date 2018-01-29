angular.module('PaperUI.directive.thingConfiguration', []) //
.directive('thingConfiguration', function() {

    var controller = function($scope, thingTypeService, thingRepository) {
        $scope.$watch('thing', function(thing) {
            if (thing.thingTypeUID) {
                getThingType(thing.thingTypeUID);
            }
        })

        $scope.bridges = [];

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
                    refreshBridges(thingType.supportedBridgeTypeUIDs);
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
        templateUrl : 'partials/directive.thingConfiguration.html'
    }
});
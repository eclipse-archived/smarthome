angular.module('PaperUI.controllers.things', [ 'PaperUI.constants', 'PaperUI.controllers.firmware', 'PaperUI.controllers.configurableServiceDialog', 'PaperUI.directive.thingConfiguration' ]) //
.controller('EditThingController', function($scope, $mdDialog, toastService, thingRepository, configService, configDescriptionService, thingService) {
    $scope.setSubtitle([ 'Things' ]);
    $scope.setHeaderText('Click the \'Save\' button to apply the changes.');

    var thingUID = $scope.path[4];

    $scope.thing = {};
    $scope.groups = [];
    $scope.isEditing = true;
    var originalThing = {};

    // used for the thing config parameters
    $scope.parameters = undefined
    $scope.configuration = {}

    $scope.update = function(thing) {
        thing.configuration = configService.setConfigDefaults(thing.configuration, $scope.parameters, true);
        if (JSON.stringify(originalThing.configuration) !== JSON.stringify(thing.configuration)) {
            thing.configuration = configService.replaceEmptyValues(thing.configuration);
            thingService.updateConfig({
                thingUID : thing.UID
            }, thing.configuration);
        }
        originalThing.configuration = thing.configuration;
        originalThing.channels = thing.channels;
        if (JSON.stringify(originalThing) !== JSON.stringify(thing)) {
            thingService.update({
                thingUID : thing.UID
            }, thing);
        }
        toastService.showDefaultToast('Thing updated');
        $scope.navigateTo('view/' + thing.UID);
    };

    var getThing = function(refresh) {
        // Get the thing
        thingRepository.getOne(function(thing) {
            return thing.UID === thingUID;
        }, function(thing) {
            $scope.thing = thing;
            angular.copy(thing, originalThing);

            $scope.setSubtitle([ 'Things', 'Edit', thing.label ]);

            // Now get the configuration information for this thing
            configDescriptionService.getByUri({
                uri : "thing:" + thing.UID
            }, function(configDescription) {
                if (configDescription) {
                    $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
                    $scope.configuration = configService.setConfigDefaults($scope.thing.configuration, $scope.parameters)
                }
            });

        }, refresh);
    }

    $scope.$watch('configuration', function() {
        if ($scope.configuration) {
            $scope.thing.configuration = $scope.configuration;
        }
    });

    getThing(true);
})

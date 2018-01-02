angular.module('PaperUI.controllers.control') //
.directive('locationControl', function() {
    var controller = function($scope) {
        var updateFormattedState = function() {
            if ($scope.item.state !== 'UNDEF' && $scope.item.state !== 'NULL') {
                var latitude = parseFloat($scope.item.state.split(',')[0]);
                var longitude = parseFloat($scope.item.state.split(',')[1]);
                $scope.formattedState = latitude + '°N ' + longitude + '°E';
            } else {
                $scope.formattedState = '- °N - °E';
            }
        }

        $scope.$watch('item.state', function() {
            updateFormattedState();
        });

        updateFormattedState();
    }

    return {
        restrict : 'E',
        scope : true,
        templateUrl : 'partials/directive.control.location.html',
        controller : controller
    }
})

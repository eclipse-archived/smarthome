angular.module('PaperUI.directive.configDescription', []) //
.directive('configDescription', function() {

    var controller = function($scope) {
        $scope.getName = function(parameter, option) {
            if (!option) {
                return undefined;
            }
            return option.name ? option.name : parameter.context == 'thing' ? option.UID : parameter.context == 'channel' ? option.id : undefined;
        }
    }

    return {
        restrict : 'E',
        scope : {
            configuration : '=',
            parameters : '=',
            expertMode : '=?',
            configArray : '=?',
            form : '=?'
        },
        templateUrl : 'partials/directive.configDescription.html',
        controller : controller
    }
});
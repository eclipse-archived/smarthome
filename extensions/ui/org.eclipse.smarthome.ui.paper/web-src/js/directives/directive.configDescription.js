angular.module('PaperUI.directive.configDescription', []) //
.directive('configDescription', function() {
    return {
        restrict : 'E',
        scope : {
            configuration : '=',
            parameters : '=',
            expertMode : '=?',
            configArray : '=?',
            form : '=?'
        },
        templateUrl : 'partials/directive.configDescription.html'
    }
});
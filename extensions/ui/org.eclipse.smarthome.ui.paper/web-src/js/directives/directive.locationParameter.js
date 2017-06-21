angular.module('PaperUI.directive.locationParameter', [ 'PaperUI.component' ]) //
.directive('locationParameter', function() {
    return {
        restrict : 'E',
        scope : {
            model : '=',
            parameter : '='
        },
        templateUrl : 'partials/directive.locationParameter.html'
    }
});

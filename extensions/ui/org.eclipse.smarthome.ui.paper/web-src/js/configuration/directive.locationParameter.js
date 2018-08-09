angular.module('PaperUI.directive.locationParameter', [ 'PaperUI.component' ]) //
.component('locationParameter', {
    bindings : {
        model : '=',
        parameter : '=',
        form : '='
    },
    templateUrl : 'partials/configuration/directive.locationParameter.html'
});

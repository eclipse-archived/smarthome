;
(function() {
    'use strict';

    angular.module('PaperUI.bindings').directive('bindingOverview', BindingOverview);

    function BindingOverview() {
        return {
            restrict : 'E',
            scope : {},
            bindToController : {
                binding : '='
            },
            controllerAs : '$ctrl',
            templateUrl : 'partials/bindings/directive.binding-overview.html',
            controller : BindingOverviewController
        }
    }

    function BindingOverviewController() {
        // nothing to do, yet
    }
})();

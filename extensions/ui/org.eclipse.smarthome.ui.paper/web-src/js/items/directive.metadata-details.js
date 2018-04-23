;
(function() {
    'use strict';

    angular.module('PaperUI.items').directive('metadata', MetaData);

    function MetaData() {
        return {
            restrict : 'E',
            scope : {},
            bindToController : {
                configDescription : '=',
                item : '='
            },
            controllerAs : '$ctrl',
            templateUrl : 'partials/items/directive.metadata-details.html',
            controller : MetadataDetailsController
        }
    }

    function MetadataDetailsController() {
        var ctrl = this;
        this.mainParameter = this.configDescription.parameters[0]

        activate();

        function activate() {
            console.log(ctrl)
        }
    }

})();
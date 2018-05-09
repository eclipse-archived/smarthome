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
                metadata : '='
            },
            controllerAs : '$ctrl',
            templateUrl : 'partials/items/directive.metadata-details.html',
            controller : MetadataDetailsController
        }
    }

    MetadataDetailsController.$inject = [ 'metadataService' ];

    function MetadataDetailsController(metadataService) {
        var ctrl = this;
        this.namespace = metadataService.URI2Namespace(this.configDescription.uri);
        this.mainParameter = this.configDescription.parameters[0];
        this.hasOptions = hasOptions;

        function hasOptions() {
            return ctrl.mainParameter.options && ctrl.mainParameter.options.length > 0;
        }
    }

})();
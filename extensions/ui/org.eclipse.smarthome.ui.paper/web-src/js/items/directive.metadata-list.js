;
(function() {
    'use strict';

    angular.module('PaperUI.items').directive('metadataList', MetaDataList);

    function MetaDataList() {
        return {
            restrict : 'E',
            scope : {},
            bindToController : {
                item : '='
            },
            controllerAs : '$ctrl',
            templateUrl : 'partials/items/directive.metadata-list.html',
            controller : MetaDataListController
        }
    }

    MetaDataListController.$inject = [ 'configDescriptionService' ];

    function MetaDataListController(configDescriptionService) {
        var ctrl = this;
        this.metadataConfigDescriptions;

        activate();

        function activate() {
            configDescriptionService.getAll({
                scheme : 'metadata'
            }, function(metadataConfigDescriptions) {
                ctrl.metadataConfigDescriptions = metadataConfigDescriptions;

                if (!ctrl.item.metadata) {
                    ctrl.item.metadata = {};
                }

                ctrl.metadataConfigDescriptions.forEach(function(metadataConfigDescription) {
                    if (!ctrl.item.metadata[metadataConfigDescription.uri]) {
                        ctrl.item.metadata[metadataConfigDescription.uri] = {
                            value : undefined
                        }
                    }
                })

            });
        }
    }

})();
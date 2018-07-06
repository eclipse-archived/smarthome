;
(function() {
    'use strict';

    angular.module('PaperUI.controllers.control').component('locationControl', {
        bindings : {
            item : '<',
            onUpdate : '&'
        },
        templateUrl : 'partials/control/directive.control.location.html',
        controller : locationController
    });

    function locationController() {
        var ctrl = this;
        this.formattedState;
        this.editMode = false;
        this.categories = [];

        this.editState = editState;
        this.updateState = updateState;
        this.getLabel = getLabel;
        this.onMapUpdate = onMapUpdate;

        this.$onChanges = onChanges;

        function onChanges(changes) {
            if (changes.item) {
                this.item = angular.copy(this.item);
                ctrl.formattedState = updateFormattedState();
            }
        }

        function updateFormattedState() {
            if (ctrl.item.state !== 'UNDEF' && ctrl.item.state !== 'NULL') {
                var latitude = parseFloat(ctrl.item.state.split(',')[0]);
                var longitude = parseFloat(ctrl.item.state.split(',')[1]);
                return latitude + '°N ' + longitude + '°E';
            } else {
                return '- °N - °E';
            }
        }

        function editState() {
            ctrl.editMode = true;
        }

        function updateState() {
            ctrl.editMode = false;
            ctrl.onUpdate({
                $event : {
                    item : ctrl.item
                }
            });
            ctrl.formattedState = updateFormattedState();
        }

        function getLabel(item, defaultLabel) {
            if (ctrl.item.name) {
                return ctrl.item.label;
            }

            if (ctrl.item.category) {
                var category = categories[ctrl.item.category];
                if (category) {
                    return category.label ? category.label : ctrl.item.category;
                }
            }

            return defaultLabel;
        }

        function onMapUpdate($event) {
            if ($event.location) {
                ctrl.item.state = $event.location;
                ctrl.updateState();
            }
        }
    }
})();
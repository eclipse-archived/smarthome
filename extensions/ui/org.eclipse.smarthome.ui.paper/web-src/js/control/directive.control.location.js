angular.module('PaperUI.controllers.control').component('locationControl', {
    bindings : {
        item : '=',
        readOnly : '<'
    },
    templateUrl : 'partials/control/directive.control.location.html',
    controller : controller
});

function controller() {
    ctrl = this;
    this.formattedState

    $onInit = activate;

    $doCheck = function() {
        var newFormattedState = updateFormattedState();
        if (newFormattedState !== ctrl.formattedState) {
            ctrl.formattedState = newFormattedState;
        }
    }

    function activate() {
        ctrl.formattedState = updateFormattedState();
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
}

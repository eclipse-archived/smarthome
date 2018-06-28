;
(function() {
    'use strict';

    angular.module('PaperUI.things').directive('profileSelection', ProfileSelection);

    function ProfileSelection() {
        return {
            restrict : 'E',
            scope : {},
            replace : true,
            bindToController : {
                profileModel : '=',
                channelKind : '='
            },
            controllerAs : '$ctrl',
            templateUrl : 'partials/things/directive.profile-selection.html',
            controller : SelectProfileController
        }
    }

    SelectProfileController.$inject = [ 'profileTypeRepository' ];

    function SelectProfileController(profileTypeRepository) {
        var ctrl = this;

        this.profileList;

        this.selectProfile = selectProfile;

        this.$onInit = activate;

        function activate() {
            return profileTypeRepository.getAll(function(profileList) {

                // filter by channel kind
                var profiles = [];
                for (var i = 0; i < profileList.length; i++) {
                    if (ctrl.channelKind === profileList[i].kind) {
                        profiles.push(profileList[i]);
                    }
                }

                ctrl.profileList = profiles.sort(function(a, b) {
                    if (a.uid === 'system:default') {
                        return -1;
                    }
                    if (b.uid === 'system:default') {
                        return 1;
                    }

                    return a.uid < b.uid ? -1 : a.uid > b.uid ? 1 : 0
                })
            });
        }

        function selectProfile(value) {
            if (ctrl.profileModel == undefined) {
                return value == 'system:default';
            }
            return (value == ctrl.profileModel) || (value == 'system:' + ctrl.profileModel);
        }
    }
})();

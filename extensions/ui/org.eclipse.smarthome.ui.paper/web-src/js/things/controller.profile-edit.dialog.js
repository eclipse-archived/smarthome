;
(function() {
    'use strict';

    angular.module('PaperUI.things').controller('ProfileEditDialogController', ProfileEditDialogController);

    function ProfileEditDialogController($scope, $mdDialog, configDescriptionService, configService, linkService, toastService, linkConfigDescription, link, channelKind) {
        var ctrl = this;
        this.link = link;
        this.channelKind = channelKind;
        this.linkConfigDescription = linkConfigDescription;
        this.expertMode = false;

        this.oldConfig = link.configuration;

        this.cancel = cancel;
        this.close = close;
        this.hasConfig = hasConfig;

        this.configuration = undefined;
        this.parameterGroups = undefined;

        $scope.$watch(function watchFunction() {
            return ctrl.link.configuration['profile'];
        }, function(newValue) {
            loadConfigDescriptionForProfile(newValue);
        });

        function cancel() {
            stripUnsetValues();
            $mdDialog.hide(false);
        }

        function hasConfig() {
            return ctrl.parameterGroups !== undefined;
        }

        function close() {
            stripUnsetValues();
            $mdDialog.hide(true);
        }

        function stripUnsetValues() {
            // strip unset values for comparison against the old configuration
            Object.keys(link.configuration).forEach(function(key) {
                if (link.configuration[key] === null) {
                    delete link.configuration[key];
                }
            });
        }

        function loadConfigDescriptionForProfile(profileName) {
            if (profileName === undefined) {
                return;
            }
            var name = profileName;
            if (!profileName.includes(':')) {
                name = "system:" + profileName;
            }
            configDescriptionService.getByUri({
                uri : "profile:" + name
            }).$promise.then(function(profileConfigDescription) {
                ctrl.parameterGroups = configService.getRenderingModel(profileConfigDescription.parameters, profileConfigDescription.parameterGroups);
                link.configuration = configService.setConfigDefaults(link.configuration, ctrl.parameterGroups);

            }, function() {
                link.configuration = {
                    profile : profileName
                };
                ctrl.parameterGroups = undefined;
            })
        }
    }
})();

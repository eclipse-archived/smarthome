;
(function() {
    'use strict';

    angular.module('PaperUI.things').controller('SelectProfileDialogController', SelectProfileDialogController);

    function SelectProfileDialogController($scope, $mdDialog, configDescriptionService, configService, linkService, toastService, linkConfigDescription, link, options) {
        var ctrl = this;
        this.link = link;
        this.linkConfigDescription = linkConfigDescription;

        this.oldConfig = link.configuration;

        this.cancel = cancel;
        this.close = close;
        this.hasOptions = hasOptions;
        this.hasConfig = hasConfig;
        this.selectedProfile = selectedProfile;

        this.configuration = undefined;
        this.parameterGroups = undefined;

        if (options === undefined) {
            if (this.linkConfigDescription.parameters.length > 0) {
                for (var i = 0; i < this.linkConfigDescription.parameters.length; i++) {
                    if (this.linkConfigDescription.parameters[i].name == "profile") {
                        this.options = linkConfigDescription.parameters[i].options;
                        break;
                    }
                }
            }
        } else {
            this.options = options;
        }

        this.options = this.options.sort(function(a, b) {
            if (a.value === 'system:default') {
                return -1;
            }
            if (b.value === 'system:default') {
                return 1;
            }

            return a.value < b.value ? -1 : a.value > b.value ? 1 : 0
        })

        $scope.$watch(function watchFunction() {
            return ctrl.link.configuration['profile'];
        }, function(newValue) {
            loadConfigDescriptionForProfile(newValue);
        });

        function cancel() {
            $mdDialog.hide(false);
        }

        function hasOptions() {
            return this.options !== null && this.options.length > 0;
        }

        function hasConfig() {
            return ctrl.parameterGroups !== undefined;
        }

        function selectedProfile(value) {
            if (ctrl.link.configuration['profile'] == undefined) {
                return value == 'system:default';
            }
            return (value == ctrl.link.configuration['profile']) || (value == 'system:' + ctrl.link.configuration['profile']);
        }

        function close() {
            // strip unset values for comparison against the old configuration
            Object.keys(link.configuration).forEach(function(key) {
                if (link.configuration[key] === null) {
                    delete link.configuration[key];
                }
            });

            $mdDialog.hide(true);
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

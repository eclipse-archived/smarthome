angular.module('PaperUI', [
  'PaperUI.controllers',
  'PaperUI.controllers.control',
  'PaperUI.controllers.setup',
  'PaperUI.controllers.configuration',
  'PaperUI.controllers.extension',
  'PaperUI.controllers.rules',
  'PaperUI.services',
  'PaperUI.services.rest',
  'PaperUI.services.repositories',
  'PaperUI.extensions',
  'ngRoute',
  'ngResource',
  'ngMaterial'
]).config(['$routeProvider', '$httpProvider', 'globalConfig', function($routeProvider, httpProvider, globalConfig) {
  $routeProvider.
	when('/control', {templateUrl: 'partials/control.html', controller: 'ControlPageController', title: 'Control', simpleHeader: true}).
	when('/setup', {redirectTo: '/setup/search'}).
	when('/setup/search', {templateUrl: 'partials/setup.html', controller: 'InboxController', title: 'Setup Wizard'}).
	when('/setup/manual-setup/choose', {templateUrl: 'partials/setup.html', controller: 'ManualSetupChooseController', title: 'Setup Wizard'}).
	when('/setup/manual-setup/configure/:thingTypeUID', {templateUrl: 'partials/setup.html', controller: 'ManualSetupConfigureController', title: 'Setup Wizard'}).
	when('/setup/wizard', {templateUrl: 'partials/setup.html', controller: 'SetupWizardController', title: 'Setup Wizard'}).
	when('/setup/wizard/bindings', {templateUrl: 'partials/setup.html', controller: 'SetupWizardController', title: 'Setup Wizard'}).
	when('/setup/wizard/search/:bindingId', {templateUrl: 'partials/setup.html', controller: 'SetupWizardController', title: 'Setup Wizard'}).
	when('/setup/wizard/thing-types/:bindingId', {templateUrl: 'partials/setup.html', controller: 'SetupWizardController', title: 'Setup Wizard'}).
	when('/setup/wizard/add/:thingTypeUID', {templateUrl: 'partials/setup.html', controller: 'SetupWizardController', title: 'Setup Wizard'}).
	when('/configuration', {redirectTo: '/configuration/bindings'}).
	when('/configuration/bindings', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/services', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/groups', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/things', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/things/view/:thingUID', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/things/edit/:thingUID', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/extensions', {templateUrl: 'partials/extensions.html', controller: 'ExtensionPageController', title: 'Extensions'}).
	when('/rules', {templateUrl: 'partials/rules.html', controller: 'RulesPageController', title: 'Rules'}).
	when('/rules/new', {templateUrl: 'partials/rules.html', controller: 'RulesPageController', title: 'Rules'}).
	when('/rules/view/:ruleUID', {templateUrl: 'partials/rules.html', controller: 'RulesPageController', title: 'Rules'}).
	when('/rules/configure/:ruleUID', {templateUrl: 'partials/rules.html', controller: 'RulesPageController', title: 'Rules'}).
	when('/preferences', {templateUrl: 'partials/preferences.html', controller: 'PreferencesPageController', title: 'Preferences'});
    if(globalConfig.defaultRoute) {  
        $routeProvider.otherwise({redirectTo: globalConfig.defaultRoute});
    } else {
        $routeProvider.otherwise({redirectTo: '/control'});
    }
}]).directive('editableitemstate', function(){
    return function($scope, $element) {
        $element.context.addEventListener('focusout', function(e){
            $scope.sendCommand($($element).html());
        });
    };
}).directive('onFinishRender', function ($timeout) {
    return {
        restrict: 'A',
        link: function (scope, element, attr) {
            if (scope.$last === true) {
                $timeout(function () {
                    scope.$emit('ngRepeatFinished');
                });
            }
        }
    }
}).run(['$location', '$rootScope', 'globalConfig', function($location, $rootScope, globalConfig) {
	var original = $location.path;
	$rootScope.$on('$routeChangeSuccess', function (event, current, previous) {
	 	if (current.hasOwnProperty('$$route')) {
        	$rootScope.title = current.$$route.title;
        	$rootScope.simpleHeader = current.$$route.simpleHeader;
        }
        $rootScope.path = $location.path().split('/');
        $rootScope.section = $rootScope.path[1];
        $rootScope.page = $rootScope.path[2];
    });
    $rootScope.asArray = function (object) {
        return $.isArray(object) ? object : object ? [ object ] : [] ;
    }
    $rootScope.itemUpdates = {};
    $rootScope.data = [];
    $rootScope.navigateToRoot = function() {
        $location.path('');
    }
    $rootScope.$location = $location;
    var advancedMode = localStorage.getItem('paperui.advancedMode');
    if(advancedMode !== 'true' && advancedMode !== 'false') {
        $rootScope.advancedMode = globalConfig.advancedDefault;
    } else {
        $rootScope.advancedMode = advancedMode === 'true';
    }
    
}]);
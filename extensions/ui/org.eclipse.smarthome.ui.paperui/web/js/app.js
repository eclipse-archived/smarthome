angular.module('PaperUI', [
  'PaperUI.controllers',
  'PaperUI.controllers.control',
  'PaperUI.controllers.setup',
  'PaperUI.controllers.configuration',
  'PaperUI.services',
  'PaperUI.services.rest',
  'PaperUI.services.repositories',
  'PaperUI.extensions',
  'ngRoute',
  'ngResource',
  'ngMaterial'
]).config(['$routeProvider', '$httpProvider', function($routeProvider) {
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
	when('/configuration/groups', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/things', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/things/view/:thingUID', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/configuration/things/edit/:thingUID', {templateUrl: 'partials/configuration.html', controller: 'ConfigurationPageController', title: 'Configuration'}).
	when('/preferences', {templateUrl: 'partials/preferences.html', controller: 'PreferencesPageController', title: 'Preferences'}).
	otherwise({redirectTo: '/control'});
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
}).run(['$location', '$rootScope', function($location, $rootScope) {
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
}]);
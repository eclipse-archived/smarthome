/* This file can be be overridden by fragments for customization */
angular.module('PaperUI.constants', [])
.constant('restConfig', {
  'restPath': '/rest',
  'eventPath': $('#authentication').data('access-token') != '{{ACCESS_TOKEN}}' ? '/rest/events?access_token=' + $('#authentication').data('access-token') : '/rest/events',
})
;

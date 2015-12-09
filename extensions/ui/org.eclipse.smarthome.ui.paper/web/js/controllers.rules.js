angular.module('PaperUI.controllers.rules', 
[]).controller('RulesPageController', function($scope, $location) {
    $scope.navigateTo = function(path) {
        $location.path('rules/' + path);
    } 
}).controller('RulesController', function($scope, $timeout, ruleRepository, ruleService, toastService) {
	$scope.setHeaderText('Shows all rules.');
	$scope.refresh = function() {
		ruleRepository.getAll(true);	
	};
	$scope.configure = function(rule) {
		$scope.navigateTo('configure/' + rule.uid);
	};
	$scope.remove = function(rule, e) {
		e.stopImmediatePropagation();
		ruleService.remove({ruleUID: rule.uid}, function() {
			$scope.refresh();
			toastService.showDefaultToast('Rule removed.');
		});
	};
	ruleRepository.getAll();
}).controller('ViewRuleController', function($scope, ruleRepository) {
	var ruleUID = $scope.path[3];
	ruleRepository.getOne(function(rule) {
		return rule.uid === ruleUID;
	}, function(rule) {
		$scope.setSubtitle([rule.name]);
		$scope.rule = rule;
	})
}).controller('NewRuleController', function($scope, itemRepository, ruleService, toastService) {
	$scope.setSubtitle(['New Rule']);
	itemRepository.getAll();
	$scope.save = function() {
		var rule = {
			"triggers" : [ {
				"id" : "trigger",
				"configuration" : {
					"eventSource" : $scope.item,
					"eventTopic" : "smarthome/items/MyTrigger/state",
					"eventTypes" : "ItemStateEvent"
				},
				"type" : "GenericEventTrigger"
			} ],
			"conditions" : [ {
				"id" : "condition",
				"configuration" : {
					"type" : "application/javascript",
					"script" : "trigger_event.itemState==" + $scope.state
				},
				"type" : "ScriptCondition"
			} ],
			"actions" : [ {
				"id" : "action",
				"configuration" : {
					"type" : "application/javascript",
					"script" : $scope.script
				},
				"type" : "ScriptAction"
			} ],
			"name" : $scope.name,
			"description" : "Sample rule based on scripts"
		}
		console.log(rule);
		ruleService.add(rule, function() {
			toastService.showDefaultToast('Rule added.');
			$scope.navigateTo('');
		});
	};
}).controller('RuleConfigureController', function($scope, ruleRepository, ruleService, toastService) {
	$scope.setSubtitle(['Configure']);
	var ruleUID = $scope.path[3];
	ruleRepository.getOne(function(rule) {
		return rule.uid === ruleUID;
	}, function(rule) {
		$scope.setSubtitle(['Configure ' + rule.name]);	
	})
	ruleService.getModuleConfigParameter({'ruleUID': ruleUID}, function(data) {
		$scope.script = data.content;
	});
	$scope.save = function() {
		ruleService.setModuleConfigParameter({'ruleUID': ruleUID}, $scope.script, function(data) {
			toastService.showDefaultToast('Rule updated successfully.');
			$scope.navigateTo('');
		});
	};
});
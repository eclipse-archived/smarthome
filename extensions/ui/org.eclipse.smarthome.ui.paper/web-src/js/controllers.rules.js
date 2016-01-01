'use strict';

angular
		.module('PaperUI.controllers.rules', [])
		.controller(
				'RulesPageController',
				function($scope, $location, $mdDialog) {
					$scope.navigateTo = function(path) {
						$location.path('rules/' + path);
					};

					$scope.openDialog = function(ctrl, url, params) {
						$mdDialog.show({
							controller : ctrl,
							templateUrl : url,
							targetEvent : params.event,
							hasBackdrop : true,
							locals : params
						});
					};

					$scope.getRuleJSON = function(sharedProperties, uid, name,
							desc) {
						var rule = {
							tags : [],
							conditions : sharedProperties
									.getModuleJSON('condition'),
							description : desc,
							name : name,
							triggers : sharedProperties
									.getModuleJSON('trigger'),
							configDescriptions : [],
							actions : sharedProperties.getModuleJSON('action')
						};

						if (uid)
							rule.uid = uid;

						return rule;
					}
				})
		.controller(
				'RulesController',
				function($scope, $timeout, ruleRepository, ruleService,
						toastService, sharedProperties) {
					$scope.setHeaderText('Shows all rules.');

					$scope.refresh = function() {
						ruleRepository.getAll(true);
					};

					$scope.configure = function(rule) {
						$scope.navigateTo('configure/' + rule.uid);
					};

					$scope.remove = function(rule, e) {
						e.stopImmediatePropagation();
						ruleService.remove({
							ruleUID : rule.uid
						}, function() {
							$scope.refresh();
							toastService.showDefaultToast('Rule removed.');
						});
					};

					ruleRepository.getAll(true);

					$scope.removePart = function(opt, id) {
						sharedProperties.removeFromArray(opt, id);
					};
					$scope.toggleEnabled = function(rule, e) {
						e.stopImmediatePropagation();
						ruleService
								.setEnabled(
										{
											ruleUID : rule.uid
										},
										(!rule.enabled).toString(),
										function() {
											$scope.refresh();
											if (rule.enabled) {
												toastService
														.showDefaultToast('Rule disabled.');
											} else {
												toastService
														.showDefaultToast('Rule enabled.');
											}
										});
					};
				})
		.controller('ViewRuleController', function($scope, ruleRepository) {
			var ruleUID = $scope.path[3];
			ruleRepository.getOne(function(rule) {
				return rule.uid === ruleUID;
			}, function(rule) {
				$scope.setSubtitle([rule.name]);
				$scope.rule = rule;
			});
		})
		.controller(
				'NewRuleController',
				function($scope, itemRepository, ruleService, toastService,
						$mdDialog, sharedProperties) {
					$scope.setSubtitle(['New Rule']);
					itemRepository.getAll();
					sharedProperties.reset();
					$scope.editing = false;
					var ruleUID = $scope.path[3];

					if ($scope.path[3]) {
						ruleService.getByUid({
							ruleUID : ruleUID
						},
								function(data) {
									$scope.name = data.name;
									$scope.description = data.description;

									sharedProperties.addArray('trigger',
											data.triggers);
									sharedProperties.addArray('action',
											data.actions);
									sharedProperties.addArray('condition',
											data.conditions);

								});
						$scope.setSubtitle(['Configure']);
						$scope.editing = true;
					}

					$scope.saveUserRule = function() {

						var rule = $scope.getRuleJSON(sharedProperties, null,
								$scope.name, $scope.description);
						ruleService.add(rule);
						toastService.showDefaultToast('Rule added.');
						$scope.navigateTo('');
					};

					$scope.updateUserRule = function() {

						var rule = $scope
								.getRuleJSON(sharedProperties, $scope.path[3],
										$scope.name, $scope.description);
						ruleService.update({
							ruleUID : $scope.path[3]
						}, rule);
						toastService.showDefaultToast('Rule updated.');
						$scope.navigateTo('');
					};

					$scope.openNewModuleDialog = function(event, type) {
						$scope.openDialog('addModuleDialogController',
								'partials/dialog.addmodule.html', {
									event : event,
									module : {},
									ruleID : $scope.path[3] || '',
									type : type
								});
					};

					$scope.openUpdateModuleDialog = function(event, type,
							module) {
						$scope.openDialog('addModuleDialogController',
								'partials/dialog.addmodule.html', {
									event : event,
									module : module,
									ruleID : $scope.path[3] || '',
									type : type
								});
					};

					$scope.aTriggers = sharedProperties.getTriggersArray();
					$scope.aActions = sharedProperties.getActionsArray();
					$scope.aConditions = sharedProperties.getConditionsArray();

				})
		.controller(
				'RuleConfigureController',
				function($scope, ruleRepository, ruleService, toastService) {
					$scope.setSubtitle(['Configure']);
					var ruleUID = $scope.path[3];

					ruleRepository.getOne(function(rule) {
						return rule.uid === ruleUID;
					}, function(rule) {
						$scope.setSubtitle(['Configure ' + rule.name]);
					});

					ruleService.getModuleConfigParameter({
						ruleUID : ruleUID
					}, function(data) {
						$scope.script = data.content;
					});

					$scope.save = function() {
						ruleService
								.setModuleConfigParameter(
										{
											ruleUID : ruleUID
										},
										$scope.script,
										function() {
											toastService
													.showDefaultToast('Rule updated successfully.');
											$scope.navigateTo('');
										});
					};
				});
angular.module('PaperUI.controllers.configuration').controller('ItemSetupController', function($scope, $timeout, $mdDialog, $filter, itemService, toastService, sharedProperties) {
    $scope.setSubtitle([ 'Items' ]);
    $scope.setHeaderText('Shows all configured Items.');
    $scope.items = [];
    $scope.refresh = function() {
        itemService.getNonRecursiveAll(function(items) {
            $scope.items = $filter('filter')(items, {
                type : '!GroupItem'
            })
        });

    };
    $scope.remove = function(item, event) {
        event.stopImmediatePropagation();
        $mdDialog.show({
            controller : 'ItemRemoveController',
            templateUrl : 'partials/dialog.removeitem.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                item : item
            }
        }).then(function() {
            $scope.refresh();
        });
    }
    $scope.getSrcURL = function(category, type) {
        return category ? '../icon/' + category.toLowerCase() : type ? '../icon/' + type.toLowerCase().replace('item', '') : '';
    }
    $scope.refresh();
}).controller('ItemConfigController', function($scope, $mdDialog, $filter, $location, toastService, itemService, itemConfig, itemRepository) {
    $scope.items = [];
    $scope.oldCategory;
    $scope.types = itemConfig.types;
    var itemName;
    var originalItem = {};
    if ($scope.path && $scope.path.length > 4) {
        itemName = $scope.path[4];
    }
    itemService.getNonRecursiveAll(function(items) {
        $scope.items = items;
        if (itemName) {
            var items = $filter('filter')(items, {
                name : itemName
            });
            if (items.length > 0) {
                $scope.item = items[0];
                angular.copy($scope.item, originalItem);
                $scope.configMode = "edit";
                $scope.srcURL = $scope.getSrcURL($scope.item.category, $scope.item.type);
                $scope.oldCategory = $scope.item.category;
                $scope.setTitle('Edit ' + $scope.item.name);
                $scope.setSubtitle([]);
            }
        } else {
            $scope.item = {};
            $scope.setTitle('Configuration');
            $scope.setSubtitle([ 'New Item' ]);
            $scope.configMode = "create";
        }

    });

    $scope.update = function() {
        putItem("Item updated.");
    }
    $scope.create = function(item) {
        putItem("Item created.");
    }

    function putItem(text) {
        if (JSON.stringify($scope.item) !== JSON.stringify(originalItem)) {
            itemService.create({
                itemName : $scope.item.name
            }, $scope.item).$promise.then(function() {
                toastService.showDefaultToast(text);
            });
        }
        $location.path('configuration/items');
    }

    $scope.renderIcon = function() {
        $scope.oldCategory = $scope.item.category;
        $scope.srcURL = $scope.getSrcURL($scope.item.category, $scope.item.type);
    }

}).controller('ItemRemoveController', function($scope, $mdDialog, $filter, $location, toastService, itemService, item) {
    $scope.item = item;
    $scope.remove = function(itemName) {
        itemService.remove({
            itemName : itemName
        }, function() {
            toastService.showDefaultToast('Item removed.');
        });
        $mdDialog.hide();
    }

    $scope.close = function() {
        $mdDialog.cancel();
    }
}).directive('itemname', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, element, attr, ctrl) {
            function customValidator(ngModelValue) {

                var items = getItems();
                if (!searchItemNameExists(ngModelValue, items)) {
                    ctrl.$setValidity('nameValidator', true);
                    if (ngModelValue != null && ngModelValue.length != 0) {
                        element.parent().removeClass('md-input-invalid');
                    }
                } else {
                    ctrl.$setValidity('nameValidator', false);
                    element.parent().addClass('md-input-invalid');
                }
                return ngModelValue;
            }
            function searchItemNameExists(value, arr) {
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i].name == value) {
                        return true;
                    }
                }
                return false;
            }
            function getItems() {
                return scope.items;
            }
            ctrl.$parsers.push(customValidator);
        }
    };
});
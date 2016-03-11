'use strict';

angular.module('PaperUI.controllers.rules').service('sharedProperties', function() {
    var triggersArray = [];
    var actionsArray = [];
    var conditionsArray = [];
    var tId = 1, aId = 1, cId = 1;
    var params = [];
    return {
        updateParams : function(elem) {
            params.push(elem);
        },
        getParams : function() {
            return params;
        },
        resetParams : function() {
            params = [];
        },
        addArray : function(type, arr) {
            arr.type = type;
            var self = this;
            angular.forEach(arr, function(value) {
                self.updateModule(arr.type, value);
            });
        },
        updateModule : function(type, value) {
            var modArr = this.getModuleArray(type);

            if (!value.id) {
                value.id = type + "_" + tId;
                modArr.push(value);
                tId++;
            } else {
                var index = this.searchArray(modArr, value.id);
                if (index != -1) {
                    modArr[index] = value;
                } else {
                    modArr.push(value);
                    tId++;
                }
            }
        },
        getTriggersArray : function() {
            return triggersArray;
        },
        getActionsArray : function() {
            return actionsArray;
        },
        getConditionsArray : function() {
            return conditionsArray;
        },
        getModuleJSON : function(mtype) {
            var $moduleJSON = [];
            var i = 1;
            var modArr = this.getModuleArray(mtype);
            modArr.mtype = mtype;
            angular.forEach(modArr, function(value) {
                var type = typeof value.uid === "undefined" ? value.type : value.uid;
                $moduleJSON.push({
                    "id" : value.id ? value.id : modArr.mtype + "_" + i,
                    "label" : value.label,
                    "description" : value.description,
                    "type" : type,
                    "configuration" : value.configuration ? value.configuration : {}
                });
                i++;
            });

            return $moduleJSON;
        },
        reset : function() {
            triggersArray = [];
            actionsArray = [];
            conditionsArray = [];
            tId = 1;
            aId = 1;
            cId = 1;

        },
        removeFromArray : function(opt, id) {
            var arr = null;
            if (angular.equals(opt, "trigger")) {
                arr = triggersArray;
            } else if (angular.equals(opt, "action")) {
                arr = actionsArray;
            } else if (angular.equals(opt, "condition")) {
                arr = conditionsArray;
            }

            var index = this.searchArray(arr, id);
            if (index != -1) {
                arr.splice(index, 1);
            }
        },
        searchArray : function(arr, uid) {

            var k;
            for (k = 0; arr != null && k < arr.length; k = k + 1) {
                if (arr[k].id === uid) {
                    return k;
                }
            }
            return -1;

        },

        getModuleArray : function(type) {
            if (type == 'trigger') {
                return triggersArray;
            } else if (type == 'action') {
                return actionsArray;
            } else if (type == 'condition') {
                return conditionsArray;
            }

        }

    }
});
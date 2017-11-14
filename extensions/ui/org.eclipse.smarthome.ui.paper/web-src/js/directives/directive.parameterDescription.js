angular.module('PaperUI.directive.parameterDescription', []) //
.directive('parameterDescription', function() {
    return {
        restrict : 'E',
        scope : {
            description : '='
        },
        templateUrl : 'partials/directive.parameterDescription.html',
        link : function(scope, element, attrs, controllers) {
            scope.showMore = function(event) {
                if (event.target === event.currentTarget) { // we clicked the 'ellipsis' <div>, not the underlying <p>.
                    element.addClass('show-more');
                }
            }

            scope.showLess = function() {
                element.removeClass('show-more');
            }
        }
    }
});
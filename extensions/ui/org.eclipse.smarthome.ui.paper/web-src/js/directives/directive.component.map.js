angular.module('PaperUI.component', []) //
.directive('mapComponent', function(mapSourceService) {
    var Drag = function(callback) {

        ol.interaction.Pointer.call(this, {
            handleDownEvent : Drag.prototype.handleDownEvent,
            handleDragEvent : Drag.prototype.handleDragEvent,
            handleMoveEvent : Drag.prototype.handleMoveEvent,
            handleUpEvent : Drag.prototype.handleUpEvent
        });

        this.coordinate_ = null;
        this.cursor_ = 'pointer';
        this.feature_ = null;
        this.previousCursor_ = undefined;
        this.callback_ = callback;

    };
    ol.inherits(Drag, ol.interaction.Pointer);

    Drag.prototype.handleDownEvent = function(evt) {
        var map = evt.map;

        var feature = map.forEachFeatureAtPixel(evt.pixel, function(feature) {
            return feature;
        });

        if (feature) {
            this.coordinate_ = evt.coordinate;
            this.feature_ = feature;
        }

        return !!feature;
    };

    Drag.prototype.handleDragEvent = function(evt) {
        var deltaX = evt.coordinate[0] - this.coordinate_[0];
        var deltaY = evt.coordinate[1] - this.coordinate_[1];

        var geometry = /** @type {ol.geom.SimpleGeometry} */
        (this.feature_.getGeometry());
        geometry.translate(deltaX, deltaY);

        this.coordinate_[0] = evt.coordinate[0];
        this.coordinate_[1] = evt.coordinate[1];
    };

    Drag.prototype.handleMoveEvent = function(evt) {
        if (this.cursor_) {
            var map = evt.map;
            var feature = map.forEachFeatureAtPixel(evt.pixel, function(feature) {
                return feature;
            });
            var element = evt.map.getTargetElement();
            if (feature) {
                if (element.style.cursor != this.cursor_) {
                    this.previousCursor_ = element.style.cursor;
                    element.style.cursor = this.cursor_;
                }
            } else if (this.previousCursor_ !== undefined) {
                element.style.cursor = this.previousCursor_;
                this.previousCursor_ = undefined;
            }
        }
    };

    Drag.prototype.handleUpEvent = function() {
        this.callback_(this.coordinate_);
        this.coordinate_ = null;
        this.feature_ = null;
        return false;
    };

    function createMap(element, coordinateCallback) {
        var mapLayer = new ol.layer.Tile({
            source : mapSourceService.getMapSource()
        });

        var drag = [];
        if (coordinateCallback) {
            drag = ol.interaction.defaults().extend([ new Drag(coordinateCallback) ]);
        }

        var map = new ol.Map({
            layers : [ mapLayer ],
            target : element,
            view : new ol.View({
                center : ol.proj.fromLonLat([ 0, 0 ]),
                zoom : 2
            }),
            interactions : drag
        });

        return map;
    }

    var createController = function(mapSourceService) {
        // return an empty controller function in case no map source is provided:
        if (!mapSourceService.getMapSource()) {
            return function($scope) {
                $scope.hasMapSource = false
                $scope.redrawMap = function() {
                }
            };
        }

        // return the map controller in case a map source is provided:
        return function($scope) {
            $scope.hasMapSource = true

            var element = 'map';
            var map;

            if ($scope.readOnly) {
                map = createMap(element, null);
            } else {
                map = createMap(element, function(dragCoordinates) {
                    $scope.$apply(function() {
                        var location = getLocation($scope.model);
                        var altitude = location[2];

                        var lonLatCoordinates = ol.proj.toLonLat(dragCoordinates);
                        var model = lonLatCoordinates[1] + ',' + lonLatCoordinates[0];
                        if (altitude != null) {
                            model += (',' + altitude);
                        }
                        $scope.model = model;
                    });
                });
            }

            var point = new ol.geom.Point(ol.proj.fromLonLat([ 0, 0 ]));

            map.addLayer(createVectorLayer(point));

            $scope.redrawMap = function() {
                setTimeout(function() {
                    if ($scope.model) {
                        updateMap(map, point, $scope.model);
                        map.getView().setZoom(14);
                    } else {
                        updateMap(map, point, [ 0, 0 ])
                    }
                    map.updateSize();
                }, 100);
            }

            $scope.$watch('model', function() {
                updateMap(map, point, $scope.model);
            })
        }
    }

    var updateMap = function(map, point, model) {
        var location = getLocation(model);
        var latitude = location[0];
        var longitude = location[1];

        var center = ol.proj.fromLonLat([ longitude, latitude ]);
        map.getView().setCenter(center);
        point.setCoordinates(center);
    }

    var createVectorLayer = function(point) {
        var iconStyle = new ol.style.Style({
            image : new ol.style.Icon({
                anchor : [ 0.5, 51 ],
                anchorXUnits : 'fraction',
                anchorYUnits : 'pixels',
                src : 'img/logo_pointer.png'
            }),
            stroke : new ol.style.Stroke({
                width : 3,
                color : [ 255, 0, 0, 1 ]
            }),
            fill : new ol.style.Fill({
                color : [ 0, 0, 255, 0.6 ]
            })
        });

        var pointFeature = new ol.Feature(point);
        var vectorLayer = new ol.layer.Vector({
            source : new ol.source.Vector({
                features : [ pointFeature ]
            }),
            style : iconStyle
        });

        return vectorLayer;
    }

    var getLocation = function(model) {
        var params = ('' + model).split(',');
        if (params.length < 2) {
            return [];
        }

        var latitude = parseFloat(params[0]);
        var longitude = parseFloat(params[1]);
        var altitude = null;
        if (params.length > 2) {
            altitude = parseFloat(params[2]);
        }

        return [ latitude, longitude, altitude ];
    }

    return {
        restrict : 'E',
        scope : {
            model : '=',
            readOnly : '='
        },
        templateUrl : 'partials/directive.component.map.html',
        controller : [ '$scope', createController(mapSourceService) ],
        link : function($scope) {
            $scope.redrawMap()
        }
    }
})

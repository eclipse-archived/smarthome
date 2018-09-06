;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('mapComponent', {
        bindings : {
            model : '<',
            readOnly : '<',
            onUpdate : '&'
        },
        templateUrl : 'partials/control/map/component.control.map.html',
        controller : MapController
    });

    MapController.$inject = [ '$timeout', 'mapSourceService' ];

    function MapController($timeout, mapSourceService) {

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
        }

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

        var ctrl = this;
        this.map;
        this.point;
        this.$onInit = redrawMap;
        this.$onChanges = onChanges;

        activate();

        function onChanges(changes) {
            if (changes.model) {
                ctrl.model = changes.model.currentValue;
                updateMap(ctrl.map, ctrl.point, ctrl.model);
            }
        }

        function redrawMap() {
            setTimeout(function() {
                if (ctrl.model && ctrl.model.length > 0) {
                    updateMap(ctrl.map, ctrl.point, ctrl.model);
                    ctrl.map.getView().setZoom(14);
                } else {
                    updateMap(ctrl.map, ctrl.point, [ 0, 0 ])
                }
                ctrl.map.updateSize();
            }, 100);
        }

        function activate() {
            if (!mapSourceService) {
                ctrl.hasMapSource = false;
                ctrl.redrawMap = function() {
                };
                return;
            }

            ctrl.hasMapSource = true
            var element = 'map';

            if (ctrl.readOnly) {
                ctrl.map = createMap(element, null);
            } else {
                ctrl.map = createMap(element, function(dragCoordinates) {
                    var location = getLocation(ctrl.model);
                    var altitude = location[2];

                    var lonLatCoordinates = ol.proj.toLonLat(dragCoordinates);
                    var model = lonLatCoordinates[1] + ',' + lonLatCoordinates[0];
                    if (altitude != null) {
                        model += (',' + altitude);
                    }

                    $timeout(function() {
                        ctrl.onUpdate({
                            $event : {
                                location : model
                            }
                        });
                    }, 0);

                });
            }
        }

        function createMap(element, coordinateCallback) {
            var mapLayer = new ol.layer.Tile({
                source : mapSourceService.getMapSource()
            });

            ctrl.point = new ol.geom.Point(ol.proj.fromLonLat([ 0, 0 ]));
            var vectorLayer = createVectorLayer(ctrl.point);

            var drag = [];
            if (coordinateCallback) {
                drag = ol.interaction.defaults().extend([ new Drag(coordinateCallback) ]);
            }

            var map = new ol.Map({
                layers : [ mapLayer, vectorLayer ],
                target : element,
                view : new ol.View({
                    center : ol.proj.fromLonLat([ 0, 0 ]),
                    zoom : 2
                }),
                interactions : drag
            });

            return map;
        }

        function updateMap(map, point, model) {
            var location = getLocation(model);
            var latitude = location[0];
            var longitude = location[1];

            if (isNaN(latitude) || isNaN(longitude)) {
                return;
            }

            var center = ol.proj.fromLonLat([ longitude, latitude ]);
            map.getView().setCenter(center);
            point.setCoordinates(center);
        }

        function createVectorLayer(point) {
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

        function getLocation(model) {
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
    }
})()

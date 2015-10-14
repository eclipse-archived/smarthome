/**
 * Eclipse Smarthome BasicUI javascript
 *
 * @author Vlad Ivanov — initial version
 */

/*eslint-env browser */
/*eslint no-undef:2*/
/*eslint no-new:0 */
/*eslint no-underscore-dangle:0*/

/*global componentHandler */

(function(o) {
	"use strict";

	var
		smarthome = {
			dataModel: {}
		};

	var
		featureSupport = {
			eventLayerXY: (function() {
				var
					event;

				if (document.createEvent === undefined) {
					event = new MouseEvent(null);
				} else {
					event = document.createEvent("MouseEvent");
				}

				return (event.layerX !== undefined);
			})(),
			pointerEvents: (document.createElement("div").style.pointerEvents !== undefined),
			customEvent: (function() {
				var
					supported = true;

				try {
					new CustomEvent("event", {});
				} catch (e) {
					supported = false;
				}

				return supported;
			})(),
			elementRemove: Element.prototype.remove !== undefined,
			flexbox: (document.createElement("div").style.flexBasis !== undefined),
			flexboxLegacy: (function() {
				var
					e = document.createElement("div");

				return (
					(e.style.boxDirection !== undefined) ||
					(e.style.webkitBoxDirection !== undefined)
				);
			})(),
			eventSource: ("EventSource" in window)
		},
		createEvent;

	// Add polyfills for unsupported features
	(function() {
		// CustomEvent
		if (featureSupport.customEvent) {
			createEvent = function(name, data) {
				return new CustomEvent(
					name, {
						detail: data
					}
				);
			};
		} else {
			createEvent = function(name, data) {
				var
					event = document.createEvent("CustomEvent");
				event.initCustomEvent(name, true, false, data);
				return event;
			};
		}

		// Element.prototype.remove
		if (!featureSupport.elementRemove) {
			Element.prototype.remove = function() {
				this.parentNode.removeChild(this);
			};
		}

		// Legacy flexbox
		if (
			!featureSupport.flexbox &&
			featureSupport.flexboxLegacy
		) {
			document.documentElement.classList.add("flexbox-legacy");
		}
	})();

	function createDOM(text) {
		var
			e = document.createElement("div");

		e.innerHTML = text;
		return e.childNodes;
	}

	function append(node, child) {
		if (child instanceof NodeList) {
			[].slice.call(child).forEach(function(e) {
				node.appendChild(e);
			});
		} else {
			node.appendChild(child);
		}
	}

	function ajax(params) {
		var
			p = params,
			type = typeof p.type !== "undefined" ? p.type : "GET",
			data = typeof p.data !== "undefined" ? p.data : "",
			headers = typeof p.headers !== "undefined" ? p.headers : {},
			request = new XMLHttpRequest();

		request.open(type, p.url, true);

		for (var h in headers) {
			request.setRequestHeader(h, headers[h]);
		}

		request.onload = function() {
			if (request.status < 200 || request.status > 400) {
				if (typeof p.error === "function") {
					p.error(request);
				}
				return;
			}
			if (typeof p.callback === "function") {
				p.callback(request);
			}
		};
		request.onerror = function() {
			if (typeof p.error === "function") {
				p.error(request);
			}
		};
		request.send(data);

		return request;
	}

	function renderTemplate(id, contents) {
		var
			text = document.getElementById(id).innerHTML;

		return text.replace(/\{([\w]+)\}/, function(match, capture) {
			//jshint unused:false
			if (typeof contents[capture] !== "undefined") {
				return contents[capture];
			} else {
				return "";
			}
		});
	}

	function HistoryStack() {
		var
			_t = this;

		_t.level = 0;

		_t.push = function(page) {
			_t.level++;
			history.pushState({page: page}, document.title.textContent);
		};

		_t.replace = function(page, url) {
			history.replaceState({page: page}, document.title.textContent, url);
		};

		window.addEventListener("popstate", function(e) {
			_t.level--;
			smarthome.UI.navigate(e.state.page, false);
		}, false);
	}

	function Modal(text) {
		var
			_t = this;

		_t.templateId = "template-modal";
		_t.text = renderTemplate(_t.templateId, {
			content: text
		});

		function onkeydown(event) {
			event = event || window.event;
			if (event.keyCode === 27) {
				_t.hide();
			}
		}

		function init() {
			document.addEventListener("keydown", onkeydown);
		}

		function destroy() {
			document.removeEventListener("keydown", onkeydown);
		}

		_t.show = function() {
			append(document.body, createDOM(_t.text));

			_t.bg = document.querySelector(o.modal);
			_t.container = _t.bg.querySelector(o.modalContainer);

			smarthome.UI.currentModal = _t;
			_t.bg.addEventListener("click", function() {
				_t.hide();
			});

			_t.container.addEventListener("click", function(event) {
				event.stopPropagation();
			});

			init();
		};

		_t.hide = function() {
			document.body.querySelector(o.modal).remove();
			delete smarthome.UI.currentModal;
			destroy();
		};
	}

	function DebounceProxy(callback, callInterval) {
		var
			_t = this,
			finished = false,
			args;

		_t.lock = false;
		_t.call = function() {
			args = arguments;
			if (!_t.lock) {
				finished = false;
				callback.apply(null, args);
				_t.lock = true;
				setTimeout(function() {
					if (!finished) {
						callback.apply(null, args);
					}
					_t.lock = false;
				}, callInterval);
			}
		};

		_t.finish = function() {
			finished = true;
		};
	}

	/* class Control */
	function Control(parentNode) {
		var
			_t = this,
			suppress = false;

		_t.parentNode = parentNode;
		_t.item = _t.parentNode.getAttribute(o.itemAttribute);
		_t.id = _t.parentNode.getAttribute(o.idAttribute);
		_t.icon = _t.parentNode.parentNode.querySelector(o.formIcon);

		if (_t.icon !== null) {
			_t.iconName = _t.icon.getAttribute(o.iconAttribute);
		}

		_t.reloadIcon = function(state) {
			// This condition should be always true, but who knows?
			if (_t.icon !== null) {
				_t.icon.setAttribute("src",
					"/icon/" +
					_t.iconName +
					"?state=" +
					state +
					"&format=" +
					smarthome.UI.iconType
				);
			}
		};

		_t.setValue = function(value) {
			_t.reloadIcon(value);
			if (suppress) {
				suppress = false;
			} else {
				_t.setValuePrivate(value);
			}
		};

		_t.setValuePrivate = function() {};

		_t.supressUpdate = function() {
			suppress = true;
		};
	}

	/* class ControlImage */
	function ControlImage(parentNode) {
		var
			_t = this;

		_t.image = parentNode.querySelector("img");
		_t.updateInterval = parseInt(parentNode.getAttribute("data-update-interval"), 10);

		if (_t.updateInterval === 0) {
			return;
		}

		_t.url = _t.image.getAttribute("src").replace(/\d+$/, "");

		var
			interval = setInterval(function() {
				if (_t.image.clientWidth === 0) {
					clearInterval(interval);
					return;
				}
				_t.image.setAttribute("src", _t.url + Math.random().toString().slice(2));
			}, _t.updateInterval * 1000);
	}

	/* class ControlText extends Control */
	function ControlText(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.setValue = function(value) {
			parentNode.innerHTML = value;
		};
	}

	/* class ControlSelection extends Control */
	function ControlSelection(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.count = _t.parentNode.getAttribute("data-count") * 1;
		_t.reset = function() {
			_t.buttons.forEach(function(button) {
				button.classList.remove(o.buttonActiveClass);
			});
		};

		_t.onclick = function(event) {
			var
				target = event.target,
				value = target.getAttribute("data-value") + "";

			if (_t.count !== 1) {
				_t.reset();
				target.classList.add(o.buttonActiveClass);
			}

			_t.parentNode.dispatchEvent(createEvent(
				"control-change", {
					item: _t.item,
					value: value
			}));
			_t.supressUpdate();
		};
		_t.valueMap = {};
		_t.buttons = [].slice.call(_t.parentNode.querySelectorAll(o.controlButton));
		_t.setValuePrivate = function(value) {
			if (_t.count === 1) {
				return;
			}

			_t.reset();
			if (
				(_t.valueMap !== undefined) &&
				(_t.valueMap[value] !== undefined)
			) {
				_t.valueMap[value].classList.add(o.buttonActiveClass);
			}
		};

		_t.buttons.forEach.call(_t.buttons, function(button) {
			var
				value = button.getAttribute("data-value") + "";

			_t.valueMap[value] = button;
			button.addEventListener("click", _t.onclick);
		});
	}

	/* class ControlRadio extends Control */
	function ControlRadio(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.value = null;

		function onRadioChange(event) {
			var
				value = event.target.getAttribute("value");

			_t.parentNode.dispatchEvent(createEvent("control-change", {
				item: _t.item,
				value: value
			}));

			setTimeout(function() {
				_t.modal.hide();
			}, 300);
		}

		_t.showModal = function() {
			var
				content = _t.parentNode.querySelector(o.selectionRows).innerHTML;

			_t.modal = new Modal(content);
			_t.modal.show();

			var
				controls = [].slice.call(_t.modal.container.querySelectorAll(o.formRadio));

			if (_t.value !== null) {
				var
					items = [].slice.call(_t.modal.container.querySelectorAll("input[type=radio]"));

				items.forEach(function(radioItem) {
					if (radioItem.value === _t.value) {
						radioItem.checked = true;
					} else {
						radioItem.checked = false;
					}
				});
			}

			controls.forEach(function(control) {
				componentHandler.upgradeElement(control, "MaterialRadio");
				control.addEventListener("change", onRadioChange);
			});
		};

		_t.setValuePrivate = function(value) {
			_t.value = "" + value;
		};

		_t.parentNode.parentNode.addEventListener("click", _t.showModal);
	}

	/* class ControlRollerblinds extends Control */
	function ControlRollerblinds(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this,
			longpressTimeout = 300,
			longPress,
			timeout;

		_t.buttonUp = _t.parentNode.querySelector(o.rollerblind.up);
		_t.buttonDown = _t.parentNode.querySelector(o.rollerblind.down);
		_t.buttonStop = _t.parentNode.querySelector(o.rollerblind.stop);

		function emitEvent(value) {
			_t.parentNode.dispatchEvent(createEvent(
				"control-change", {
					item: _t.item,
					value: value
			}));
		}

		function onMouseDown(command) {
			longPress = false;

			timeout = setTimeout(function() {
				longPress = true;
				emitEvent(command);
			}, longpressTimeout);
		}

		function onMouseUp(command) {
			clearTimeout(timeout);
			if (longPress) {
				emitEvent("STOP");
			} else {
				emitEvent(command);
			}
		}

		function onStop() {
			emitEvent("STOP");
		}

		var
			upButtonMouseUp = onMouseUp.bind(null, "UP"),
			upButtonMouseDown = onMouseDown.bind(null, "UP"),

			downButtonMouseUp = onMouseUp.bind(null, "DOWN"),
			downButtonMouseDown = onMouseDown.bind(null, "DOWN");

		// Up button
		_t.buttonUp.addEventListener("touchstart", upButtonMouseDown);
		_t.buttonUp.addEventListener("mousedown", upButtonMouseDown);

		_t.buttonUp.addEventListener("touchend", upButtonMouseUp);
		_t.buttonUp.addEventListener("mouseup", upButtonMouseUp);

		// Down button
		_t.buttonDown.addEventListener("touchstart", downButtonMouseDown);
		_t.buttonDown.addEventListener("mousedown", downButtonMouseDown);

		_t.buttonDown.addEventListener("touchend", downButtonMouseUp);
		_t.buttonDown.addEventListener("mouseup", downButtonMouseUp);

		// Stop button
		_t.buttonStop.addEventListener("mousedown", onStop);
		_t.buttonStop.addEventListener("touchstart", onStop);
	}

	/* class ControlSetpoint extends Control */
	function ControlSetpoint(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.up = _t.parentNode.querySelector(o.setpoint.up);
		_t.down = _t.parentNode.querySelector(o.setpoint.down);

		_t.value = _t.parentNode.getAttribute("data-value");
		_t.max = parseFloat(_t.parentNode.getAttribute("data-max"));
		_t.min = parseFloat(_t.parentNode.getAttribute("data-min"));
		_t.step = parseFloat(_t.parentNode.getAttribute("data-step"));

		_t.value = isNaN(parseFloat(_t.value)) ? 0 : parseFloat(_t.value);

		_t.setValuePrivate = function(value) {
			_t.value = value * 1;
		};

		function onMouseDown(up) {
			var
				value = _t.value + ((up === true) ? _t.step : -_t.step );

			value = value > _t.max ? _t.max : value;
			value = value < _t.min ? _t.min : value;

			_t.parentNode.dispatchEvent(createEvent(
				"control-change", {
					item: _t.item,
					value: value
			}));

			_t.value = value;
		}

		var
			increaseHandler = onMouseDown.bind(null, true),
			decreaseHandler = onMouseDown.bind(null, false);

		_t.up.addEventListener("mousedown", increaseHandler);
		_t.up.addEventListener("touchstart", increaseHandler);

		_t.down.addEventListener("mousedown", decreaseHandler);
		_t.down.addEventListener("touchstart", decreaseHandler);
	}
	/* class Colorpicker */
	function Colorpicker(parentNode, color, callback) {
		var
			_t = this;

		/* rgb2hsv and hsv2rgb are modified versions from http://axonflux.com/handy-rgb-to-hsl-and-rgb-to-hsv-color-model-c */
		function rgb2hsv(rgbColor) {
			var
				r = rgbColor.r,
				g = rgbColor.g,
				b = rgbColor.b;

			r = r / 255;
			g = g / 255;
			b = b / 255;

			var
				max = Math.max(r, g, b),
				min = Math.min(r, g, b);

			var
				h,
				s,
				v = max;

			var
				d = max - min;

			s = max === 0 ? 0 : d / max;

			if (max === min) {
				h = 0; // achromatic
			} else {
				switch (max) {
					case r:
						h = (g - b) / d + (g < b ? 6 : 0);
						break;
					case g:
						h = (b - r) / d + 2;
						break;
					case b:
						h = (r - g) / d + 4;
						break;
				}
				h /= 6;
			}

			return {
				h: h,
				s: s,
				v: v
			};
		}

		function hsv2hsl(hsvColor) {
			var
				hue = hsvColor.h,
				sat = hsvColor.s,
				val = hsvColor.v;

			var
				d = ((2 - sat) * val),
				c = d < 1 ? d : 2 - d;

			return {
				h: hue,
				s: c === 0 ? 0 : sat * val / c,
				l: d / 2
			};
		}

		_t.container = parentNode;
		_t.value = color;
		_t.hsvValue = rgb2hsv(color);
		_t.interval = null;

		_t.colorpicker = _t.container.querySelector(o.colorpicker.colorpicker);
		_t.image = _t.container.querySelector(o.colorpicker.image);
		_t.background = _t.container.querySelector(o.colorpicker.background);
		_t.handle = _t.container.querySelector(o.colorpicker.handle);
		_t.slider = _t.container.querySelector(o.colorpicker.slider);
		_t.button = _t.container.querySelector(o.controlButton);

		componentHandler.upgradeElement(_t.button, "MaterialButton");
		componentHandler.upgradeElement(_t.button, "MaterialRipple");

		function updateValue(event) {
			var
				pos;

			if (event.touches !== undefined) {
				pos = {
					x: event.touches[0].pageX - _t.colorpicker.offsetLeft,
					y: event.touches[0].pageY - _t.colorpicker.offsetTop
				};
			} else {
				if (featureSupport.eventLayerXY && featureSupport.pointerEvents) {
					pos = {
						x: event.layerX,
						y: event.layerY
					};
				} else {
					pos = {
						x: event.pageX - _t.colorpicker.offsetLeft,
						y: event.pageY - _t.colorpicker.offsetTop
					};
				}
			}
			var
				maxR = _t.image.clientWidth / 2,
				offsetX = pos.x - maxR,
				offsetY = pos.y - maxR,
				r = (offsetY * offsetY) + (offsetX * offsetX);

			if (r > (maxR * maxR)) {
				var
					ratio = 1 - Math.abs(maxR / Math.sqrt(r));

				pos.x -= (offsetX * ratio);
				pos.y -= (offsetY * ratio);
			}

			_t.handle.style.left = (pos.x / _t.image.clientWidth) * 100 + "%";
			_t.handle.style.top = (pos.y / _t.image.clientWidth) * 100 + "%";

			var
				angle = offsetX >= 0 ?
						(Math.PI * 2 - Math.atan(offsetY / offsetX) + Math.PI / 2) / (Math.PI * 2) :
						(Math.PI * 2 - Math.atan(offsetY / offsetX) - Math.PI / 2) / (Math.PI * 2),
				hsv = {
					h: isNaN(angle) ? 0 : angle,
					s: Math.sqrt(r) / maxR,
					v: 1
				},
				hsl = hsv2hsl(hsv);

			_t.hsvValue = {
				h: hsv.h,
				s: hsv.s,
				v: _t.slider.value / 100
			};

			hsl.l = hsl.l < 0.5 ? 0.5 : hsl.l;
			_t.background.style.background = "hsl(" + hsl.h * 360 + ", 100%, " + (Math.round(hsl.l * 100)) + "%)";
		}

		function setColor(c) {
			var
				hsv = rgb2hsv(c);

			_t.slider.value = hsv.v * 100;

			var
				x = 50 + Math.round(hsv.s * Math.cos(2 * Math.PI * hsv.h) * 50),
				y = 50 + Math.round(hsv.s * Math.sin(2 * Math.PI * hsv.h) * 50);

			_t.handle.style.top = x + "%";
			_t.handle.style.left = y + "%";

			hsv.v = 1;

			var
				correctedrgb = Colorpicker.hsv2rgb(hsv);

			_t.background.style.background =
				"rgb(" +
					Math.round(correctedrgb.r) + "," +
					Math.round(correctedrgb.g) + "," +
					Math.round(correctedrgb.b) + ")";
		}

		function onWindowMouseup() {
			if (_t.interval !== null) {
				clearInterval(_t.interval);
				_t.interval = null;
			}
			window.removeEventListener("mouseup", onWindowMouseup);
		}

		function onMouseDown(event) {
			_t.interval = setInterval(function() {
				callback(_t.hsvValue);
			}, 300);

			smarthome.changeListener.pause();
			window.addEventListener("mouseup", onWindowMouseup);

			updateValue(event);
			callback(_t.hsvValue);

			event.stopPropagation();
		}

		function onMove(event) {
			if (
				(event.touches === undefined) &&
				(!(event.buttons & 0x01))
			) {
				return;
			}

			updateValue(event);

			event.stopPropagation();
			event.preventDefault();
		}

		function onMouseUp(event) {
			if (_t.interval !== null) {
				clearInterval(_t.interval);
				_t.interval = null;
			}
			smarthome.changeListener.resume();
			window.removeEventListener("mouseup", onWindowMouseup);
			event.stopPropagation();
		}

		_t.debounceProxy = new DebounceProxy(function() {
			callback(_t.hsvValue);
		}, 200);

		// Some browsers fire onchange while the slider handle is being moved.
		// This is incorrect, according to the specs, but it's impossible to detect,
		// so DebounceProxy is used
		function onSliderChange() {
			_t.hsvValue.v = _t.slider.value / 100;
			smarthome.changeListener.pause();

			_t.debounceProxy.call();
		}

		function onSliderFinish() {
			_t.hsvValue.v = _t.slider.value / 100;

			_t.debounceProxy.call();
			_t.debounceProxy.finish();
			smarthome.changeListener.resume();
		}

		_t.slider.addEventListener("change", onSliderChange);

		_t.slider.addEventListener("touchend", onSliderFinish);
		_t.slider.addEventListener("mouseup", onSliderFinish);

		_t.image.addEventListener("mousedown", onMove);
		_t.image.addEventListener("mousemove", onMove);

		_t.image.addEventListener("touchmove", onMove);
		_t.image.addEventListener("touchstart", onMove);

		_t.image.addEventListener("touchend", onMouseUp);
		_t.image.addEventListener("mouseup", onMouseUp);

		_t.image.addEventListener("mousedown", onMouseDown);
		_t.image.addEventListener("touchstart", onMouseDown);

		setColor(color);
	}

	Colorpicker.hsv2rgb = function(hsvColor) {
		var
			h = hsvColor.h,
			s = hsvColor.s,
			v = hsvColor.v,
			r,
			g,
			b;

		var
			i = Math.floor(h * 6),
			f = h * 6 - i,
			p = v * (1 - s),
			q = v * (1 - f * s),
			t = v * (1 - (1 - f) * s);

		switch (i % 6) {
			case 0:
				r = v;
				g = t;
				b = p;
				break;
			case 1:
				r = q;
				g = v;
				b = p;
				break;
			case 2:
				r = p;
				g = v;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = v;
				break;
			case 4:
				r = t;
				g = p;
				b = v;
				break;
			case 5:
				r = v;
				g = p;
				b = q;
				break;
		}

		return {
			r: r * 255,
			g: g * 255,
			b: b * 255
		};
	};

	/* class ControlColorpicker extends Control */
	function ControlColorpicker(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this,
			repeatInterval = 300,
			interval;

		function hex2rgb(hex) {
			return {
				r: parseInt(hex.substr(1, 2), 16),
				g: parseInt(hex.substr(3, 2), 16),
				b: parseInt(hex.substr(5, 2), 16)
			};
		}

		_t.value = hex2rgb(_t.parentNode.getAttribute("data-value"));
		_t.buttonUp = _t.parentNode.querySelector(o.colorpicker.up);
		_t.buttonDown = _t.parentNode.querySelector(o.colorpicker.down);
		_t.buttonPick = _t.parentNode.querySelector(o.colorpicker.pick);

		_t.setValue = function(value) {
			var
				t = value.split(","),
				hsv = {
					h: t[0] / 360,
					s: t[1] / 100,
					v: t[2] / 100
				};
			_t.value = Colorpicker.hsv2rgb(hsv);
		};

		function emitEvent(value) {
			_t.parentNode.dispatchEvent(createEvent(
				"control-change", {
					item: _t.item,
					value: value
			}));
		}

		function onMouseDown(command) {
			interval = setInterval(function() {
				emitEvent(command);
			}, repeatInterval);
		}

		function onMouseUp() {
			clearInterval(interval);
		}

		function onPick() {
			_t.modal = new Modal(renderTemplate("template-colorpicker"));
			_t.modal.show();
			_t.modal.container.classList.add(o.colorpicker.modalClass);

			_t.modalControl = new Colorpicker(_t.modal.container, _t.value, function(color) {
				_t.value = Colorpicker.hsv2rgb(color);
				emitEvent(
					Math.round((color.h * 360) % 360) + "," +
					Math.round((color.s * 100) % 100) + "," +
					Math.round(color.v * 100)
				);
			});

			_t.modal.container.querySelector(o.colorpicker.button).addEventListener("click", function() {
				_t.modal.hide();
			});
		}

		var
			upButtonMouseDown = onMouseDown.bind(null, "INCREASE"),
			downButtonMouseDown = onMouseDown.bind(null, "DECREASE");

		// Up button
		_t.buttonUp.addEventListener("touchstart", upButtonMouseDown);
		_t.buttonUp.addEventListener("mousedown", upButtonMouseDown);

		_t.buttonUp.addEventListener("touchend", onMouseUp);
		_t.buttonUp.addEventListener("mouseup", onMouseUp);

		// Down button
		_t.buttonDown.addEventListener("touchstart", downButtonMouseDown);
		_t.buttonDown.addEventListener("mousedown", downButtonMouseDown);

		_t.buttonDown.addEventListener("touchend", onMouseUp);
		_t.buttonDown.addEventListener("mouseup", onMouseUp);

		// Stop button
		_t.buttonPick.addEventListener("click", onPick);
	}
	/* class ControlSwitch extends Control */
	function ControlSwitch(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.input = _t.parentNode.querySelector("input[type=checkbox]");
		_t.input.addEventListener("change", function() {
			_t.parentNode.dispatchEvent(createEvent("control-change", {
				item: _t.item,
				value: _t.input.checked ? "ON" : "OFF"
			}));
			_t.supressUpdate();
		});

		_t.setValuePrivate = function(v) {
			var
				value = v === "ON";

			_t.input.checked = value;

			if (value) {
				_t.parentNode.MaterialSwitch.on();
			} else {
				_t.parentNode.MaterialSwitch.off();
			}
		};
	}

	/* class ControlSlider extends Control */
	function ControlSlider(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.input = _t.parentNode.querySelector("input[type=range]");
		_t.locked = false;

		(function() {
			var
				value = parseInt(_t.input.getAttribute("data-state"), 10);

			if (isNaN(value)) {
				_t.input.value = 0;
			} else {
				_t.input.value = value;
			}

			if (_t.input.MaterialSlider) {
				_t.input.MaterialSlider.change();
			}
		})();

		function emitEvent() {
			_t.parentNode.dispatchEvent(createEvent("control-change", {
				item: _t.item,
				value: _t.input.value
			}));
			_t.supressUpdate();
		}

		_t.debounceProxy = new DebounceProxy(function() {
			emitEvent();
		}, 200);

		_t.input.addEventListener("change", function() {
			_t.debounceProxy.call();
		});

		_t.setValuePrivate = function(value) {
			if (_t.locked) {
				_t.reloadIcon(value);
				return;
			}
			_t.input.value = value;
			_t.input.MaterialSlider.change();
		};

		var
			unlockTimeout = null;

		function onChangeStart() {
			if (unlockTimeout !== null) {
				clearTimeout(unlockTimeout);
			}
			_t.locked = true;
			smarthome.changeListener.pause();
		}

		function onChangeEnd() {
			// mouseUp is fired earlier than the value is changed
			// quite a dirty hack, but I don't see any other way
			_t.debounceProxy.call();
			setTimeout(function() {
				smarthome.changeListener.resume();
			}, 5);
			unlockTimeout = setTimeout(function() {
				_t.locked = false;
			}, 300);
		}

		_t.input.addEventListener("touchstart", onChangeStart);
		_t.input.addEventListener("mousedown", onChangeStart);

		_t.input.addEventListener("touchend", onChangeEnd);
		_t.input.addEventListener("mouseup", onChangeEnd);
	}

	/* class ControlLink */
	function ControlLink(parentNode) {
		Control.call(this, parentNode);

		var
			_t = this;

		_t.target = parentNode.getAttribute("data-target");
		_t.container = parentNode.parentNode.querySelector(o.textLink.value);

		_t.setValuePrivate = function(value) {
			if (_t.container !== null) {
				_t.container.innerHTML = value;
			}
		};

		parentNode.parentNode.addEventListener("click", function() {
			smarthome.UI.navigate(_t.target, true);
		});
	}

	function controlChangeHandler(event) {
		ajax({
			type: "POST",
			url: "/rest/items/" + event.detail.item,
			data: event.detail.value,
			headers: {"Content-Type": "text/plain"}
		});
	}

		function UI(root) {
		/* const */
		var
			NavigationState = {
				Loading: 1,
				Idle: 2
			};

		var
			_t = this,
			state = NavigationState.Idle,
			historyStack = new HistoryStack();

		_t.page = document.body.getAttribute("data-page-id");
		_t.sitemap = document.body.getAttribute("data-sitemap");
		_t.destination = null;
		_t.root = root;
		_t.loading = _t.root.querySelector(o.uiLoadingBar);
		_t.layoutTitle = document.querySelector(o.layoutTitle);
		_t.iconType = document.body.getAttribute(o.iconTypeAttribute);

		function setTitle(title) {
			document.title = title;
			_t.layoutTitle.innerHTML = title;
		}

		function replaceContent(xmlResponse) {
			var
				page = xmlResponse.documentElement,
				nodeArray = [];

			if (page.tagName !== "page") {
				return;
			}

			[].forEach.call(page.childNodes, function(node) {
				if (!(node instanceof Text)) {
					nodeArray.push(node);
				}
			});

			setTitle(nodeArray[0].textContent);

			var
				contentElement = document.querySelector(".page-content");

			while (contentElement.firstChild) {
				contentElement.removeChild(contentElement.firstChild);
			}

			contentElement.insertAdjacentHTML("beforeend", nodeArray[1].textContent);
		}

		_t.upgradeComponents = function() {
			var
				upgrade = componentHandler.upgradeElement;

			[].slice.call(document.querySelectorAll(o.formControls)).forEach(function(e) {
				switch (e.getAttribute("data-control-type")) {
				case "setpoint":
				case "rollerblind":
				case "colorpicker":
				case "buttons":
					[].slice.call(e.querySelectorAll("button")).forEach(function(button) {
						upgrade(button, "MaterialButton");
						upgrade(button, "MaterialRipple");
					});
					break;
				case "checkbox":
					upgrade(e, "MaterialSwitch");
					break;
				case "slider":
					upgrade(e.querySelector("input[type=range]"), "MaterialSlider");
					break;
				default:
					break;
				}
			});
		};

		_t.showLoadingBar = function() {
			_t.loading.style.display = "block";
		};

		_t.hideLoadingBar = function() {
			_t.loading.style.display = "";
		};

		_t.navigateCallback = function(request) {
			state = NavigationState.Idle;

			if (_t.pushState) {
				historyStack.push(_t.page);
			}

			replaceContent(request.responseXML);

			if (_t.pushState) {
				historyStack.replace(_t.newPage, _t.destination);
			}

			_t.page = _t.newPage;
			_t.upgradeComponents();
			_t.initControls();

			_t.hideLoadingBar();

			if (_t.sitemap !== _t.page) {
				_t.header.classList.remove("navigation-home");
				_t.header.classList.add("navigation-page");
			} else {
				_t.header.classList.add("navigation-home");
				_t.header.classList.remove("navigation-page");
			}

			smarthome.changeListener.navigate(_t.page);
		};

		_t.navigate = function(page, pushState) {
			if (state !== NavigationState.Idle) {
				return;
			}

			state = NavigationState.Loading;
			_t.pushState =
				((pushState === undefined) ||
				(pushState === true));
			_t.newPage = page;

			_t.showLoadingBar();
			_t.destination = "/basicui/app?w=" + page + "&sitemap=" + smarthome.UI.sitemap;

			ajax({
				url: _t.destination + "&__async=true",
				callback: _t.navigateCallback
			});

			if (smarthome.UI.currentModal !== undefined) {
				smarthome.UI.currentModal.hide();
			}
		};

		_t.initControls = function() {
			smarthome.dataModel = {};

			function appendControl(control) {
				if (
					(smarthome.dataModel[control.item] === undefined) ||
					(smarthome.dataModel[control.item].widgets === undefined)
				) {
					smarthome.dataModel[control.item] = { widgets: [] };
				}

				smarthome.dataModel[control.item].widgets.push(control);
			}

			[].forEach.call(document.querySelectorAll(o.formControls), function(e) {
				/*eslint no-fallthrough:0*/
				switch (e.getAttribute("data-control-type")) {
				case "setpoint":
					appendControl(new ControlSetpoint(e));
					break;
				case "rollerblind":
					appendControl(new ControlRollerblinds(e));
					break;
				case "buttons":
					appendControl(new ControlSelection(e));
					break;
				case "selection":
					appendControl(new ControlRadio(e));
					break;
				case "checkbox":
					appendControl(new ControlSwitch(e));
					break;
				case "slider":
					appendControl(new ControlSlider(e));
					break;
				case "chart":
				case "image":
					new ControlImage(e);
					break;
				case "image-link":
					new ControlImage(e);
				case "text-link":
				case "group":
					appendControl(new ControlLink(e));
					break;
				case "text":
					appendControl(new ControlText(e));
					break;
				case "colorpicker":
					appendControl(new ControlColorpicker(e));
					break;
				default:
					break;
				}
				/*eslint no-fallthrough:0*/
				e.addEventListener("control-change", controlChangeHandler);
			});
		};

		historyStack.replace(_t.page, document.location.href);

		(function() {
			_t.header = document.querySelector(o.layoutHeader);
			if (_t.sitemap !== _t.page) {
				_t.header.classList.remove("navigation-home");
				_t.header.classList.add("navigation-page");
			}

			document.querySelector(o.backButton).addEventListener("click", function() {
				if (historyStack.level > 0) {
					history.back();
				} else {
					location.href = location.origin + "/basicui/app?sitemap=" + smarthome.UI.sitemap;
				}
			});
		})();
	}

	function AbstractChangeListener() {
		this.paused = false;

		this.pause = function() {
			this.paused = true;
		};

		this.resume = function() {
			this.paused = false;
		};
	}

	function ChangeListenerEventsource() {
		AbstractChangeListener.call(this);

		var
			_t = this;

		_t.navigate = function(){};
		_t.source = new EventSource("/rest/events?topics=smarthome/items/*/state");
		_t.source.addEventListener("message", function(payload) {
			if (_t.paused) {
				return;
			}

			var
				data = JSON.parse(payload.data),
				dataPayload = JSON.parse(data.payload),
				value = dataPayload.value,
				item = (function(topic) {
					topic = topic.split("/");
					return topic[topic.length - 2];
				})(data.topic);

			if (!(item in smarthome.dataModel)) {
				return;
			}

			smarthome.dataModel[item].widgets.forEach(function(widget) {
				widget.setValue(value);
			});
		});
	}

	function ChangeListenerLongpolling() {
		AbstractChangeListener.call(this);

		var
			_t = this;

		_t.sitemap = document.body.getAttribute("data-sitemap");
		_t.page = document.body.getAttribute("data-page-id");

		function applyChanges(response) {
			try {
				response = JSON.parse(response);
			} catch (e) {
				return;
			}

			function walkWidgets(widgets) {
				widgets.forEach(function(widget) {
					if (widget.item === undefined) {
						return;
					}

					var
						item = widget.item.name,
						value = widget.item.state;

					smarthome.dataModel[item].widgets.forEach(function(w) {
						if (value !== "NULL") {
							w.setValue(value);
						}
					});
				});
			}

			if (response.leaf) {
				walkWidgets(response.widgets);
			} else {
				response.widgets.forEach(function(frameWidget) {
					walkWidgets(frameWidget.widgets);
				});
			}
		}

		function start() {
			var
				cacheSupression = Math.random().toString(16).slice(2);

			_t.request = ajax({
				url: "/rest/sitemaps/" + _t.sitemap + "/" + _t.page + "?_=" + cacheSupression,
				headers: {"X-Atmosphere-Transport": "long-polling"},
				callback: function(request) {
					if (!_t.paused) {
						applyChanges(request.responseText);
					}
					setTimeout(function() {
						start();
					}, 1);
				},
				error: function() {
					// Wait 1s and restart long-polling
					setTimeout(function() {
						start();
					}, 1000);
				}
			});
		}

		_t.navigate = function(page) {
			_t.request.abort();
			_t.page = page;
			start();
		};

		_t.pause = function() {
			_t.request.abort();
			_t.paused = true;
		};

		_t.resume = function() {
			_t.paused = false;
			start();
		};

		start();
	}

	function ChangeListener() {
		if (featureSupport.eventSource) {
			ChangeListenerEventsource.call(this);
		} else {
			ChangeListenerLongpolling.call(this);
		}
	}

	document.addEventListener("DOMContentLoaded", function() {
		smarthome.UI = new UI(document);
		smarthome.UI.initControls();
		smarthome.changeListener = new ChangeListener();
	});
})({
	itemAttribute: "data-item",
	idAttribute: "data-id",
	iconAttribute: "data-icon",
	iconTypeAttribute: "data-icon-type",
	controlButton: "button",
	buttonActiveClass: "mdl-button--accent",
	modal: ".mdl-modal",
	modalContainer: ".mdl-modal__content",
	selectionRows: ".mdl-form__selection-rows",
	formControls: ".mdl-form__control",
	formRadio: ".mdl-radio",
	formIcon: ".mdl-form__icon img",
	uiLoadingBar: ".ui__loading",
	layoutTitle: ".mdl-layout-title",
	layoutHeader: ".mdl-layout__header",
	backButton: ".navigation__button-back",
	rollerblind: {
		up: ".mdl-form__rollerblind--up",
		down: ".mdl-form__rollerblind--down",
		stop: ".mdl-form__rollerblind--stop"
	},
	setpoint: {
		up: ".mdl-form__setpoint--up",
		down: ".mdl-form__setpoint--down"
	},
	colorpicker: {
		up: ".mdl-form__colorpicker--up",
		down: ".mdl-form__colorpicker--down",
		pick: ".mdl-form__colorpicker--pick",
		modalClass: "mdl-modal--colorpicker",
		image: ".colorpicker__image",
		handle: ".colorpicker__handle",
		slider: ".colorpicker__brightness",
		background: ".colorpicker__background",
		colorpicker: ".colorpicker",
		button: ".colorpicker__buttons > button"
	},
	textLink: {
		value: ".mdl-form__text-link-value"
	}
});

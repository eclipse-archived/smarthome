/*eslint-env browser */
/*eslint no-undef:2*/
/*eslint no-underscore-dangle:0*/

(function(o) {
	'use strict';

	var
		smarthome = {};
	
	function extend(b, a) {
		for (var c in a) {
			b[c] = a[c];
		}
	}
	
	function createDOM(text) {
		var 
			e = document.createElement('div');
		
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
			type = typeof p.type != "undefined" ? p.type : "GET",
			data = typeof p.data != "undefined" ? p.data : "",
			headers = typeof p.headers != "undefined" ? p.headers : {},
			request = new XMLHttpRequest();

		request.open(type, p.url, true);

		for (var h in headers) {
			request.setRequestHeader(h, headers[h]);
		}
		
		request.onload = function() {
			if (request.status < 200 || request.status > 400) {
				return;
			}
			if (typeof p.callback == "function") {
				p.callback(request);
			}
		};
		request.send(data);
	}
	
	function renderTemplate(id, contents) {
		var
			text = document.getElementById(id).innerHTML;
		
		return text.replace(/\{([\w]+)\}/, function(match, capture) {
			//jshint unused:false
			if (typeof contents[capture] != "undefined") {
				return contents[capture];
			} else {
				return '';
			}
		});
	}
	
	function HistoryStack(ui) {
		var
			_t = this,
			levelChangeCallback,
			stack = [];
		
		_t.push = function(page) {
			history.pushState({page: page}, document.title.textContent);
		}
		
		_t.replace = function(page, url) {
			history.replaceState({page: page}, document.title.textContent, url);
		}
		
		_t.onpop = function() {
			
		}
		
		window.addEventListener('popstate', function(e) {
			smarthome.UI.navigate(e.state.page, false);
		}, false);
	}

	function UI(root) {
		/* const */
		var 
			NavigationState = {
				Loading: 1,
				Idle:    2
			};
		
		var
			_t = this,
			state = NavigationState.Idle,
			historyStack = new HistoryStack();

		_t.page = document.body.getAttribute("data-page-id");
		_t.destination = null;
		_t.root = root;
		_t.loading = _t.root.querySelector(o.uiLoadingBar);
		_t.layoutTitle = document.querySelector(o.layoutTitle);
		
		function setTitle(title) {
			document.title = title;
			_t.layoutTitle.innerHTML = title;
		}
		
		function replaceContent(xmlResponse) {
			console.log(xmlResponse);
			
			var
				page = xmlResponse.documentElement;

			if (page.tagName !== "page") {
				console.error("Unexcepted response received");
				return;
			}

			setTitle(page.children[0].innerHTML);

			var
				contentElement = document.querySelector(".page-content");

			while (contentElement.firstChild) {
				contentElement.removeChild(contentElement.firstChild);
			}
			
			contentElement.insertAdjacentHTML("beforeend", page.children[1].textContent);
		}
		
		_t.upgradeComponents = function() {
			var
				upgrade = componentHandler.upgradeElement;
						
			[].forEach.call(document.querySelectorAll(o.formControls), function(e) {
				switch (e.getAttribute("data-control-type")) {
				case "setpoint":
				case "rollerblind":
				case "colorpicker":
				case "selection":
					[].forEach.call(e.querySelectorAll("button"), function(button) {
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
		}
				
		_t.showLoadingBar = function() {
			_t.loading.style.display = 'block';
		};

		_t.hideLoadingBar = function() {
			_t.loading.style.display = '';
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
		};
		
		_t.navigate = function(page, pushState) {
			if (state != NavigationState.Idle) {
				return;
			}

			state = NavigationState.Loading;
			_t.pushState = 
				((pushState === undefined) ||
				 (pushState == true));
			_t.newPage = page;
			
			_t.showLoadingBar();
			_t.destination = "/classicui/app?w=" + page;

			ajax({
				url: _t.destination + "&__async=true",
				callback: _t.navigateCallback
			});

			if (smarthome.UI.currentModal !== undefined) {
				smarthome.UI.currentModal.hide();
			}
		};
		
		_t.initControls = function() {
			[].forEach.call(document.querySelectorAll(o.formControls), function(e) {
				switch (e.getAttribute("data-control-type")) {
				case "setpoint":
					new ControlSetpoint(e);
					break;
				case "rollerblind":
					new ControlRollerblinds(e);
					break;
				case "buttons":
					new ControlSelection(e);
					break;
				case "selection":
					new ControlRadio(e);
					break;
				case "checkbox":
					new ControlSwitch(e);
					break;
				case "slider":
					new ControlSlider(e);
					break;
				case "text-link":
				case "group":
					new ControlLink(e);
					break;
				case "colorpicker":
					new ControlColorpicker(e);
					break;
				default:
					break;
				}
				e.addEventListener("control-change", controlChangeHandler);
			});
		};
		
		historyStack.replace(_t.page, document.location.href);
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
			if (event.keyCode == 27) {
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

			_t.bg = document.querySelector(o.modal),
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
	
	function DebounceProxy(callback, timeout) {
		var
			_t = this;
		
		function clear() {
			if (_t.timeout !== undefined) {
				clearTimeout(_t.timeout);
			}
		}
		
		_t.call = function() {
			clear();
			_t.timeout = setTimeout(callback, timeout);
		};
		
		_t.finish = function() {
			clear();
			callback();
		};
	}
	
	/* class Control */
	function Control(parentNode) {
		var
			_t = this;
		
		_t.parentNode = parentNode;
		_t.item = _t.parentNode.getAttribute(o.itemAttribute);
	}

	/* class ControlSelection extends Control */
	function ControlSelection(parentNode) {
		extend(this, new Control(parentNode));
		
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
				value = target.getAttribute("data-value") + '';

			if (_t.count != 1) {
				_t.reset();
				target.classList.add(o.buttonActiveClass);
			}

			_t.parentNode.dispatchEvent(new CustomEvent(
				"control-change", {
				detail: {
					item: _t.item,
					value: value
				}
			}));
		};
		_t.valueMap = {};
		_t.buttons = [].slice.call(_t.parentNode.querySelectorAll(o.controlButton));
		_t.setValue = function(value) {
			_t.reset();
			if (_t.valueMap !== undefined) {
				_t.valueMap[value].classList.add(o.buttonActiveClass);
			}
		};
		
		_t.buttons.forEach.call(_t.buttons, function(button) {
			var
				value = button.getAttribute("data-value") + '';

			_t.valueMap[value] = button;
			button.addEventListener("click", _t.onclick);
		});
	}

	/* class ControlRadio extends Control */
	function ControlRadio(parentNode) {
		extend(this, new Control(parentNode));
		
		var
			_t = this;
		
		function onRadioChange(event) {
			var
				value = event.target.getAttribute("value");
			
			_t.parentNode.dispatchEvent(new CustomEvent("control-change", {
				detail: {
					item: _t.item,
					value: value
				}
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

			controls.forEach(function(control) {
				componentHandler.upgradeElement(control, "MaterialRadio");
				control.addEventListener("change", onRadioChange);
			});
		};
		
		_t.parentNode.parentNode.addEventListener("click", _t.showModal);
	}
	
	/* class ControlRollerblinds extends Control */
	function ControlRollerblinds(parentNode) {
		extend(this, new Control(parentNode));
		
		var
			_t = this,
			longpressTimeout = 300,
			longPress,
			timeout;

		_t.buttonUp = _t.parentNode.querySelector(o.rollerblind.up);
		_t.buttonDown = _t.parentNode.querySelector(o.rollerblind.down);
		_t.buttonStop = _t.parentNode.querySelector(o.rollerblind.stop);

		function emitEvent(value) {
			_t.parentNode.dispatchEvent(new CustomEvent(
				"control-change", {
				detail: {
					item: _t.item,
					value: value
				}
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
		extend(this, new Control(parentNode));

		var
			_t = this;

		_t.up = _t.parentNode.querySelector(o.setpoint.up);
		_t.down = _t.parentNode.querySelector(o.setpoint.down);

		_t.value = _t.parentNode.getAttribute("data-value");
		_t.max = parseFloat(_t.parentNode.getAttribute("data-max"));
		_t.min = parseFloat(_t.parentNode.getAttribute("data-min"));
		_t.step = parseFloat(_t.parentNode.getAttribute("data-step"));

		_t.value = isNaN(parseFloat(_t.value)) ? 0 : parseFloat(_t.value);

		function onMouseDown(up) {
			var
				value = _t.value + ((up === true) ? _t.step : -_t.step );

			value = value > _t.max ? _t.max : value;
			value = value < _t.min ? _t.min : value;

			_t.parentNode.dispatchEvent(new CustomEvent(
				"control-change", {
				detail: {
					item: _t.item,
					value: value
				}
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
	/* class ControlColorpicker extends Control */
	function ControlColorpicker(parentNode) {
		extend(this, new Control(parentNode));

		var
			_t = this,
			repeatInterval = 300,
			interval;

		(function(hex) {
			_t.value = {
				r: parseInt(hex.substr(0, 2), 16),
				g: parseInt(hex.substr(2, 2), 16),
				b: parseInt(hex.substr(4, 2), 16)
			};
		})(_t.parentNode.getAttribute("data-value"));

		_t.buttonUp = _t.parentNode.querySelector(o.colorpicker.up);
		_t.buttonDown = _t.parentNode.querySelector(o.colorpicker.down);
		_t.buttonPick = _t.parentNode.querySelector(o.colorpicker.pick);

		/* rgb2hsv and hsv2rgb are modified versions from http://axonflux.com/handy-rgb-to-hsl-and-rgb-to-hsv-color-model-c */
		function rgb2hsl(rgbColor) {
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

		function hsv2rgb(hsvColor) {
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
		}

		function emitEvent(value) {
			_t.parentNode.dispatchEvent(new CustomEvent(
				"control-change", {
				detail: {
					item: _t.item,
					value: value
				}
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

			var
				button = _t.modal.container.querySelector(o.controlButton);
			componentHandler.upgradeElement(button, "MaterialButton");
			componentHandler.upgradeElement(button, "MaterialRipple");
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
		extend(this, new Control(parentNode));

		var
			_t = this;
		
		_t.input = _t.parentNode.querySelector("input[type=checkbox]");
		_t.input.addEventListener("change", function() {
			_t.parentNode.dispatchEvent(new CustomEvent("control-change", {
				detail: {
					item: _t.item,
					value: _t.input.checked ? "ON" : "OFF"
				}
			}));
		});
	}
	
	/* class ControlSlider extends Control */
	function ControlSlider(parentNode) {
		extend(this, new Control(parentNode));

		var
			_t = this;
		
		_t.input = _t.parentNode.querySelector("input[type=range]");
		
		_t.input.addEventListener("change", function() {
			_t.parentNode.dispatchEvent(new CustomEvent("control-change", {
				detail: {
					item: _t.item,
					value: _t.input.value
				}
			}));
		});
	}
	
	/* class ControlLink */
	function ControlLink(parentNode) {
		var
			_t = this;
		
		_t.target = parentNode.getAttribute("data-target");
		
		parentNode.parentNode.addEventListener("click", function() {
			smarthome.UI.navigate(_t.target, true);
		});
	}

	function controlChangeHandler(event) {
		console.log(event.detail);
		
		ajax({
			type: "POST",
			url: "/rest/items/" + event.detail.item,
			data: event.detail.value,
			headers: {"Content-Type": "text/plain"}
		});
	}
	
	document.addEventListener("DOMContentLoaded", function() {
		smarthome.UI = new UI(document);
		smarthome.UI.initControls();
	});
})({
	itemAttribute: "data-item",
	controlButton: "button",
	buttonActiveClass: "mdl-button--accent",
	modal: ".mdl-modal",
	modalContainer: ".mdl-modal__content",
	selectionRows: ".mdl-form__selection-rows",
	formControls: ".mdl-form__control",
	formRadio: ".mdl-radio",
	uiLoadingBar: ".ui__loading",
	layoutTitle: ".mdl-layout-title",
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
		modalClass: "mdl-modal--colorpicker"
	}
});

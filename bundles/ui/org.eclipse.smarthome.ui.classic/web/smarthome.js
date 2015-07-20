/* jshint unused:strict */
/* jshint strict: true */

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
	
	function HistoryStack() {
		var
			_t = this,
			levelChangeCallback,
			stack = [];
		
		_t.push = function() {
			
		}
		
		_t.pop = function() {
			
		}
		
		// TODO: bind document navigation event
		// TODO: emit an event when stack level is changed
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
			state = NavigationState.Idle;

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

			[].forEach.call(contentElement.children, function(e) {
				e.remove();
			});
			
			contentElement.insertAdjacentHTML("beforeend", page.children[1].textContent);
		}
		
		_t.upgradeComponents = function() {
			var
				upgrade = componentHandler.upgradeElement;
						
			[].forEach.call(document.querySelectorAll(o.formControls), function(e) {
				switch (e.getAttribute("data-control-type")) {
				case "setpoint":
				case "rollershutter":
				case "colorpicker":
				case "selection":
					[].forEach.call(e.querySelectorAll("button"), function(button) {
						upgrade(button, "MaterialButton");
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
			
			replaceContent(request.responseXML);
			_t.upgradeComponents();
			_t.initControls();

			_t.hideLoadingBar();
		};
		
		_t.navigate = function(page) {
			if (state != NavigationState.Idle) {
				return;
			}
			
			state = NavigationState.Loading;
			_t.showLoadingBar();
			
			ajax({
				url: "/classicui/app?w=" + page + "&__async=true",
				callback: _t.navigateCallback
			});
		};
		
		_t.initControls = function() {
			[].forEach.call(document.querySelectorAll(o.formControls), function(e) {
				switch (e.getAttribute("data-control-type")) {
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
				default:
					break;
				}
				e.addEventListener("control-change", controlChangeHandler);
			});
		};
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

			var
				modalBg = document.querySelector(o.modal),
				modalContainer = modalBg.querySelector(o.modalContainer);

			modalBg.addEventListener("click", function() {
				_t.hide();
			});

			modalContainer.addEventListener("click", function(event) {
				event.stopPropagation();
			});

			init();
		};

		_t.hide = function() {
			document.body.querySelector(o.modal).remove();
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
				controls = [].slice.call(modalBg.querySelectorAll(o.formRadio));

			controls.forEach(function(control) {
				componentHandler.upgradeElement(control, "MaterialRadio");
				control.addEventListener("change", onRadioChange);
			});
		};
		
		_t.parentNode.parentNode.addEventListener("click", _t.showModal);
	}
	
	/* class ControlColorpicker extends Control */
	function ControlColorpicker(parentNode) {
		extend(this, new Control(parentNode));
		
		var
			_t = this;
				
		_t.showModal = function() {
			var
				content = "";

			_t.modal = new Modal(content);
			_t.modal.show();
		};

		_t.parentNode.addEventListener("click", _t.showModal);
	}
	
	/* class ControlRollerblinds extends Control */
	function ControlRollerblinds(parentNode) {
		extend(this, new Control(parentNode));
		
		var
			_t = this;
				
	}
	
	/* class ControlSetpoint extends Control */
	function ControlSetpoint(parentNode) {
		extend(this, new Control(parentNode));

		var
			_t = this;
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
			smarthome.UI.navigate(_t.target);
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
	layoutTitle: ".mdl-layout-title"
});

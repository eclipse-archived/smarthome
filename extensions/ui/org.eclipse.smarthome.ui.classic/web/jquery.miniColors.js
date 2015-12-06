/*
 * jQuery MiniColors: A tiny color picker built on jQuery
 *
 * Copyright Cory LaViska for A Beautiful Site, LLC. (http://www.abeautifulsite.net/)
 *
 * Dual-licensed under the MIT and GPL Version 2 licenses
 *
*/
if(jQuery) (function($) {
	
	// The minicolors object (public methods and settings)
	$.minicolors = {
		
		// Default settings
		settings: {
			defaultSlider: 'hue',
			letterCase: 'lowercase',
			hideSpeed: 100,
			showSpeed: 100,
			animationSpeed: 100,
			animationEasing: 'swing'
		},
		
		// Initialized all controls of type=minicolors
		init: function() {
			$('INPUT[type=minicolors]').each( function() {
				init( $(this) );
			});
		},
		
		// Refresh the controls
		refresh: function(input) {
			input = $(input);
			if( input.length > 0 ) {
				updateFromInput(input);
			} else {
				$('INPUT[type=minicolors]').each( function() {
					updateFromInput($(this));
				});
			}
		},
		
		// Shows the specified control
		show: function(input) {
			show( $(input).eq(0) );
		},
		
		// Hides all controls
		hide: function() {
			hide();
		},
		
		// Utility to convert a hex string to RGB(A) object
		rgbObject: function(input) {
			var hex = parseHex($(input).val(), true),
				rgb = hex2rgb(hex),
				opacity = input.attr('data-opacity');
			if( !rgb ) return null;
			if( opacity !== undefined ) $.extend(rgb, { a: parseFloat(opacity) });
			return rgb;
		},
		
		// Utility to convert a hex string to an RGB(A) string
		rgbString: function(input) {
			var hex = parseHex($(input).val(), true),
				rgb = hex2rgb(hex),
				opacity = input.attr('data-opacity');
			if( !rgb ) return null;
			if( opacity === undefined ) {
				return 'rgb(' + rgb.r + ', ' + rgb.g + ', ' + rgb.b + ')';
			} else {
				return 'rgba(' + rgb.r + ', ' + rgb.g + ', ' + rgb.b + ', ' + parseFloat(opacity) + ')';
			}
		},

		// ADDED FOR USE IN ECLIPSE SMARTHOME!
		// Utility to convert a hex string to an HSB string
		hsbString: function(input) {
			var hex = parseHex($(input).val(), true),
				hsb = hex2hsb(hex);
			if( !hsb ) return null;
			return hsb.h + ',' + hsb.s + ',' + hsb.b;
		}
		
	};
	
	// Initialize all input[type=minicolors] elements
	function init(input) {
		
		var minicolors = $('<span class="minicolors" />'),
			sliderType = input.attr('data-slider') || $.minicolors.settings.defaultSlider;
		
		if( input.data('initialized') ) return;
		
		// The wrapper
		minicolors
			.attr('class', input.attr('data-class'))
			.attr('style', input.attr('data-style'))
			.toggleClass('minicolors-swatch-left', input.attr('data-swatch-position') === 'left' )
			.toggleClass('minicolors-with-opacity', input.attr('data-opacity') !== undefined );
		
		// Custom positioning
		if( input.attr('data-position') !== undefined ) {
			$.each(input.attr('data-position').split(' '), function() {
				minicolors.addClass('minicolors-position-' + this);
			});
		}
		
		// The input
		input
			.data('initialized', true)
			.attr('data-default', input.attr('data-default') || '')
			.attr('data-slider', sliderType)
			.prop('size', 7)
			.prop('maxlength', 7)
			.wrap(minicolors)
			.after(
				'<span class="minicolors-panel minicolors-slider-' + sliderType + '">' + 
					'<span class="minicolors-slider">' + 
						'<span class="minicolors-picker"></span>' +
					'</span>' + 
					'<span class="minicolors-opacity-slider">' + 
						'<span class="minicolors-picker"></span>' +
					'</span>' +
					'<span class="minicolors-grid">' +
						'<span class="minicolors-grid-inner"></span>' +
						'<span class="minicolors-picker"><span></span></span>' +
					'</span>' +
				'</span>'
			);
		
		// Prevent text selection in IE
		input.parent().find('.minicolors-panel').on('selectstart', function() { return false; }).end();
		
		// Detect swatch position
		if( input.attr('data-swatch-position') === 'left' ) {
			// Left
			input.before('<span class="minicolors-swatch"><span></span></span>');
		} else {
			// Right
			input.after('<span class="minicolors-swatch"><span></span></span>');
		}
		
		// Disable textfield
		if( input.attr('data-textfield') === 'false' ) input.addClass('minicolors-hidden');
		
		// Inline controls
		if( input.attr('data-control') === 'inline' ) input.parent().addClass('minicolors-inline');
		
		updateFromInput(input);
		
	}
	
	// Shows the specified dropdown panel
	function show(input) {
		
		var minicolors = input.parent(),
			panel = minicolors.find('.minicolors-panel');
		
		// Do nothing if uninitialized, disabled, or already open
		if( !input.data('initialized') || input.prop('disabled') || minicolors.hasClass('minicolors-focus') ) return;
		
		hide();
		
		minicolors.addClass('minicolors-focus');
		panel
			.stop(true, true)
			.fadeIn($.minicolors.settings.showSpeed);
		
		return input;
		
	}
	
	// Hides all dropdown panels
	function hide() {
		
		$('.minicolors:not(.minicolors-inline)').each( function() {
			
			var minicolors = $(this),
				input = minicolors.find('INPUT');
			
			minicolors.find('.minicolors-panel').fadeOut($.minicolors.settings.hideSpeed, function() {
				minicolors.removeClass('minicolors-focus');
			});
			
		});
		
	}
	
	// Moves the selected picker
	function move(target, event, animate) {
		
		var input = target.parents('.minicolors').find('INPUT'),
			picker = target.find('[class$=-picker]'),
			offsetX = target.offset().left,
			offsetY = target.offset().top,
			x = Math.round(event.pageX - offsetX),
			y = Math.round(event.pageY - offsetY),
			duration = animate ? $.minicolors.settings.animationSpeed : 0,
			wx, wy, r, phi;
			
		
		// Touch support
		if( event.originalEvent.changedTouches ) {
			x = event.originalEvent.changedTouches[0].pageX - offsetX;
			y = event.originalEvent.changedTouches[0].pageY - offsetY;
		}
		
		// Constrain picker to its container
		if( x < 0 ) x = 0;
		if( y < 0 ) y = 0;
		if( x > target.width() ) x = target.width();
		if( y > target.height() ) y = target.height();
		
		// Constrain color wheel values to the wheel
		if( target.parent().is('.minicolors-slider-wheel') && picker.parent().is('.minicolors-grid') ) {
			wx = 120 - x;
			wy = 120 - y;
			r = Math.sqrt(wx * wx + wy * wy);
			phi = Math.atan2(wy, wx);
			if( phi < 0 ) phi += Math.PI * 2;
			if( r > 120 ) {
				r = 120;
				x = 120 - (120 * Math.cos(phi));
				y = 120 - (120 * Math.sin(phi));
			}
			x = Math.round(x);
			y = Math.round(y);
		}
		
		// Move the picker
		if( target.is('.minicolors-grid') ) {
			picker
				.stop(true)
				.animate({
					top: y + 'px',
					left: x + 'px'
				}, duration, $.minicolors.settings.animationEasing, function() {
					updateFromControl(input);
				});
		} else {
			picker
				.stop(true)
				.animate({
					top: y + 'px'
				}, duration, $.minicolors.settings.animationEasing, function() {
					updateFromControl(input);
				});
		}
		
	}
	
	// Sets the input based on the color picker values
	function updateFromControl(input) {
		
		function getCoords(picker, container) {
			
			var left, top;
			if( !picker.length || !container ) return null;
			left = picker.offset().left;
			top = picker.offset().top;
			
			return {
				x: left - container.offset().left + (picker.outerWidth() / 2),
				y: top - container.offset().top + (picker.outerHeight() / 2)
			};
			
		}
		
		var hue, saturation, brightness, opacity, rgb, hex, x, y, r, phi,
			
			// Helpful references
			minicolors = input.parent(),
			panel = minicolors.find('.minicolors-panel'),
			swatch = minicolors.find('.minicolors-swatch'),
			hasOpacity = input.attr('data-opacity') !== undefined,
			sliderType = input.attr('data-slider'),
			
			// Panel objects
			grid = minicolors.find('.minicolors-grid'),
			slider = minicolors.find('.minicolors-slider'),
			opacitySlider = minicolors.find('.minicolors-opacity-slider'),
			
			// Picker objects
			gridPicker = grid.find('[class$=-picker]'),
			sliderPicker = slider.find('[class$=-picker]'),
			opacityPicker = opacitySlider.find('[class$=-picker]'),
			
			// Picker positions
			gridPos = getCoords(gridPicker, grid),
			sliderPos = getCoords(sliderPicker, slider),
			opacityPos = getCoords(opacityPicker, opacitySlider);
		
		// Determine HSB values
		switch(sliderType) {
			
			case 'wheel':
				// Calculate hue, saturation, and brightness
				x = (grid.width() / 2) - gridPos.x;
				y = (grid.height() / 2) - gridPos.y;
				r = Math.sqrt(x * x + y * y);
				phi = Math.atan2(y, x);
				if( phi < 0 ) phi += Math.PI * 2;
				if( r > 120 ) {
					r = 120;
					gridPos.x = 111 - (120 * Math.cos(phi));
					gridPos.y = 111 - (120 * Math.sin(phi));
				}
				saturation = keepWithin(r / 1.2, 0, 100);
				hue = keepWithin(phi * 180 / Math.PI, 0, 360);
				brightness = keepWithin(100 - Math.floor(sliderPos.y * (100 / slider.height())), 0, 100);
				hex = hsb2hex({
					h: hue,
					s: saturation,
					b: brightness
				});
				
				// Update UI
				slider.css('backgroundColor', hsb2hex({ h: hue, s: saturation, b: 100 }));
				break;
			
			case 'saturation':
				// Calculate hue, saturation, and brightness
				hue = keepWithin(parseInt(gridPos.x * (360 / grid.width())), 0, 360);
				saturation = keepWithin(100 - Math.floor(sliderPos.y * (100 / slider.height())), 0, 100);
				brightness = keepWithin(100 - Math.floor(gridPos.y * (100 / grid.height())), 0, 100);
				hex = hsb2hex({
					h: hue,
					s: saturation,
					b: brightness
				});
				
				// Update UI
				slider.css('backgroundColor', hsb2hex({ h: hue, s: 100, b: brightness }));
				minicolors.find('.minicolors-grid-inner').css('opacity', saturation / 100);
				break;
			
			case 'brightness':
				// Calculate hue, saturation, and brightness
				hue = keepWithin(parseInt(gridPos.x * (360 / grid.width())), 0, 360);
				saturation = keepWithin(100 - Math.floor(gridPos.y * (100 / grid.height())), 0, 100);
				brightness = keepWithin(100 - Math.floor(sliderPos.y * (100 / slider.height())), 0, 100);
				hex = hsb2hex({
					h: hue,
					s: saturation,
					b: brightness
				});
				
				// Update UI
				slider.css('backgroundColor', hsb2hex({ h: hue, s: saturation, b: 100 }));
				minicolors.find('.minicolors-grid-inner').css('opacity', 1 - (brightness / 100));
				break;
			
			default:
				// Calculate hue, saturation, and brightness
				hue = keepWithin(360 - parseInt(sliderPos.y * (360 / slider.height())), 0, 360);
				saturation = keepWithin(Math.floor(gridPos.x * (100 / grid.width())), 0, 100);
				brightness = keepWithin(100 - Math.floor(gridPos.y * (100 / grid.height())), 0, 100);
				hex = hsb2hex({
					h: hue,
					s: saturation,
					b: brightness
				});
				
				// Update UI
				grid.css('backgroundColor', hsb2hex({ h: hue, s: 100, b: 100 }));
				break;
			
		}
		
		// Determine opacity
		if( hasOpacity ) {
			opacity = parseFloat(1 - (opacityPos.y / opacitySlider.height())).toFixed(2);
		} else {
			opacity = 1;
		}
		
		// Update input control
		input.val(hex);
		if( hasOpacity ) input.attr('data-opacity', opacity);
		
		// Set swatch color
		swatch.find('SPAN').css({
			backgroundColor: hex,
			opacity: opacity
		});
		
		// Fire change event
		if( hex + opacity !== input.data('last-change') ) {
			input
				.data('last-change', hex + opacity)
				.trigger('change', input);
		}
		
	}
	
	// Sets the color picker values from the input
	function updateFromInput(input, preserveInputValue) {
		
		var hex,
			hsb,
			opacity,
			x, y, r, phi,
			
			// Helpful references
			minicolors = input.parent(),
			swatch = minicolors.find('.minicolors-swatch'),
			hasOpacity = input.attr('data-opacity') !== undefined,
			sliderType = input.attr('data-slider'),
			
			
			// Panel objects
			grid = minicolors.find('.minicolors-grid'),
			slider = minicolors.find('.minicolors-slider'),
			opacitySlider = minicolors.find('.minicolors-opacity-slider'),
			
			// Picker objects
			gridPicker = grid.find('[class$=-picker]'),
			sliderPicker = slider.find('[class$=-picker]'),
			opacityPicker = opacitySlider.find('[class$=-picker]');
		
		// Determine hex/HSB values
		hex = convertCase(parseHex(input.val(), true));
		if( !hex ) hex = convertCase(parseHex(input.attr('data-default'), true));
		hsb = hex2hsb(hex);
		
		// Update input value
		if( !preserveInputValue ) input.val(hex);
		
		// Determine opacity value
		if( hasOpacity ) {
			opacity = input.attr('data-opacity') === '' ? 1 : keepWithin(parseFloat(input.attr('data-opacity')).toFixed(2), 0, 1);
			input.attr('data-opacity', opacity);
			swatch.find('SPAN').css('opacity', opacity);
			
			// Set opacity picker position
			y = keepWithin(opacitySlider.height() - (opacitySlider.height() * opacity), 0, opacitySlider.height());
			opacityPicker.css('top', y + 'px');
		}
		
		// Update swatch
		swatch.find('SPAN').css('backgroundColor', hex);
		
		// Determine picker locations
		switch(sliderType) {
			
			case 'wheel':
				// Set grid position
				r = keepWithin(Math.ceil(hsb.s * 1.2), 0, grid.height() / 2);
				phi = hsb.h * Math.PI / 180;
				x = keepWithin(120 - Math.cos(phi) * r, 0, grid.width());
				y = keepWithin(120 - Math.sin(phi) * r, 0, grid.height());
				gridPicker.css({
					top: y + 'px',
					left: x + 'px'
				});
				
				// Set slider position
				y = 240 - (hsb.b / (100 / grid.height()));
				if( hex === '' ) y = 0;
				sliderPicker.css('top', y + 'px');
				
				// Update panel color
				slider.css('backgroundColor', hsb2hex({ h: hsb.h, s: hsb.s, b: 100 }));
				break;
			
			case 'saturation':
				// Set grid position
				x = keepWithin((5 * hsb.h) / 12, 0, 120);
				y = keepWithin(grid.height() - Math.ceil(hsb.b / (100 / grid.height())), 0, grid.height());
				gridPicker.css({
					top: y + 'px',
					left: x + 'px'
				});				
				
				// Set slider position
				y = keepWithin(slider.height() - (hsb.s * (slider.height() / 100)), 0, slider.height());
				sliderPicker.css('top', y + 'px');
				
				// Update UI
				slider.css('backgroundColor', hsb2hex({ h: hsb.h, s: 100, b: hsb.b }));
				minicolors.find('.minicolors-grid-inner').css('opacity', hsb.s / 100);
				
				break;
			
			case 'brightness':
				// Set grid position
				x = keepWithin((5 * hsb.h) / 12, 0, 120);
				y = keepWithin(grid.height() - Math.ceil(hsb.s / (100 / grid.height())), 0, grid.height());
				gridPicker.css({
					top: y + 'px',
					left: x + 'px'
				});				
				
				// Set slider position
				y = keepWithin(slider.height() - (hsb.b * (slider.height() / 100)), 0, slider.height());
				sliderPicker.css('top', y + 'px');
				
				// Update UI
				slider.css('backgroundColor', hsb2hex({ h: hsb.h, s: hsb.s, b: 100 }));
				minicolors.find('.minicolors-grid-inner').css('opacity', 1 - (hsb.b / 100));
				break;
			
			default:
				// Set grid position
				x = keepWithin(Math.ceil(hsb.s / (100 / grid.width())), 0, grid.width());
				y = keepWithin(grid.height() - Math.ceil(hsb.b / (100 / grid.height())), 0, grid.height());
				gridPicker.css({
					top: y + 'px',
					left: x + 'px'
				});
				
				// Set slider position
				y = keepWithin(slider.height() - (hsb.h / (360 / slider.height())), 0, slider.height());
				sliderPicker.css('top', y + 'px');
				
				// Update panel color
				grid.css('backgroundColor', hsb2hex({ h: hsb.h, s: 100, b: 100 }));
				break;
				
		}
		
	}
	
	// Converts to the letter case specified in $.minicolors.settings.letterCase
	function convertCase(string) {
		return $.minicolors.settings.letterCase === 'uppercase' ? string.toUpperCase() : string.toLowerCase();
	}
	
	// Parses a string and returns a valid hex string when possible
	function parseHex(string, expand) {
		string = string.replace(/[^A-F0-9]/ig, '');
		if( string.length !== 3 && string.length !== 6 ) return '';
		if( string.length === 3 && expand ) {
			string = string[0] + string[0] + string[1] + string[1] + string[2] + string[2];
		}
		return '#' + string;
	}
	
	// Keeps value within min and max
	function keepWithin(value, min, max) {
		if( value < min ) value = min;
		if( value > max ) value = max;
		return value;
	}
	
	// Converts an HSB object to an RGB object
	function hsb2rgb(hsb) {
		var rgb = {};
		var h = Math.round(hsb.h);
		var s = Math.round(hsb.s * 255 / 100);
		var v = Math.round(hsb.b * 255 / 100);
		if(s === 0) {
			rgb.r = rgb.g = rgb.b = v;
		} else {
			var t1 = v;
			var t2 = (255 - s) * v / 255;
			var t3 = (t1 - t2) * (h % 60) / 60;
			if( h === 360 ) h = 0;
			if( h < 60 ) { rgb.r = t1; rgb.b = t2; rgb.g = t2 + t3; }
			else if( h < 120 ) {rgb.g = t1; rgb.b = t2; rgb.r = t1 - t3; }
			else if( h < 180 ) {rgb.g = t1; rgb.r = t2; rgb.b = t2 + t3; }
			else if( h < 240 ) {rgb.b = t1; rgb.r = t2; rgb.g = t1 - t3; }
			else if( h < 300 ) {rgb.b = t1; rgb.g = t2; rgb.r = t2 + t3; }
			else if( h < 360 ) {rgb.r = t1; rgb.g = t2; rgb.b = t1 - t3; }
			else { rgb.r = 0; rgb.g = 0; rgb.b = 0; }
		}
		return {
			r: Math.round(rgb.r),
			g: Math.round(rgb.g),
			b: Math.round(rgb.b)
		};
	}
	
	// Converts an RGB object to a hex string
	function rgb2hex(rgb) {
		var hex = [
			rgb.r.toString(16),
			rgb.g.toString(16),
			rgb.b.toString(16)
		];
		$.each(hex, function(nr, val) {
			if (val.length === 1) hex[nr] = '0' + val;
		});
		return '#' + hex.join('');
	}
	
	// Converts an HSB object to a hex string
	function hsb2hex(hsb) {
		return rgb2hex(hsb2rgb(hsb));
	}
	
	// Converts a hex string to an HSB object
	function hex2hsb(hex) {
		var hsb = rgb2hsb(hex2rgb(hex));
		if( hsb.s === 0 ) hsb.h = 360;
		return hsb;
	}
	
	// Converts an RGB object to an HSB object
	function rgb2hsb(rgb) {
		var hsb = { h: 0, s: 0, b: 0 };
		var min = Math.min(rgb.r, rgb.g, rgb.b);
		var max = Math.max(rgb.r, rgb.g, rgb.b);
		var delta = max - min;
		hsb.b = max;
		hsb.s = max !== 0 ? 255 * delta / max : 0;
		if( hsb.s !== 0 ) {
			if( rgb.r === max ) {
				hsb.h = (rgb.g - rgb.b) / delta;
			} else if( rgb.g === max ) {
				hsb.h = 2 + (rgb.b - rgb.r) / delta;
			} else {
				hsb.h = 4 + (rgb.r - rgb.g) / delta;
			}
		} else {
			hsb.h = -1;
		}
		hsb.h *= 60;
		if( hsb.h < 0 ) {
			hsb.h += 360;
		}
		hsb.s *= 100/255;
		hsb.b *= 100/255;
		return hsb;
	}
	
	// Converts a hex string to an RGB object
	function hex2rgb(hex) {
		hex = parseInt(((hex.indexOf('#') > -1) ? hex.substring(1) : hex), 16);
		return {
			r: hex >> 16,
			g: (hex & 0x00FF00) >> 8,
			b: (hex & 0x0000FF)
		};
	}
	
	// A bit of magic...
	$(window).on('load', function() {
		
		// Auto-initialize
		$.minicolors.init();
		
		$(document)
			// Hide on clicks outside of the control
			.on('mousedown touchstart', function(event) {
				if( !$(event.target).parents().add(event.target).hasClass('minicolors') ) {
					hide();
				}
			})
			// Start moving
			.on('mousedown touchstart', '.minicolors-grid, .minicolors-slider, .minicolors-opacity-slider', function(event) {
				var target = $(this);
				event.preventDefault();
				$(document).data('minicolors-target', target);
				move(target, event, true);
			})
			// Move pickers
			.on('mousemove touchmove', function(event) {
				var target = $(document).data('minicolors-target');
				if( target ) move(target, event);
			})
			// Stop moving
			.on('mouseup touchend', function() {
				$(this).removeData('minicolors-target');
			})
			// Toggle panel when swatch is clicked
			.on('mousedown touchstart', '.minicolors-swatch', function(event) {
				var input = $(this).parent().find('INPUT'),
					minicolors = input.parent();
				if( minicolors.hasClass('minicolors-focus') ) {
					hide(input);
				} else {
					show(input);
				}
			})
			// Show on focus
			.on('focus', 'INPUT[type=minicolors]', function(event) {
				var input = $(this);
				show(input);
			})
			// Fix hex and hide on blur
			.on('blur', 'INPUT[type=minicolors]', function(event) {
				var input = $(this);
				input.val( convertCase(parseHex(input.val() !== '' ? input.val() : convertCase(parseHex(input.attr('data-default'), true)), true)) );
				hide(input);
			})
			// Handle keypresses
			.on('keydown', 'INPUT[type=minicolors]', function(event) {
				var input = $(this);
				switch(event.keyCode) {
					case 9: // tab
						hide();
						break;
					case 27: // esc
						hide();
						input.blur();
						break;
				}
			})
			// Update on keyup
			.on('keyup', 'INPUT[type=minicolors]', function(event) {
				var input = $(this);
				updateFromInput(input, true);
			})
			// Update on paste
			.on('paste', 'INPUT[type=minicolors]', function(event) {
				var input = $(this);
				setTimeout( function() {
					updateFromInput(input, true);
				}, 1);
			});
		
	});
	
})(jQuery);
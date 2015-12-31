/*!
 * Angular Material Design
 * https://github.com/angular/material
 * @license MIT
 * v0.10.1
 */
(function( window, angular, undefined ){
"use strict";

/**
 * @ngdoc module
 * @name material.components.menu
 */

angular.module('material.components.menu', [
  'material.core',
  'material.components.backdrop'
])
.directive('mdMenu', MenuDirective)
.controller('mdMenuCtrl', MenuController);

/**
 * @ngdoc directive
 * @name mdMenu
 * @module material.components.menu
 * @restrict E
 * @description
 *
 * Menus are elements that open when clicked. They are useful for displaying
 * additional options within the context of an action.
 *
 * Every `md-menu` must specify exactly two child elements. The first element is what is
 * left in the DOM and is used to open the menu. This element is called the trigger element.
 * The trigger element's scope has access to `$mdOpenMenu($event)`
 * which it may call to open the menu. By passing $event as argument, the
 * corresponding event is stopped from propagating up the DOM-tree.
 *
 * The second element is the `md-menu-content` element which represents the
 * contents of the menu when it is open. Typically this will contain `md-menu-item`s,
 * but you can do custom content as well.
 *
 * <hljs lang="html">
 * <md-menu>
 *  <!-- Trigger element is a md-button with an icon -->
 *  <md-button ng-click="$mdOpenMenu($event)" class="md-icon-button" aria-label="Open sample menu">
 *    <md-icon md-svg-icon="call:phone"></md-icon>
 *  </md-button>
 *  <md-menu-content>
 *    <md-menu-item><md-button ng-click="doSomething()">Do Something</md-button></md-menu-item>
 *  </md-menu-content>
 * </md-menu>
 * </hljs>

 * ## Sizing Menus
 *
 * The width of the menu when it is open may be specified by specifying a `width`
 * attribute on the `md-menu-content` element.
 * See the [Material Design Spec](http://www.google.com/design/spec/components/menus.html#menus-specs)
 * for more information.
 *
 *
 * ## Aligning Menus
 *
 * When a menu opens, it is important that the content aligns with the trigger element.
 * Failure to align menus can result in jarring experiences for users as content
 * suddenly shifts. To help with this, `md-menu` provides serveral APIs to help
 * with alignment.
 *
 * ### Target Mode
 *
 * By default, `md-menu` will attempt to align the `md-menu-content` by aligning
 * designated child elements in both the trigger and the menu content.
 *
 * To specify the alignment element in the `trigger` you can use the `md-menu-origin`
 * attribute on a child element. If no `md-menu-origin` is specified, the `md-menu`
 * will be used as the origin element.
 *
 * Similarly, the `md-menu-content` may specify a `md-menu-align-target` for a
 * `md-menu-item` to specify the node that it should try and align with.
 *
 * In this example code, we specify an icon to be our origin element, and an
 * icon in our menu content to be our alignment target. This ensures that both
 * icons are aligned when the menu opens.
 *
 * <hljs lang="html">
 * <md-menu>
 *  <md-button ng-click="$mdOpenMenu($event)" class="md-icon-button" aria-label="Open some menu">
 *    <md-icon md-menu-origin md-svg-icon="call:phone"></md-icon>
 *  </md-button>
 *  <md-menu-content>
 *    <md-menu-item>
 *      <md-button ng-click="doSomething()" aria-label="Do something">
 *        <md-icon md-menu-align-target md-svg-icon="call:phone"></md-icon>
 *        Do Something
 *      </md-button>
 *    </md-menu-item>
 *  </md-menu-content>
 * </md-menu>
 * </hljs>
 *
 * Sometimes we want to specify alignment on the right side of an element, for example
 * if we have a menu on the right side a toolbar, we want to right align our menu content.
 *
 * We can specify the origin by using the `md-position-mode` attribute on both
 * the `x` and `y` axis. Right now only the `x-axis` has more than one option.
 * You may specify the default mode of `target target` or
 * `target-right target` to specify a right-oriented alignment target. See the
 * position section of the demos for more examples.
 *
 * ### Menu Offsets
 *
 * It is sometimes unavoidable to need to have a deeper level of control for
 * the positioning of a menu to ensure perfect alignment. `md-menu` provides
 * the `md-offset` attribute to allow pixel level specificty of adjusting the
 * exact positioning.
 *
 * This offset is provided in the format of `x y` or `n` where `n` will be used
 * in both the `x` and `y` axis.
 *
 * For example, to move a menu by `2px` from the top, we can use:
 * <hljs lang="html">
 * <md-menu md-offset="2 0">
 *   <!-- menu-content -->
 * </md-menu>
 * </hljs>
 *
 * @usage
 * <hljs lang="html">
 * <md-menu>
 *  <md-button ng-click="$mdOpenMenu($event)" class="md-icon-button">
 *    <md-icon md-svg-icon="call:phone"></md-icon>
 *  </md-button>
 *  <md-menu-content>
 *    <md-menu-item><md-button ng-click="doSomething()">Do Something</md-button></md-menu-item>
 *  </md-menu-content>
 * </md-menu>
 * </hljs>
 *
 * @param {string} md-position-mode The position mode in the form of
             `x`, `y`. Default value is `target`,`target`. Right now the `x` axis
             also suppports `target-right`.
 * @param {string} md-offset An offset to apply to the dropdown after positioning
             `x`, `y`. Default value is `0`,`0`.
 *
 */

function MenuDirective($mdMenu) {
  return {
    restrict: 'E',
    require: 'mdMenu',
    controller: 'mdMenuCtrl', // empty function to be built by link
    scope: true,
    compile: compile
  };

  function compile(templateElement) {
    templateElement.addClass('md-menu');
    var triggerElement = templateElement.children()[0];
    if (!triggerElement.hasAttribute('ng-click')) {
      triggerElement = triggerElement.querySelector('[ng-click]');
    }
    triggerElement && triggerElement.setAttribute('aria-haspopup', 'true');
    if (templateElement.children().length != 2) {
      throw Error('Invalid HTML for md-menu. Expected two children elements.');
    }
    return link;
  }

  function link(scope, element, attrs, mdMenuCtrl) {
    // Move everything into a md-menu-container and pass it to the controller
    var menuContainer = angular.element(
      '<div class="md-open-menu-container md-whiteframe-z2"></div>'
    );
    var menuContents = element.children()[1];
    menuContainer.append(menuContents);
    mdMenuCtrl.init(menuContainer);

    scope.$on('$destroy', function() {
      menuContainer.remove();
      mdMenuCtrl.close();
    });

  }
}
MenuDirective.$inject = ["$mdMenu"];

function MenuController($mdMenu, $attrs, $element, $scope) {

  var menuContainer;
  var ctrl = this;
  var triggerElement;

  // Called by our linking fn to provide access to the menu-content
  // element removed during link
  this.init = function(setMenuContainer) {
    menuContainer = setMenuContainer;
    triggerElement = $element[0].querySelector('[ng-click]');
  };

  // Uses the $mdMenu interim element service to open the menu contents
  this.open = function openMenu(ev) {
    ev && ev.stopPropagation();

    ctrl.isOpen = true;
    triggerElement.setAttribute('aria-expanded', 'true');
    $mdMenu.show({
      scope: $scope,
      mdMenuCtrl: ctrl,
      element: menuContainer,
      target: $element[0]
    });
  };
  // Expose a open function to the child scope for html to use
  $scope.$mdOpenMenu = this.open;

  // Use the $mdMenu interim element service to close the menu contents
  this.close = function closeMenu(skipFocus) {
    if ( !ctrl.isOpen ) return;

    ctrl.isOpen = false;
    triggerElement.setAttribute('aria-expanded', 'false');
    $mdMenu.hide();

    if (!skipFocus) {
      $element.children()[0].focus();
    }
  };

  // Build a nice object out of our string attribute which specifies the
  // target mode for left and top positioning
  this.positionMode = function() {
    var attachment = ($attrs.mdPositionMode || 'target').split(' ');

    // If attachment is a single item, duplicate it for our second value.
    // ie. 'target' -> 'target target'
    if (attachment.length == 1) {
      attachment.push(attachment[0]);
    }

    return {
      left: attachment[0],
      top: attachment[1]
    };
  };

  // Build a nice object out of our string attribute which specifies
  // the offset of top and left in pixels.
  this.offsets = function() {
    var offsets = ($attrs.mdOffset || '0 0').split(' ').map(parseFloat);
    if (offsets.length == 2) {
      return {
        left: offsets[0],
        top: offsets[1]
      };
    } else if (offsets.length == 1) {
      return {
        top: offsets[0],
        left: offsets[0]
      };
    } else {
      throw Error('Invalid offsets specified. Please follow format <x, y> or <n>');
    }
  };
}
MenuController.$inject = ["$mdMenu", "$attrs", "$element", "$scope"];

angular.module('material.components.menu')
.provider('$mdMenu', MenuProvider);

/*
 * Interim element provider for the menu.
 * Handles behavior for a menu while it is open, including:
 *    - handling animating the menu opening/closing
 *    - handling key/mouse events on the menu element
 *    - handling enabling/disabling scroll while the menu is open
 *    - handling redrawing during resizes and orientation changes
 *
 */

function MenuProvider($$interimElementProvider) {
  var MENU_EDGE_MARGIN = 8;

  menuDefaultOptions.$inject = ["$$rAF", "$window", "$mdUtil", "$mdTheming", "$mdConstant", "$document"];
  return $$interimElementProvider('$mdMenu')
    .setDefaults({
      methods: ['target'],
      options: menuDefaultOptions
    });

  /* ngInject */
  function menuDefaultOptions($$rAF, $window, $mdUtil, $mdTheming, $mdConstant, $document) {
    var animator = $mdUtil.dom.animator;

    return {
      parent: 'body',
      onShow: onShow,
      onRemove: onRemove,
      hasBackdrop: true,
      disableParentScroll: true,
      skipCompile: true,
      preserveScope: true,
      themable: true
    };

    /**
     * Boilerplate interimElement onShow function
     * Handles inserting the menu into the DOM, positioning it, and wiring up
     * various interaction events
     */
    function onShow(scope, element, opts) {

      // Sanitize and set defaults on opts
      buildOpts(opts);

      // Wire up theming on our menu element
      $mdTheming.inherit(opts.menuContentEl, opts.target);

      // Register various listeners to move menu on resize/orientation change
      handleResizing();

      // Disable scrolling
      if (opts.disableParentScroll) {
        opts.restoreScroll = $mdUtil.disableScrollAround(opts.element);
      }

      if (opts.backdrop) {
        $mdTheming.inherit(opts.backdrop, opts.parent);
        opts.parent.append(opts.backdrop);
      }
      showMenu();

      // Return the promise for when our menu is done animating in
      return animator
          .waitTransitionEnd(element, {timeout: 370})
          .finally( function(response) {
            opts.cleanupInteraction = activateInteraction();
            return response;
          });

      /** Check for valid opts and set some sane defaults */
      function buildOpts() {
        if (!opts.target) {
          throw Error(
            '$mdMenu.show() expected a target to animate from in options.target'
          );
        }
        angular.extend(opts, {
          alreadyOpen: false,
          isRemoved: false,
          target: angular.element(opts.target), //make sure it's not a naked dom node
          parent: angular.element(opts.parent),
          menuContentEl: angular.element(element[0].querySelector('md-menu-content')),
          backdrop: opts.hasBackdrop && $mdUtil.createBackdrop(scope, "md-menu-backdrop md-click-catcher")
        });
      }

      /** Wireup various resize listeners for screen changes */
      function handleResizing() {
        opts.resizeFn = function() {
          positionMenu(element, opts);
        };
        angular.element($window).on('resize', opts.resizeFn);
        angular.element($window).on('orientationchange', opts.resizeFn);
      }

      /**
       * Place the menu into the DOM and call positioning related functions
       */
      function showMenu() {
        opts.parent.append(element);

        element.removeClass('md-leave');
        // Kick off our animation/positioning but first, wait a few frames
        // so all of our computed positions/sizes are accurate
        $$rAF(function() {
          $$rAF(function() {
            positionMenu(element, opts);
            // Wait a frame before fading in menu (md-active) so that we don't trigger
            // transitions on the menu position changing
            $$rAF(function() {
              element.addClass('md-active');
              opts.alreadyOpen = true;
              element[0].style[$mdConstant.CSS.TRANSFORM] = '';
            });
          });
        });
      }


      /**
       * Activate interaction on the menu. Wire up keyboard listerns for
       * clicks, keypresses, backdrop closing, etc.
       */
      function activateInteraction() {
        element.addClass('md-clickable');

        // close on backdrop click
        opts.backdrop && opts.backdrop.on('click', function(e) {
          e.preventDefault();
          e.stopPropagation();
          scope.$apply(function() {
            opts.mdMenuCtrl.close(true);
          });
        });

        // Wire up keyboard listeners.
        // Close on escape, focus next item on down arrow, focus prev item on up
        opts.menuContentEl.on('keydown', function(ev) {
          scope.$apply(function() {
            switch (ev.keyCode) {
              case $mdConstant.KEY_CODE.ESCAPE: opts.mdMenuCtrl.close(); break;
              case $mdConstant.KEY_CODE.UP_ARROW: focusMenuItem(ev, opts.menuContentEl, opts, -1); break;
              case $mdConstant.KEY_CODE.DOWN_ARROW: focusMenuItem(ev, opts.menuContentEl, opts, 1); break;
            }
          });
        });

        // Close menu on menu item click, if said menu-item is not disabled
        var captureClickListener = function(e) {
          var target = e.target;
          // Traverse up the event until we get to the menuContentEl to see if
          // there is an ng-click and that the ng-click is not disabled
          do {
            if (target == opts.menuContentEl[0]) return;
            if (hasAnyAttribute(target, ['ng-click', 'ng-href', 'ui-sref'])) {
              if (!target.hasAttribute('disabled')) {
                close();
              }
              break;
            }
          } while (target = target.parentNode)

          function close() {
            scope.$apply(function() {
              opts.mdMenuCtrl.close();
            });
          }

          function hasAnyAttribute(target, attrs) {
            if (!target) return false;
            for (var i = 0, attr; attr = attrs[i]; ++i) {
              var altForms = [attr, 'data-' + attr, 'x-' + attr];
              for (var j = 0, rawAttr; rawAttr = altForms[j]; ++j) {
                if (target.hasAttribute(rawAttr)) {
                  return true;
                }
              }
            }
            return false;
          }
        };
        opts.menuContentEl[0].addEventListener('click', captureClickListener, true);

        // kick off initial focus in the menu on the first element
        var focusTarget = opts.menuContentEl[0].querySelector('[md-menu-focus-target]');
        if (!focusTarget) focusTarget = opts.menuContentEl[0].firstElementChild.firstElementChild;
        focusTarget.focus();

        return function cleanupInteraction() {
          element.removeClass('md-clickable');
          opts.backdrop.off('click');
          opts.menuContentEl.off('keydown');
          opts.menuContentEl[0].removeEventListener('click', captureClickListener, true);
        };
      }
    }

    /**
      * Takes a keypress event and focuses the next/previous menu
      * item from the emitting element
      * @param {event} e - The origin keypress event
      * @param {angular.element} menuEl - The menu element
      * @param {object} opts - The interim element options for the mdMenu
      * @param {number} direction - The direction to move in (+1 = next, -1 = prev)
      */
    function focusMenuItem(e, menuEl, opts, direction) {
      var currentItem = $mdUtil.getClosest(e.target, 'MD-MENU-ITEM');

      var items = $mdUtil.nodesToArray(menuEl[0].children);
      var currentIndex = items.indexOf(currentItem);

      // Traverse through our elements in the specified direction (+/-1) and try to
      // focus them until we find one that accepts focus
      for (var i = currentIndex + direction; i >= 0 && i < items.length; i = i + direction) {
        var focusTarget = items[i].firstElementChild || items[i];
        var didFocus = attemptFocus(focusTarget);
        if (didFocus) {
          break;
        }
      }
    }

    /**
     * Attempts to focus an element. Checks whether that element is the currently
     * focused element after attempting.
     * @param {HTMLElement} el - the element to attempt focus on
     * @returns {bool} - whether the element was successfully focused
     */
    function attemptFocus(el) {
      if (el && el.getAttribute('tabindex') != -1) {
        el.focus();
        if ($document[0].activeElement == el) {
          return true;
        } else {
          return false;
        }
      }
    }

    /**
     * Boilerplate interimElement onRemove function
     * Handles removing the menu from the DOM, cleaning up the element
     * and removing various listeners
     */
    function onRemove(scope, element, opts) {
      opts.isRemoved = true;
      element.addClass('md-leave');

      opts.cleanupInteraction();

      // Disable resizing handlers
      angular.element($window).off('resize', opts.resizeFn);
      angular.element($window).off('orientationchange', opts.resizeFn);
      opts.resizeFn = undefined;

      // Wait for animate out, then remove from the DOM
      return animator
        .waitTransitionEnd(element, { timeout: 370 })
        .finally(function() {
          element.removeClass('md-active');

          opts.backdrop && opts.backdrop.remove();
          if (element[0].parentNode === opts.parent[0]) {
            opts.parent[0].removeChild(element[0]);
          }
          opts.restoreScroll && opts.restoreScroll();
        });
    }

    /**
     * Computes menu position and sets the style on the menu container
     * @param {HTMLElement} el - the menu container element
     * @param {object} opts - the interim element options object
     */
    function positionMenu(el, opts) {
      if (opts.isRemoved) return;

      var containerNode = el[0],
          openMenuNode = el[0].firstElementChild,
          openMenuNodeRect = openMenuNode.getBoundingClientRect(),
          boundryNode = opts.parent[0],
          boundryNodeRect = boundryNode.getBoundingClientRect();

      var originNode = opts.target[0].querySelector('[md-menu-origin]') || opts.target[0],
          originNodeRect = originNode.getBoundingClientRect();


      var bounds = {
        left: boundryNodeRect.left + MENU_EDGE_MARGIN,
        top: Math.max(boundryNodeRect.top, 0) + MENU_EDGE_MARGIN,
        bottom: Math.max(boundryNodeRect.bottom, Math.max(boundryNodeRect.top, 0) + boundryNodeRect.height) - MENU_EDGE_MARGIN,
        right: boundryNodeRect.right - MENU_EDGE_MARGIN
      };


      var alignTarget, alignTargetRect, existingOffsets;
      var positionMode = opts.mdMenuCtrl.positionMode();

      if (positionMode.top == 'target' || positionMode.left == 'target' || positionMode.left == 'target-right') {
        // TODO: Allow centering on an arbitrary node, for now center on first menu-item's child
        alignTarget = firstVisibleChild();
        if (!alignTarget) {
          throw Error('Error positioning menu. No visible children.');
        }

        alignTarget = alignTarget.firstElementChild || alignTarget;
        alignTarget = alignTarget.querySelector('[md-menu-align-target]') || alignTarget;
        alignTargetRect = alignTarget.getBoundingClientRect();

        existingOffsets = {
          top: parseFloat(containerNode.style.top || 0),
          left: parseFloat(containerNode.style.left || 0)
        };
      }

      var position = { };
      var transformOrigin = 'top ';

      switch (positionMode.top) {
        case 'target':
          position.top = existingOffsets.top + originNodeRect.top - alignTargetRect.top;
          break;
        // Future support for mdMenuBar
        // case 'top':
        //   position.top = originNodeRect.top;
        //   break;
        // case 'bottom':
        //   position.top = originNodeRect.top + originNodeRect.height;
        //   break;
        default:
          throw new Error('Invalid target mode "' + positionMode.top + '" specified for md-menu on Y axis.');
      }

      switch (positionMode.left) {
        case 'target':
          position.left = existingOffsets.left + originNodeRect.left - alignTargetRect.left;
          transformOrigin += 'left';
          break;
        case 'target-right':
          position.left = originNodeRect.right - openMenuNodeRect.width + (openMenuNodeRect.right - alignTargetRect.right);
          transformOrigin += 'right';
          break;
        // Future support for mdMenuBar
        // case 'left':
        //   position.left = originNodeRect.left;
        //   transformOrigin += 'left';
        //   break;
        // case 'right':
        //   position.left = originNodeRect.right - containerNode.offsetWidth;
        //   transformOrigin += 'right';
        //   break;
        default:
          throw new Error('Invalid target mode "' + positionMode.left + '" specified for md-menu on X axis.');
      }

      var offsets = opts.mdMenuCtrl.offsets();
      position.top += offsets.top;
      position.left += offsets.left;

      clamp(position);

      el.css({
        top: position.top + 'px',
        left: position.left + 'px'
      });

      containerNode.style[$mdConstant.CSS.TRANSFORM_ORIGIN] = transformOrigin;

      // Animate a scale out if we aren't just repositioning
      if (!opts.alreadyOpen) {
        containerNode.style[$mdConstant.CSS.TRANSFORM] = 'scale(' +
          Math.min(originNodeRect.width / containerNode.offsetWidth, 1.0) + ',' +
          Math.min(originNodeRect.height / containerNode.offsetHeight, 1.0) +
        ')';
      }

      /**
       * Clamps the repositioning of the menu within the confines of
       * bounding element (often the screen/body)
       */
      function clamp(pos) {
        pos.top = Math.max(Math.min(pos.top, bounds.bottom - containerNode.offsetHeight), bounds.top);
        pos.left = Math.max(Math.min(pos.left, bounds.right - containerNode.offsetWidth), bounds.left);
      }

      /**
        * Gets the first visible child in the openMenuNode
        * Necessary incase menu nodes are being dynamically hidden
        */
      function firstVisibleChild() {
        for (var i = 0; i < openMenuNode.children.length; ++i) {
          if (window.getComputedStyle(openMenuNode.children[i]).display != 'none') {
            return openMenuNode.children[i];
          }
        }
      }
    }
  }
}
MenuProvider.$inject = ["$$interimElementProvider"];

})(window, window.angular);
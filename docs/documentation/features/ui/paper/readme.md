---
layout: documentation
---

# Paper UI 

The Paper UI is an HTML5 application for the Eclipse SmartHome framework. The primary focus of the Paper UI is to provide a testing tool for binding developers. For example with the help of the UI developers can test the discovery functionality of a binding. Moreover the Paper UI can be seen as an example of how applications can use the new APIs and how items can be rendered. The Paper UI implements Google's Material Design and is responsive, so that it smoothly renders on different screen sizes. All modern browsers (Safari, Chrome, Firefox) besides the Internet Explorer are supported in their newest version. The Internet explorer is mainly lacking support for SSE.

Even if the Eclipse SmartHome framework is very flexible, the Paper UI is quite simple and only supports limited use cases. In contrast to the configuration files it is not possible to manage items and links individually. The user only sees things and the corresponding control interface.  

## Features

The following features are implemented:

* Control of items	 
* Inbox & discovery of things
* Manual setup of things
* Binding information
* Configuration of things
* Group management
* Event support for item state updates, thing status updates and new inbox entries

## Known Limitations

Some of the framework capabilities are not yet supported:

* Item limitations
    * ImageItem and LocationItem not properly supported
    * Option lists not supported
    * Limited support of categories
    * Tags not supported 
* Channel groups not supported in Thing and Item view
* Sitemaps not supported
* Configuration parameter limitations
    * Option list not supported
    * Context not supported
    * Minimum and maximum not supported
    * Filter not supported
* No Minification of CSS and JS files, so the performance of the application is not optimal

## Customization

The Paper UI can easily be customized with fragments without changing the original code. OSGi fragments allow to override files in the host bundle. The file `css/theme.css` can be used for style customization. Other resources like the logo can also be overridden. To provide custom logic the `js/extensions.js` and `partials/include.extension.html` files can used.
 
## FAQ
 
### Why is it named Paper UI?
 
Google's Material Design approach uses so called "cards", which looks like paper. As the Paper UI also uses this card, it was decided to call it Paper UI.

### Why does it not support sitemaps?
 
Sitemaps require the Xtext DSL. The Paper UI aims to provide a full UI-only experience without any need for modifying configuration files. Thus the Paper UI can not make use of Sitemaps now, until it is refactored to have DSL support optional as it was done for items and things.
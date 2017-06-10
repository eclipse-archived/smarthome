!function(a){"use strict";function b(a){var b=document.createElement("div");return b.innerHTML=a,b.childNodes}function c(a,b){b instanceof NodeList?[].slice.call(b).forEach(function(b){a.appendChild(b)}):a.appendChild(b)}function d(a){var b=a,c=void 0!==b.type?b.type:"GET",d=void 0!==b.data?b.data:"",e=void 0!==b.headers?b.headers:{},f=new XMLHttpRequest;f.open(c,b.url,!0);for(var g in e)f.setRequestHeader(g,e[g]);return f.onload=function(){if(f.status<200||f.status>400)return void("function"==typeof b.error&&b.error(f));"function"==typeof b.callback&&b.callback(f)},f.onerror=function(){"function"==typeof b.error&&b.error(f)},f.send(d),f}function e(a,b){return document.getElementById(a).innerHTML.replace(/\{([\w]+)\}/,function(a,c){return void 0!==b[c]?b[c]:""})}function f(){var a=this;a.level=0,a.push=function(b){a.level++,history.pushState({page:b},document.title.textContent)},a.replace=function(a,b){history.replaceState({page:a},document.title.textContent,b)},window.addEventListener("popstate",function(b){a.level--,D.UI.navigate(b.state.page,!1)},!1)}function g(d){function f(a){a=a||window.event,27===a.keyCode&&i.hide()}function g(){document.addEventListener("keydown",f)}function h(){void 0!==i.onHide&&i.onHide(),document.removeEventListener("keydown",f)}var i=this;i.templateId="template-modal",i.text=e(i.templateId,{content:d}),i.show=function(){c(document.body,b(i.text)),i.bg=document.querySelector(a.modal),i.container=i.bg.querySelector(a.modalContainer),D.UI.currentModal=i,i.bg.addEventListener("click",function(){i.hide()}),i.container.addEventListener("click",function(a){a.stopPropagation()}),g()},i.hide=function(){document.body.querySelector(a.modal).remove(),delete D.UI.currentModal,h()}}function h(a,b){var c,d=this,e=!1;d.lock=!1,d.call=function(){c=arguments,d.lock||(e=!1,a.apply(null,c),d.lock=!0,setTimeout(function(){e||a.apply(null,c),d.lock=!1},b))},d.finish=function(){e=!0}}function i(a,b){function c(a){a.widget.setVisible(a.visibility)}var d=this;d.queue=[],d.timeout=null,d.processEvents=function(){for(d.timeout=null;0!==d.queue.length;)c(d.queue[0]),d.queue=d.queue.slice(1)},d.push=function(c){d.queue.length>b||(d.queue.push(c),null===d.timeout?d.timeout=setTimeout(d.processEvents,a):(clearTimeout(d.timeout),d.timeout=setTimeout(d.processEvents,a)))}}function j(b){var c=this,d=!1;c.parentNode=b,c.formRow=b.parentNode,c.item=c.parentNode.getAttribute(a.itemAttribute),c.id=c.parentNode.getAttribute(a.idAttribute),c.icon=c.parentNode.parentNode.querySelector(a.formIcon),c.visible=!c.formRow.classList.contains(a.formRowHidden),c.label=c.parentNode.parentNode.querySelector(a.formLabel),null!==c.icon&&(c.iconName=c.icon.getAttribute(a.iconAttribute)),c.reloadIcon=function(a){null!==c.icon&&c.icon.setAttribute("src","/icon/"+c.iconName+"?state="+a+"&format="+D.UI.iconType)},c.setVisible=function(b){b?c.formRow.classList.remove(a.formRowHidden):c.formRow.classList.add(a.formRowHidden),c.visible=b},c.setValue=function(a,b){c.reloadIcon(b),d?d=!1:c.setValuePrivate(a,b)},c.setValuePrivate=function(){},c.setLabel=function(){},c.suppressUpdate=function(){d=!0},c.setLabelColor=function(a){null!==c.label&&(c.label.style.color=a)},c.setValueColor=function(a){c.parentNode.style.color=a}}function k(b){var c=this;c.parentNode=b,c.id=c.parentNode.getAttribute(a.idAttribute),c.visible=!c.parentNode.classList.contains(a.formHidden),c.title=c.parentNode.querySelector(a.formTitle),c.setVisible=function(b){b?c.parentNode.classList.remove(a.formHidden):c.parentNode.classList.add(a.formHidden),c.visible=b},c.setLabel=function(a){c.title.innerHTML=a},c.setValue=function(){},c.suppressUpdate=function(){}}function l(a,b){b&&j.call(this,a);var c=this;if(c.image=a.querySelector("img"),c.updateInterval=parseInt(a.getAttribute("data-update-interval"),10),c.url=a.getAttribute("data-proxied-url"),c.validUrl="true"===a.getAttribute("data-valid-url"),c.setValuePrivate=function(a,b){b.startsWith("data:")?c.image.setAttribute("src",b):"UNDEF"!==b||c.validUrl?c.image.setAttribute("src",c.url+"&t="+Date.now()):c.image.setAttribute("src","images/none.png")},0!==c.updateInterval){c.updateInterval<100&&(c.updateInterval=100);var d=setInterval(function(){if(0===c.image.clientWidth)return void clearInterval(d);c.image.setAttribute("src",c.url+"&t="+Date.now())},c.updateInterval)}}function m(a){j.call(this,a);var b=this;b.hasValue="true"===b.parentNode.getAttribute("data-has-value"),b.setValuePrivate=function(c){b.hasValue&&(a.innerHTML=c)}}function n(b){j.call(this,b);var c=this;c.hasValue="true"===c.parentNode.getAttribute("data-has-value"),c.value=c.parentNode.parentNode.querySelector(a.formValue),c.count=1*c.parentNode.getAttribute("data-count"),c.suppressUpdateButtons=!1,c.reset=function(){c.buttons.forEach(function(b){b.classList.remove(a.buttonActiveClass)})},c.onclick=function(){var b=this.getAttribute("data-value")+"";1!==c.count&&(c.reset(),this.classList.add(a.buttonActiveClass)),c.parentNode.dispatchEvent(C("control-change",{item:c.item,value:b})),c.suppressUpdateButtons=!0},c.valueMap={},c.buttons=[].slice.call(c.parentNode.querySelectorAll(a.controlButton)),c.setValuePrivate=function(b,d){if(c.hasValue&&(c.value.innerHTML=b),1!==c.count){if(c.suppressUpdateButtons)return void(c.suppressUpdateButtons=!1);c.reset(),void 0!==c.valueMap&&void 0!==c.valueMap[d]&&c.valueMap[d].classList.add(a.buttonActiveClass)}},c.setValueColor=function(a){c.value.style.color=a},c.buttons.forEach.call(c.buttons,function(a){var b=a.getAttribute("data-value")+"";c.valueMap[b]=a,a.addEventListener("click",c.onclick)})}function o(b){function c(a){if(a.stopPropagation(),"input"===a.target.tagName.toLowerCase()){var b=a.target.getAttribute("value");d.parentNode.dispatchEvent(C("control-change",{item:d.item,value:b})),setTimeout(function(){d.modal.hide()},300)}}j.call(this,b);var d=this,e=b.getAttribute("data-value-map");d.value=null,d.valueNode=b.parentNode.querySelector(a.formValue),d.valueMap=null!==e?JSON.parse(e):{},d.showModal=function(){var b=d.parentNode.querySelector(a.selectionRows).innerHTML;d.modal=new g(b),d.modal.show();var e=[].slice.call(d.modal.container.querySelectorAll(a.formRadio));if(null!==d.value){[].slice.call(d.modal.container.querySelectorAll("input[type=radio]")).forEach(function(a){a.value===d.value?a.checked=!0:a.checked=!1})}e.forEach(function(a){componentHandler.upgradeElement(a,"MaterialRadio"),a.addEventListener("click",c)})},d.setValuePrivate=function(a,b){d.value=""+b,void 0!==d.valueMap[b]?d.valueNode.innerHTML=d.valueMap[b]:d.valueNode.innerHTML=""},d.setValueColor=function(a){d.valueNode.style.color=a},d.parentNode.parentNode.addEventListener("click",d.showModal)}function p(b){function c(a){i.parentNode.dispatchEvent(C("control-change",{item:i.item,value:a}))}function d(a,b){g=!1,l=!0,h=setTimeout(function(){g=!0,c(a)},k),b.stopPropagation(),b.preventDefault()}function e(a,b){clearTimeout(h),l&&(l=!1,c(g?"STOP":a),b.stopPropagation(),b.preventDefault())}function f(a){c("STOP"),a.stopPropagation(),a.preventDefault()}j.call(this,b);var g,h,i=this,k=300,l=!1;i.buttonUp=i.parentNode.querySelector(a.rollerblind.up),i.buttonDown=i.parentNode.querySelector(a.rollerblind.down),i.buttonStop=i.parentNode.querySelector(a.rollerblind.stop),i.hasValue="true"===i.parentNode.getAttribute("data-has-value"),i.valueNode=i.parentNode.parentNode.querySelector(a.formValue),i.setValuePrivate=function(a){i.hasValue&&("DOWN"===a?a="100":"UP"===a&&(a="0"),i.valueNode.innerHTML=a)};var m=e.bind(null,"UP"),n=d.bind(null,"UP"),o=e.bind(null,"DOWN"),p=d.bind(null,"DOWN");i.buttonUp.addEventListener("touchstart",n),i.buttonUp.addEventListener("mousedown",n),i.buttonUp.addEventListener("touchend",m),i.buttonUp.addEventListener("mouseup",m),i.buttonUp.addEventListener("mouseleave",m),i.buttonDown.addEventListener("touchstart",p),i.buttonDown.addEventListener("mousedown",p),i.buttonDown.addEventListener("touchend",o),i.buttonDown.addEventListener("mouseup",o),i.buttonDown.addEventListener("mouseleave",o),i.buttonStop.addEventListener("mousedown",f),i.buttonStop.addEventListener("touchstart",f)}function q(b){function c(a,b){var c=d.value+(a===!0?d.step:-d.step);c=c>d.max?d.max:c,c=c<d.min?d.min:c,d.parentNode.dispatchEvent(C("control-change",{item:d.item,value:c})),d.value=c,b.stopPropagation(),b.preventDefault()}j.call(this,b);var d=this;d.up=d.parentNode.querySelector(a.setpoint.up),d.down=d.parentNode.querySelector(a.setpoint.down),d.value=d.parentNode.getAttribute("data-value"),d.max=parseFloat(d.parentNode.getAttribute("data-max")),d.min=parseFloat(d.parentNode.getAttribute("data-min")),d.step=parseFloat(d.parentNode.getAttribute("data-step")),d.value=isNaN(parseFloat(d.value))?0:parseFloat(d.value),d.valueNode=d.parentNode.parentNode.querySelector(a.formValue),d.setValuePrivate=function(a,b){d.value=1*b,d.valueNode.innerHTML=a};var e=c.bind(null,!0),f=c.bind(null,!1);d.up.addEventListener("mousedown",e),d.up.addEventListener("touchstart",e),d.down.addEventListener("mousedown",f),d.down.addEventListener("touchstart",f)}function r(b,c,d){function e(a){var b=a.r,c=a.g,d=a.b;b/=255,c/=255,d/=255;var e,f,g=Math.max(b,c,d),h=Math.min(b,c,d),i=g,j=g-h;if(f=0===g?0:j/g,g===h)e=0;else{switch(g){case b:e=(c-d)/j+(c<d?6:0);break;case c:e=(d-b)/j+2;break;case d:e=(b-c)/j+4}e/=6}return{h:e,s:f,v:i}}function f(a){var b=a.h,c=a.s,d=a.v,e=(2-c)*d,f=e<1?e:2-e;return{h:b,s:0===f?0:c*d/f,l:e/2}}function g(a){var b;b=void 0!==a.touches?{x:a.touches[0].pageX-p.colorpicker.offsetLeft,y:a.touches[0].pageY-p.colorpicker.offsetTop}:{x:a.pageX-p.colorpicker.offsetLeft,y:a.pageY-p.colorpicker.offsetTop};var c=p.image.clientWidth/2,d=b.x-c,e=b.y-c,g=e*e+d*d;if(g>c*c){var h=1-Math.abs(c/Math.sqrt(g));b.x-=d*h,b.y-=e*h}p.handle.style.left=b.x/p.image.clientWidth*100+"%",p.handle.style.top=b.y/p.image.clientWidth*100+"%";var i=d>=0?(2*Math.PI-Math.atan(e/d)+Math.PI/2)/(2*Math.PI):(2*Math.PI-Math.atan(e/d)-Math.PI/2)/(2*Math.PI),j={h:isNaN(i)?0:i,s:Math.sqrt(g)/c,v:1},k=f(j);p.hsvValue={h:j.h,s:j.s,v:p.slider.value/100},k.l=k.l<.5?.5:k.l,p.background.style.background="hsl("+360*k.h+", 100%, "+Math.round(100*k.l)+"%)"}function i(a){var b=e(a);p.slider.value=100*b.v;var c=50+Math.round(b.s*Math.cos(2*Math.PI*b.h)*50),d=50+Math.round(b.s*Math.sin(2*Math.PI*b.h)*50);p.handle.style.top=c+"%",p.handle.style.left=d+"%",b.v=1;var f=r.hsv2rgb(b);p.background.style.background="rgb("+Math.round(f.r)+","+Math.round(f.g)+","+Math.round(f.b)+")"}function j(){null!==p.interval&&(clearInterval(p.interval),p.interval=null),p.isBeingChanged=!1,window.removeEventListener("mouseup",j)}function k(a){p.interval=setInterval(function(){d(p.hsvValue)},300),window.addEventListener("mouseup",j),g(a),d(p.hsvValue),p.isBeingChanged=!0,a.stopPropagation()}function l(a){(void 0!==a.touches||1&a.buttons)&&(g(a),a.stopPropagation(),a.preventDefault())}function m(a){null!==p.interval&&(clearInterval(p.interval),p.interval=null),window.removeEventListener("mouseup",j),p.isBeingChanged=!1,a.stopPropagation()}function n(){p.hsvValue.v=p.slider.value/100,p.debounceProxy.call()}function o(){p.hsvValue.v=p.slider.value/100,p.debounceProxy.call(),p.debounceProxy.finish()}var p=this;p.container=b,p.value=c,p.hsvValue=e(c),p.interval=null,p.isBeingChanged=!1,p.colorpicker=p.container.querySelector(a.colorpicker.colorpicker),p.image=p.container.querySelector(a.colorpicker.image),p.background=p.container.querySelector(a.colorpicker.background),p.handle=p.container.querySelector(a.colorpicker.handle),p.slider=p.container.querySelector(a.colorpicker.slider),p.button=p.container.querySelector(a.controlButton),componentHandler.upgradeElement(p.button,"MaterialButton"),componentHandler.upgradeElement(p.button,"MaterialRipple"),p.debounceProxy=new h(function(){d(p.hsvValue)},200),p.updateColor=function(a){p.isBeingChanged||i(a)},p.slider.addEventListener("change",n),p.slider.addEventListener("touchend",o),p.slider.addEventListener("mouseup",o),p.image.addEventListener("mousedown",l),p.image.addEventListener("mousemove",l),p.image.addEventListener("touchmove",l),p.image.addEventListener("touchstart",l),p.image.addEventListener("touchend",m),p.image.addEventListener("mouseup",m),p.image.addEventListener("mousedown",k),p.image.addEventListener("touchstart",k),i(c)}function s(b){function c(a){return{r:parseInt(a.substr(1,2),16),g:parseInt(a.substr(3,2),16),b:parseInt(a.substr(5,2),16)}}function d(a){l.parentNode.dispatchEvent(C("control-change",{item:l.item,value:a}))}function f(a){l.pressed=!0,l.longPress=!1,k=setInterval(function(){l.longPress=!0,d(a)},m)}function h(a){l.pressed&&(l.longPress||d(a),l.pressed=!1,l.longPress=!1,clearInterval(k))}function i(){l.modal=new g(e("template-colorpicker")),l.modal.show(),l.modal.container.classList.add(a.colorpicker.modalClass),l.modal.onHide=function(){l.modalControl=null,l.modal=null},l.modalControl=new r(l.modal.container,l.value,function(a){l.value=r.hsv2rgb(a),d(Math.round(360*a.h%360)+","+Math.round(100*a.s%100)+","+Math.round(100*a.v))}),l.modal.container.querySelector(a.colorpicker.button).addEventListener("click",function(){l.modal.hide()})}j.call(this,b);var k,l=this,m=300;l.value=c(l.parentNode.getAttribute("data-value")),l.modalControl=null,l.buttonUp=l.parentNode.querySelector(a.colorpicker.up),l.buttonDown=l.parentNode.querySelector(a.colorpicker.down),l.buttonPick=l.parentNode.querySelector(a.colorpicker.pick),l.longPress=!1,l.pressed=!1,l.setValue=function(a){var b=a.split(","),c={h:b[0]/360,s:b[1]/100,v:b[2]/100};l.value=r.hsv2rgb(c),null!==l.modalControl&&l.modalControl.updateColor(l.value)};var n=f.bind(null,"INCREASE"),o=f.bind(null,"DECREASE"),p=h.bind(null,"ON"),q=h.bind(null,"OFF");l.buttonUp.addEventListener("touchstart",n),l.buttonUp.addEventListener("mousedown",n),l.buttonUp.addEventListener("mouseleave",p),l.buttonUp.addEventListener("touchend",p),l.buttonUp.addEventListener("mouseup",p),l.buttonDown.addEventListener("touchstart",o),l.buttonDown.addEventListener("mousedown",o),l.buttonDown.addEventListener("touchend",q),l.buttonDown.addEventListener("mouseup",q),l.buttonDown.addEventListener("mouseleave",q),l.buttonPick.addEventListener("click",i)}function t(b){j.call(this,b);var c=this;c.input=c.parentNode.querySelector("input[type=checkbox]"),c.input.addEventListener("change",function(){c.parentNode.dispatchEvent(C("control-change",{item:c.item,value:c.input.checked?"ON":"OFF"}))}),c.hasValue="true"===c.parentNode.getAttribute("data-has-value"),c.valueNode=c.parentNode.parentNode.querySelector(a.formValue),c.setValuePrivate=function(a,b){var d="ON"===b;c.input.checked!==d&&(c.input.checked=d,d?c.parentNode.MaterialSwitch.on():c.parentNode.MaterialSwitch.off()),c.hasValue&&(c.valueNode.innerHTML=a)},c.setValueColor=function(a){c.valueNode.style.color=a}}function u(b){function c(){f.parentNode.dispatchEvent(C("control-change",{item:f.item,value:f.input.value}))}function d(){null!==g&&clearTimeout(g),f.locked=!0,D.changeListener.pause()}function e(){f.debounceProxy.call(),setTimeout(function(){D.changeListener.resume()},5),g=setTimeout(function(){f.locked=!1},300),f.debounceProxy.finish()}j.call(this,b);var f=this;f.input=f.parentNode.querySelector("input[type=range]"),f.hasValue="true"===f.parentNode.getAttribute("data-has-value"),f.valueNode=f.parentNode.parentNode.querySelector(a.formValue),f.locked=!1,function(){var a=parseInt(f.input.getAttribute("data-state"),10);isNaN(a)?f.input.value=0:f.input.value=a,f.input.MaterialSlider&&f.input.MaterialSlider.change()}(),f.debounceProxy=new h(function(){c()},200),f.input.addEventListener("change",function(){f.debounceProxy.call()}),f.setValuePrivate=function(a,b){if(f.hasValue&&(f.valueNode.innerHTML=a),f.locked)return void f.reloadIcon(b);f.input.value=b,f.input.MaterialSlider.change()},f.setValueColor=function(a){f.valueNode.style.color=a};var g=null;f.input.addEventListener("touchstart",d),f.input.addEventListener("mousedown",d),f.input.addEventListener("touchend",e),f.input.addEventListener("mouseup",e)}function v(b){j.call(this,b);var c=this;c.target=b.getAttribute("data-target"),c.hasValue="true"===c.parentNode.getAttribute("data-has-value"),c.container=b.parentNode.querySelector(a.formValue),c.setValuePrivate=function(a){c.hasValue&&(c.container.innerHTML=a)},c.setValueColor=function(a){c.container.style.color=a},b.parentNode.addEventListener("click",function(){D.UI.navigate(c.target,!0)})}function w(a){d({type:"POST",url:"/rest/items/"+a.detail.item,data:a.detail.value,headers:{"Content-Type":"text/plain"}})}function x(b){function c(a){var b=a.documentElement,c=[];if("page"===b.tagName){[].forEach.call(b.childNodes,function(a){a instanceof Text||c.push(a)}),g.setTitle(c[0].textContent);for(var d=document.querySelector(".page-content");d.firstChild;)d.removeChild(d.firstChild);d.insertAdjacentHTML("beforeend",c[1].textContent)}}var e={Loading:1,Idle:2},g=this,h=e.Idle,i=new f;g.page=document.body.getAttribute("data-page-id"),g.sitemap=document.body.getAttribute("data-sitemap"),g.destination=null,g.root=b,g.loading=g.root.querySelector(a.uiLoadingBar),g.layoutTitle=document.querySelector(a.layoutTitle),g.iconType=document.body.getAttribute(a.iconTypeAttribute),g.notification=document.querySelector(a.notify),g.setTitle=function(a){document.querySelector("title").innerHTML=a,g.layoutTitle.innerHTML=a},g.upgradeComponents=function(){var b=componentHandler.upgradeElement;[].slice.call(document.querySelectorAll(a.formControls)).forEach(function(a){switch(a.getAttribute("data-control-type")){case"setpoint":case"rollerblind":case"colorpicker":case"buttons":[].slice.call(a.querySelectorAll("button")).forEach(function(a){b(a,"MaterialButton"),b(a,"MaterialRipple")});break;case"checkbox":b(a,"MaterialSwitch");break;case"slider":b(a.querySelector("input[type=range]"),"MaterialSlider")}})},g.showLoadingBar=function(){g.loading.style.display="block"},g.hideLoadingBar=function(){g.loading.style.display=""},g.navigateCallback=function(a){h=e.Idle,g.pushState&&i.push(g.page),c(a.responseXML),g.pushState&&i.replace(g.newPage,g.destination),g.page=g.newPage,g.upgradeComponents(),g.initControls(),g.hideLoadingBar(),g.sitemap!==g.page?(g.header.classList.remove("navigation-home"),g.header.classList.add("navigation-page")):(g.header.classList.add("navigation-home"),g.header.classList.remove("navigation-page")),D.changeListener.navigate(g.page)},g.navigate=function(a,b){h===e.Idle&&(h=e.Loading,g.pushState=void 0===b||b===!0,g.newPage=a,g.showLoadingBar(),g.destination="/basicui/app?w="+a+"&sitemap="+D.UI.sitemap,d({url:g.destination+"&subscriptionId="+D.subscriptionId+"&__async=true",callback:g.navigateCallback}),void 0!==D.UI.currentModal&&D.UI.currentModal.hide())},g.initControls=function(){function b(a){void 0!==D.dataModelLegacy[a.item]&&void 0!==D.dataModelLegacy[a.item].widgets||void 0!==a.item&&(D.dataModelLegacy[a.item]={widgets:[]}),void 0!==a.item&&D.dataModelLegacy[a.item].widgets.push(a),D.dataModel[a.id]=a}D.dataModel={},D.dataModelLegacy={},[].forEach.call(document.querySelectorAll(a.formControls),function(a){switch(a.getAttribute("data-control-type")){case"setpoint":b(new q(a));break;case"rollerblind":b(new p(a));break;case"buttons":b(new n(a));break;case"selection":b(new o(a));break;case"checkbox":b(new t(a));break;case"slider":b(new u(a));break;case"chart":case"image":b(new l(a,!0));break;case"image-link":b(new l(a,!1));case"text-link":case"group":b(new v(a));break;case"text":b(new m(a));break;case"colorpicker":b(new s(a));break;case"video":case"webview":case"mapview":b(new j(a))}a.addEventListener("control-change",w)}),[].forEach.call(document.querySelectorAll(a.form),function(a){b(new k(a))})},g.showNotification=function(b){g.notification.innerHTML=b,g.notification.classList.remove(a.notifyHidden)},g.hideNotification=function(){g.notification.classList.add(a.notifyHidden)},i.replace(g.page,document.location.href),function(){g.header=document.querySelector(a.layoutHeader),g.sitemap!==g.page&&(g.header.classList.remove("navigation-home"),g.header.classList.add("navigation-page")),document.querySelector(a.backButton).addEventListener("click",function(){i.level>0?history.back():location.href=location.origin+"/basicui/app?sitemap="+D.UI.sitemap})}()}function y(){this.paused=!1,this.pause=function(){this.paused=!0},this.resume=function(){this.paused=!1}}function z(a){y.call(this);var b=this;b.navigate=function(){},b.source=new EventSource(a),b.source.addEventListener("event",function(a){if(!b.paused){var c,d,e=JSON.parse(a.data);if(e.widgetId in D.dataModel||e.widgetId===D.UI.page){if("string"==typeof e.label&&e.label.indexOf("[")!==-1&&e.label.indexOf("]")!==-1){var f=e.label.indexOf("[");c=e.label.substr(f+1,e.label.lastIndexOf("]")-(f+1)),d=e.label.substr(0,f)+c}else c=e.item.state;if(e.widgetId===D.UI.page&&void 0!==d)D.UI.setTitle(d);else if(void 0!==D.dataModel[e.widgetId]){var g=D.dataModel[e.widgetId];g.visible!==e.visibility?D.UI.layoutChangeProxy.push({widget:g,visibility:e.visibility}):(g.setValue(c,e.item.state),void 0!==e.label&&g.setLabel(e.label),void 0!==e.labelcolor?g.setLabelColor(e.labelcolor):g.setLabelColor(""),void 0!==e.valuecolor?g.setValueColor(e.valuecolor):g.setValueColor(""))}}}}),b.source.onerror=function(){b.source.close(),b.connectionError()}}function A(){function a(a){function b(a){a.forEach(function(a){if(void 0!==a.item){var b=a.item.name,c=a.item.state,d=a.label,e=a.labelcolor,f=a.valuecolor;void 0!==D.dataModelLegacy[b]&&D.dataModelLegacy[b].widgets.forEach(function(a){"NULL"!==c&&a.setValue(c,c),void 0!==d&&a.setLabel(d),void 0!==e?a.setLabelColor(e):a.setLabelColor(""),void 0!==f?a.setValueColor(f):a.setValueColor("")})}})}try{a=JSON.parse(a)}catch(c){return}a.leaf?b(a.widgets):a.widgets.forEach(function(a){b(a.widgets)})}function b(){var e=Math.random().toString(16).slice(2);c.request=d({url:"/rest/sitemaps/"+c.sitemap+"/"+c.page+"?_="+e,headers:{"X-Atmosphere-Transport":"long-polling"},callback:function(d){c.paused||a(d.responseText),setTimeout(function(){b()},1)},error:function(){setTimeout(function(){b()},1e3)}})}y.call(this);var c=this;c.sitemap=document.body.getAttribute("data-sitemap"),c.page=document.body.getAttribute("data-page-id"),c.navigate=function(a){c.request.abort(),c.page=a,b()},c.pause=function(){c.request.abort(),c.paused=!0},c.resume=function(){c.paused=!1,b()},b()}function B(){function b(a){E.eventSource?z.call(f,a):A.call(f)}function c(){f.startSubscriber(f.subscribeResponse),f.subscribeResponse=null}var f=this;f.subscribeRequestURL="/rest/sitemaps/events/subscribe",f.reconnectInterval=null,f.subscribeResponse=null,f.suppressErrorsState=!1,f.connectionRestored=function(a){clearInterval(f.reconnectInterval),f.navigate=c,f.subscribeResponse=a,D.UI.hideNotification(),D.UI.navigate(D.UI.page,!1)},f.connectionError=function(){if(!f.suppressErrorsState){var b=e(a.notifyTemplateOffline,{});D.UI.showNotification(b),f.reconnectInterval=setInterval(function(){d({url:f.subscribeRequestURL,type:"POST",callback:f.connectionRestored})},1e4)}},f.suppressErrors=function(){f.suppressErrorsState=!0},f.startSubscriber=function(a){var c,d,e,f,g,h;try{c=JSON.parse(a.responseText)}catch(i){return}if("CREATED"===c.status){try{d=c.context.headers.Location[0]}catch(i){return}e=d.split("/"),g=e[e.length-1],f=document.body.getAttribute("data-sitemap"),h=document.body.getAttribute("data-page-id"),D.subscriptionId=g,b(d+"?sitemap="+f+"&pageid="+h)}},d({url:f.subscribeRequestURL,type:"POST",callback:f.startSubscriber})}var C,D={dataModel:{}},E={eventLayerXY:function(){var a;return a=void 0===document.createEvent?new MouseEvent(null):document.createEvent("MouseEvent"),void 0!==a.layerX}(),pointerEvents:void 0!==document.createElement("div").style.pointerEvents,customEvent:function(){var a=!0;try{new CustomEvent("event",{})}catch(b){a=!1}return a}(),elementRemove:void 0!==Element.prototype.remove,flexbox:void 0!==document.createElement("div").style.flexBasis,flexboxLegacy:function(){var a=document.createElement("div");return void 0!==a.style.boxDirection||void 0!==a.style.webkitBoxDirection}(),eventSource:"EventSource"in window};!function(){C=E.customEvent?function(a,b){return new CustomEvent(a,{detail:b})}:function(a,b){var c=document.createEvent("CustomEvent");return c.initCustomEvent(a,!0,!1,b),c},E.elementRemove||(Element.prototype.remove=function(){this.parentNode.removeChild(this)}),!E.flexbox&&E.flexboxLegacy&&document.documentElement.classList.add("flexbox-legacy")}(),r.hsv2rgb=function(a){var b,c,d,e=a.h,f=a.s,g=a.v,h=Math.floor(6*e),i=6*e-h,j=g*(1-f),k=g*(1-i*f),l=g*(1-(1-i)*f);switch(h%6){case 0:b=g,c=l,d=j;break;case 1:b=k,c=g,d=j;break;case 2:b=j,c=g,d=l;break;case 3:b=j,c=k,d=g;break;case 4:b=l,c=j,d=g;break;case 5:b=g,c=j,d=k}return{r:255*b,g:255*c,b:255*d}},document.addEventListener("DOMContentLoaded",function(){D.UI=new x(document),D.UI.layoutChangeProxy=new i(100,50),D.UI.initControls(),D.changeListener=new B,window.addEventListener("beforeunload",function(){D.changeListener.suppressErrors()})})}({itemAttribute:"data-item",idAttribute:"data-widget-id",iconAttribute:"data-icon",iconTypeAttribute:"data-icon-type",controlButton:"button",buttonActiveClass:"mdl-button--accent",modal:".mdl-modal",modalContainer:".mdl-modal__content",selectionRows:".mdl-form__selection-rows",form:".mdl-form",formTitle:".mdl-form__title",formHidden:"mdl-form--hidden",formControls:".mdl-form__control",formRowHidden:"mdl-form__row--hidden",formValue:".mdl-form__value",formRadio:".mdl-radio",formRadioControl:".mdl-radio__button",formIcon:".mdl-form__icon img",formLabel:".mdl-form__label",uiLoadingBar:".ui__loading",layoutTitle:".mdl-layout-title",layoutHeader:".mdl-layout__header",backButton:".navigation__button-back",rollerblind:{up:".mdl-form__rollerblind--up",down:".mdl-form__rollerblind--down",stop:".mdl-form__rollerblind--stop"},setpoint:{up:".mdl-form__setpoint--up",down:".mdl-form__setpoint--down"},colorpicker:{up:".mdl-form__colorpicker--up",down:".mdl-form__colorpicker--down",pick:".mdl-form__colorpicker--pick",modalClass:"mdl-modal--colorpicker",image:".colorpicker__image",handle:".colorpicker__handle",slider:".colorpicker__brightness",background:".colorpicker__background",colorpicker:".colorpicker",button:".colorpicker__buttons > button"},notify:".mdl-notify__container",notifyHidden:"mdl-notify--hidden",notifyTemplateOffline:"template-offline-notify"});
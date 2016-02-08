!function(a){"use strict";function b(a){var b=document.createElement("div");return b.innerHTML=a,b.childNodes}function c(a,b){b instanceof NodeList?[].slice.call(b).forEach(function(b){a.appendChild(b)}):a.appendChild(b)}function d(a){var b=a,c="undefined"!=typeof b.type?b.type:"GET",d="undefined"!=typeof b.data?b.data:"",e="undefined"!=typeof b.headers?b.headers:{},f=new XMLHttpRequest;f.open(c,b.url,!0);for(var g in e)f.setRequestHeader(g,e[g]);return f.onload=function(){return f.status<200||f.status>400?void("function"==typeof b.error&&b.error(f)):void("function"==typeof b.callback&&b.callback(f))},f.onerror=function(){"function"==typeof b.error&&b.error(f)},f.send(d),f}function e(a,b){var c=document.getElementById(a).innerHTML;return c.replace(/\{([\w]+)\}/,function(a,c){return"undefined"!=typeof b[c]?b[c]:""})}function f(){var a=this;a.level=0,a.push=function(b){a.level++,history.pushState({page:b},document.title.textContent)},a.replace=function(a,b){history.replaceState({page:a},document.title.textContent,b)},window.addEventListener("popstate",function(b){a.level--,B.UI.navigate(b.state.page,!1)},!1)}function g(d){function f(a){a=a||window.event,27===a.keyCode&&i.hide()}function g(){document.addEventListener("keydown",f)}function h(){document.removeEventListener("keydown",f)}var i=this;i.templateId="template-modal",i.text=e(i.templateId,{content:d}),i.show=function(){c(document.body,b(i.text)),i.bg=document.querySelector(a.modal),i.container=i.bg.querySelector(a.modalContainer),B.UI.currentModal=i,i.bg.addEventListener("click",function(){i.hide()}),i.container.addEventListener("click",function(a){a.stopPropagation()}),g()},i.hide=function(){document.body.querySelector(a.modal).remove(),delete B.UI.currentModal,h()}}function h(a,b){var c,d=this,e=!1;d.lock=!1,d.call=function(){c=arguments,d.lock||(e=!1,a.apply(null,c),d.lock=!0,setTimeout(function(){e||a.apply(null,c),d.lock=!1},b))},d.finish=function(){e=!0}}function i(b){var c=this,d=!1;c.parentNode=b,c.item=c.parentNode.getAttribute(a.itemAttribute),c.id=c.parentNode.getAttribute(a.idAttribute),c.icon=c.parentNode.parentNode.querySelector(a.formIcon),null!==c.icon&&(c.iconName=c.icon.getAttribute(a.iconAttribute)),c.reloadIcon=function(a){null!==c.icon&&c.icon.setAttribute("src","/icon/"+c.iconName+"?state="+a+"&format="+B.UI.iconType)},c.setValue=function(a){c.reloadIcon(a),d?d=!1:c.setValuePrivate(a)},c.setValuePrivate=function(){},c.supressUpdate=function(){d=!0}}function j(a){var b=this;if(b.image=a.querySelector("img"),b.updateInterval=parseInt(a.getAttribute("data-update-interval"),10),0!==b.updateInterval){b.url=b.image.getAttribute("src").replace(/\d+$/,"");var c=setInterval(function(){return 0===b.image.clientWidth?void clearInterval(c):void b.image.setAttribute("src",b.url+Math.random().toString().slice(2))},1e3*b.updateInterval)}}function k(a){i.call(this,a);var b=this;b.setValue=function(b){a.innerHTML=b}}function l(b){i.call(this,b);var c=this;c.count=1*c.parentNode.getAttribute("data-count"),c.reset=function(){c.buttons.forEach(function(b){b.classList.remove(a.buttonActiveClass)})},c.onclick=function(){var b=this.getAttribute("data-value")+"";1!==c.count&&(c.reset(),this.classList.add(a.buttonActiveClass)),c.parentNode.dispatchEvent(A("control-change",{item:c.item,value:b})),c.supressUpdate()},c.valueMap={},c.buttons=[].slice.call(c.parentNode.querySelectorAll(a.controlButton)),c.setValuePrivate=function(b){1!==c.count&&(c.reset(),void 0!==c.valueMap&&void 0!==c.valueMap[b]&&c.valueMap[b].classList.add(a.buttonActiveClass))},c.buttons.forEach.call(c.buttons,function(a){var b=a.getAttribute("data-value")+"";c.valueMap[b]=a,a.addEventListener("click",c.onclick)})}function m(b){function c(a){if(a.stopPropagation(),"input"===a.target.tagName.toLowerCase()){var b=a.target.getAttribute("value");d.parentNode.dispatchEvent(A("control-change",{item:d.item,value:b})),setTimeout(function(){d.modal.hide()},300)}}i.call(this,b);var d=this,e=b.getAttribute("data-value-map");d.value=null,d.valueNode=b.parentNode.querySelector(a.formValue),null!==e?d.valueMap=JSON.parse(e):d.valueMap={},d.showModal=function(){var b=d.parentNode.querySelector(a.selectionRows).innerHTML;d.modal=new g(b),d.modal.show();var e=[].slice.call(d.modal.container.querySelectorAll(a.formRadio));if(null!==d.value){var f=[].slice.call(d.modal.container.querySelectorAll("input[type=radio]"));f.forEach(function(a){a.value===d.value?a.checked=!0:a.checked=!1})}e.forEach(function(a){componentHandler.upgradeElement(a,"MaterialRadio"),a.addEventListener("click",c)})},d.setValuePrivate=function(a){d.value=""+a,void 0!==d.valueMap[a]?d.valueNode.innerHTML=d.valueMap[a]:d.valueNode.innerHTML=""},d.parentNode.parentNode.addEventListener("click",d.showModal)}function n(b){function c(a){j.parentNode.dispatchEvent(A("control-change",{item:j.item,value:a}))}function d(a){g=!1,l=!0,h=setTimeout(function(){g=!0,c(a)},k)}function e(a){clearTimeout(h),l&&(l=!1,c(g?"STOP":a))}function f(){c("STOP")}i.call(this,b);var g,h,j=this,k=300,l=!1;j.buttonUp=j.parentNode.querySelector(a.rollerblind.up),j.buttonDown=j.parentNode.querySelector(a.rollerblind.down),j.buttonStop=j.parentNode.querySelector(a.rollerblind.stop),j.hasValue="true"===j.parentNode.getAttribute("data-has-value"),j.valueNode=j.parentNode.parentNode.querySelector(a.formValue),j.setValuePrivate=function(a){j.hasValue&&("DOWN"===a?a="100":"UP"===a&&(a="0"),j.valueNode.innerHTML=a)};var m=e.bind(null,"UP"),n=d.bind(null,"UP"),o=e.bind(null,"DOWN"),p=d.bind(null,"DOWN");j.buttonUp.addEventListener("touchstart",n),j.buttonUp.addEventListener("mousedown",n),j.buttonUp.addEventListener("touchend",m),j.buttonUp.addEventListener("mouseup",m),j.buttonUp.addEventListener("mouseleave",m),j.buttonDown.addEventListener("touchstart",p),j.buttonDown.addEventListener("mousedown",p),j.buttonDown.addEventListener("touchend",o),j.buttonDown.addEventListener("mouseup",o),j.buttonDown.addEventListener("mouseleave",o),j.buttonStop.addEventListener("mousedown",f),j.buttonStop.addEventListener("touchstart",f)}function o(b){function c(a,b){var c=d.value+(a===!0?d.step:-d.step);c=c>d.max?d.max:c,c=c<d.min?d.min:c,d.parentNode.dispatchEvent(A("control-change",{item:d.item,value:c})),d.value=c,b.stopPropagation(),b.preventDefault()}i.call(this,b);var d=this;d.up=d.parentNode.querySelector(a.setpoint.up),d.down=d.parentNode.querySelector(a.setpoint.down),d.value=d.parentNode.getAttribute("data-value"),d.max=parseFloat(d.parentNode.getAttribute("data-max")),d.min=parseFloat(d.parentNode.getAttribute("data-min")),d.step=parseFloat(d.parentNode.getAttribute("data-step")),d.value=isNaN(parseFloat(d.value))?0:parseFloat(d.value),d.valueNode=d.parentNode.parentNode.querySelector(a.formValue),d.setValuePrivate=function(a){d.value=1*a,d.valueNode.innerHTML=a};var e=c.bind(null,!0),f=c.bind(null,!1);d.up.addEventListener("mousedown",e),d.up.addEventListener("touchstart",e),d.down.addEventListener("mousedown",f),d.down.addEventListener("touchstart",f)}function p(b,c,d){function e(a){var b=a.r,c=a.g,d=a.b;b/=255,c/=255,d/=255;var e,f,g=Math.max(b,c,d),h=Math.min(b,c,d),i=g,j=g-h;if(f=0===g?0:j/g,g===h)e=0;else{switch(g){case b:e=(c-d)/j+(d>c?6:0);break;case c:e=(d-b)/j+2;break;case d:e=(b-c)/j+4}e/=6}return{h:e,s:f,v:i}}function f(a){var b=a.h,c=a.s,d=a.v,e=(2-c)*d,f=1>e?e:2-e;return{h:b,s:0===f?0:c*d/f,l:e/2}}function g(a){var b;b=void 0!==a.touches?{x:a.touches[0].pageX-q.colorpicker.offsetLeft,y:a.touches[0].pageY-q.colorpicker.offsetTop}:C.eventLayerXY&&C.pointerEvents?{x:a.layerX,y:a.layerY}:{x:a.pageX-q.colorpicker.offsetLeft,y:a.pageY-q.colorpicker.offsetTop};var c=q.image.clientWidth/2,d=b.x-c,e=b.y-c,g=e*e+d*d;if(g>c*c){var h=1-Math.abs(c/Math.sqrt(g));b.x-=d*h,b.y-=e*h}q.handle.style.left=b.x/q.image.clientWidth*100+"%",q.handle.style.top=b.y/q.image.clientWidth*100+"%";var i=d>=0?(2*Math.PI-Math.atan(e/d)+Math.PI/2)/(2*Math.PI):(2*Math.PI-Math.atan(e/d)-Math.PI/2)/(2*Math.PI),j={h:isNaN(i)?0:i,s:Math.sqrt(g)/c,v:1},k=f(j);q.hsvValue={h:j.h,s:j.s,v:q.slider.value/100},k.l=k.l<.5?.5:k.l,q.background.style.background="hsl("+360*k.h+", 100%, "+Math.round(100*k.l)+"%)"}function i(a){var b=e(a);q.slider.value=100*b.v;var c=50+Math.round(b.s*Math.cos(2*Math.PI*b.h)*50),d=50+Math.round(b.s*Math.sin(2*Math.PI*b.h)*50);q.handle.style.top=c+"%",q.handle.style.left=d+"%",b.v=1;var f=p.hsv2rgb(b);q.background.style.background="rgb("+Math.round(f.r)+","+Math.round(f.g)+","+Math.round(f.b)+")"}function j(){null!==q.interval&&(clearInterval(q.interval),q.interval=null),window.removeEventListener("mouseup",j)}function k(a){q.interval=setInterval(function(){d(q.hsvValue)},300),B.changeListener.pause(),window.addEventListener("mouseup",j),g(a),d(q.hsvValue),a.stopPropagation()}function l(a){(void 0!==a.touches||1&a.buttons)&&(g(a),a.stopPropagation(),a.preventDefault())}function m(a){null!==q.interval&&(clearInterval(q.interval),q.interval=null),B.changeListener.resume(),window.removeEventListener("mouseup",j),a.stopPropagation()}function n(){q.hsvValue.v=q.slider.value/100,B.changeListener.pause(),q.debounceProxy.call()}function o(){q.hsvValue.v=q.slider.value/100,q.debounceProxy.call(),q.debounceProxy.finish(),B.changeListener.resume()}var q=this;q.container=b,q.value=c,q.hsvValue=e(c),q.interval=null,q.colorpicker=q.container.querySelector(a.colorpicker.colorpicker),q.image=q.container.querySelector(a.colorpicker.image),q.background=q.container.querySelector(a.colorpicker.background),q.handle=q.container.querySelector(a.colorpicker.handle),q.slider=q.container.querySelector(a.colorpicker.slider),q.button=q.container.querySelector(a.controlButton),componentHandler.upgradeElement(q.button,"MaterialButton"),componentHandler.upgradeElement(q.button,"MaterialRipple"),q.debounceProxy=new h(function(){d(q.hsvValue)},200),q.slider.addEventListener("change",n),q.slider.addEventListener("touchend",o),q.slider.addEventListener("mouseup",o),q.image.addEventListener("mousedown",l),q.image.addEventListener("mousemove",l),q.image.addEventListener("touchmove",l),q.image.addEventListener("touchstart",l),q.image.addEventListener("touchend",m),q.image.addEventListener("mouseup",m),q.image.addEventListener("mousedown",k),q.image.addEventListener("touchstart",k),i(c)}function q(b){function c(a){return{r:parseInt(a.substr(1,2),16),g:parseInt(a.substr(3,2),16),b:parseInt(a.substr(5,2),16)}}function d(a){l.parentNode.dispatchEvent(A("control-change",{item:l.item,value:a}))}function f(a){k=setInterval(function(){d(a)},m)}function h(){clearInterval(k)}function j(){l.modal=new g(e("template-colorpicker")),l.modal.show(),l.modal.container.classList.add(a.colorpicker.modalClass),l.modalControl=new p(l.modal.container,l.value,function(a){l.value=p.hsv2rgb(a),d(Math.round(360*a.h%360)+","+Math.round(100*a.s%100)+","+Math.round(100*a.v))}),l.modal.container.querySelector(a.colorpicker.button).addEventListener("click",function(){l.modal.hide()})}i.call(this,b);var k,l=this,m=300;l.value=c(l.parentNode.getAttribute("data-value")),l.buttonUp=l.parentNode.querySelector(a.colorpicker.up),l.buttonDown=l.parentNode.querySelector(a.colorpicker.down),l.buttonPick=l.parentNode.querySelector(a.colorpicker.pick),l.setValue=function(a){var b=a.split(","),c={h:b[0]/360,s:b[1]/100,v:b[2]/100};l.value=p.hsv2rgb(c)};var n=f.bind(null,"INCREASE"),o=f.bind(null,"DECREASE");l.buttonUp.addEventListener("touchstart",n),l.buttonUp.addEventListener("mousedown",n),l.buttonUp.addEventListener("mouseleave",h),l.buttonUp.addEventListener("touchend",h),l.buttonUp.addEventListener("mouseup",h),l.buttonDown.addEventListener("touchstart",o),l.buttonDown.addEventListener("mousedown",o),l.buttonDown.addEventListener("touchend",h),l.buttonDown.addEventListener("mouseup",h),l.buttonDown.addEventListener("mouseleave",h),l.buttonPick.addEventListener("click",j)}function r(a){i.call(this,a);var b=this;b.input=b.parentNode.querySelector("input[type=checkbox]"),b.input.addEventListener("change",function(){b.parentNode.dispatchEvent(A("control-change",{item:b.item,value:b.input.checked?"ON":"OFF"})),b.supressUpdate()}),b.setValuePrivate=function(a){var c="ON"===a;b.input.checked=c,c?b.parentNode.MaterialSwitch.on():b.parentNode.MaterialSwitch.off()}}function s(a){function b(){e.parentNode.dispatchEvent(A("control-change",{item:e.item,value:e.input.value})),e.supressUpdate()}function c(){null!==f&&clearTimeout(f),e.locked=!0,B.changeListener.pause()}function d(){e.debounceProxy.call(),setTimeout(function(){B.changeListener.resume()},5),f=setTimeout(function(){e.locked=!1},300)}i.call(this,a);var e=this;e.input=e.parentNode.querySelector("input[type=range]"),e.locked=!1,function(){var a=parseInt(e.input.getAttribute("data-state"),10);isNaN(a)?e.input.value=0:e.input.value=a,e.input.MaterialSlider&&e.input.MaterialSlider.change()}(),e.debounceProxy=new h(function(){b()},200),e.input.addEventListener("change",function(){e.debounceProxy.call()}),e.setValuePrivate=function(a){return e.locked?void e.reloadIcon(a):(e.input.value=a,void e.input.MaterialSlider.change())};var f=null;e.input.addEventListener("touchstart",c),e.input.addEventListener("mousedown",c),e.input.addEventListener("touchend",d),e.input.addEventListener("mouseup",d)}function t(b){i.call(this,b);var c=this;c.target=b.getAttribute("data-target"),c.container=b.parentNode.querySelector(a.formValue),c.setValuePrivate=function(a){null!==c.container&&(c.container.innerHTML=a)},b.parentNode.addEventListener("click",function(){B.UI.navigate(c.target,!0)})}function u(a){d({type:"POST",url:"/rest/items/"+a.detail.item,data:a.detail.value,headers:{"Content-Type":"text/plain"}})}function v(b){function c(a){document.title=a,h.layoutTitle.innerHTML=a}function e(a){var b=a.documentElement,d=[];if("page"===b.tagName){[].forEach.call(b.childNodes,function(a){a instanceof Text||d.push(a)}),c(d[0].textContent);for(var e=document.querySelector(".page-content");e.firstChild;)e.removeChild(e.firstChild);e.insertAdjacentHTML("beforeend",d[1].textContent)}}var g={Loading:1,Idle:2},h=this,i=g.Idle,p=new f;h.page=document.body.getAttribute("data-page-id"),h.sitemap=document.body.getAttribute("data-sitemap"),h.destination=null,h.root=b,h.loading=h.root.querySelector(a.uiLoadingBar),h.layoutTitle=document.querySelector(a.layoutTitle),h.iconType=document.body.getAttribute(a.iconTypeAttribute),h.upgradeComponents=function(){var b=componentHandler.upgradeElement;[].slice.call(document.querySelectorAll(a.formControls)).forEach(function(a){switch(a.getAttribute("data-control-type")){case"setpoint":case"rollerblind":case"colorpicker":case"buttons":[].slice.call(a.querySelectorAll("button")).forEach(function(a){b(a,"MaterialButton"),b(a,"MaterialRipple")});break;case"checkbox":b(a,"MaterialSwitch");break;case"slider":b(a.querySelector("input[type=range]"),"MaterialSlider")}})},h.showLoadingBar=function(){h.loading.style.display="block"},h.hideLoadingBar=function(){h.loading.style.display=""},h.navigateCallback=function(a){i=g.Idle,h.pushState&&p.push(h.page),e(a.responseXML),h.pushState&&p.replace(h.newPage,h.destination),h.page=h.newPage,h.upgradeComponents(),h.initControls(),h.hideLoadingBar(),h.sitemap!==h.page?(h.header.classList.remove("navigation-home"),h.header.classList.add("navigation-page")):(h.header.classList.add("navigation-home"),h.header.classList.remove("navigation-page")),B.changeListener.navigate(h.page)},h.navigate=function(a,b){i===g.Idle&&(i=g.Loading,h.pushState=void 0===b||b===!0,h.newPage=a,h.showLoadingBar(),h.destination="/basicui/app?w="+a+"&sitemap="+B.UI.sitemap,d({url:h.destination+"&__async=true",callback:h.navigateCallback}),void 0!==B.UI.currentModal&&B.UI.currentModal.hide())},h.initControls=function(){function b(a){(void 0===B.dataModel[a.item]||void 0===B.dataModel[a.item].widgets)&&(B.dataModel[a.item]={widgets:[]}),B.dataModel[a.item].widgets.push(a)}B.dataModel={},[].forEach.call(document.querySelectorAll(a.formControls),function(a){switch(a.getAttribute("data-control-type")){case"setpoint":b(new o(a));break;case"rollerblind":b(new n(a));break;case"buttons":b(new l(a));break;case"selection":b(new m(a));break;case"checkbox":b(new r(a));break;case"slider":b(new s(a));break;case"chart":case"image":new j(a);break;case"image-link":new j(a);case"text-link":case"group":b(new t(a));break;case"text":b(new k(a));break;case"colorpicker":b(new q(a))}a.addEventListener("control-change",u)})},p.replace(h.page,document.location.href),function(){h.header=document.querySelector(a.layoutHeader),h.sitemap!==h.page&&(h.header.classList.remove("navigation-home"),h.header.classList.add("navigation-page")),document.querySelector(a.backButton).addEventListener("click",function(){p.level>0?history.back():location.href=location.origin+"/basicui/app?sitemap="+B.UI.sitemap})}()}function w(){this.paused=!1,this.pause=function(){this.paused=!0},this.resume=function(){this.paused=!1}}function x(){w.call(this);var a=this;a.navigate=function(){},a.source=new EventSource("/rest/events?topics=smarthome/items/*/state"),a.source.addEventListener("message",function(b){if(!a.paused){var c=JSON.parse(b.data),d=JSON.parse(c.payload),e=d.value,f=function(a){return a=a.split("/"),a[a.length-2]}(c.topic);f in B.dataModel&&B.dataModel[f].widgets.forEach(function(a){a.setValue(e)})}})}function y(){function a(a){function b(a){a.forEach(function(a){if(void 0!==a.item){var b=a.item.name,c=a.item.state;B.dataModel[b].widgets.forEach(function(a){"NULL"!==c&&a.setValue(c)})}})}try{a=JSON.parse(a)}catch(c){return}a.leaf?b(a.widgets):a.widgets.forEach(function(a){b(a.widgets)})}function b(){var e=Math.random().toString(16).slice(2);c.request=d({url:"/rest/sitemaps/"+c.sitemap+"/"+c.page+"?_="+e,headers:{"X-Atmosphere-Transport":"long-polling"},callback:function(d){c.paused||a(d.responseText),setTimeout(function(){b()},1)},error:function(){setTimeout(function(){b()},1e3)}})}w.call(this);var c=this;c.sitemap=document.body.getAttribute("data-sitemap"),c.page=document.body.getAttribute("data-page-id"),c.navigate=function(a){c.request.abort(),c.page=a,b()},c.pause=function(){c.request.abort(),c.paused=!0},c.resume=function(){c.paused=!1,b()},b()}function z(){C.eventSource?x.call(this):y.call(this)}var A,B={dataModel:{}},C={eventLayerXY:function(){var a;return a=void 0===document.createEvent?new MouseEvent(null):document.createEvent("MouseEvent"),void 0!==a.layerX}(),pointerEvents:void 0!==document.createElement("div").style.pointerEvents,customEvent:function(){var a=!0;try{new CustomEvent("event",{})}catch(b){a=!1}return a}(),elementRemove:void 0!==Element.prototype.remove,flexbox:void 0!==document.createElement("div").style.flexBasis,flexboxLegacy:function(){var a=document.createElement("div");return void 0!==a.style.boxDirection||void 0!==a.style.webkitBoxDirection}(),eventSource:"EventSource"in window};!function(){A=C.customEvent?function(a,b){return new CustomEvent(a,{detail:b})}:function(a,b){var c=document.createEvent("CustomEvent");return c.initCustomEvent(a,!0,!1,b),c},C.elementRemove||(Element.prototype.remove=function(){this.parentNode.removeChild(this)}),!C.flexbox&&C.flexboxLegacy&&document.documentElement.classList.add("flexbox-legacy")}(),p.hsv2rgb=function(a){var b,c,d,e=a.h,f=a.s,g=a.v,h=Math.floor(6*e),i=6*e-h,j=g*(1-f),k=g*(1-i*f),l=g*(1-(1-i)*f);switch(h%6){case 0:b=g,c=l,d=j;break;case 1:b=k,c=g,d=j;break;case 2:b=j,c=g,d=l;break;case 3:b=j,c=k,d=g;break;case 4:b=l,c=j,d=g;break;case 5:b=g,c=j,d=k}return{r:255*b,g:255*c,b:255*d}},document.addEventListener("DOMContentLoaded",function(){B.UI=new v(document),B.UI.initControls(),B.changeListener=new z})}({itemAttribute:"data-item",idAttribute:"data-id",iconAttribute:"data-icon",iconTypeAttribute:"data-icon-type",controlButton:"button",buttonActiveClass:"mdl-button--accent",modal:".mdl-modal",modalContainer:".mdl-modal__content",selectionRows:".mdl-form__selection-rows",formControls:".mdl-form__control",formValue:".mdl-form__value",formRadio:".mdl-radio",formRadioControl:".mdl-radio__button",formIcon:".mdl-form__icon img",uiLoadingBar:".ui__loading",layoutTitle:".mdl-layout-title",layoutHeader:".mdl-layout__header",backButton:".navigation__button-back",rollerblind:{up:".mdl-form__rollerblind--up",down:".mdl-form__rollerblind--down",stop:".mdl-form__rollerblind--stop"},setpoint:{up:".mdl-form__setpoint--up",down:".mdl-form__setpoint--down"},colorpicker:{up:".mdl-form__colorpicker--up",down:".mdl-form__colorpicker--down",pick:".mdl-form__colorpicker--pick",modalClass:"mdl-modal--colorpicker",image:".colorpicker__image",handle:".colorpicker__handle",slider:".colorpicker__brightness",background:".colorpicker__background",colorpicker:".colorpicker",button:".colorpicker__buttons > button"}});
// Version 0.3
function getLiferayFacesResponseNameSpace() {
	var forms = document.getElementsByTagName('form');
	var result = '';
	if (forms.length > 0) {
		var formId = forms[0].id;
		result = formId.substring(0, formId.indexOf(':'));
	}
	return result;
}
function getPortletElement(id) {
	var portletElementId;
	if (id.charAt(0) == ':') portletElementId = getLiferayFacesResponseNameSpace() + id;
	else portletElementId = getLiferayFacesResponseNameSpace() + ':' + id;
	return document.getElementById(portletElementId);
}
function isElementEnabled(id) {
	var el = getPortletElement(id);
	if (!el) return false;
	var attr = el.getAttribute('disabled');
	var disabled = attr && attr.length > 0;
	return !disabled;
}
function gotoPortletElement(id, settings) {
	var portletElementId;
	if (id.charAt(0) == ':') portletElementId = getLiferayFacesResponseNameSpace() + id;
	else portletElementId = getLiferayFacesResponseNameSpace() + ':' + id;
	var el = document.getElementById(portletElementId);
	if (el) $('html, body').scrollTo(el, 500, settings);
}
function expandSelectOptions(element) {
	var supportsMouseEventConstructors = document.implementation.hasFeature("MouseEvent","4.0");
	var event;
	if (supportsMouseEventConstructors) {
		event = new MouseEvent('mousedown', {bubbles: true, cancelable: true});
	} else {
		event = document.createEvent('MouseEvents');
		event.initMouseEvent('mousedown', true, true, window);
	}
	element.dispatchEvent(event);
};
function viewport() {
	var e = window, a = 'inner';
	if (!('innerWidth' in window)) {
		a = 'client';
		e = document.documentElement || document.body;
	}
	return { width: e[a+'Width'], height: e[a+'Height'] }
}
function getPosition(element) {
	var xPosition = 0;
	var yPosition = 0;
	while(element) {
		xPosition += (element.offsetLeft - element.scrollLeft + element.clientLeft);
		yPosition += (element.offsetTop - element.scrollTop + element.clientTop);
		element = element.offsetParent;
	}
	return { x: xPosition, y: yPosition };
}
function forceRedraw(element){
	var oh = element.offsetHeight;
}
function forceRedrawEx(element){
	if (!element) return;
	var n = document.createTextNode(' ');
	var disp = element.style.display;
	element.appendChild(n);
	element.style.display = 'none';
	setTimeout(function(){
		element.style.display = disp;
		n.parentNode.removeChild(n);
	},24); // you can play with this timeout to make it as short as possible
}


/*for pickers*/
function removeClassName(elem, className) {
	elem.className = elem.className.replace(className, "").trim();
}

function addCSSClass(elem, className) {
	removeClassName(elem, className);
	elem.className = (elem.className + " " + className).trim();
}

function hasClass(element, cls) {
	return (' ' + element.className + ' ').indexOf(' ' + cls + ' ') > -1;
}

String.prototype.trim = function () {
	return this.replace(/^\s+|\s+$/, "");
};

function getOffsetRect (elem) {
	// (1)
	var box = elem.getBoundingClientRect();

	var body = document.body;
	var docElem = document.documentElement;

	// (2)
	var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop;
	var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft;

	// (3)
	var clientTop = docElem.clientTop || body.clientTop || 0;
	var clientLeft = docElem.clientLeft || body.clientLeft || 0;

	// (4)
	var top = box.top + scrollTop - clientTop;
	var left = box.left + scrollLeft - clientLeft;

	return {top: Math.round(top), left: Math.round(left)};
}

function getPageHeight(){
	var body = document.body,
			html = document.documentElement;

	var height = Math.max( body.scrollHeight, body.offsetHeight,
			html.clientHeight, html.scrollHeight, html.offsetHeight );
	return height;
}

function getPageWidth(){
	var body = document.body,
		html = document.documentElement;

	var width = Math.max( body.scrollWidth, body.offsetWidth,
		html.clientWidth, html.scrollWidth, html.offsetWidth );
	return width;
}
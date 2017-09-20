function Picker() {
    var self = this;
    this._el = this._findEl();
    document.body.appendChild(this._el);

    Object.defineProperty(this, "el", {
        get: function () {
            return this._el;
        }
    });

    var selectButton = this._el.querySelector(".select-button");
    selectButton.onclick = function () {
        self._flash();
    };
    var cancelButton = this._el.querySelector(".cancel-button");
    cancelButton.onclick = function(){
        self._overlay();
    };
    var emptyButton = this._el.querySelector(".empty-button");
    emptyButton.onclick = function () {
        self._flash(true);
    };
}

Picker.prototype.show = function (key, boundElem) {
    this._initContext.apply(this, arguments);

    this._onShow();

    this._overlay();
    /*var width = boundElem.offsetWidth;
    if (width < 600) width = 600;
    this._el.querySelector(".picker").style.width = width + "px";*/
};

Picker.prototype._initContext = function (key, boundElem) {
    this._key = key;
    this._boundElem = boundElem;
};

Picker.prototype._onShow = function () {
};

Picker.prototype.setValue = function (value) {
};

Picker.prototype._flash = function () {
    this._overlay();
};

Picker.prototype._findEl = function () {
    return document.getElementById(this._getElId());
};

Picker.prototype._getElId = function () {
    return "id";
};

Picker.prototype._overlay = function () {
    var el = this._findEl();
    if (el) {
        if (el.style.visibility == "visible") {
            el.style.visibility = "hidden";
        } else {
            el.style.visibility = "visible";
        }
    }
};

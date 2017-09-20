function Picker2() {
    var self = this;
    this._el = this._findEl();
    document.body.appendChild(this._el);
    this.container = null;
    this.rowId = null;

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

Picker2.prototype.show = function () {
    this._initContext.apply(this, arguments);

    this._onShow();

    this._overlay();
};

Picker2.prototype._initContext = function (key, boundElem) {
    this._key = key;
    this.container = key.substring(0, key.indexOf("*"));
    this.rowId = key.substring(key.lastIndexOf(":") + 1);
    this._boundElem = boundElem;
};

Picker2.prototype._onShow = function () {
};

Picker2.prototype.setValue = function (value) {
};

Picker2.prototype._flash = function () {
    this._overlay();
};

Picker2.prototype._findEl = function () {
    return document.getElementById(this._getElId());
};

Picker2.prototype._getElId = function () {
    return "id";
};

Picker2.prototype._overlay = function () {
    var el = this._findEl();
    if (el) {
        if (el.style.visibility == "visible") {
            el.style.visibility = "hidden";
        } else {
            el.style.visibility = "visible";
        }
    }
};

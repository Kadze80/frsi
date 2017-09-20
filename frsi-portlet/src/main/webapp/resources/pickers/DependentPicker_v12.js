function DependentPicker() {
    Picker2.call(this);

    this._allItems = [];
    this._items = [];
    this._filterField = null;
}

DependentPicker.prototype = Object.create(Picker2.prototype);
DependentPicker.prototype.constructor = DependentPicker;

DependentPicker.prototype.loadData = function (items) {
    this._allItems = items;
};

DependentPicker.prototype._initContext = function (key, boundElem) {
    Picker2.prototype._initContext.apply(this, arguments);
    var hiddenInput = document.getElementById(key);
    this._filterField = hiddenInput.getAttribute("data-filter-field");

    var th = this;
    if (this._filterField !== null && this._key.indexOf("$D.") > -1) {
        var parentKey = this._key.substring(0, this._key.lastIndexOf('.'));
        var rowId = parentKey.substring(parentKey.indexOf("$D."));
        if (rowId.lastIndexOf(".") === rowId.indexOf(".")) {
            parentKey = parentKey.substring(0, parentKey.indexOf("$D.")) + rowId.substring(rowId.indexOf(".") + 1);
        }
        var parentEl = document.getElementById(parentKey);
        var value = parseInt(parentEl.value);
        if (!parentEl) {
            this._items = this._allItems;
        } else {
            this._items = this._allItems.filter(function (rec, i) {
                return rec[th._filterField] === value;
            });
        }
    } else {
        this._items = this._allItems;
    }
};

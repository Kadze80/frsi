function Table(struct, parentEl) {
    this.struct = struct;
    this._tableEl = parentEl.querySelector(".picker-table");
    this._data = [];

    Object.defineProperty(this, "tableEl", {
        get: function () {
            return this._tableEl;
        }
    });
}

Table.prototype.loadData = function (data) {
    var self = this;
    self.clearData();
    self._data = data;
    var tbodyEl = self._tableEl.querySelector('tbody');
    for (var i = 0; i < data.length; i++) {
        var rec = data[i];

        var trEl = document.createElement("tr");
        trEl.onclick = function (e) {
            var t = e.target || e.srcElement;
            if (t) {
                var tr = t.tagName.toLowerCase() === 'td' ? t.parentNode : t;
                self._setSelected(tr);
            }
        };

        for (var colIndex = 0; colIndex < self.struct.columns.length; colIndex++) {
            var col = self.struct.columns[colIndex];
            var tdEl = document.createElement("td");
            if (col.hidden === true) {
                tdEl.className = "hidden-column"
            }
            var value = rec[col.name];
            if (value) {
                tdEl.appendChild(this._createCell(col, value));
            }
            trEl.appendChild(tdEl);

            if (i % 2 == 0) {
                removeClassName(trEl, 'alternateRow');
                addCSSClass(trEl, 'normalRow');
            } else {
                removeClassName(trEl, 'normalRow');
                addCSSClass(trEl, 'alternateRow');
            }
        }
        tbodyEl.appendChild(trEl);
    }
    if (self.onDataLoad) {
        self.onDataLoad(data);
    }
};

Table.prototype.clearData = function () {
    var tbodyEl = this._tableEl.querySelector('tbody');
    if (tbodyEl) {
        while (tbodyEl.firstChild) {
            tbodyEl.removeChild(tbodyEl.firstChild);
        }
    }
    this._data = [];
};

Table.prototype.selectByKeyValue = function (keyValue) {
    var trEl = this._findTrElByKeyValue(keyValue);
    if (trEl == null)
        this._clearSelection();
    else
        this._setSelected(trEl);
};

Table.prototype.getSelectedRec = function () {
    var selectedTrEl = this._tableEl.querySelector('tbody tr.selected-row');
    if (selectedTrEl) {
        return this._findRecByTrEl(selectedTrEl);
    } else {
        return null;
    }
};

Table.prototype._findTrElByKeyValue = function (keyValue) {
    var keyColumnIndex = this._getKeyColumnIndex();
    if (keyColumnIndex != null) {
        var tbodyEl = this._tableEl.querySelector("tbody");
        var trElements = tbodyEl.childNodes;
        for (var i = 0; i < trElements.length; i++) {
            var trEl = trElements[i];
            var tdEl = trEl.childNodes.item(keyColumnIndex);
            if (tdEl.childNodes.length > 0 && tdEl.childNodes[0].nodeValue == keyValue) {
                return trEl;
            }
        }
    }
    return null;
};

Table.prototype._createCell = function (col, value) {
    return document.createTextNode(value);
};

Table.prototype._setSelected = function (trEl) {
    this._clearSelection();
    addCSSClass(trEl, "selected-row");
    if (this.onSelect) {
        var rec = this._findRecByTrEl(trEl);
        this.onSelect(rec, trEl);
    }
};

Table.prototype._clearSelection = function () {
    var selectedRowEl = this._tableEl.querySelector("tr.selected-row");
    if (selectedRowEl != null) {
        removeClassName(selectedRowEl, "selected-row");
    }
};

Table.prototype._findRecByTrEl = function (trEl) {
    var keyColumnIndex = this._getKeyColumnIndex();
    if (keyColumnIndex == -1) {
        return null;
    }
    var children = trEl.childNodes;
    var keyValue = null;
    if (children.length >= keyColumnIndex && children[keyColumnIndex].childNodes.length > 0) {
        keyValue = children[keyColumnIndex].childNodes[0].nodeValue;
    }
    if (keyValue !== null) {
        return this._getRecByKeyValue(keyValue);
    } else {
        return null;
    }

};

Table.prototype._getKeyColumnIndex = function () {
    var index = -1;
    for (var i = 0; i < this.struct.columns.length; i++) {
        var col = this.struct.columns[i];
        index++;
        if (col.key === true) {
            return index;
        }
    }
    return -1;
};

Table.prototype._getRecByKeyValue = function (keyValue) {
    var keyColumnIndex = this._getKeyColumnIndex();
    if (keyColumnIndex == -1) {
        return;
    }
    var keyColumn = this.struct.columns[keyColumnIndex];
    for (var i = 0; i < this._data.length; i++) {
        var rec = this._data[i];
        if (rec[keyColumn.name] == keyValue) {
            return rec;
        }
    }
    return null;
};

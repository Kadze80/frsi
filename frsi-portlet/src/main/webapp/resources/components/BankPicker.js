function BankPicker() {
    Picker.apply(this, arguments);

    this._data = [];
    this._currentValue = null;
    this._struct = {
        columns: [
            {
                name: 'rec_id',
                title: 'REC_ID',
                key: true,
                hidden: true
            },
            {
                name: 'name_ru',
                title: 'Нименование'
            }
        ]
    };

    this._table = this._initTable();

    this._initFilter();
}

BankPicker.prototype = Object.create(Picker.prototype);
BankPicker.prototype.constructor = Picker;

BankPicker.prototype.loadData = function (ref_data) {
    this._data = ref_data;
    this._table.loadData(this._data);
};

BankPicker.prototype.setValue = function (value) {
    this._currentValue = value;
    this._table.selectByKeyValue(value);
};

BankPicker.prototype._onShow = function () {
    this._clearFilter();
};

BankPicker.prototype._flash = function (empty) {
    var key = "", caption = "";
    if(!empty) {
        var selectedRec = this._table.getSelectedRec();

        if (selectedRec) {
            key = selectedRec[this._boundElem.getAttribute("refCode")];
            caption = selectedRec[this._boundElem.getAttribute("refCaption")];
        }
    }


    if (this._hiddenInput && this._boundElem) {
        this._hiddenInput.value = key;
        this._boundElem.value = caption;

        if (this._boundElem.tagName.toUpperCase() === 'TEXTAREA') {
            textAreaAdjust(this._boundElem);

            fireChangeEvent(this._hiddenInput);
            fireChangeEvent(this._boundElem);
        }
    }

    this._overlay();

    if (this._boundElem)
        this._boundElem.focus();
};

BankPicker.prototype._initTable = function () {
    var self = this;
    var table = new Table(this._struct, this._el);
    var pickerData = this._el.querySelector(".picker-data");
    pickerData.appendChild(table.tableEl);
    table.onSelect = function (rec, trEl) {
        var selectButton = self._el.querySelector(".select-button");
        selectButton.disabled = false;
    };
    table.onDataLoad = function (data) {
        var selectButton = self._el.querySelector(".select-button");
        selectButton.disabled = true;
    };
    return table;
};

BankPicker.prototype._initFilter = function () {
    var self = this;
    var bankNameInput = this._el.querySelector('#filter_bank_name');
    bankNameInput.onkeyup = function () {
        self._filter(bankNameInput.value);
    };
};

BankPicker.prototype._filter = function (filterValue) {
    var filteredData;
    if (filterValue) {
        filteredData = this._data.filter(function (rec) {
            if (!rec.name_ru) {
                return false;
            }
            return rec.name_ru.toLowerCase().indexOf(filterValue.toLowerCase()) > -1;
        });
    } else {
        filteredData = this._data;
    }
    this._table.loadData(filteredData);
    if (this._currentValue != null)
        this._table.selectByKeyValue(this._currentValue);
};

BankPicker.prototype._clearFilter = function () {
    var bankNameInput = this._el.querySelector('#filter_bank_name');
    bankNameInput.value = "";
    var selectButton = this._el.querySelector(".select-button");
    selectButton.disabled = true;

    this._table.loadData(this._data);
};

BankPicker.prototype._getElId = function () {
    return "picker-bank";
};

BankPicker.prototype._initContext = function (key, textarea, hiddenInput) {
    Picker.prototype._initContext.apply(this, arguments);
    this._hiddenInput = hiddenInput;
};

function pickBank(key) {
    var textarea = document.getElementById("caption:" + key);
    var hiddenInput = document.getElementById(key);

    if (textarea != null && hiddenInput != null) {
        bankPicker.show(key, textarea, hiddenInput);
        bankPicker.setValue(hiddenInput.value);
    }
}

var bankPicker;
if (reference_data.hasOwnProperty("bank")) {
    bankPicker = new BankPicker();
    bankPicker.loadData(reference_data.bank);
}

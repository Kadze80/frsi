function BankPicker2() {
    Picker2.apply(this, arguments);

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

BankPicker2.prototype = Object.create(Picker2.prototype);
BankPicker2.prototype.constructor = Picker2;

BankPicker2.prototype.loadData = function (ref_data) {
    this._data = ref_data;
    this._table.loadData(this._data);
};

BankPicker2.prototype.setValue = function (value) {
    this._currentValue = value;
    this._table.selectByKeyValue(value);
};

BankPicker2.prototype._onShow = function () {
    this._clearFilter();
};

BankPicker2.prototype._flash = function (empty) {
    var selectedRec = {
        name_ru:"",rec_id:""
    };
    if(!empty){
        selectedRec = this._table.getSelectedRec();
    }

    /*if (selectedRec) {
        key = selectedRec[this._boundElem.getAttribute("refCode")];
        caption = selectedRec[this._boundElem.getAttribute("refCaption")];
    }


    if (this._hiddenInput && this._boundElem) {
        this._hiddenInput.value = key;
        this._boundElem.value = caption;

        if (this._boundElem.tagName.toUpperCase() === 'TEXTAREA') {
            textAreaAdjust(this._boundElem);

            fireChangeEvent(this._hiddenInput);
            fireChangeEvent(this._boundElem);
        }
    }*/

    var adapter = {name_ru: selectedRec.name_ru, "name_ru.rec_id": selectedRec.rec_id};

    var inputs = document.querySelectorAll('[data-receive^="ref_bank."][id*="' + this.container + '*"][id$=":' + this.rowId + '"]');
    for (var i = 0; i < inputs.length; i++) {
        var input = inputs[i];

        var dataValueSender = input.getAttribute("data-receive");
        var fieldName = dataValueSender.substring(dataValueSender.indexOf(".") + 1);

        input.value = adapter[fieldName];
        fireChangeEvent(input);

        if (input.tagName.toUpperCase() === 'TEXTAREA') {
            textAreaAdjust(input);
        }
    }

    this._overlay();

    if (this._boundElem)
        this._boundElem.focus();
};

BankPicker2.prototype._initTable = function () {
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

BankPicker2.prototype._initFilter = function () {
    var self = this;
    var bankNameInput = this._el.querySelector('#filter_bank_name_v2');
    bankNameInput.onkeyup = function () {
        self._filter(bankNameInput.value);
    };
};

BankPicker2.prototype._filter = function (filterValue) {
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

BankPicker2.prototype._clearFilter = function () {
    var bankNameInput = this._el.querySelector('#filter_bank_name_v2');
    bankNameInput.value = "";
    var selectButton = this._el.querySelector(".select-button");
    selectButton.disabled = true;

    this._table.loadData(this._data);
};

BankPicker2.prototype._getElId = function () {
    return "picker-bank-v2";
};

BankPicker2.prototype._initContext = function (key, textarea, hiddenInput) {
    Picker2.prototype._initContext.apply(this, arguments);
    this._hiddenInput = hiddenInput;
};

function pick_bank(key) {
    var textarea = document.getElementById("caption:" + key);
    var hiddenInput = document.getElementById(key);

    if (textarea != null && hiddenInput != null) {
        bankPicker2.show(key, textarea, hiddenInput);
        bankPicker2.setValue(hiddenInput.value);
    }
}

var bankPicker2;
if (reference_data.hasOwnProperty("pick_bank")) {
    bankPicker2 = new BankPicker2();
    bankPicker2.loadData(reference_data.pick_bank);
}

function LegalPersonPicker() {
    Picker.apply(this, arguments);

    this._legalPersons = [];
    this._currentLegalPersonValue = null;
    this._structLegalPerson = {
        columns: [
            {
                name: 'rec_id',
                title: 'REC_ID',
                key: true,
                hidden: true
            },
            {
                name: 'idn',
                title: 'БИН'
            },
            {
                name: 'name_ru',
                title: 'Нименование'
            }
        ]
    };

    this._tableLegalPerson = this._initTable();

    this._initFilterLegalPerson();
}

LegalPersonPicker.prototype = Object.create(Picker.prototype);
LegalPersonPicker.prototype.constructor = Picker;

LegalPersonPicker.prototype._getElId = function () {
    return "picker-legal-person";
};

LegalPersonPicker.prototype._initContext = function (key, textarea, hiddenInput) {
    Picker.prototype._initContext.apply(this, arguments);
    this._hiddenInput = hiddenInput;
};

LegalPersonPicker.prototype._initTable = function () {
    var el = this._findEl();
    var table = new Table(this._structLegalPerson, el);
    var pickerData = el.querySelector(".picker-data");
    pickerData.appendChild(table.tableEl);
    table.onSelect = function (rec, trEl) {
        var selectButton = el.querySelector(".select-button");
        selectButton.disabled = false;
    };
    table.onDataLoad = function (data) {
        var selectButton = el.querySelector(".select-button");
        selectButton.disabled = true;
    };
    return table;
};

LegalPersonPicker.prototype._onShow = function () {
    this._clearFilterLegalPerson();

    var selectButton = this._findEl().querySelector(".select-button");
    selectButton.disabled = true;
};

LegalPersonPicker.prototype.loadLegalPersons = function (legalPersons) {
    this._legalPersons = legalPersons;
    this._tableLegalPerson.loadData(legalPersons);
};

LegalPersonPicker.prototype.setValue = function (value) {
    this._currentValue = value;
    this._tableLegalPerson.selectByKeyValue(value);
};

LegalPersonPicker.prototype._initFilterLegalPerson = function () {
    var self = this;
    var el = this._findEl();
    var nameInput = el.querySelector('#filter_legal_person_name_lp');
    var binInput = el.querySelector('#filter_legal_person_bin_lp');
    nameInput.onkeyup = binInput.onkeyup = function () {
        self._filterLegalPerson(el.querySelector('#filter_legal_person_bin_lp').value, el.querySelector('#filter_legal_person_name_lp').value);
    };
};

LegalPersonPicker.prototype._filterLegalPerson = function (filterBin, filterName) {
    var filteredData;
    if (filterBin || filterName) {
        filteredData = this._legalPersons.filter(function (rec) {
            if (!rec.name_ru) {
                return false;
            }
            var result = true;
            if (filterBin) {
                result = rec.idn.toLowerCase().indexOf(filterBin.toLowerCase()) > -1;
            }
            if (result && filterName) {
                result = rec.name_ru.toLowerCase().indexOf(filterName.toLowerCase()) > -1;
            }
            return result;
        });
    } else {
        filteredData = this._legalPersons;
    }
    this._tableLegalPerson.loadData(filteredData);
    if (this._currentLegalPersonValue != null)
        this._tableLegalPerson.selectByKeyValue(this._currentLegalPersonValue);

    if (this._currentValue != null)
        this._tableLegalPerson.selectByKeyValue(this._currentValue);
};

LegalPersonPicker.prototype._clearFilterLegalPerson = function () {
    this._el.querySelector('#filter_legal_person_name_lp').value = "";
    this._el.querySelector('#filter_legal_person_bin_lp').value = "";
    this._tableLegalPerson.loadData(this._legalPersons);
};

LegalPersonPicker.prototype._flash = function (empty) {
    var key = "", caption = "";
    if(!empty) {
        var selectedRec = this._tableLegalPerson.getSelectedRec();

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

function pickLegalPerson(key) {
    var textarea = document.getElementById("caption:" + key);
    var hiddenInput = document.getElementById(key);

    if (textarea != null && hiddenInput != null) {
        legalPersonPicker.show(key, textarea, hiddenInput);
        legalPersonPicker.setValue(hiddenInput.value);
    }
}

var legalPersonPicker;
if (reference_data.hasOwnProperty("legalperson")) {
    legalPersonPicker = new LegalPersonPicker();
    legalPersonPicker.loadLegalPersons(reference_data.legalperson);
}
function LegalPersonPicker2() {
    DependentPicker.call(this);

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

LegalPersonPicker2.prototype = Object.create(DependentPicker.prototype);
LegalPersonPicker2.prototype.constructor = LegalPersonPicker2;

LegalPersonPicker2.prototype._getElId = function () {
    return "picker-legal-person-v2";
};

LegalPersonPicker2.prototype._initContext = function (key, textarea, hiddenInput) {
    DependentPicker.prototype._initContext.apply(this, arguments);
    this._hiddenInput = hiddenInput;
};

LegalPersonPicker2.prototype._initTable = function () {
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

LegalPersonPicker2.prototype._onShow = function () {
    this._clearFilterLegalPerson();

    var selectButton = this._findEl().querySelector(".select-button");
    selectButton.disabled = true;
};

LegalPersonPicker2.prototype.setValue = function (value) {
    this._currentValue = value;
    this._tableLegalPerson.selectByKeyValue(value);
};

LegalPersonPicker2.prototype._initFilterLegalPerson = function () {
    var self = this;
    var el = this._findEl();
    var nameInput = el.querySelector('#filter_legal_person_name_lp_v2');
    var binInput = el.querySelector('#filter_legal_person_bin_lp_v2');
    nameInput.onkeyup = binInput.onkeyup = function () {
        self._filterLegalPerson(el.querySelector('#filter_legal_person_bin_lp_v2').value, el.querySelector('#filter_legal_person_name_lp_v2').value);
    };
};

LegalPersonPicker2.prototype._filterLegalPerson = function (filterBin, filterName) {
    var filteredData;
    if (filterBin || filterName) {
        filteredData = this._items.filter(function (rec) {
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
        filteredData = this._items;
    }
    this._tableLegalPerson.loadData(filteredData);
    if (this._currentLegalPersonValue != null)
        this._tableLegalPerson.selectByKeyValue(this._currentLegalPersonValue);

    if (this._currentValue != null)
        this._tableLegalPerson.selectByKeyValue(this._currentValue);
};

LegalPersonPicker2.prototype._clearFilterLegalPerson = function () {
    this._el.querySelector('#filter_legal_person_name_lp_v2').value = "";
    this._el.querySelector('#filter_legal_person_bin_lp_v2').value = "";
    this._tableLegalPerson.loadData(this._items);
};

LegalPersonPicker2.prototype._flash = function (empty) {
    var selectedRec = {
        name_ru:"", rec_id:""
    };
    if(!empty){
        selectedRec = this._tableLegalPerson.getSelectedRec();
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

    var inputs = document.querySelectorAll('[data-receive^="ref_legal_person_v."][id*="' + this.container + '*"][id$=":' + this.rowId + '"]');
    if (inputs.length === 0) {
        // ищем по старому пикеру
        inputs = document.querySelectorAll('[data-receive^="ref_legal_person."][id*="' + this.container + '*"][id$=":' + this.rowId + '"]');
    }
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

function pick_legalperson(key) {
    var textarea = document.getElementById("caption:" + key);
    var hiddenInput = document.getElementById(key);

    if (textarea != null && hiddenInput != null) {
        legalPersonPicker2.show(key, textarea, hiddenInput);
        legalPersonPicker2.setValue(hiddenInput.value);
    }
}

var legalPersonPicker2;
if (reference_data.hasOwnProperty("pick_legalperson")) {
    legalPersonPicker2 = new LegalPersonPicker2();
    legalPersonPicker2.loadData(reference_data.pick_legalperson);
}
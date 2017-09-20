function PersonPicker2() {
    Picker2.apply(this, arguments);

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
            },
            {
                name: 'country_id',
                title: 'COUNTRY_ID',
                hidden: true
            }
        ]
    };

    this._initTab();

    this._tableLegalPerson = this._initTable();

    this._initFilterLegalPerson();
}

PersonPicker2.prototype = Object.create(Picker2.prototype);
PersonPicker2.prototype.constructor = Picker2;

PersonPicker2.prototype._getElId = function () {
    return "picker-person-v2";
};

PersonPicker2.prototype._initTab = function () {
    var self = this;
    var el = this._findEl();
    var tabs = el.querySelectorAll('.tabs ul li span');
    for (var i = 0; i < tabs.length; i++) {
        var span = tabs[i];
        span.onclick = function () {
            self._selectTab(this);
        };
    }

    var checkboxMinor = document.getElementById('natural_person_minor_v2');
    checkboxMinor.onclick = function () {
        var idnInput = document.getElementById('natural_person_idn_v2');
        if (checkboxMinor.checked) {
            idnInput.value = 'несовершеннолетний';
            addCSSClass(idnInput, 'readOnly');
        } else {
            idnInput.value = '';
            removeClassName(idnInput, 'readOnly');
        }
        idnInput.disabled = checkboxMinor.checked;
    };

    var checkboxNoIDN = document.getElementById('non_resident_no_idn_v2');
    checkboxNoIDN.onclick = function () {
        var idnInput = document.getElementById('non_resident_idn_v2');
        if (checkboxNoIDN.checked) {
            idnInput.value = 'нерезидент РК';
            addCSSClass(idnInput, 'readOnly');
        } else {
            idnInput.value = '';
            removeClassName(idnInput, 'readOnly');
        }
        idnInput.disabled = checkboxNoIDN.checked;
    };
};

PersonPicker2.prototype._initTable = function () {
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

PersonPicker2.prototype._selectTab = function (tab) {
    var el = this._findEl();
    var tabs = el.querySelectorAll('.tabs ul li span');
    var tabIndex;
    for (var j = 0; j < tabs.length; j++) {
        var t = tabs[j];
        var container = el.querySelector("div.picker-body div.tab-container:nth-child(" + (j + 2) + ")");
        if (t == tab) {
            addCSSClass(t, "selected");
            removeClassName(container, "hidden-tab");
            tabIndex = j;
        } else {
            removeClassName(t, "selected");
            addCSSClass(container, "hidden-tab");
        }
    }
    this._onTabSelect(tab, tabIndex);
};

PersonPicker2.prototype._onTabSelect = function (tab, tabIndex) {
    var selectButtonDisabled = true;
    switch (tabIndex) {
        case 0:
            selectButtonDisabled = this._tableLegalPerson.getSelectedRec() == null;
            break;
        case 1:
        case 2:
            selectButtonDisabled = false;
            break;
    }
    this._findEl().querySelector(".select-button").disabled = selectButtonDisabled;
};

PersonPicker2.prototype._getCurrentTabIndex = function () {
    var el = this._findEl();
    var currentTab = el.querySelector('.tabs ul li span.selected');
    if (!currentTab) {
        return -1;
    }
    var tabs = el.querySelectorAll('.tabs ul li span');
    for (var i = 0; i < tabs.length; i++) {
        if (tabs[i] == currentTab) {
            return i;
        }
    }
    return -1;
};

PersonPicker2.prototype._onShow = function () {
    var firstTab = this._findEl().querySelector(".tabs ul li:first-child span");
    this._selectTab(firstTab);

    this._clearFilterLegalPerson();

    document.getElementById('natural_person_name_v2').value = '';
    document.getElementById('natural_person_idn_v2').value = '';
    document.getElementById('natural_person_idn_v2').disabled = false;
    document.getElementById('natural_person_minor_v2').checked = false;

    document.getElementById('non_resident_name_v2').value = '';
    document.getElementById('non_resident_idn_v2').value = '';
    document.getElementById('non_resident_idn_v2').disabled = false;
    document.getElementById('non_resident_no_idn_v2').checked = false;
    document.getElementById('non_resident_country_v2').selectedIndex = 0;

    var selectButton = this._findEl().querySelector(".select-button");
    selectButton.disabled = true;
};

PersonPicker2.prototype.loadLegalPersons = function (legalPersons) {
    this._legalPersons = legalPersons;
    this._tableLegalPerson.loadData(legalPersons);
};

PersonPicker2.prototype.loadCountries = function (countries) {
    this._countries = countries;
    var select = document.getElementById('non_resident_country_v2');
    for (var i = 0; i < countries.length; i++) {
        var country = countries[i];
        var optionEl = document.createElement("option");
        optionEl.value = country.rec_id;
        optionEl.innerHTML = country.name_ru;
        select.appendChild(optionEl);
    }
};

PersonPicker2.prototype._initFilterLegalPerson = function () {
    var self = this;
    var el = this._findEl();
    var nameInput = el.querySelector('#filter_legal_person_name_v2');
    var binInput = el.querySelector('#filter_legal_person_bin_v2');
    nameInput.onkeyup = binInput.onkeyup = function () {
        self._filterLegalPerson(el.querySelector('#filter_legal_person_bin_v2').value, el.querySelector('#filter_legal_person_name_v2').value);
    };
};

PersonPicker2.prototype._filterLegalPerson = function (filterBin, filterName) {
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
};

PersonPicker2.prototype._clearFilterLegalPerson = function () {
    this._el.querySelector('#filter_legal_person_name_v2').value = "";
    this._el.querySelector('#filter_legal_person_bin_v2').value = "";
    this._tableLegalPerson.loadData(this._legalPersons);
};

PersonPicker2.prototype._flash = function (empty) {
    var receiverPrefix = this._key.substring(0, this._key.indexOf('*') + 1);
    var receiverSuffix = this._key.substring(this._key.indexOf(':'));

    var name = "";
    var idn = "";
    var country = -1;
    var countryName = "";
    if(!empty) {
        var currentTabIndex = this._getCurrentTabIndex();
        switch (currentTabIndex) {
            case 0:
                var selectedRec = this._tableLegalPerson.getSelectedRec();
                if (selectedRec) {
                    name = selectedRec.name_ru;
                    idn = selectedRec.idn;
                    country = selectedRec.country_id;
                    countryName = selectedRec.country_name;
                }
                break;
            case 1:
                name = document.getElementById('natural_person_name_v2').value;
                idn = document.getElementById('natural_person_idn_v2').value;
                country = 1347; //KZ
                countryName = "Казахстан";
                break;
            case 2:
                name = document.getElementById('non_resident_name_v2').value;
                idn = document.getElementById('non_resident_idn_v2').value;
                var countryEl = document.getElementById('non_resident_country_v2');
                if (countryEl.selectedIndex > 0) {
                    country = countryEl.options[countryEl.selectedIndex].value;
                    countryName = countryEl.options[countryEl.selectedIndex].text;
                } else {
                    country = "";
                    countryName = "";
                }
                break;
        }
    }

    /*if (this._boundElem) {
        this._boundElem.value = name;
        if (this._boundElem.tagName.toUpperCase() === 'TEXTAREA') {
            textAreaAdjust(this._boundElem);
            fireChangeEvent(this._boundElem);
        }
    }*/

    var adapter = {bin: idn, name: name, name_country: countryName, "name_country.rec_id": country};

    var inputs = document.querySelectorAll('[data-receive^="person2."][id*="' + this.container + '*"][id$=":' + this.rowId + '"]');
    console.info("1", inputs);
    for (var i = 0; i < inputs.length; i++) {
        var input = inputs[i];

        var dataValueSender = input.getAttribute("data-receive");
        var fieldName = dataValueSender.substring(dataValueSender.indexOf(".") + 1);
        console.info("2", fieldName);
        input.value = adapter[fieldName];
        fireChangeEvent(input);

        if (input.tagName.toUpperCase() === 'TEXTAREA') {
            textAreaAdjust(input);
        }
    }

    if (this._boundElem.tagName.toUpperCase() === 'TEXTAREA') {
        textAreaAdjust(this._boundElem);
    }

    fireChangeEvent(this._boundElem);
    this._overlay();
};

function pick_person2(key) {
    var textarea = document.getElementById(key);
    if (textarea != null) {
        personPicker2.show(key, textarea);
    }
}

var personPicker2;
if (reference_data.hasOwnProperty("pick_person2")) {
    personPicker2 = new PersonPicker2();
    personPicker2.loadLegalPersons(reference_data.pick_person2);
    personPicker2.loadCountries(reference_data.country);
}
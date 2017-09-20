function PersonPicker() {
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

PersonPicker.prototype = Object.create(Picker.prototype);
PersonPicker.prototype.constructor = Picker;

PersonPicker.prototype._getElId = function () {
    return "picker-person";
};

PersonPicker.prototype._initTab = function () {
    var self = this;
    var el = this._findEl();
    var tabs = el.querySelectorAll('.tabs ul li span');
    for (var i = 0; i < tabs.length; i++) {
        var span = tabs[i];
        span.onclick = function () {
            self._selectTab(this);
        };
    }

    var checkboxMinor = document.getElementById('natural_person_minor');
    checkboxMinor.onclick = function () {
        var idnInput = document.getElementById('natural_person_idn');
        if (checkboxMinor.checked) {
            idnInput.value = 'несовершеннолетний';
            addCSSClass(idnInput, 'readOnly');
        } else {
            idnInput.value = '';
            removeClassName(idnInput, 'readOnly');
        }
        idnInput.disabled = checkboxMinor.checked;
    };

    var checkboxNoIDN = document.getElementById('non_resident_no_idn');
    checkboxNoIDN.onclick = function () {
        var idnInput = document.getElementById('non_resident_idn');
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

PersonPicker.prototype._initTable = function () {
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

PersonPicker.prototype._selectTab = function (tab) {
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

PersonPicker.prototype._onTabSelect = function (tab, tabIndex) {
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

PersonPicker.prototype._getCurrentTabIndex = function () {
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

PersonPicker.prototype._onShow = function () {
    var firstTab = this._findEl().querySelector(".tabs ul li:first-child span");
    this._selectTab(firstTab);

    this._clearFilterLegalPerson();

    document.getElementById('natural_person_name').value = '';
    document.getElementById('natural_person_idn').value = '';
    document.getElementById('natural_person_idn').disabled = false;
    document.getElementById('natural_person_minor').checked = false;

    document.getElementById('non_resident_name').value = '';
    document.getElementById('non_resident_idn').value = '';
    document.getElementById('non_resident_idn').disabled = false;
    document.getElementById('non_resident_no_idn').checked = false;
    document.getElementById('non_resident_country').selectedIndex = 0;

    var selectButton = this._findEl().querySelector(".select-button");
    selectButton.disabled = true;
};

PersonPicker.prototype.loadLegalPersons = function (legalPersons) {
    this._legalPersons = legalPersons;
    this._tableLegalPerson.loadData(legalPersons);
};

PersonPicker.prototype.loadCountries = function (countries) {
    this._countries = countries;
    var select = document.getElementById('non_resident_country');
    for (var i = 0; i < countries.length; i++) {
        var country = countries[i];
        var optionEl = document.createElement("option");
        optionEl.value = country.rec_id;
        optionEl.innerHTML = country.name_ru;
        select.appendChild(optionEl);
    }
};

PersonPicker.prototype._initFilterLegalPerson = function () {
    var self = this;
    var el = this._findEl();
    var nameInput = el.querySelector('#filter_legal_person_name');
    var binInput = el.querySelector('#filter_legal_person_bin');
    nameInput.onkeyup = binInput.onkeyup = function () {
        self._filterLegalPerson(el.querySelector('#filter_legal_person_bin').value, el.querySelector('#filter_legal_person_name').value);
    };
};

PersonPicker.prototype._filterLegalPerson = function (filterBin, filterName) {
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

PersonPicker.prototype._clearFilterLegalPerson = function () {
    this._el.querySelector('#filter_legal_person_name').value = "";
    this._el.querySelector('#filter_legal_person_bin').value = "";
    this._tableLegalPerson.loadData(this._legalPersons);
};

PersonPicker.prototype._flash = function (empty) {
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
                name = document.getElementById('natural_person_name').value;
                idn = document.getElementById('natural_person_idn').value;
                country = 1347; //KZ
                countryName = "Казахстан";
                break;
            case 2:
                name = document.getElementById('non_resident_name').value;
                idn = document.getElementById('non_resident_idn').value;
                var countryEl = document.getElementById('non_resident_country');
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

    if (receiverPrefix.indexOf('fs_sr') == 0) {
        if (receiverPrefix.indexOf('_1_array') > -1) {
            var nameInput = document.getElementById(receiverPrefix + 'name' + receiverSuffix);
            if (nameInput) {
                nameInput.value = name;
                fireChangeEvent(nameInput);
            }
            var idnInput = document.getElementById(receiverPrefix + 'bin_iin' + receiverSuffix);
            if (idnInput) {
                idnInput.value = idn;
                fireChangeEvent(idnInput);
            }
            var countryInput = document.getElementById(receiverPrefix + 'country' + receiverSuffix);
            if (countryInput) {
                countryInput.value = country;
                fireChangeEvent(countryInput);
            }
            var countryCaptionInput = document.getElementById('textarea:' + receiverPrefix + 'country' + receiverSuffix);
            if (countryCaptionInput) {
                countryCaptionInput.value = countryName;
                fireChangeEvent(countryCaptionInput);
            }
        }
        if (receiverPrefix.indexOf('_2_array') > -1) {
            var nameInput = document.getElementById(receiverPrefix + 'name' + receiverSuffix);
            if (nameInput) {
                nameInput.value = name;
                fireChangeEvent(nameInput);
            }
            var idnInput = document.getElementById(receiverPrefix + 'bin' + receiverSuffix);
            if (idnInput) {
                idnInput.value = idn;
                fireChangeEvent(idnInput);
            }
        }
    } else if (receiverPrefix.indexOf('fs_oi_mfo') == 0) {
        var nameInput = document.getElementById(receiverPrefix + 'name' + receiverSuffix);
        if (nameInput) {
            nameInput.value = name;
            fireChangeEvent(nameInput);
        }
        var idnInput = document.getElementById(receiverPrefix + 'v3' + receiverSuffix);
        if (idnInput) {
            idnInput.value = idn;
            fireChangeEvent(idnInput);
        }
    } else {
        var nameInput = document.getElementById(this._key);
        if (nameInput) {
            nameInput.value = name;
            fireChangeEvent(nameInput);
        }
    }

    if (this._boundElem.tagName.toUpperCase() === 'TEXTAREA') {
        textAreaAdjust(this._boundElem);
    }

    fireChangeEvent(this._boundElem);
    this._overlay();
};

function pickPerson(key) {
    var textarea = document.getElementById(key);
    if (textarea != null) {
        personPicker.show(key, textarea);
    }
}

var personPicker;
if (reference_data.hasOwnProperty("person")) {
    personPicker = new PersonPicker();
    personPicker.loadLegalPersons(reference_data.person);
    personPicker.loadCountries(reference_data.country);
}
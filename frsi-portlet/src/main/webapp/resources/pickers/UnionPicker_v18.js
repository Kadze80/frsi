function UnionPicker() {
    this.contaner = null;
    this.rowId = null;
}

UnionPicker.prototype.initContext = function (key, boundElem, viewModel) {
    this._key = key;
    this.container = key.substring(0, key.indexOf("*"));
    this.rowId = key.substring(key.lastIndexOf(":") + 1);
    this._boundElem = boundElem;
    this.viewModel = viewModel;
};

UnionPicker.prototype.setValue = function (value) {
    console.info("setValue1", value);
    console.info("setValue1.1", '[data-receive^="' + this.viewModel.name.toLowerCase() + '."][id*="' + this.container + '*"][id$=":' + this.rowId + '"]');
    var inputs = document.querySelectorAll('[data-receive^="' + this.viewModel.name.toLowerCase() + '."][id*="' + this.container + '*"][id$=":' + this.rowId + '"]');
    console.info("setValue2", inputs);
    for (var i = 0; i < inputs.length; i++) {
        var input = inputs[i];

        var dataValueSender = input.getAttribute("data-receive");
        var fieldName = dataValueSender.substring(dataValueSender.indexOf(".") + 1);
        input.value = value[fieldName];
        fireChangeEvent(input);

        console.info("setValue3", dataValueSender, fieldName);

        if (input.tagName.toUpperCase() === 'TEXTAREA') {
            textAreaAdjust(input);
        }
    }

    if (this._boundElem)
        this._boundElem.focus();
};

UnionPicker.prototype.show = function (key, texterea, hiddeninput) {

};

var unionPicker = new UnionPicker();

function pick_unionperson(key) {
    var hiReportDate = document.querySelector("[id$='hiReportDate']");
    var reportDateStr = hiReportDate.value;
    // var reportDateStr = $("#" + "#{applicationBean.liferayFacesResponseNamespace}\\:dynamic-form\\:hiReportDate").val();
    var textarea = document.getElementById("caption:" + key);

    var viewModel = {
        "name": "ref_unionpersons_v",
        "columns": [
            {"name": "rec_id", "key": true, "hidden": true, "valueType":"NUMBER_0", "targetColumnName":"name_ru.rec_id"},
            {"name": "name_ru", "title": "Наименование", "valueType":"STRING"},
            {"name": "idn", "title": "ИИН/БИН", "valueType":"STRING"},
            {"name": "type_name", "title": "Тип лица", "valueType":"STRING"},
            {"name": "is_tax", "hidden" : true, "valueType":"BOOLEAN"}
        ],
        "sortFields": [{"name": "name_ru"}],
        "filterColumn": "name_ru"
    };

    unionPicker.initContext(key, textarea, viewModel);

    var ajaxBody = {
        viewModel: viewModel, reportDate:new Date(reportDateStr)
    };
    rcShowUnionPicker([{name: 'pickParam', value: JSON.stringify(ajaxBody)}]);
}

function onUnionPickerLoaded() {

    PF('wUnionPickerDlg').show();
}

function onSelectUnionPickerRow () {
    var trEl = this;
    if (trEl) {
        clearUnionPickerSelection();
        addCSSClass(trEl, "selected-row");
    }
}

function clearUnionPickerSelection() {
    var selectedRowEl = document.querySelector("#unionPickerTable .selected-row");
    if (selectedRowEl !== undefined && selectedRowEl !== null) {
        removeClassName(selectedRowEl, "selected-row");
    }
}

function passUnionPickerValue(empty) {
    var recId;
    if (empty) {
        recId = null;
    } else {
        var selectedRowEl = document.querySelector("#unionPickerTable .selected-row");
        if (selectedRowEl === null || selectedRowEl === undefined)
            return;
        recId = selectedRowEl.cells[0].innerHTML;
    }
    rcPassUnionPickerValue([{name: 'selectedRecId', value: recId}]);
}

function onReceiveUnionPickerValue() {
    var input = document.querySelector("[id$='unionPickerForm:unionPickerSelectedValue']");
    var json = input.value;
    var selectedValue = null;
    try {
        selectedValue = JSON.parse(json);
    } catch (e){
        console.error(e);
        return;
    }

    unionPicker.setValue(selectedValue);

    PF('wUnionPickerDlg').hide();
}

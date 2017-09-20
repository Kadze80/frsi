function GeneralPicker() {
    this.contaner = null;
    this.rowId = null;
}

GeneralPicker.prototype.initContext = function (key, boundElem, viewModel) {
    this._key = key;
    this.container = key.substring(0, key.indexOf("*"));
    this.rowId = key.substring(key.lastIndexOf(":") + 1);
    this._boundElem = boundElem;
    this.viewModel = viewModel;
};

GeneralPicker.prototype.setValue = function (value) {
    var inputs = document.querySelectorAll('[data-receive^="' + this.viewModel.name.toLowerCase() + '."][id*="' + this.container + '*"][id$=":' + this.rowId + '"]');
    for (var i = 0; i < inputs.length; i++) {
        var input = inputs[i];

        var dataValueSender = input.getAttribute("data-receive");
        var fieldName = dataValueSender.substring(dataValueSender.indexOf(".") + 1);
        input.value = value[fieldName];
        fireChangeEvent(input);

        if (input.tagName.toUpperCase() === 'TEXTAREA') {
            textAreaAdjust(input);
        }
    }

    if (this._boundElem)
        this._boundElem.focus();
};

GeneralPicker.prototype.show = function (key, texterea, hiddeninput) {

};

var generalPicker = new GeneralPicker();

function showPicker(key) {
    var hiReportDate = document.querySelector("[id$='hiReportDate']");
    var reportDateStr = hiReportDate.value;
    // var reportDateStr = $("#" + "#{applicationBean.liferayFacesResponseNamespace}\\:dynamic-form\\:hiReportDate").val();
    var textarea = document.getElementById("caption:" + key);

    var viewModelName = textarea.getAttribute("viewModel");
    if (!viewModelName) {
        alert('viewModel attribute not set');
        return;
    }
    var viewModel = null;
    for (var i = 0; i < jsons.length; i++) {
        var item = jsons[i];
        if (item.name.toLowerCase() === viewModelName.toLowerCase()) {
            try {
                viewModel = JSON.parse(item.json);
            } catch (e) {
                alert("Can't parse viewmodel json " + item.json);
            }
            break;
        }
    }
    if (viewModel === null) {
        alert("Can't find ViewModel " + viewModelName);
        return;
    }

    generalPicker.initContext(key, textarea, viewModel);

    var ajaxBody = {
        viewModel: viewModel, reportDate:new Date(reportDateStr)
    };
    rcShowPicker([{name: 'pickParam', value: JSON.stringify(ajaxBody)}]);
}

function onPickerLoaded() {

    PF('wPickerDlg').show();
}

function onSelectRow () {
    var trEl = this;
    if (trEl) {
        clearSelection();
        addCSSClass(trEl, "selected-row");
    }
}

function clearSelection() {
    var selectedRowEl = document.querySelector("#pickerTable .selected-row");
    if (selectedRowEl !== undefined && selectedRowEl !== null) {
        removeClassName(selectedRowEl, "selected-row");
    }
}

function passPickerValue(empty) {
    var recId;
    if (empty) {
        recId = null;
    } else {
        var selectedRowEl = document.querySelector("#pickerTable .selected-row");
        if (selectedRowEl === null || selectedRowEl === undefined)
            return;
        recId = selectedRowEl.cells[0].innerHTML;
    }
    rcPassPickerValue([{name: 'selectedRecId', value: recId}]);
}

function onReceivePickerValue() {
    var input = document.querySelector("[id$='pickerForm:pickerSelectedValue']");
    var json = input.value;
    var selectedValue = null;
    try {
        selectedValue = JSON.parse(json);
    } catch (e){
        console.error(e);
        return;
    }

    generalPicker.setValue(selectedValue);

    PF('wPickerDlg').hide();
}

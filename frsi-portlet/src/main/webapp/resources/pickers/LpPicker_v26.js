function pick_legalperson(receiverId) {
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccLpPicker:lpPickerForm:lpPickerKey');
    if (receiverInfo) receiverInfo.value = receiverId;

    var hiReportDate = document.querySelector("[id$='hiReportDate']");
    var reportDateStr = hiReportDate.value;
    var ajaxBody = {
        reportDate:new Date(reportDateStr)
    };
    var params = [{
        name: 'pickParam',
        value: JSON.stringify(ajaxBody)
    }];
    var hiddenInput = document.getElementById(receiverId);
    var filterField = hiddenInput.getAttribute("data-filter-field");
    var filter = null;
    if (filterField !== null) {
        var decodedData = window.atob(filterField);
        try {
            filter = JSON.parse(decodedData);
        } catch (e){
            console.info("ERR JSON: ", decodedData);
            filter = null;
            alert("Error has been occured duaring parsing json: "+decodedData);
        }
        console.info("Filter", filter);
        if (filter !== null
            && (filter.strValue === undefined || filter.strValue === null)
            && (filter.boolValue === undefined || filter.boolValue === null)
            && (filter.dateValue === undefined || filter.dateValue === null)
            && (filter.lngValue === undefined || filter.lngValue === null)
            && (filter.dblValue === undefined || filter.dblValue === null)
            && receiverId.indexOf("$D.") > -1) {
            var parentKey = receiverId.substring(0, receiverId.lastIndexOf('.'));
            var rowId = parentKey.substring(parentKey.indexOf("$D."));
            if (rowId.lastIndexOf(".") === rowId.indexOf(".")) {
                parentKey = parentKey.substring(0, parentKey.indexOf("$D.")) + rowId.substring(rowId.indexOf(".") + 1);
            }
            var parentEl = document.getElementById(parentKey);
            if (parentEl) {
                var value = parseInt(parentEl.value);
                filter.lngValue = value;
            }
        }
        params.push({
            name: "pickFilter",
            value: JSON.stringify(filter)
        });
    }
    rcShowLpPicker(params);
}
function pass_legalperson(empty) {
    PF('wLpPickerDlg').hide();
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccLpPicker:lpPickerForm:lpPickerKey');
    var receiverId = receiverInfo.value;
    var receiver = document.getElementById(receiverId);
    var receiverCaption = document.getElementById("caption:" + receiverId);
    var pickedObject = {
        rec_id:"",name_ru:""
    };
    if(!empty) {
        var pickedValue = document.getElementById(rns + ':ccLpPicker:lpPickerForm:lpPickerSelectedValue');
        var json = decodeURIComponent(pickedValue.value);
        try {
            pickedObject = JSON.parse(json);
        } catch (e) {
            console.info("ERR JSON: ", json);
            console.log(e);
        }
    }
    if (receiver && pickedObject) {
        receiver.value = pickedObject.rec_id;
        receiverCaption.value = pickedObject.nin;

        var container = receiverId.substring(0, receiverId.indexOf("*"));
        var rowId = receiverId.substring(receiverId.lastIndexOf(":") + 1);

        var adapter = {name_ru: pickedObject.name_ru, "name_ru.rec_id": pickedObject.rec_id};

        var inputs = document.querySelectorAll('[data-receive^="ref_legal_person_v."][id*="' + container + '*"][id$=":' + rowId + '"]');
        if (inputs.length === 0) {
            // ищем по старому пикеру
            inputs = document.querySelectorAll('[data-receive^="ref_legal_person."][id*="' + container + '*"][id$=":' + rowId + '"]');
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

        fireChangeEvent(receiver);
    }
    PF('statusDialog').hide();
    textAreaAdjustAll();
}

function onLpPickerLoaded() {

    PF('wLpPickerDlg').show();
}
function pick_securityinfo(receiverId) {
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickSecurityInfo2:form:receiverInfo');
    if (receiverInfo) receiverInfo.value = receiverId;
    PF('wDialogPickSecurityInfo_v2').show();
}
function pass_securityinfo(empty) {
    PF('wDialogPickSecurityInfo_v2').hide();
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickSecurityInfo2:form:receiverInfo');
    var receiverId = receiverInfo.value;
    var receiver = document.getElementById(receiverId);
    var receiverCaption = document.getElementById("caption:" + receiverId);
    var pickedObject = {
        rec_id:"",nin:"",issuer_name:"",issuer_rec_id:"",variety_name:"",variety_code:"",currency_name:"",currency_code:"",maturity_date:""
    };
    if(!empty) {
        var pickedValue = document.getElementById(rns + ':ccDialogPickSecurityInfo2:form:pickedValue');
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

        var adapter = {
            nin: pickedObject.nin,
            "nin.rec_id": pickedObject.rec_id,
            issuer_name: pickedObject.issuer_name,
            "issuer_name.rec_id": pickedObject.issuer_rec_id,
            variety_name: pickedObject.variety_name,
            "variety_name.rec_id": pickedObject.variety_code,
            currency: pickedObject.currency_name,
            "currency.rec_id": pickedObject.currency_code,
            maturity_date: pickedObject.maturity_date
        };

        var inputs = document.querySelectorAll('[data-receive^="ref_securities."][id*="' + container + '*"][id$=":' + rowId + '"]');
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

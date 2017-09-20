function pickSecurityInfo(receiverId) {
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickSecurityInfo:form:receiverInfo');
    if (receiverInfo) receiverInfo.value = receiverId;
    PF('wDialogPickSecurityInfo').show();
}
function passSecurityInfo() {
    PF('wDialogPickSecurityInfo').hide();
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickSecurityInfo:form:receiverInfo');
    var receiverId = receiverInfo.value;
    var receiver = document.getElementById(receiverId);
    var receiverCaption = document.getElementById("caption:" + receiverId);
    var pickedValue = document.getElementById(rns + ':ccDialogPickSecurityInfo:form:pickedValue');
    var json = decodeURIComponent(pickedValue.value);
    var pickedObject = null;
    try { pickedObject = JSON.parse(json); } catch (e) { console.log(e); }
    if (receiver && pickedObject) {
        receiver.value = pickedObject.rec_id;
        receiverCaption.value = pickedObject.nin;
        var receiverPrefix = receiverId.substring(0, receiverId.indexOf('*') + 1);
        var receiverSuffix = receiverId.substring(receiverId.indexOf(':'));
        var issuerRecId = document.getElementById(receiverPrefix + 'name_emitter' + receiverSuffix);
        var issuerName = document.getElementById("caption:" + receiverPrefix + 'name_emitter' + receiverSuffix);
        if (issuerName && issuerRecId) {
            issuerName.value = pickedObject.issuer_name;
            issuerRecId.value = pickedObject.issuer_rec_id;
        }
        var varietyCode = document.getElementById(receiverPrefix + 'vid_cb' + receiverSuffix);
        if (varietyCode) varietyCode.value = pickedObject.variety_code;
        if(varietyCode && varietyCode.onchange) varietyCode.onchange();
        var varietyName = document.getElementById("caption:" + receiverPrefix + 'vid_cb' + receiverSuffix);
        if (varietyName) varietyName.value = pickedObject.variety_name;
        var currencyCode = document.getElementById(receiverPrefix + 'currency_cb' + receiverSuffix);
        if (currencyCode) currencyCode.value = pickedObject.currency_code;
        /*var countryCode = document.getElementById(receiverPrefix + 'name_country_emitter' + receiverSuffix);
        if (countryCode) countryCode.value = pickedObject.issuer_country_code;*/
        var maturityDate = document.getElementById(receiverPrefix + 'date_maturity' + receiverSuffix);
        if (maturityDate) maturityDate.value = pickedObject.maturity_date;

        fireChangeEvent(receiver);
    }
    PF('statusDialog').hide();
    textAreaAdjustAll();
}

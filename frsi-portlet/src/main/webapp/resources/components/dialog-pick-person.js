function pickPerson(receiverId) {
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickPerson:hidden:receiverInfo');
    if (receiverInfo) receiverInfo.value = receiverId;
    PF('wDialogPickPerson').show();
}
function passPerson() {
    PF('wDialogPickPerson').hide();
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickPerson:hidden:receiverInfo');
    var receiverId = receiverInfo.value;
    var receiver = document.getElementById(receiverId);
    var pickedValue = document.getElementById(rns + ':ccDialogPickPerson:hidden:pickedValue');
    var json = decodeURIComponent(pickedValue.value);
    var pickedObject = null;
    try { pickedObject = JSON.parse(json); } catch (e) { console.log(e); }
    if (receiver && pickedObject) {
        var receiverPrefix = receiverId.substring(0, receiverId.indexOf('*') + 1);
        var receiverSuffix = receiverId.substring(receiverId.indexOf(':'));
        if (receiverPrefix.indexOf('fs_sr') == 0) {
            if (receiverPrefix.indexOf('_1_array') > -1) {
                var name = document.getElementById(receiverPrefix + 'name' + receiverSuffix);
                if (name) name.value = pickedObject.name;
                var idn = document.getElementById(receiverPrefix + 'bin_iin' + receiverSuffix);
                if (idn) idn.value = pickedObject.idn;
                var countryId = document.getElementById(receiverPrefix + 'country' + receiverSuffix);
                if (countryId) countryId.value = pickedObject.country_id;
            }
            if (receiverPrefix.indexOf('_2_array') > -1) {
                var idn = document.getElementById(receiverPrefix + 'bin' + receiverSuffix);
                if (idn) idn.value = pickedObject.idn;
                var name = document.getElementById(receiverPrefix + 'name' + receiverSuffix);
                if (name) name.value = pickedObject.name;
            }
        } else if (receiverPrefix.indexOf('fs_oi_mfo') == 0) {
            var idn = document.getElementById(receiverPrefix + 'v3' + receiverSuffix);
            if (idn) idn.value = pickedObject.idn;
            var name = document.getElementById(receiverPrefix + 'name' + receiverSuffix);
            if (name) name.value = pickedObject.name;
        } else {
            receiver.value = pickedObject.name;
        }
    }
    PF('statusDialog').hide();
    textAreaAdjustAll();
}

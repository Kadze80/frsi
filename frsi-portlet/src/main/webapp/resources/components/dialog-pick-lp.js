function pickLegalPerson(receiverId) {
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickLegalPerson:hidden:receiverInfo');
    if (receiverInfo) receiverInfo.value = receiverId;
    PF('wDialogPickLegalPerson').show();
}
function passLegalPerson() {
    PF('wDialogPickLegalPerson').hide();
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickLegalPerson:hidden:receiverInfo');
    var receiverId = receiverInfo.value;
    var receiver = document.getElementById(receiverId);
    var receiverCaption = document.getElementById('caption:' + receiverId);
    var pickedValue = document.getElementById(rns + ':ccDialogPickLegalPerson:hidden:pickedValue');
    if (receiver && pickedValue && receiverCaption) {
        if (pickedValue.value != null) {
            var json = decodeURIComponent(pickedValue.value);
            var jsonValue = {recId: null, caption: ''};
            try {
                jsonValue = JSON.parse(json);
            } catch (e) {
                console.log(e);
            }
            receiver.value = jsonValue.recId;
            receiverCaption.value = jsonValue.caption;
        } else {
            receiver.value = null;
            receiverCaption.value = null;
        }
    }
    PF('statusDialog').hide();
    textAreaAdjustAll();
}
function pickBank(receiverId) {
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickBank:hidden:receiverInfo');
    if (receiverInfo) receiverInfo.value = receiverId;
    PF('wDialogPickBank').show();
}
function passBank() {
    PF('wDialogPickBank').hide();
    var rns = getLiferayFacesResponseNameSpace();
    var receiverInfo = document.getElementById(rns + ':ccDialogPickBank:hidden:receiverInfo');
    var receiverId = receiverInfo.value;
    var receiver = document.getElementById(receiverId);
    var receiverCaption = document.getElementById('caption:' + receiverId);
    var pickedValue = document.getElementById(rns + ':ccDialogPickBank:hidden:pickedValue');
    if (receiver && pickedValue && receiverCaption) {
        if (pickedValue.value != null) {
            var json = decodeURIComponent(pickedValue.value);
            var jsonValue = {recId: null, caption: ''};
            try {
                jsonValue = JSON.parse(json);
            } catch (e) {
                console.log(e);
            }
            console.info("pickvalue", json, jsonValue);
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

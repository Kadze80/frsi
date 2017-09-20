function pickSecurity(receiverId) {
	var rns = getLiferayFacesResponseNameSpace();
	var receiverInfo = document.getElementById(rns + ':ccDialogPickSecurity:hidden:receiverInfo');
	if (receiverInfo) receiverInfo.value = receiverId;
	PF('wDialogPickSecurity').show();
}
function passSecurityIssuer() {
	PF('wDialogPickSecurity').hide();
	var rns = getLiferayFacesResponseNameSpace();
	var receiverInfo = document.getElementById(rns + ':ccDialogPickSecurity:hidden:receiverInfo');
	var receiverId = receiverInfo.value;
	var receiver = document.getElementById(receiverId);
	var pickedValue = document.getElementById(rns + ':ccDialogPickSecurity:hidden:pickedIssuerValue');
	if (receiver && pickedValue) receiver.value = pickedValue.value;
	PF('statusDialog').hide();
	textAreaAdjustAll();
}
function passSecurity() {
	PF('wDialogPickSecurity').hide();
	var rns = getLiferayFacesResponseNameSpace();
	var receiverInfo = document.getElementById(rns + ':ccDialogPickSecurity:hidden:receiverInfo');
	var receiverId = receiverInfo.value;
	var receiver = document.getElementById(receiverId);
	var pickedValue = document.getElementById(rns + ':ccDialogPickSecurity:hidden:pickedSecurityValue');
	if (receiver && pickedValue) receiver.value = pickedValue.value;
	PF('statusDialog').hide();
	textAreaAdjustAll();
}

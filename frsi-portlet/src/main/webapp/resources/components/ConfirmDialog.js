/**
 * Created by nuriddin on 5/25/16.
 */
function ConfirmDialog() {
    var self = this;
    this._el = this._findEl();
    document.body.appendChild(this._el);

    this._el.onclick = function(){
        self._overlay();
    };
    this._el.querySelector(".confirm-dialog-content").onclick = function(e){
        e.stopPropagation ? e.stopPropagation() : (e.cancelBubble=true);
    };

    var yesButton = this._el.querySelector("#confirm-button-yes");
    yesButton.onclick = function () {
        self._apply();
    };
    var noButton = this._el.querySelector("#confirm-button-no");
    noButton.onclick = function () {
        self._overlay();
    };
}

ConfirmDialog.prototype._findEl = function () {
    return document.getElementById("confirm-dialog-wrapper");
};

ConfirmDialog.prototype._overlay = function () {
    var el = this._findEl();
    if (el) {
        if (el.style.visibility == "visible") {
            el.style.visibility = "hidden";
        } else {
            var box = getOffsetRect(this._button);
            var dialogContentEl = el.querySelector(".confirm-dialog-content");
            dialogContentEl.style.left = box.left+"px";
            var height = dialogContentEl.offsetHeight;
            if ((box.top + this._button.offsetHeight + height) >= getPageHeight()){
                dialogContentEl.style.top = box.top - height + "px";
            } else {
                dialogContentEl.style.top = box.top + this._button.offsetHeight + "px";
            }
            el.style.visibility = "visible";
        }
    }
};

ConfirmDialog.prototype.show = function (tableId, element) {
    this._button = element;
    this._tableId = tableId;
    this._overlay();
};

ConfirmDialog.prototype._apply = function () {
    delRow(this._tableId, this._button);
    this._overlay();
};

function confirmDelete(tableId, element){
    confirmDeleteDialog.show(tableId, element);
}

var confirmDeleteDialog = new ConfirmDialog();

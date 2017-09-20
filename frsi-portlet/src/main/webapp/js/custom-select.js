// Version 1.14
if (typeof Object.create !== 'function') {
    Object.create = function (o) {
        var F = function () {
        };
        F.prototype = o;
        return new F();
    };
}

singleDialogObj = {
    name: "some_ref",

    data: {columns: [], items: []},

    key: null,

    createEl: function () {
        var that = this;

        var template = document.getElementById("ref_template_single");
        if (template == null || template == undefined)
            return;

        this.el = template.cloneNode(true);
        this.el.setAttribute("id", this.name);
        document.body.appendChild(this.el);

        this.el.onclick = function(){
            that.overlay();
        };
        this.el.querySelector(".s-dialog-content").onclick = function(e){
            e.stopPropagation ? e.stopPropagation() : (e.cancelBubble=true);
        };

        var tableEl = this.el.getElementsByTagName("table")[0];
        while (tableEl.firstChild) {
            tableEl.removeChild(tableEl.firstChild);
        }

        if (this.data) {
            var trEl = document.createElement("tr");
            for (var i = 0; i < that.data.columns.length; i++) {
                var colObj = that.data.columns[i];

                if (colObj.hidden === true)
                    continue;

                var thEl = document.createElement("th");
                if (colObj.style)
                    thEl.setAttribute("style", colObj.style);
                if (colObj.className)
                    thEl.className = colObj.className;
                var textEl = document.createTextNode(colObj.title ? colObj.title : colObj.name);
                thEl.appendChild(textEl);
                trEl.appendChild(thEl);
            }
            tableEl.appendChild(trEl);
            tableEl.appendChild(this.createFirstRow());

            if (this.data.items) {
                this.data.items.forEach(function (rec) {
                    var json = decodeURIComponent(rec);
                    var jsonRec = {};
                    try { jsonRec = JSON.parse(json); } catch (e) { console.info("json:", json); }
                    var trEl = document.createElement("tr");
                    for (var i = 0; i < that.data.columns.length; i++) {
                        var colObj = that.data.columns[i];

                        var tdEl = document.createElement("td");
                        tdEl.setAttribute("colName", colObj.name);
                        var cellValue  = jsonRec[colObj.name];
                        var textEl = document.createTextNode(cellValue);
                        tdEl.appendChild(textEl);
                        trEl.appendChild(tdEl);

                        if (colObj.hidden === true) {
                            tdEl.className = "hidden-column";
                        }
                    }
                    tableEl.appendChild(trEl);

                    trEl.onclick = function (e) {
                        that.onSelectRow(e);
                    };
                });
            }
        }
    },

    onSelectRow:function (e) {
        var t = e.target || e.srcElement;
        if (t) {
            this.select(t.parentNode);
            this.flash();
        }
    },

    createFirstRow: function () {
        var that = this;
        var trEl = document.createElement("tr");
        addCSSClass(trEl,'empty-row');
        trEl.innerHTML = "<td colspan='"+this.data.columns.length+"'>&nbsp</td>";
        trEl.onclick = function (e) {
            that.onSelectRow(e);
        };
        return trEl;
    },

    getValueObj: function () {
        var valueObj = {};
        var el = this.el;
        var captionColumnIndex = null, keyColumnIndex = this.getKeyColumnIndex();
        var targetColumnIndexes = [];
        for (var i = 0; i < this.data.columns.length; i++) {
            var col = this.data.columns[i];
            if (col.name == this.textarea.getAttribute("refCaption")) {
                captionColumnIndex = i;
            }
            if (col.targetColumnName != undefined && col.targetColumnName != null && col.targetColumnName != "") {
                targetColumnIndexes.push({name: col.targetColumnName, index: i});
            }
        }
        var selectedRowEl = el.querySelector(".data-table .selected-row");
        if (selectedRowEl != null) {
            if(hasClass(selectedRowEl, 'empty-row')){
                valueObj.key=valueObj.caption="";
                valueObj.targetColumnValues = [];

                for (var j = 0; j < targetColumnIndexes.length; j++) {
                    var obj = targetColumnIndexes[j];
                        valueObj.targetColumnValues.push({
                            name: obj.name,
                            value: ""
                        });
                }

                return valueObj
            }

            var children = selectedRowEl.childNodes;
            if (keyColumnIndex !== null && children[keyColumnIndex].childNodes.length > 0) {
                valueObj.key = children[keyColumnIndex].childNodes[0].nodeValue;
            }
            if (captionColumnIndex !== null && children[captionColumnIndex].childNodes.length > 0) {
                valueObj.caption = children[captionColumnIndex].childNodes[0].nodeValue;
            }
            valueObj.targetColumnValues = [];
            for (var j = 0; j < targetColumnIndexes.length; j++) {
                var obj = targetColumnIndexes[j];
                if (children[obj.index].childNodes.length > 0) {
                    valueObj.targetColumnValues.push({
                        name: obj.name,
                        value: children[obj.index].childNodes[0].nodeValue
                    });
                }
            }
        }
        return valueObj;
    },

    overlay: function () {
        if (this.el === undefined)
            this.createEl();
        if (this.el) {
            if(this.el.style.visibility == "visible"){
                this.el.style.visibility = "hidden";
            } else {
                var box = getOffsetRect(this.textarea);
                var dialogContentEl = this.el.getElementsByClassName("s-dialog-content")[0];
                var height = dialogContentEl.offsetHeight;
                var width = dialogContentEl.offsetWidth;

                if ((box.left + width) >= getPageWidth()) {
                    dialogContentEl.style.left = box.left + this.textarea.offsetWidth - width + "px";
                } else {
                    dialogContentEl.style.left = box.left + "px";
                }

                if ((box.top + this.textarea.offsetHeight + height) >= getPageHeight()){
                    dialogContentEl.style.top = box.top - height + "px";
                } else {
                    dialogContentEl.style.top = box.top + this.textarea.offsetHeight + "px";
                }
                this.el.style.visibility = "visible";
            }
        }
    },

    show: function (key, hiddenInput, textarea) {
        this.key = key;
        this.hiddenInput = hiddenInput;
        this.textarea = textarea;
        this.overlay();
        var width=textarea.offsetWidth;
        if(width<600) width = 600;
        this.el.querySelector(".s-dialog-content").style.width = width + "px";
    },

    flash: function () {
        var valueObj = this.getValueObj();

        if (this.hiddenInput && this.textarea) {
            this.hiddenInput.value = valueObj.key;
            this.textarea.value = valueObj.caption;

            var p1 = this.key.substring(0, this.key.indexOf("*") + 1);
            var p2 = this.key.substring(this.key.indexOf(":"));

            for (var i = 0; i < valueObj.targetColumnValues.length; i++) {
                var obj = valueObj.targetColumnValues[i];
                var targetKey = p1 + obj.name + p2;
                var targetKeyElement = document.getElementById(targetKey);
                targetKeyElement.value = valueObj.key;
                var targetCaptionElement = document.getElementById("caption:" + targetKey);
                targetCaptionElement.value = obj.value;

                if (targetCaptionElement.tagName == "TEXTAREA") {
                    textAreaAdjust(targetCaptionElement);
                }
            }

            textAreaAdjust(this.textarea);
            fireChangeEvent(this.hiddenInput);
        }

        this.overlay();

        if (this.textarea)
            this.textarea.focus();
    },

    setValue: function (keyValue) {
        var trEl = this.findTrElByKeyValue(keyValue);
        if (trEl == null)
            this.clearSelection();
        else
            this.select(trEl);
    },

    findTrElByKeyValue:function(keyValue){
        if (this.el == null || this.el == undefined)
            return;
        var keyColumnIndex = this.getKeyColumnIndex();
        if (keyColumnIndex != null) {
            var table = this.el.querySelector("table");
            var trElements = table.childNodes;
            for (var i = 1; i < trElements.length; i++) {
                var trEl = trElements[i];
                var tdEl = trEl.childNodes.item(keyColumnIndex);
                if (tdEl.childNodes.length > 0 && tdEl.childNodes[0].nodeValue == keyValue) {
                    return trEl;
                }
            }
        }
        return null;
    },

    getKeyColumnIndex: function () {
        var keyColumnIndex = null;
        for (var i = 0; i < this.data.columns.length; i++) {
            if (this.data.columns[i].key === true) {
                keyColumnIndex = i;
                break;
            }
        }
        return keyColumnIndex;
    },

    getCaptionByKey:function(keyValue, captionColumnName){
        var keyColumnName;
        for (var i = 0; i < this.data.columns.length; i++) {
            if (this.data.columns[i].key === true) {
                keyColumnName = this.data.columns[i].name;
                break;
            }
        }
        if(keyColumnName) {
            for (var i = 0; i < this.data.items.length; i++) {
                var recObj = this.data.items[i];
                var json = decodeURIComponent(recObj);
                var jsonRec = {};
                try { jsonRec = JSON.parse(json); } catch (e) { console.log(e); }
                if (jsonRec[keyColumnName] == keyValue)
                    return jsonRec[captionColumnName];
            }
        }
        return "";
    },

    select: function (trEl) {
        this.clearSelection();
        addCSSClass(trEl, "selected-row");
    },

    clearSelection: function () {
        var selectedRowEl = document.querySelector("#" + this.name + " .data-table .selected-row");
        if (selectedRowEl != null) {
            removeClassName(selectedRowEl, "selected-row");
        }
    }
};

multiDialogObj = {
    name: "some_ref",

    data: {columns: [], items: []},

    key: null,

    createEl: function () {
        var that = this;

        var template = document.getElementById("ref_template_multiple");
        if (template == null || template == undefined)
            return;

        this.el = template.cloneNode(true);
        this.el.setAttribute("id", this.name);
        document.body.appendChild(this.el);

        this.el.onclick = function(){
            that.overlay();
        };
        this.el.querySelector(".m-dialog-content").onclick = function(e){
            e.stopPropagation ? e.stopPropagation() : (e.cancelBubble=true);
        };

        var tableEl = this.el.getElementsByTagName("table")[0];
        while (tableEl.firstChild) {
            tableEl.removeChild(tableEl.firstChild);
        }

        if (this.data) {
            var trEl = document.createElement("tr");

            trEl.appendChild(document.createElement("th"));
            for (var i = 0; i < that.data.columns.length; i++) {
                var colObj = that.data.columns[i];

                if (colObj.hidden === true)
                    continue;

                var thEl = document.createElement("th");
                if (colObj.style)
                    thEl.setAttribute("style", colObj.style);
                if (colObj.className)
                    thEl.className = colObj.className;
                var textEl = document.createTextNode(colObj.title ? colObj.title : colObj.name);
                thEl.appendChild(textEl);
                trEl.appendChild(thEl);
            }
            tableEl.appendChild(trEl);

            if (this.data.items) {
                this.data.items.forEach(function (rec) {
                    var json = decodeURIComponent(rec);
                    var jsonRec = {};
                    try { jsonRec = JSON.parse(json); } catch (e) { console.log(e); }
                    var trEl = document.createElement("tr");

                    var td = document.createElement("td");
                    var checkboxEl = document.createElement("input");
                    checkboxEl.type = "checkbox";
                    checkboxEl.onclick = function (e) {
                        e.stopPropagation ? e.stopPropagation() : (e.cancelBubble=true);

                        var t = e.target || e.srcElement;
                        if (t) {
                            if (t.checked) {
                                that.select(t.parentNode.parentNode);
                            }else {
                                that.unselect(t.parentNode.parentNode);
                            }
                        }
                    };

                    td.appendChild(checkboxEl);
                    trEl.appendChild(td);

                    trEl.onclick = function (e) {
                        var t = e.target || e.srcElement;
                        if (t) {
                            var currTrEl;
                            if(t.type=="input")
                                currTrEl = t.parentNode.parentNode;
                            else
                                currTrEl = t.parentNode;
                            if (currTrEl.querySelector("input").checked)
                                that.unselect(currTrEl);
                            else
                                that.select(currTrEl);
                        }
                    };

                    for (var i = 0; i < that.data.columns.length; i++) {
                        var colObj = that.data.columns[i];

                        var tdEl = document.createElement("td");
                        tdEl.setAttribute("colName", colObj.name);
                        var textEl = document.createTextNode(jsonRec[colObj.name]);
                        tdEl.appendChild(textEl);
                        trEl.appendChild(tdEl);

                        if (colObj.hidden === true) {
                            tdEl.className = "hidden-column";
                        }
                    }
                    tableEl.appendChild(trEl);
                });
            }
        }

        var selectBtnEl = this.el.querySelector(".m-dialog-select-button");
        selectBtnEl.onclick = function (e) {
            that.flash();
        };
        var cancelBtnEl = this.el.querySelector(".m-dialog-cancel-button");
        cancelBtnEl.onclick = function (e) {
            that.overlay();
        };

        var emptyBtnEl = this.el.querySelector(".m-dialog-empty-button");
        emptyBtnEl.onclick = function (e) {
            var selectedRowEls = tableEl.querySelectorAll("tr.selected-row");
            for (var i = 0; i < selectedRowEls.length; i++) {
                that.unselect(selectedRowEls[i]);
            }
            that.flash();
        };
    },

    getValueObj: function () {
        var el = this.el;
        var selectedRowEls = el.querySelectorAll(".data-table .selected-row");
        var valueObj = {};
        var captionColumnIndex = null, keyColumnIndex = this.getKeyColumnIndex();
        var targetColumnIndexes = [];
        for (var i = 0; i < this.data.columns.length; i++) {
            var col = this.data.columns[i];
            if (col.name == this.textarea.getAttribute("refCaption")) {
                captionColumnIndex = i + 1;
            }
            if (col.targetColumnName != undefined && col.targetColumnName != null && col.targetColumnName != "") {
                targetColumnIndexes.push({name: col.targetColumnName, index: i + 1});
            }
        }
        if (keyColumnIndex !== null && captionColumnIndex !== null) {
            var codes = [], keys = [], targetValues = [];
            for (var i = 0; i < selectedRowEls.length; i++) {
                var selectedRowEl = selectedRowEls[i];
                var keyTdEl = selectedRowEl.childNodes.item(keyColumnIndex);
                if (keyTdEl.childNodes.length > 0) {
                    keys.push(keyTdEl.childNodes[0].nodeValue);
                }
                var tdEl = selectedRowEl.childNodes.item(captionColumnIndex);
                if (tdEl.childNodes.length > 0) {
                    codes.push(tdEl.childNodes[0].nodeValue);
                }
            }

            if (codes.length > 0) {
                valueObj.key = JSON.stringify({values: keys});
                var strCodes = "";
                for (var i = 0; i < codes.length; i++) {
                    if (i > 0) strCodes += ", ";
                    var code = codes[i];
                    strCodes += code == "-1" ? "Выберите данные из справочника" : code;
                }
                valueObj.caption = strCodes;
            }

            valueObj.targetColumnValues = [];
            for (var j = 0; j < targetColumnIndexes.length; j++) {
                var obj = targetColumnIndexes[j];
                var captions = [];
                for (var i = 0; i < selectedRowEls.length; i++) {
                    var selectedRowEl2 = selectedRowEls[i];
                    var tdEl2 = selectedRowEl2.childNodes.item(obj.index);
                    if (tdEl2.childNodes.length > 0) {
                        var v = tdEl2.childNodes[0].nodeValue;
                        if (v != undefined && v != null && v != "")
                            captions.push(v);
                    }
                }
                var strCaptions = "";
                for (var i = 0; i < captions.length; i++) {
                    if (i > 0) strCaptions += ", ";
                    var caption = captions[i];
                    strCaptions += caption == "-1" ? "Выберите данные из справочника" : caption;
                }
                valueObj.targetColumnValues.push({
                    name: obj.name,
                    value: strCaptions
                });
            }
        }
        return valueObj;
    },

    overlay: function () {
        if (this.el === undefined)
            this.createEl();
        if (this.el) {
            if(this.el.style.visibility == "visible"){
                this.el.style.visibility = "hidden";
            } else {
                var box = getOffsetRect(this.textarea);
                var dialogContentEl = this.el.getElementsByClassName("m-dialog-content")[0];
                var height = dialogContentEl.offsetHeight;
                var width = dialogContentEl.offsetWidth;
                console.info("width: "+width);
                if ((box.left + width) >= getPageWidth()) {
                    dialogContentEl.style.left = box.left + this.textarea.offsetWidth - width + "px";
                } else {
                    dialogContentEl.style.left = box.left + "px";
                }

                if ((box.top + this.textarea.offsetHeight + height) >= getPageHeight()){
                    dialogContentEl.style.top = box.top - height + "px";
                } else {
                    dialogContentEl.style.top = box.top + this.textarea.offsetHeight + "px";
                }
                this.el.style.visibility = "visible";
            }
        }
    },

    show: function (key, hiddenInput, textarea) {
        this.key = key;
        this.hiddenInput = hiddenInput;
        this.textarea = textarea;
        this.overlay();
        var width=textarea.offsetWidth;
        if(width<600) width = 600;
        this.el.querySelector(".m-dialog-content").style.width = width + "px";
    },

    flash: function () {
        var valueObj = this.getValueObj();

        if (this.hiddenInput && this.textarea) {
            this.hiddenInput.value = valueObj.key == undefined ? null : valueObj.key;
            this.textarea.value = valueObj.caption == undefined ? null : valueObj.caption;

            var p1 = this.key.substring(0, this.key.indexOf("*") + 1);
            var p2 = this.key.substring(this.key.indexOf(":"));

            for (var i = 0; i < valueObj.targetColumnValues.length; i++) {
                var obj = valueObj.targetColumnValues[i];
                var targetKey = p1 + obj.name + p2;
                var targetKeyElement = document.getElementById(targetKey);
                targetKeyElement.value = valueObj.key == undefined ? null : valueObj.key;
                var targetCaptionElement = document.getElementById("caption:" + targetKey);
                targetCaptionElement.value = obj.value == undefined ? null : obj.value;

                if (targetCaptionElement.tagName == "TEXTAREA") {
                    textAreaAdjust(targetCaptionElement);
                }
            }

            textAreaAdjust(this.textarea);
            fireChangeEvent(this.hiddenInput);
        }

        this.overlay();

        if (this.textarea)
            this.textarea.focus();
    },

    setValue: function (keyValue) {
        if (this.el == null || this.el == undefined)
            return;

        this.clearSelection();

        var codes = [];
        if (keyValue !== null && keyValue !== undefined) {
            try {
                var objValue = JSON.parse(keyValue);
                if (objValue.values != null && objValue.values != undefined
                    && Object.prototype.toString.call(objValue.values) === '[object Array]') {
                    codes = objValue.values;
                }
            } catch (e) {
                return;
            }
        }
        var keyColumnIndex = this.getKeyColumnIndex();
        if (keyColumnIndex != null) {

            var table = this.el.querySelector("table");
            var trElements = table.childNodes;
            for (var i = 1; i < trElements.length; i++) {
                var trEl = trElements[i];
                var tdEl = trEl.childNodes.item(keyColumnIndex);
                if (tdEl.childNodes.length > 0 && codes.indexOf(tdEl.childNodes[0].nodeValue) > -1) {
                    this.select(trEl);
                }
            }
        }
    },

    getKeyColumnIndex: function () {
        var keyColumnIndex = null;
        for (var i = 0; i < this.data.columns.length; i++) {
            if (this.data.columns[i].key === true) {
                keyColumnIndex = i;
                break;
            }
        }
        return ++keyColumnIndex;
    },

    select: function (trEl) {
        trEl.className = "selected-row";
        var chekboxes = trEl.getElementsByTagName("input");
        if (chekboxes.length > 0)
            chekboxes[0].checked = true;

        var el = this.el;
        var selectBtnEl = el.querySelector(".m-dialog-select-button");
        if (selectBtnEl.hasAttribute("disabled")) {
            selectBtnEl.removeAttribute("disabled");
        }
    },

    unselect:function(trEl){
        trEl.className = "";
        trEl.querySelector("input").checked = false;
    },

    clearSelection: function () {
        var selectedRowEls = document.querySelectorAll("#" + this.name + " .data-table .selected-row");
        for (var i = 0; i < selectedRowEls.length; i++) {
            var selectedRowEl = selectedRowEls[i];
            selectedRowEl.className = "";
            var checkboxes = selectedRowEl.getElementsByTagName("input");
            if (checkboxes.length > 0)
                checkboxes[0].checked = false;
        }
    }
};

dialogs = [];

function overlay() {
    var textarea = null, hiddenInput = null;
    var rowName = null;
    var children = this.parentNode.getElementsByTagName("textarea");
    if (children.length > 0) {
        textarea = children[0];
    }
    children = this.parentNode.getElementsByTagName("input");
    if (children.length > 0 && children[0].type == "hidden") {
        hiddenInput = children[0];
        rowName = hiddenInput.getAttribute("name");
    }

    if (textarea != null && hiddenInput != null) {
        var dialogName = textarea.getAttribute("viewModel");
        if (!dialogName || dialogName == null) {
            dialogName = textarea.getAttribute("ref");
        }
        if(dialogName) {
            var fd = dialogs.filter(function (d) {
                return d.name == dialogName;
            });

            if (fd.length > 0) {
                fd[0].show(rowName, hiddenInput, textarea);
                fd[0].setValue(hiddenInput.value);
            }
        }
    }
}

function setCaption(){
    var textarea = null, hiddenInput = this;
    var children = this.parentNode.getElementsByTagName("textarea");
    if (children.length > 0) {
        textarea = children[0];
    }

    if (textarea != null) {
        var refName = textarea.getAttribute("ref");
        var fd = dialogs.filter(function (d) {
            return d.name == refName;
        });
        if (fd.length > 0) {
            var captionValue = fd[0].getCaptionByKey(hiddenInput.value, textarea.getAttribute("refCaption"));
            textarea.value = captionValue;
            textAreaAdjust(textarea);
        }
    }
}

function textAreaAdjust(o) {
    o.style.height = "1px";
    o.style.height = (o.scrollHeight-5)+"px";
}

function textAreaAdjustAll(){
    var textareas = document.querySelectorAll("textarea.autoHeight");
    for (var i = 0; i < textareas.length; i++)
        textAreaAdjust(textareas[i]);
}

function getOffsetRect(elem) {
    // (1)
    var box = elem.getBoundingClientRect();

    var body = document.body;
    var docElem = document.documentElement;

    // (2)
    var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop;
    var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft;

    // (3)
    var clientTop = docElem.clientTop || body.clientTop || 0;
    var clientLeft = docElem.clientLeft || body.clientLeft || 0;

    // (4)
    var top  = box.top +  scrollTop - clientTop;
    var left = box.left + scrollLeft - clientLeft;

    return { top: Math.round(top), left: Math.round(left) };
}

function decodeString(str) {
    var json = decodeURIComponent(str);
    var jsonRec = {};
    try {
        jsonRec = JSON.parse(json);
    } catch (e) {
        console.info(e, json);
    }
    return jsonRec;
}

function normalizeData(reference_data) {
    for (var p in reference_data) {
        if (reference_data.hasOwnProperty(p)) {
            reference_data[p] = reference_data[p].map(function (item) {
                return decodeString(item);
            });
        }
    }
}
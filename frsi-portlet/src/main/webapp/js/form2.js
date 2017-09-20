function Provider() {

}

Provider.prototype.setRowId = function (rowId) {
    this.rowId = rowId;
};

Provider.prototype.setField = function (field) {
    this.field = field;
};

Provider.prototype.getIndicator = function (ind) {
    var rowId = ind.substring(ind.lastIndexOf(":") + 1);
    var field = ind.substring(ind.indexOf("*") + 1, ind.indexOf(":"));
    if (rowId.toLowerCase() == '$dr') {
        ind = ind.substring(0, ind.lastIndexOf(":") + 1) + this.rowId;
    }
    if (field.toLowerCase() == '$dc')
        ind = ind.substring(0, ind.indexOf("*") + 1) + this.field + ind.substring(ind.indexOf(":"));

    return ind;
};

Provider.prototype.get = function (ind, dt) {
    var id = this.getIndicator(ind);
    var element = document.getElementById(id);
    if (!element) return null;
    if (dt.charAt(0) == 'n')
        return this.__getNumberValue(element);
    else if (dt == 'd')
        return moment(element.value, 'DD.MM.YYYY');
    else
        return element.value;
};

Provider.prototype.__getNumberValue = function (element) {
    var value;
    if ($(element).is('[class*=maskMoney]'))
        value = $(element).autoNumeric('get');
    else
        value = element.value.replace(/\s/g, '');
    return value === '' || isNaN(value) ? 0 : parseFloat(value);
};

Provider.prototype.set = function (ind, value, dt) {
    var id = this.getIndicator(ind);
    var element = document.getElementById(id);
    if (!element) return;
    if (dt.charAt(0) == 'n')
        this.__setNumberValue(element, value);
    else if (dt == 'd')
        element.value = moment(value).format('DD.MM.YYYY');
    else
        element.value = value;
};

Provider.prototype.__setNumberValue = function (element, value) {
    if ($(element).is('[class*=maskMoney]')) { // className contains
        $(element).autoNumeric('set', value);
    } else element.value = round2(value);
};

Provider.prototype.eachRow = function (ind, cb, templ, container, dt) {
    var rowIds = this.getRowIds(container, templ);
    for (var i = 0; i < rowIds.length; i++) {
        this.setRowId(rowIds[i]);
        set(ind, cb.call(this), dt);
    }
};

Provider.prototype.sum = function () {
    if (arguments.length == 3) {
        return this.__sumDynRow(arguments[0], arguments[1], arguments[2]);
    } else if (arguments.length == 5)
        return this.__sumRange(arguments[0], arguments[1], arguments[2], arguments[3], arguments[4]);
    else
        return 0;
};

Provider.prototype.__sumDynRow = function (cb, templ, container) {
    var rowIds = this.getRowIds(container, templ);
    var res = 0;
    for (var i = 0; i < rowIds.length; i++) {
        this.setRowId(rowIds[i]);
        res += cb.call(this);
    }
    return res;
};

Provider.prototype.__sumRange = function (cb, orientation, start, end, container) {
    var res = 0, i = 0;
    if (orientation == 'ROW') {
        var rowIds = this.getRangeRowIds(container, start, end);
        res = 0;
        for (i = 0; i < rowIds.length; i++) {
            this.setRowId(rowIds[i]);
            res += cb.call(this);
        }
        return res;
    } else if (orientation == 'FIELD') {
        var fields = this.getRangeFields(container, start, end);
        res = 0;
        for (i = 0; i < fields.length; i++) {
            this.setField(fields[i]);
            res += cb.call(this);
        }
        return res;
    }
    console.error("__sumRange", orientation, start, end, container);
    return 0;
};

Provider.prototype.getRowIds = function (cont, templ) {
    templ = templ.toLowerCase();
    var prefix = templ.substring(0, templ.lastIndexOf(".n") + 1);
    var inputs = document.querySelectorAll("input[type=text][id^='" + cont + "']");
    var rowIds = [];
    for (var i = 0; i < inputs.length; i++) {
        var id = inputs[i].id;
        var rowId = id.substring(id.lastIndexOf(':') + 1);
        if (rowId.toLowerCase().startsWith(prefix) && rowIds.indexOf(rowId) == -1)
            rowIds.push(rowId);
    }
    return rowIds;
};

Provider.prototype.getRangeRowIds = function (cont, start, end) {
    var inputs = document.querySelectorAll("input[type=text][id^='" + cont + "']");
    var rowIds = [];
    var templ, s1, s2;
    if (start.indexOf('.')>-1) {
        templ = start.substring(0, start.lastIndexOf(".") + 1).toLowerCase();
        s1 = start.substring(start.lastIndexOf(".") + 1);
        s2 = end.substring(end.lastIndexOf(".") + 1);
    } else {
        s1 = start;
        s2 = end;
    }
    var n1 = parseInt(s1);
    var n2 = parseInt(s2);
    for (var i = 0; i < inputs.length; i++) {
        var id = inputs[i].id;
        var rowId = id.substring(id.lastIndexOf(':') + 1);
        var s;
        if (templ != null) {
            if (!rowId.toLowerCase().startsWith(templ)) continue;
            s = rowId.substring(rowId.lastIndexOf(".") + 1);
        } else {
            if (rowId.indexOf(".") > -1) continue;
            s = rowId;
        }
        var n;
        try {
            n = parseInt(s);
        } catch (a) {
            continue;
        }
        if (n >= n1 && n <= n2 && rowIds.indexOf(rowId) == -1)
            rowIds.push(rowId);
    }
    return rowIds;
};

Provider.prototype.getRangeFields = function (cont, start, end) {
    var fields = this.fields[cont];
    if (!fields) {
        console.error("getRangeFields", cont, start, end);
        return [];
    }
    var i1 = fields.indexOf(start);
    var i2 = fields.indexOf(end);
    var arr = [];
    if (i1 == -1 || i2 == -1) {
        console.error("getRangeFields", cont, start, end);
        return [];
    }
    for (var i = i1; i <= i2; i++) {
        arr.push(fields[i]);
    }
    return arr;
};


var provider = new Provider();

function get(ind, dt) {
    return provider.get(ind, dt);
}

function set(ind, value, dt) {
    provider.set(ind, value, dt);
}

function sum() {
    return provider.sum.apply(provider, arguments);
}

function eachRow(ind, cb, templ, container, dt) {
    provider.eachRow(ind, cb, templ, container, dt);
}

function setFields(fields) {
    provider.fields = fields;
}
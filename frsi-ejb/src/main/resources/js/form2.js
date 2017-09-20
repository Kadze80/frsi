/**
 * Created by nuriddin on 10/7/16.
 */
function get(ind, dt) {
    if (dt.charAt(0) == 'n')
        return p.getNumber(ind);
    if (dt.charAt(0) == 's')
        return p.getString(ind);
    if (dt.charAt(0) == 'd')
        return p.getDate(ind);
    if (dt.charAt(0) == 'b')
        return p.getBoolean(ind);
}

function set(ind, value, dt) {
    p.set(ind, value, dt);
}

function sum() {
    var cb;
    if (arguments.length == 3) {
        cb = new Packages.form.calcfield2.DoubleCallback(arguments[0]);
        return p.sumDynRow(cb, arguments[1], arguments[2]);
    } else if (arguments.length == 5) {
        cb = new Packages.form.calcfield2.DoubleCallback(arguments[0]);
        return p.sumRange(cb, arguments[1], arguments[2], arguments[3], arguments[4]);
    } else
        return 0;
}

function count(templ, container) {
    return p.count(templ, container);
}

function eachRow(ind, cb, templ, container, dt) {
    var cb = new Packages.form.calcfield2.ObjectCallback(arguments[0]);
    p.eachRow(ind, cb, templ, container, dt);
}

function setFields(fields) {
    p.setFields(fields);
}
/**    " +
 * Created by nuriddin on 9/8/16.
 */
function sum() {
    var keyIds = [];
    for (var i = 1; i < arguments.length; i++) {
        keyIds[i - 1] = arguments[i];
    }
    var expr = arguments[0];
    var impl = {run: expr};
    var callback = new Packages.parser.DoubleCallback(impl);
    return sumFunction.exec(callback, keyIds);
}

function getRespondentIdn() {
    return p.getRespondentIdn();
}

function getRespondentRecId() {
    return p.getRespondentRecId();
}

function setFields(fields) {
    p.setFields(fields);
}
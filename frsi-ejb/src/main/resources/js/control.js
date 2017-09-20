/**
 * Created by nuriddin on 10/13/16.
 */
importClass(Packages.entities.ValueType);
importClass(Packages.entities.Param);
importClass(Packages.entities.Variant);

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

function sum(f) {
    var cb = new Packages.form.calcfield2.DoubleCallback(f);
    return p.sum(cb);
}

function ext(name, dt) {
    var params = [];
    if (arguments.length > 2) {
        for (var i = 2; i < arguments.length; i++) {
            params.push(arguments[i]);
        }
    }
    var vt = p.toValueType(dt);
    var v = p.getExt(name, vt, params);
    return getValue(v);
}

function pr(name, value, dt) {
    var v = new Variant();
    v.valueType = p.toValueType(dt);
    switch (dt) {
        case 'b':
            v.boolValue = value;
            break;
        case 's':
            v.strValue = value;
            break;
        case 'd':
            v.dateValue = value;
            break;
        case 'n0':
            v.lngValue = value;
            break;
        case 'n1':
        case 'n2':
        case 'n3':
        case 'n4':
        case 'n5':
        case 'n6':
        case 'n7':
        case 'n8':
            v.dblValue = value;
            break;
    }
    return new Param(name, v);
}

function ref(ref, refCol, recId, dt) {
    var vt = p.toValueType(dt);
    var v = p.getRef(ref, refCol, recId, vt);
    return getValue(v);
}

function getValue(v) {
    var vt = v.valueType;
    switch (vt){
        case ValueType.BOOLEAN:
            return v.boolValue;
        case ValueType.STRING:
            return v.strValue;
        case ValueType.DATE:
            return v.dateValue;
        case ValueType.NUMBER_0:
            return v.lngValue;
        case ValueType.NUMBER_1:
        case ValueType.NUMBER_2:
        case ValueType.NUMBER_3:
        case ValueType.NUMBER_4:
        case ValueType.NUMBER_5:
        case ValueType.NUMBER_6:
        case ValueType.NUMBER_7:
        case ValueType.NUMBER_8:
            return v.dblValue;
    }
}
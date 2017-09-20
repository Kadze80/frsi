/**
 * Created by Nuriddin.Baideuov on 16.09.2015.
 */

function setInputValue(id, value) {
    document.validateNumber(value);
    document.setValue(id, document.autoNumeric(id, value));
}

function getInputValue(id) {
    var value = document.getValue(id);
    return getMaskedInputValue(value);
}

function getMaskedInputValue(value) {
    return value == null || value == undefined || value == '' || value.isEmpty() || isNaN(value) ? 0 : parseFloat(value);
}

function hasRateA(currencyId) {
    return document.hasRateA(currencyId);
}

function sumInputValuesByGroup(groupId) {
    return document.sumInputValuesByGroup(groupId);
}

function sumInputValuesByKeyValueRange(inputs, minId, maxId) {
    return document.sumInputValuesByKeyValueRange(inputs, minId, maxId);
}

function getTableInputs(tableId) {
    return document.getTableInputs(tableId);
}

function updateDynamicRowsColumn(colGroupId, argColumns) {
    document.updateDynamicRowsColumn(colGroupId, argColumns);
}
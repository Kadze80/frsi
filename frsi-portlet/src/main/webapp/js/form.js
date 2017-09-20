// Version 15
jQuery(function($) {
    initAllMasks();
    initAllDatePickers();
    initAllTimePickers();
});

function getTableInputs(tableId) {
    var inputs = document.querySelectorAll('input[type=text]');
    var tableInputs = [];
    for (var i = 0; i < inputs.length; i++)
        if (inputs[i].id && inputs[i].id.substring(0, tableId.length) === tableId)
            tableInputs.push(inputs[i]);
    return tableInputs;
}
function sumInputValuesByKeyValueRange(inputs, minId, maxId) {
    var minLastColonPos = minId.lastIndexOf(':');
    var minLastDotPos = minId.lastIndexOf('.');
    var minIdPrefix = minLastDotPos < minLastColonPos ? minId.substring(0, minLastColonPos + 1) : minId.substring(0, minLastDotPos + 1);
    var minKeyValue = minLastDotPos < minLastColonPos ? parseInt(minId.substring(minLastColonPos + 1)) : parseInt(minId.substring(minLastDotPos + 1));
    var maxKeyValue = minLastDotPos < minLastColonPos ? parseInt(maxId.substring(minLastColonPos + 1)) : parseInt(maxId.substring(minLastDotPos + 1));
    var groupInputs = [];
    for (var i = 0; i < inputs.length; i++) {
        var lastColonPos = inputs[i].id.lastIndexOf(':');
        var lastDotPos = inputs[i].id.lastIndexOf('.');
        var idPrefix = lastDotPos < lastColonPos ? inputs[i].id.substring(0, lastColonPos + 1) : inputs[i].id.substring(0, lastDotPos + 1);
        var keyValue = lastDotPos < lastColonPos ? parseInt(inputs[i].id.substring(lastColonPos + 1)) : parseInt(inputs[i].id.substring(lastDotPos + 1));
        if (idPrefix === minIdPrefix && minKeyValue <= keyValue && keyValue <= maxKeyValue) groupInputs.push(inputs[i]);
    }
    return sumInputValues(groupInputs);
}
function sumInputValuesByGroup(groupId) {
    var lastColonPos = groupId.lastIndexOf(':');
    var idPrefix = groupId.substring(0, lastColonPos) + ":$D." + groupId.substring(lastColonPos + 1) + ".";
    var inputs = document.querySelectorAll('input[type=text]');
    var groupInputs = [];
    for (var i = 0; i < inputs.length; i++)
        if (inputs[i].id.substring(0, idPrefix.length) == idPrefix) groupInputs.push(inputs[i]);
    return sumInputValues(groupInputs);
}
function sumInputValuesByRange(groupId, minMinorId, maxMinorId) {
    var lastColonPos = groupId.lastIndexOf(':');
    var idPrefix = groupId.substring(0, lastColonPos) + ":$D." + groupId.substring(lastColonPos + 1) + ".";
    var inputs = document.querySelectorAll('input[type=text]');
    var groupInputs = [];
    for (var i = 0; i < inputs.length; i++)
        if (inputs[i].id.substring(0, idPrefix.length) == idPrefix) {
            var minorId = parseInt(inputs[i].id.substring(inputs[i].id.lastIndexOf('.') + 1));
            if (minorId >= minMinorId && minorId <= maxMinorId) groupInputs.push(inputs[i]);
        }
    return sumInputValues(groupInputs);
}
function sumInputValues(inputs) {
    var sum = 0;
    for (var i = 0; i < inputs.length; i++) {
        var value = getMaskedInputValue(inputs[i]);
        sum += parseFloat(value);
    }
    return sum;
}
function getInputValue(id) {
    var element = document.getElementById(id);
    return getMaskedInputValue(element);
}
function setInputValue(id, value) {
    var element = document.getElementById(id);
    if (!element) return null;
    if ($(element).is('[class*=maskMoney]')) { // className contains
        $(element).autoNumeric('set', value);
    } else element.value = round2(value);
}
function getMaskedInputValue(element) {
    if (!element) return null;
    var value;
    if ($(element).is('[class*=maskMoney]'))
        value = $(element).autoNumeric('get');
    else
        value = element.value.replace(/\s/g, '');
    return value === '' || isNaN(value) ? 0 : parseFloat(value);
}
function round0(value) {
    return Math.round(value * 1) / 1;
}
function round1(value) {
    return Math.round(value * 10) / 10;
}
function round2(value) {
    return Math.round(value * 100) / 100;
}
function round3(value) {
    return Math.round(value * 1000) / 1000;
}
function round4(value) {
    return Math.round(value * 10000) / 10000;
}
function round5(value) {
    return Math.round(value * 100000) / 100000;
}
function round6(value) {
    return Math.round(value * 1000000) / 1000000;
}
function round7(value) {
    return Math.round(value * 10000000) / 10000000;
}
function round8(value) {
    return Math.round(value * 100000000) / 100000000;
}

function addRow(tableId, groupId, dynamicRowName) {
    var rowIdPrefix;
    if (groupId.indexOf('$D.') == -1)
        rowIdPrefix = tableId + ":$D." + groupId + ".";
    else
        rowIdPrefix = tableId + ":" + groupId + ".";
    var table = document.getElementById(tableId);
    var rows = table.querySelectorAll('tr[id^="' + rowIdPrefix + '"]');
    var childRows = [];
    var i;
    for (i = 0; rows[i]; i++) {
        if (rows[i].id.substring(rowIdPrefix.length).indexOf('.') == -1) {
            childRows.push(rows[i]);
        }
    }
    var maxMinorId = 0;
    for (i = 0; childRows[i]; i++) {
        var id = childRows[i].id;
        var minorId = parseInt(id.substring(id.lastIndexOf('.') + 1));
        if (minorId > maxMinorId) maxMinorId = minorId;
    }
    var groupRow = document.getElementById(tableId + ":" + groupId);
    var row = table.insertRow(groupRow.rowIndex + rows.length + 1);
    row.id = rowIdPrefix + ++maxMinorId;
    childRows.push(row);
    addCells(tableId, row, dynamicRowName);
    //$.getScript(fixTable(true,1));
    var btnFix = document.getElementById(getLiferayFacesResponseNameSpace() + ':dynamic-form:fix');
    btnFix.click();
}

function addRowGroup(tableId, groupId, groupName) {
    var rowIdPrefix;
    if (groupId.indexOf('$D.') == -1)
        rowIdPrefix = tableId + ":$D." + groupId + ".";
    else
        rowIdPrefix = tableId + ":" + groupId + ".";
    var table = document.getElementById(tableId);
    var childRows = table.querySelectorAll('tr[id^="' + rowIdPrefix + '"]');

    var i;
    var maxMinorId = 0;
    for (i = 0; childRows[i]; i++) {
        var id = childRows[i].id;
        var s = id.substring(rowIdPrefix.length);
        if (s.indexOf(".") > -1) s = s.substring(0, s.indexOf('.'));
        var minorId = parseInt(s);
        if (minorId > maxMinorId) maxMinorId = minorId;
    }
    var groupRow = document.getElementById(tableId + ":" + groupId); console.info(rowIdPrefix, maxMinorId);
    addRows(groupRow.rowIndex + childRows.length, tableId, groupName, rowIdPrefix + ++maxMinorId);
    var btnFix = document.getElementById(getLiferayFacesResponseNameSpace() + ':dynamic-form:fix');
    btnFix.click();
}

function addGroupRow(tableId, groupName, rowId, rowIndex, rowIdPrefix) {
    console.info('addGroupRow', tableId, groupName, rowId, rowIndex, rowIdPrefix);
    var tbody = document.querySelector('#'+tableId +' tbody');
    var row = tbody.insertRow(rowIndex);
    row.id = rowIdPrefix + (rowId == null ? '' : ('.' + rowId));
    console.info('addGroupRow rowId', rowIdPrefix + (rowId == null ? '' : ('.' + rowId)));
    addCells(tableId, row, groupName, rowId);
}

function delRow(tableId, element) {
    var table = document.getElementById(tableId);
    var row;
    var parent = element.parentNode;
    while(parent) {
        if (parent.tagName === "TR") {
            row = parent;
            break;
        }
        parent = parent.parentNode;
    }
    var rowIdPrefix = row.id.substring(0, row.id.lastIndexOf('.') + 1);
    destroyAllDatePickers();
    destroyAllTimePickers();
    delChildRows(table, row);
    table.deleteRow(row.rowIndex);
    renumberRows(tableId, rowIdPrefix);
    initAllDatePickers();
    initAllTimePickers();
    updateCalculatedFields();
}

function delChildRows(table, row) {
    var rowIdPrefix = row.id + '.';
    var rows = table.querySelectorAll('tr[id^="' + rowIdPrefix + '"]');
    if (rows.length > 0) {
        var index = rows[0].rowIndex;
        for (var i = 0; i < rows.length; i++) {
            table.deleteRow(index);
        }
    }
}

function renumberRows(tableId, rowIdPrefix, newRowIdPrefix) {
    var groupPrefix = newRowIdPrefix ? newRowIdPrefix.substring(newRowIdPrefix.indexOf('$D.') + 3) : rowIdPrefix.substring(rowIdPrefix.indexOf('$D.') + 3);
    var table = document.getElementById(tableId);
    var rows = table.querySelectorAll('tr[id^="' + rowIdPrefix + '"]');
    var childRows = [];
    var i;
    for (i = 0; rows[i]; i++) {
        if (rows[i].id.substring(rowIdPrefix.length).indexOf('.') == -1) {
            childRows.push(rows[i]);
        }
    }

    if (childRows.length == 0)
        return;

    for (i = 0; childRows[i]; i++) {
        var oldRowId = childRows[i].id;
        var oldRowIdSuffix = oldRowId.substring(oldRowId.indexOf('$D.'));
        var oldRowIdSuffixText = oldRowIdSuffix.substring(oldRowIdSuffix.indexOf('.') + 1);
        var newIdNum = i + 1;
        var newRowId = (newRowIdPrefix ? newRowIdPrefix : rowIdPrefix) + newIdNum;
        var newRowIdSuffixText = groupPrefix + newIdNum;
        renumberChildNodes(childRows[i], oldRowIdSuffixText, newRowIdSuffixText);
        childRows[i].id = newRowId;
        var rowIdPrefix2 = oldRowId + ".";
        renumberRows(tableId, rowIdPrefix2, newRowId + '.');
    }
}

function renumberChildNodes(node, oldRowIdSuffixText, newRowIdSuffixText) {
    for(var i = 0; i < node.childNodes.length; i++) {
        var childNode = node.childNodes[i];
        renumberNode(childNode, oldRowIdSuffixText, newRowIdSuffixText);
        if(childNode.childNodes.length > 0) renumberChildNodes(childNode, oldRowIdSuffixText, newRowIdSuffixText);
    }
}
function renumberNode(node, oldRowIdSuffixText, newRowIdSuffixText) {
    if (node.nodeName === "#text") {
        if (oldRowIdSuffixText.indexOf('group.') < 0) {
            if (node.nodeValue === oldRowIdSuffixText) node.nodeValue = newRowIdSuffixText;
        } else {
            var oldMinorIdText = oldRowIdSuffixText.substring(oldRowIdSuffixText.lastIndexOf('.') + 1);
            var newMinorIdText = newRowIdSuffixText.substring(newRowIdSuffixText.lastIndexOf('.') + 1);
            if (node.nodeValue === oldMinorIdText) node.nodeValue = newMinorIdText;
        }
    }
    var oldRowIdSuffix = '$D.' + oldRowIdSuffixText;
    var newRowIdSuffix = '$D.' + newRowIdSuffixText;
    if (node.nodeName === "INPUT") {
        if (node.id) {
            var newId = node.id.replace(oldRowIdSuffix, newRowIdSuffix);
            node.id = newId;
            node.name = newId;
        }
        if (node.type === "button" && node.onclick) {
            var oldOnclick = node.getAttribute('onclick');
            var newOnclick = oldOnclick.replace(oldRowIdSuffix, newRowIdSuffix);
            node.setAttribute('onclick', newOnclick);
        }
    }
    if (node.nodeName === "SELECT") {
        if (node.id) {
            var newId = node.id.replace(oldRowIdSuffix, newRowIdSuffix);
            node.id = newId;
            node.name = newId;
        }
    }
    if (node.nodeName === "TEXTAREA") {
        if (node.id) {
            var newId = node.id.replace(oldRowIdSuffix, newRowIdSuffix);
            node.id = newId;
            node.name = newId;
        }
    }
}
function updateDynamicRowsColumn(colGroupId, argColumns) {
    var lastColonPos = colGroupId.lastIndexOf(':');
    var idPrefix = colGroupId.substring(0, lastColonPos) + ":$D." + colGroupId.substring(lastColonPos + 1) + ".";
    var inputs = document.querySelectorAll('input[type=text]');
    var colGroupInputs = [];
    for (var i = 0; i < inputs.length; i++)
        if (inputs[i].id.substring(0, idPrefix.length) == idPrefix) colGroupInputs.push(inputs[i]);
    for (var i = 0; i < colGroupInputs.length; i++) {
        var colId = colGroupInputs[i].id;
        var starPos = colId.indexOf('*');
        var firstColonPos = colId.indexOf(':');
        var idPart1 = colId.substring(0, starPos + 1);
        var idPart2 = colId.substring(firstColonPos);
        var sum = 0;
        for (var j = 0; j < argColumns.length; j++) {
            var argId = idPart1 + argColumns[j] + idPart2;
            sum += getInputValue(argId);
        }
        setInputValue(colId, sum);
    }
}

function addColGroup(tableId, parentId) {
    var table = document.getElementById(tableId);

    var colIdPrefix;
    if (parentId.indexOf('$C.') == -1)
        colIdPrefix = tableId + ":$C." + parentId + ".";
    else
        colIdPrefix = tableId + ":" + parentId + ".";
    var childCols = table.querySelectorAll('col[id^="' + colIdPrefix + '"]');

    var i;
    var maxMinorId = 0;
    for (i = 0; childCols[i]; i++) {
        var id = childCols[i].id;
        var s = id.substring(colIdPrefix.length, id.lastIndexOf(':'));
        if (s.indexOf(".") > -1)
            s = s.substring(0, s.indexOf('.'));
        var minorId = parseInt(s);
        if (minorId > maxMinorId)
            maxMinorId = minorId;
    }
    ++maxMinorId;

    var groupColId = tableId + ':' + parentId + ':';
    var groupCol = table.querySelector('col[id^="' + groupColId + '"]');
    var groupColIndex = [].indexOf.call(groupCol.parentNode.children, groupCol);
    addColGroupCols(table, groupColIndex + childCols.length, colIdPrefix + maxMinorId, tableId, groupCol.getAttribute('data-dynamicGroupLabel'));

    var alltrs = table.querySelectorAll('tr');
    for (i = 0; i < alltrs.length; i++) {
        var tr = alltrs[i];
        var cellTag = tr.parentNode.tagName == 'TBODY' ? 'td' : 'th';
        var q = cellTag + '[id^="' + groupColId + '"]';
        var groupEl = tr.querySelector(q);
        if(!groupEl){
            console.info('NULL', q, tr);
            continue;
        }
        var rowId = groupEl.id.substring(groupEl.id.lastIndexOf(":") + 1);
        /*var cell = tr.insertCell(groupEl.cellIndex + childRows.length + 1);
         cell.id = colIdPrefix + maxMinorId + ':' + rowId;
         cell.innerHTML = 'new cell';*/
        var childCells = tr.querySelectorAll(cellTag + '[id^="' + colIdPrefix + '"]');
        var cellIndex = groupEl.cellIndex + childCells.length;
        var cellText = '' + maxMinorId;
        if (groupEl.getAttribute('data-cellText')) {
            cellText = groupEl.getAttribute('data-cellText') + '.' + cellText;
        }
        if (cellTag == 'th' && childCells.length > 0) {
            incColspan(tr, colIdPrefix);
        }
        addColGroupCells(tr, rowId, cellIndex, colIdPrefix + maxMinorId, tableId, groupEl.getAttribute('data-dynamicGroupLabel'), cellText, childCells.length == 0);
    }
}

function incColspan(tr, colIdPrefix, colspan) {
    var th = tr.querySelector('th[id^="' + colIdPrefix + '"][data-colspan]');
    if (th) {
        var colspan = th.getAttribute('colspan');
        th.setAttribute('colspan', ++colspan);
    }
}

function decColspan(tr, colIdPrefix, colspan) {
    var th = tr.querySelector('th[id^="' + colIdPrefix + '"][data-colspan]');
    if (th) {
        var colspan = th.getAttribute('colspan');
        th.setAttribute('colspan', --colspan);
    }
}

function addColGroupCellsForDynamicRows(row, rowId, cellIndex, parentId, tableId, dynamicColGroup) {
    var table = document.getElementById(tableId);
    var colIdPrefix;
    if (parentId.indexOf('$C.') == -1)
        colIdPrefix = tableId + ":$C." + parentId + ".";
    else
        colIdPrefix = tableId + ":" + parentId + ".";

    var childRows = table.querySelectorAll('col[id^="' + colIdPrefix + '"]');
    var minorIds = [];
    for (i = 0; i < childRows.length; i++) {
        var id = childRows[i].id;
        var s = id.substring(colIdPrefix.length, id.lastIndexOf(':'));
        if (s.indexOf(".") > -1)
            s = s.substring(0, s.indexOf('.'));
        var minorId = parseInt(s);
        if (minorIds.indexOf(minorId) == -1)
            minorIds.push(minorId);
    }

    var i;
    var allcells = [];
    for (i = 0; i < minorIds.length; i++) {
        var minorId = minorIds[i];
        var cells = addColGroupCells(row, rowId, cellIndex, colIdPrefix + minorId, tableId, dynamicColGroup);
        cellIndex += cells.length;
        /*for (var j = 0; j < cells.length; j++) {
         formatRow(cells[j], rowId);
         }*/
        allcells = allcells.concat(cells);
    }
    return allcells;
}
function delColGroup(tableId, element, cellText) {
    var table = document.getElementById(tableId);
    var th = getNearestParent(element, 'TH');
    var currentTr = getNearestParent(th, 'TR');
    var celIdPrefix = th.id.substring(0, th.id.lastIndexOf(':'));
    destroyAllDatePickers();
    destroyAllTimePickers();

    var i;
    var cols = table.querySelectorAll('col[id^="' + celIdPrefix + ':' + '"], col[id^="' + celIdPrefix + '.' + '"]');
    console.info('delColGroup', celIdPrefix, cols, th);
    if (cols.length > 0) {
        var parentNode = cols[0].parentNode;
        for (i = 0; i < cols.length; i++) {
            parentNode.removeChild(cols[i]);
        }
    }

    var groupIdPrefix = celIdPrefix.substring(0, celIdPrefix.lastIndexOf('.') + 1);
    var groupCellText = cellText.indexOf('.') > -1 ? cellText.substring(0, cellText.indexOf('.')) : '';
    renumberCells(groupIdPrefix, table, 'col', groupCellText);

    var trs = table.querySelectorAll('tr');
    var siblings = currentTr.querySelectorAll('th[id^="' + groupIdPrefix + '"]');
    for (i = 0; i < trs.length; i++) {
        var tr = trs[i];
        var cellTag = tr.parentNode.tagName == 'TBODY' ? 'td' : 'th';
        var q = cellTag + '[id^="' + celIdPrefix+':' + '"], ' + cellTag + '[id^="' + celIdPrefix + '.' + '"]';
        var cells = tr.querySelectorAll(q);
        for (var j = 0; j < cells.length; j++) {
            if (siblings.length > 1 && cellTag == 'th' && j == 0) {
                var th = tr.querySelector('th[id^="' + groupIdPrefix + '"][data-colspan]');
                if (th) continue;
            }
            tr.removeChild(cells[j]);
        }
        if (cellTag == 'th') {
            decColspan(tr, groupIdPrefix);
        }
        renumberCells(groupIdPrefix, tr, cellTag, groupCellText);
    }

    initAllDatePickers();
    initAllTimePickers();
    updateCalculatedFields();
}

function getNearestParent(element, parentTag) {
    var parent = element.parentNode;
    while(parent) {
        if (parent.tagName === parentTag) {
            return parent;
        }
        parent = parent.parentNode;
    }
    return null;
}

function renumberCells(colIdPrefix, parentNode, tagName, groupCellText) {
    var childCols = parentNode.querySelectorAll(tagName+'[id^="' + colIdPrefix+'"]');

    if (childCols.length == 0)
        return;
    var i;
    var newIdNum = 0;
    var predOldIdNum = '';
    for (i = 0; i < childCols.length; i++) {
        var oldColId = childCols[i].id;
        var idPart1 = oldColId.substring(0, oldColId.lastIndexOf(':'));
        var idPart2 = oldColId.substring(oldColId.lastIndexOf(':'));
        var idPrefix = idPart1.substring(0, colIdPrefix.length);
        var idSuffix = idPart1.substring(colIdPrefix.length);
        if (idSuffix.indexOf('.') == -1)
            idSuffix = idPart2;
        else
            idSuffix = idSuffix.substring(idSuffix.indexOf('.')) + idPart2;

        var oldIdNum = idPart1.substring(colIdPrefix.length);
        if (oldIdNum.indexOf('.') > -1)
            oldIdNum = oldIdNum.substring(0, oldIdNum.indexOf('.'));

        if (oldIdNum !== predOldIdNum) {
            newIdNum++;
            predOldIdNum = oldIdNum;
        }
        var newId = idPrefix + newIdNum + idSuffix;
        var oldColName = oldColId.substring(oldColId.indexOf(':') + 1, oldColId.lastIndexOf(':'));
        var newColName = newId.substring(newId.indexOf(':') + 1, newId.lastIndexOf(':'));
        var oldCellText = ''+oldIdNum;
        var newCellText = ''+newIdNum;
        if(groupCellText){
            oldCellText = groupCellText + '.' +oldCellText;
            newCellText = groupCellText + '.' +newCellText;
        }
        var cellText = childCols[i].getAttribute('data-cellText');
        if (cellText) {
            oldCellText += '.' + cellText;
            newCellText += '.' + cellText;
        }
        renumberChildNodesColName(childCols[i], oldColName, newColName, oldCellText, newCellText);

        childCols[i].id = newId;
    }
}

function renumberChildNodesColName(node, oldColName, newColName, oldCellText, newCellText) {
    for(var i = 0; i < node.childNodes.length; i++) {
        var childNode = node.childNodes[i];
        renumberNodeColName(childNode, oldColName, newColName, oldCellText, newCellText);
        if(childNode.childNodes.length > 0) renumberChildNodesColName(childNode, oldColName, newColName, oldCellText, newCellText);
    }
}
function renumberNodeColName(node, oldColName, newColName, oldCellText, newCellText) {
    if (node.nodeName === "#text") {
        if (node.nodeValue === oldCellText) node.nodeValue = newCellText;
    }
    if (node.nodeName === "INPUT") {
        if (node.id) {
            var newId = node.id.replace(oldColName, newColName);
            node.id = newId;
            node.name = newId;
        }
        if (node.type === "button" && node.onclick) {
            var oldOnclick = node.getAttribute('onclick');
            var newOnclick = oldOnclick.replace(oldColName, newColName);
            node.setAttribute('onclick', newOnclick);
        }
    }
    if (node.nodeName === "SELECT") {
        if (node.id) {
            var newId = node.id.replace(oldColName, newColName);
            node.id = newId;
            node.name = newId;
        }
    }
    if (node.nodeName === "TEXTAREA") {
        if (node.id) {
            var newId = node.id.replace(oldColName, newColName);
            node.id = newId;
            node.name = newId;
        }
    }
}
function formatCell(cell, colName, cellText) {
    var drIdValue = '$DynamicColName';
    var drIdText = '$DynamicColNameText';
    var spans;
    var inputs;
    var selects;
    var textareas;

    if (cell.getAttribute('data-cellText')) {
        cellText += '.' + cell.getAttribute('data-cellText');
    }

    spans = cell.getElementsByTagName('span');
    for (var i = 0; i < spans.length; i++) {
        if (spans[i].innerHTML.indexOf(drIdValue) > -1) spans[i].innerHTML = cellText;
    }
    inputs = cell.getElementsByTagName('input');
    for (var i = 0; i < inputs.length; i++) {
        var inputType = inputs[i].getAttribute('type');
        if (inputs[i].id) inputs[i].id = inputs[i].id.replace(drIdValue, colName);
        if (inputs[i].name) inputs[i].name = inputs[i].name.replace(drIdValue, colName);
        if ($(inputs[i]).is('[class*=maskMoney]')) initMask(inputs[i]);
        if ($(inputs[i]).hasClass('datepicker')) initDatePicker(inputs[i]);
        if ($(inputs[i]).hasClass('timepicker')) initTimePicker(inputs[i]);
        if (inputType === 'button' && inputs[i].onclick) {
            var func = inputs[i].getAttribute('onclick');
            func = func.replace(drIdText, cellText).replace(drIdValue, colName);
            inputs[i].setAttribute('onclick', func);
        }
    }
    selects = cell.getElementsByTagName('select');
    for (var i = 0; i < selects.length; i++) {
        if (selects[i].id) selects[i].id = selects[i].id.replace(drIdValue, colName);
        if (selects[i].name) selects[i].name = selects[i].name.replace(drIdValue, colName);
    }
    textareas = cell.getElementsByTagName('textarea');
    for (var i = 0; i < textareas.length; i++) {
        if (textareas[i].id) textareas[i].id = textareas[i].id.replace(drIdValue, colName);
        if (textareas[i].name) textareas[i].name = textareas[i].name.replace(drIdValue, colName);
    }
    cell.removeAttribute('data-templ');
}

function formatRow(cell, rowId) {
    var drIdValue = '$DynamicRowId';
    var spans;
    var inputs;
    var selects;
    var textareas;
    var i;

    spans = cell.getElementsByTagName('span');
    for (i = 0; i < spans.length; i++) {
        if (spans[i].innerHTML.indexOf(drIdValue) > -1) spans[i].innerHTML = rowId.replace('$D.','').replace('group.','');
    }
    inputs = cell.getElementsByTagName('input');
    for (i = 0; i < inputs.length; i++) {
        var inputType = inputs[i].getAttribute('type');
        if (inputs[i].id) inputs[i].id = inputs[i].id.replace(drIdValue, rowId);
        if (inputs[i].name) inputs[i].name = inputs[i].name.replace(drIdValue, rowId);
        if ($(inputs[i]).is('[class*=maskMoney]')) initMask(inputs[i]);
        if ($(inputs[i]).hasClass('datepicker')) initDatePicker(inputs[i]);
        if ($(inputs[i]).hasClass('timepicker')) initTimePicker(inputs[i]);
        if (inputType === 'button' && inputs[i].onclick) {
            var func = inputs[i].getAttribute('onclick');
            func = func.replace(drIdValue, rowId);
            inputs[i].setAttribute('onclick', func);
        }
    }
    selects = cell.getElementsByTagName('select');
    for (i = 0; i < selects.length; i++) {
        if (selects[i].id) selects[i].id = selects[i].id.replace(drIdValue, rowId);
        if (selects[i].name) selects[i].name = selects[i].name.replace(drIdValue, rowId);
    }
    textareas = cell.getElementsByTagName('textarea');
    for (i = 0; i < textareas.length; i++) {
        if (textareas[i].id) textareas[i].id = textareas[i].id.replace(drIdValue, rowId);
        if (textareas[i].name) textareas[i].name = textareas[i].name.replace(drIdValue, rowId);
    }
}

function formatRowAndCell(cell, rowId, colName, cellText) {
    var drIdValue = '$DynamicRowId';
    var dcIdValue = '$DynamicColName';
    var spans;
    var inputs;
    var selects;
    var textareas;
    var i;

    if (cell.getAttribute('data-cellText')) {
        cellText += '.' + cell.getAttribute('data-cellText');
    }

    spans = cell.getElementsByTagName('span');
    for (i = 0; i < spans.length; i++) {
        if (spans[i].innerHTML.indexOf(drIdValue) > -1) spans[i].innerHTML = rowId.replace('$D.','').replace('group.','');
        if (spans[i].innerHTML.indexOf(dcIdValue) > -1) spans[i].innerHTML = cellText;
    }
    inputs = cell.getElementsByTagName('input');
    for (i = 0; i < inputs.length; i++) {
        var inputType = inputs[i].getAttribute('type');
        if (inputs[i].id) inputs[i].id = inputs[i].id.replace(drIdValue, rowId).replace(dcIdValue, colName);
        if (inputs[i].name) inputs[i].name = inputs[i].name.replace(drIdValue, rowId).replace(dcIdValue, colName);
        if ($(inputs[i]).is('[class*=maskMoney]')) initMask(inputs[i]);
        if ($(inputs[i]).hasClass('datepicker')) initDatePicker(inputs[i]);
        if ($(inputs[i]).hasClass('timepicker')) initTimePicker(inputs[i]);
        if (inputType === 'button' && inputs[i].onclick) {
            var func = inputs[i].getAttribute('onclick');
            func = func.replace(drIdValue, rowId).replace(dcIdValue, colName);
            inputs[i].setAttribute('onclick', func);
        }
    }
    selects = cell.getElementsByTagName('select');
    for (i = 0; i < selects.length; i++) {
        if (selects[i].id) selects[i].id = selects[i].id.replace(drIdValue, rowId).replace(dcIdValue, colName);
        if (selects[i].name) selects[i].name = selects[i].name.replace(drIdValue, rowId).replace(dcIdValue, colName);
    }
    textareas = cell.getElementsByTagName('textarea');
    for (i = 0; i < textareas.length; i++) {
        if (textareas[i].id) textareas[i].id = textareas[i].id.replace(drIdValue, rowId).replace(dcIdValue, colName);
        if (textareas[i].name) textareas[i].name = textareas[i].name.replace(drIdValue, rowId).replace(dcIdValue, colName);
    }
}

function insertTh(row, cellIndex) {
    var cell = document.createElement('th');
    if (cellIndex < row.cells.length)
        row.insertBefore(cell, row.cells[cellIndex]);
    else
        row.appendChild(cell);
    return cell;
}

function insertCol(table, colIndex) {
    var col = document.createElement("col");
    var cols = table.getElementsByTagName('col');
    var parentNode = cols[0].parentNode;
    if (colIndex < cols.length)
        parentNode.insertBefore(col, cols[colIndex]);
    else
        parentNode.appendChild(col);
    return col;
}

function onUpdateHiddenValue() {
    var rns = getLiferayFacesResponseNameSpace();
    var hiddenValue = document.getElementById(rns + ':hidden:hiddenValue');
    var hiddenObject = null;
    try { hiddenObject = JSON.parse(hiddenValue.value); } catch (e) {}
    if (hiddenObject && hiddenObject.tag && hiddenObject.tag === 'balanceAccountDetails') {
        var tableId = hiddenObject.tableId;
        var rowId = hiddenObject.rowId;
        var table = document.getElementById(tableId);
        var row = document.getElementById(tableId + ":" + rowId);
        var details = hiddenObject.data;
        for (var i = 0; i < details.length; i++) {
            var parentcode = details[i].parentcode;
            var code = details[i].code;
            var detailRow = table.insertRow(row.rowIndex + i + 1);
            detailRow.id = tableId + ":" + code;
            var cell0 = detailRow.insertCell(0); // hidden
            var cell1 = detailRow.insertCell(1);
            var cell2 = detailRow.insertCell(2);
            var cell3 = detailRow.insertCell(3);
            var cell4 = detailRow.insertCell(4);
            var cell5 = detailRow.insertCell(5);
            cell0.innerHTML = parentcode;
            cell1.innerHTML = code;
            cell3.innerHTML = details[i].name;
            var input = document.createElement('input');
            input.type = "text";
            input.id = "balance_accounts_array*sum:code:" + code;
            input.name = input.id;
            input.className = "maskMoney0";
            input.setAttribute("onkeydown", "moveFocus(event,this);");
            input.setAttribute("onfocus", "this.select();");
            input.setAttribute("onchange", "updateCalculatedFieldsByParent('" + parentcode + "', false);makeDirty();");
            cell4.appendChild(input);
            initMask(input);
        }
        var btnPlus = row.querySelector("input[type=button][value='+']");
        var btnMinus = row.querySelector("input[type=button][value='-']");
        btnPlus.disabled = true;
        btnMinus.disabled = false;
    }
}
function delBalanceAccountDetails(button) {
    var table = $(button).closest('table')[0];
    var groupRow = $(button).closest('tr')[0];
    var hasDot = groupRow.id.charAt(groupRow.id.length - 1) === ".";
    var groupId = hasDot ? groupRow.id.substring(0, groupRow.id.length - 1) : groupRow.id;
    var groupIdDot = hasDot ? groupRow.id : groupRow.id + ".";
    var groupPrefix = groupId.substring(0, groupId.length - 3);
    var rows = table.querySelectorAll("tr[id^='" + groupPrefix + "']");
    for (var i = 0; i < rows.length; i++)
        if (rows[i].id != groupId && rows[i].id != groupIdDot) table.deleteRow(rows[i].rowIndex);
    var btnPlus = groupRow.querySelector("input[type=button][value='+']");
    var btnMinus = groupRow.querySelector("input[type=button][value='-']");
    btnPlus.disabled = false;
    btnMinus.disabled = true;
}

function gotoElement(id, settings) {
    var el = document.getElementById(id);
    if (el) {
        var duration = 500;
        if (el.tagName === 'INPUT' && el.type == 'hidden') {
            // через hidden input находим textarea
            var displayEl = el.parentNode.querySelector('textarea');
            if (!displayEl)
                displayEl = el.parentNode.querySelector('input[type=text]');
            el = displayEl;
        }

        settings['onAfter'] = function () {
            el.focus();
        };
        $.scrollTo(el, duration, settings);
        if (el.addEventListener) {
            // focus/blur на стадии перехвата срабатывают во всех браузерах
            // поэтому используем их
            el.addEventListener('blur', onElementBlur, true);
        } else {
            // ветка для IE8-, где нет стадии перехвата, но есть focusin/focusout
            el.onfocusout = onElementBlur;
        }
        onElementFocus(el);
    }
}

function moveFocus(e, el) {
    $el = $(el);
    switch (e.keyCode) {
        case 13:
            var inputs = el.form.querySelectorAll('input[type=text],select');
            var idx = -1;
            for (var i = 0; i < inputs.length; i++)
                if (inputs[i].id === el.id) {
                    idx = i;
                    break;
                }
            if ($el.hasClass('datepicker')) $el.datepicker("hide");
            if (el.nodeName === 'INPUT') e.preventDefault();
            if (-1 < idx && idx < inputs.length - 1) {
                if ($(inputs[idx + 1]).hasClass('datepicker')) {
                    // Walkaround for bug in datepicker
                    window.setTimeout($.proxy(function () {
                        inputs[idx + 1].focus();
                    }, this), 10);
                } else inputs[idx + 1].focus();
            }
            break;
        case 38:
            var $row = $el.closest('tr');
            var colNum = $el.closest('td').index();
            var inputs = [];
            while ($row.index() > 0 && !inputs.length) {
                $row = $row.prev();
                inputs = $row[0].cells[colNum].querySelectorAll('input[type=text],select');
            }
            if ($el.hasClass('datepicker')) $el.datepicker("hide");
            if (el.nodeName === 'INPUT') e.preventDefault();
            if (inputs.length) inputs[0].focus();
            break;
        case 40:
            var rowCount = $el.closest('table').find('tr').length;
            var $row = $el.closest('tr');
            var colNum = $el.closest('td').index();
            var inputs = [];
            while ($row.index() < rowCount-1 && !inputs.length) {
                $row = $row.next();
                inputs = $row[0].cells[colNum].querySelectorAll('input[type=text],select');
            }
            if ($el.hasClass('datepicker')) $el.datepicker("hide");
            if (el.nodeName === 'INPUT') e.preventDefault();
            if (inputs.length) inputs[0].focus();
            break;
    }
}

function initMask(input) {
    var sep = ' ';
    var dec = '.';
    var empty = 'zero';
    var max =  '9999999999999';
    var min = '-9999999999999';
    if (input.classList.contains('maskMoney0')) $(input).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min, vMax: max, wEmpty: empty});
    if (input.classList.contains('maskMoney1')) $(input).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.9', vMax: max+'.9', wEmpty: empty});
    if (input.classList.contains('maskMoney2')) $(input).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.99', vMax: max+'.99', wEmpty: empty});
    if (input.classList.contains('maskMoney3')) $(input).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.999', vMax: max+'.999', wEmpty: empty});
    if (input.classList.contains('maskMoney4')) $(input).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.9999', vMax: max+'.9999', wEmpty: empty});
    if (input.classList.contains('maskMoney5')) $(input).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.99999', vMax: max+'.99999', wEmpty: empty});
    if (input.classList.contains('maskMoney6')) $(input).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.999999', vMax: max+'.999999', wEmpty: empty});
    if (input.classList.contains('maskMoney7')) $(input).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.9999999', vMax: max+'.9999999', wEmpty: empty});
    if (input.classList.contains('maskMoney8')) $(input).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.99999999', vMax: max+'.99999999', wEmpty: empty});
}
function initAllMasks() {
    var sep = ' ';
    var dec = '.';
    var empty = 'zero';
    var max =  '9999999999999';
    var min = '-9999999999999';
    $('input:text.maskMoney0').each(function() { $(this).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min, vMax: max, wEmpty: empty}); });
    $('input:text.maskMoney1').each(function() { $(this).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.9', vMax: max+'.9', wEmpty: empty}); });
    $('input:text.maskMoney2').each(function() { $(this).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.99', vMax: max+'.99', wEmpty: empty}); });
    $('input:text.maskMoney3').each(function() { $(this).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.999', vMax: max+'.999', wEmpty: empty}); });
    $('input:text.maskMoney4').each(function() { $(this).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.9999', vMax: max+'.9999', wEmpty: empty}); });
    $('input:text.maskMoney5').each(function() { $(this).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.99999', vMax: max+'.99999', wEmpty: empty}); });
    $('input:text.maskMoney6').each(function() { $(this).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.999999', vMax: max+'.999999', wEmpty: empty}); });
    $('input:text.maskMoney7').each(function() { $(this).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.9999999', vMax: max+'.9999999', wEmpty: empty}); });
    $('input:text.maskMoney8').each(function() { $(this).autoNumeric('init', {aSep: sep, aDec: dec, vMin: min+'.99999999', vMax: max+'.99999999', wEmpty: empty}); });
}
function destroyAllMasks() {
    $("input[type='text'][class*='maskMoney']").each(function() {
        var $input = $(this);
        try {
            var value = $input.autoNumeric('get');
            $input.autoNumeric('destroy');
            $input.val(value);
        } catch(e) {
            console.log("Not an autonumeric field: " + $input.attr("name"));
        }
    });
}
function initDatePicker(input) {
    $(input).datepicker($.datepicker.regional["ru"]);
}
function initAllDatePickers() {
    $('input:text.datepicker').each(function() {
        $(this).datepicker($.datepicker.regional["ru"]);
    });
}
function destroyAllDatePickers() {
    $('input:text.datepicker').datepicker("destroy");
}
function initTimePicker(input) {
    //$(input).timepicker($.timepicker.regional["ru"]);
    $('input.timepicker').timepicker({
        timeFormat: 'HH:mm:ss',
        regional: 'ru',
        secondText: 'Секунды',
        timeText: 'Время',
        closeText: 'Готово',
        currentText: 'Сейчас'
    });
}
function initAllTimePickers() {
    $('input:text.timepicker').each(function() {
        //$(this).timepicker($.timepicker.regional["ru"]);
        $('input.timepicker').timepicker({
            timeFormat: 'HH:mm:ss',
            regional: 'ru',
            secondText: 'Секунды',
            timeText: 'Время',
            closeText: 'Готово',
            currentText: 'Сейчас'
        });
    });
}
function destroyAllTimePickers() {
    $('input:text.timepicker').timepicker("destroy");
}
function updateLayout() {
    var wLayoutMain = PF('wLayoutMain');
    if (wLayoutMain) {
        var position = getPosition(wLayoutMain.jq[0]);
        var height = 320;
        var marginBottom = 64;
        var vpHeight = viewport().height;
        if (vpHeight && position.y < vpHeight) height = vpHeight - position.y - marginBottom;
        wLayoutMain.jq[0].style.height = height + 'px';
        wLayoutMain.layout.resizeAll();
        if (wLayoutMain.layout.south.options.initClosed) wLayoutMain.layout.resizers.south[0].className += ' ui-widget-content ui-corner-all'; // Walkaround for PF's bug
    }
}

function onElementFocus(el) {
    el.className = el.className + ' ' + 'focused';
}
function onElementBlur() {
    this.className = this.className.replace('focused','');
    /*if (this.readOnly){
        this.className = 'readOnly';
    }else{
        this.className = '';
    }*/
}

function compareStrings(javaStr, jsStr) {
    return javaStr === jsStr;
}

/**
 * Обозначает элемент как измененный
 * @param elem
 */
function makeDirty() {
    var elem = document.getElementById("hiDirtyReport");
    if (elem) {
        elem.value = 'true';
        fireChangeEvent(elem);
    }
}

/**
 * Обозначает изменненные элементы как "чистыми"
 */
function makeReportClear() {
    var elem = document.getElementById("hiDirtyReport");
    if (elem) {
        elem.value = 'false';
        fireChangeEvent(elem);
    }
}

/**
 * Излучает событье изменения
 * @param hiddenInput
 */
function fireChangeEvent(elem){
    if ((elem.matches && !elem.matches("input[type='hidden']")) || (elem.matchesSelector && !elem.matchesSelector("input[type='hidden']"))) {
        if (elem.onchange)
            elem.onchange();
    } else {
        var event = document.createEvent("Event");
        event.initEvent("change", false, false);
        elem.dispatchEvent(event);
    }
}

/**
 * Есть ли изменения в отчете
 * @returns {boolean}
 */
function isReportDirty() {
    var elem = document.getElementById("hiDirtyReport");
    if (elem) {
        return elem.value == 'true';
    }
    return false;
}

/**
 * Отслеживает изменение layout где находится основная форма "luForm"
 */
function onResizeFormLayout(height){
    /* изменяет высоту div где находится основная таблица для фиксации*/
    var elements = document.getElementsByClassName("wrapper");
    for(var i = 0; i < elements.length; i++){
        if(elements[i].style.height != null && elements[i].style.height != "") {
            elements[i].style.height = height + "px";
        }
    }
}
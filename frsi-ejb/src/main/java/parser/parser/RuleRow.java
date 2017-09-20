package parser.parser;

import parser.Group;
import parser.GroupKey;
import parser.Row;

import java.util.*;

/**
 * Created by nuriddin on 9/4/16.
 */
public class RuleRow implements Comparable<RuleRow> {
    private String recId;
    private boolean dynamicRow;//динамическая строка (определяется по аттрибуту keyValue правилы: если содержит букву n - динамический  )
    private RuleRow parent; //родительская строка
    private List<RuleRow> children; //дочерние строки
    private List<ParsedRule> groupingRules; //группирующие правила
    private List<ParsedRule> nonGroupingRules; //негруппирующие (с функциямии аггрегации или без них)
    private String sourceDynamicRowId;
    private boolean onlySelfReference;
    private String groupFormCode;
    private Date groupReportDate;
    private String groupContainer;
    private boolean hasAggrFunctions;
    private int priority;
    private Map<GroupKey, Group> groups;
    private Map<String, Set<String>> rowInputIds = null; // cached rowIds
    private Map<String, Row> rows;

    public RuleRow(String recId) {
        this.recId = recId;
        parent = null;
        children = new ArrayList<RuleRow>();
        groupingRules = new ArrayList<ParsedRule>();
        nonGroupingRules = new ArrayList<ParsedRule>();
        groups = new HashMap<GroupKey, Group>();
        rows = new HashMap<String, Row>();
    }

    public String getRecId() {
        return recId;
    }

    public void setRecId(String recId) {
        this.recId = recId;
    }

    public RuleRow getParent() {
        return parent;
    }

    public void setParent(RuleRow parent) {
        this.parent = parent;
    }

    public List<RuleRow> getChildren() {
        return children;
    }

    public void setChildren(List<RuleRow> children) {
        this.children = children;
    }

    public List<ParsedRule> getGroupingRules() {
        return groupingRules;
    }

    public void setGroupingRules(List<ParsedRule> groupingRules) {
        this.groupingRules = groupingRules;
    }

    public List<ParsedRule> getNonGroupingRules() {
        return nonGroupingRules;
    }

    public void setNonGroupingRules(List<ParsedRule> nonGroupingRules) {
        this.nonGroupingRules = nonGroupingRules;
    }

    public boolean isDynamicRow() {
        return dynamicRow;
    }

    public void setDynamicRow(boolean dynamicRow) {
        this.dynamicRow = dynamicRow;
    }

    public String getSourceDynamicRowId() {
        return sourceDynamicRowId;
    }

    public void setSourceDynamicRowId(String sourceDynamicRowId) {
        this.sourceDynamicRowId = sourceDynamicRowId;
    }

    public String getGroupFormCode() {
        return groupFormCode;
    }

    public void setGroupFormCode(String groupFormCode) {
        this.groupFormCode = groupFormCode;
    }

    public Date getGroupReportDate() {
        return groupReportDate;
    }

    public void setGroupReportDate(Date groupReportDate) {
        this.groupReportDate = groupReportDate;
    }

    public boolean isOnlySelfReference() {
        return onlySelfReference;
    }

    public void setOnlySelfReference(boolean onlySelfReference) {
        this.onlySelfReference = onlySelfReference;
    }

    public String getGroupContainer() {
        return groupContainer;
    }

    public void setGroupContainer(String groupContainer) {
        this.groupContainer = groupContainer;
    }

    public boolean hasAggrFunctions() {
        return hasAggrFunctions;
    }

    public void setHasAggrFunctions(boolean hasAggrFunctions) {
        this.hasAggrFunctions = hasAggrFunctions;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void addGroup(Group group) {
        groups.put(group.getKey(), group);
    }

    public Map<GroupKey, Group> getGroups() {
        return groups;
    }

    public Group getGroup(GroupKey groupKey){
        Group group;
        if (!groups.containsKey(groupKey)) {
            group = new Group(groupKey);
            groups.put(groupKey, group);
            group.setIndex(groups.size());
        } else {
            group = groups.get(groupKey);
        }
        return group;
    }

    public Map<String, Set<String>> getRowInputIds() {
        return rowInputIds;
    }

    public void setRowInputIds(Map<String, Set<String>> rowInputIds) {
        this.rowInputIds = rowInputIds;
    }

    public Map<String, Row> getRows() {
        return rows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RuleRow that = (RuleRow) o;

        return recId.equals(that.recId);

    }

    @Override
    public int hashCode() {
        return recId.hashCode();
    }

    @Override
    public int compareTo(RuleRow o) {
        if (this.priority > o.priority)
            return 1;
        else if (this.priority < o.priority)
            return -1;
        else
            return this.recId.compareTo(o.recId);
    }
}

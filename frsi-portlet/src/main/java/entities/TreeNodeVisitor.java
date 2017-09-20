package entities;

import org.primefaces.model.TreeNode;

public interface TreeNodeVisitor {
    boolean visit(TreeNode node);
}

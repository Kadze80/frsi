package util;

import entities.TreeNodeVisitor;
import org.primefaces.model.TreeNode;

public class Helper {

    public static void scanTreeNode(TreeNode node, TreeNodeVisitor visitor) {
        doScan(node, visitor);
    }

    private static boolean doScan(TreeNode node, TreeNodeVisitor visitor) {
        if (node != null) {
            if (visitor.visit(node)) {
                return true;
            }
            for (TreeNode child : node.getChildren()) {
                boolean stop = doScan(child, visitor);
                if (stop) {
                    return true;
                }
            }
        }
        return false;
    }
}

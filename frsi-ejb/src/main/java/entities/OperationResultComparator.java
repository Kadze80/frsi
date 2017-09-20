package entities;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by nuriddin on 8/17/16.
 */
public class OperationResultComparator implements Comparator<ApproveResultItem>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ApproveResultItem o1, ApproveResultItem o2) {
        if(o1.getResultType()==null){
            return -1;
        }
        if(o2.getResultType()==null){
            return 1;
        }
        if (!o1.getResultType().equals(o2.getResultType())) {
            if (o1.getResultType().equals(ApproveResultItem.ResultType.SUCCESS)) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }
}

package parser.functions;

/**
 * Created by nuriddin on 9/7/16.
 */
public abstract class AbstractFunction {

    protected ContextProvider context;

    public AbstractFunction(ContextProvider context) {
        this.context = context;
    }

    public abstract String getName();
}

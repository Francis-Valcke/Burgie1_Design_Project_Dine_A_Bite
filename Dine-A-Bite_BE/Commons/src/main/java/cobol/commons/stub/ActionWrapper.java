package cobol.commons.stub;

public class ActionWrapper {

    private Action action;
    private int priority;
    private boolean retry;
    private boolean success;

    public ActionWrapper(Action action, int priority, boolean retry) {
        this.action = action;
        this.priority = priority;
        this.retry = retry;
    }

    public Action getAction() {
        return action;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isRetry() {
        return retry;
    }

    public boolean isSuccess() {
        return success;
    }

    public void reset(){
        success = false;
    }

    public boolean execute(){
        success = action.execute();
        return success;
    }

}

package cobol.commons.stub;

public class ActionWrapper {

    private Action action;
    private int priority;

    public ActionWrapper(Action action, int priority) {
        this.action = action;
        this.priority = priority;
    }

    public Action getAction() {
        return action;
    }

    public int getPriority() {
        return priority;
    }

    public void execute(){
        action.execute();
    }

}

package cobol.commons.stub;

import java.io.IOException;

public interface Action {

    int PRIORITY_HIGHEST = 0;
    int PRIORITY_HIGH = 10;
    int PRIORITY_NORMAL = 20;
    int PRIORITY_LOW = 30;
    int PRIORITY_LOWEST = 40;

    void execute();

}

package cobol.services.standmanager.standhandler;

/**
 * Order:
 *  - Food: menu item (see Food class for more info)
 *  - clientid: id of client who ordered, (currently unused)
 *  - remtime: remaining time of order, this is initialized on Food preparation time. Used to calculate schedule time in scheduler.
 *  TODO: add flag for - "preparation"
 *                     - "done"
 *                     - "picked up"
 *
 */
public class Order {
    private Food f;
    private int clientid;
    private int remtime;

    public Order(Food f, int clientid) {
        this.f = f;
        this.clientid = clientid;
        this.remtime = f.getTime();
    }

    public int getClientid() {
        return clientid;
    }

    public Food getF() {
        return f;
    }

    public int getRemtime() {
        return remtime;
    }

    public void setRemtime(int remtime) {
        this.remtime = remtime;
    }
}


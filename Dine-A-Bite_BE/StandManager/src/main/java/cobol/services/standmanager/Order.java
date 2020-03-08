package cobol.services.standmanager;

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


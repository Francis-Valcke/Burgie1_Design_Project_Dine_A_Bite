package cobol.services.ordermanager.dbmenu;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

class StockId implements Serializable{
    int stand_id;
    int food_id;
}

@Entity @IdClass(StockId.class)
public class Stock {
    @Id
    private int stand_id;
    @Id
    private int food_id;
    private int count;


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStand_id() {
        return stand_id;
    }

    public void setStand_id(int stand_id) {
        this.stand_id = stand_id;
    }

    public int getFood_id() {
        return food_id;
    }

    public void setFood_id(int food_id) {
        this.food_id = food_id;
    }
}

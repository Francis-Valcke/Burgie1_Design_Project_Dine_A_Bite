package cobol.services.ordermanager.dbmenu;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="orderitem")
public class OrderItem implements Serializable {

    @ManyToOne
    @JoinColumn(name="id", nullable=false)
    private Order order;

    @Id
    @Column
    @GeneratedValue
    private int itemId;

    @Column
    private String foodname;
    @Column
    private int amount;


    public OrderItem(){

    }

    public OrderItem(String foodname, int amount) {
        this.foodname = foodname;
        this.amount = amount;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getFoodname() {
        return foodname;
    }

    public void setFoodname(String foodname) {
        this.foodname = foodname;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "foodname='" + foodname + '\'' +
                ", amount=" + amount +
                '}';
    }
}

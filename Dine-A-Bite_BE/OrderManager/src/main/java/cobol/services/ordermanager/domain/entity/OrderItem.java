package cobol.services.ordermanager.domain.entity;



import cobol.commons.order.CommonOrderItem;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="orderitem")
public class OrderItem implements Serializable {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id")
    private Order order;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int itemId;

    @Column
    private String foodName;
    @Column
    private int amount;


    public OrderItem(){

    }

    public OrderItem(String foodName, int amount) {
        this.foodName = foodName;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem )) return false;
        return itemId!=0 && itemId==(((OrderItem) o).getItemId());
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

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodname) {
        this.foodName = foodname;
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
                "foodName='" + foodName + '\'' +
                ", amount=" + amount +
                '}';
    }

    public CommonOrderItem asCommonOrderItem() {
        CommonOrderItem commonOrderItem = new CommonOrderItem(
                this.foodName,
                this.amount
        );

        return commonOrderItem;
    }
}

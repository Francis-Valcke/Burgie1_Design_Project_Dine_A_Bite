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


    // ---- Constructors ---- //

    public OrderItem(){

    }

    public OrderItem(CommonOrderItem orderItem, Order order) {
        this.amount= orderItem.getAmount();
        this.foodName= orderItem.getFoodName();
        this.order= order;
    }

    // ---- Transformers ---- //

    public CommonOrderItem asCommonOrderItem() {

        return new CommonOrderItem(
                this.foodName,
                this.amount
        );
    }


    // ---- Getters and Setters ---- //
    public void setOrder(Order order) {
        this.order = order;
    }

    public int getItemId() {
        return itemId;
    }

    public String getFoodName() {
        return foodName;
    }

    public int getAmount() { return amount; }

    // ---- Extra ---- //
    @Override
    public String toString() {
        return "OrderItem{" +
                "foodName='" + foodName + '\'' +
                ", amount=" + amount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem )) return false;
        return itemId!=0 && itemId==(((OrderItem) o).getItemId());
    }

    @Override
    public int hashCode() {
        return itemId;
    }
}

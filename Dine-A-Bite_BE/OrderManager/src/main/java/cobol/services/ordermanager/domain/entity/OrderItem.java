package cobol.services.ordermanager.domain.entity;



import cobol.commons.order.CommonOrderItem;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * This class is part of the Order object and specifies the amount of one specific
 * 'food' item and the price on the moment the order was placed
 */
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
    @Column
    private BigDecimal price;


    // ---- Constructors ---- //

    public OrderItem(){

    }

    public OrderItem(CommonOrderItem orderItem, Order order) {
        this.amount= orderItem.getAmount();
        this.price= orderItem.getPrice();
        this.foodName= orderItem.getFoodName();
        this.order= order;
    }

    // ---- Transformers ---- //

    public CommonOrderItem asCommonOrderItem() {

        return new CommonOrderItem(
                this.foodName,
                this.amount,
                this.price
        );
    }


    // ---- Getters and Setters ---- //
    public void setOrder(Order order) {
        this.order = order;
    }

    public int getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getFoodName() {
        return foodName;
    }

    // ---- Extra ---- //
    @Override
    public String toString() {
        return "OrderItem{" +
                "foodName='" + foodName + '\'' +
                ", amount=" + amount +
                ", price=" + price +
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

package cobol.services.dataset.domain.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@IdClass(Stock.StockId.class)
public class Stock {

    @Id
    private long standId;
    @Id
    private long foodId;

    private int count;

    //@ManyToOne()
    //@JoinColumn(name = "id")
    //@MapsId(value = "foodId")
    //private Food food;
    //
    //@ManyToOne()
    //@JoinColumn(name = "id")
    //@MapsId(value = "standId")
    //private Stand stand;

    public Stock() {
    }

    public Stock(long standId, long foodId, int count) {
        this.standId = standId;
        this.foodId = foodId;
        this.count = count;
    }

    //public Stock(Food food, Stand stand, int stock) {
    //    this.food = food;
    //    this.stand = stand;
    //    this.stock = stock;
    //}

    public static class StockId implements Serializable {

        private long standId;

        private long foodId;

        public StockId() {
        }

        public StockId(long standId, long foodId) {
            this.standId = standId;
            this.foodId = foodId;
        }
    }
}

package cobol.dataset.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Stock.StockId.class)
public class Stock {

    @Id
    private long standId;
    @Id
    private long foodId;

    private int stock;

    //@ManyToOne()
    //@JoinColumn(name = "id")
    //@MapsId(value = "foodId")
    //private Food food;
    //
    //@ManyToOne()
    //@JoinColumn(name = "id")
    //@MapsId(value = "standId")
    //private Stand stand;
    //
    //public Stock(Food food, Stand stand, int stock) {
    //    this.food = food;
    //    this.stand = stand;
    //    this.stock = stock;
    //}

    public static class StockId implements Serializable {

        private long standId;

        private long foodId;

        public StockId(long standId, long foodId) {
            this.standId = standId;
            this.foodId = foodId;
        }
    }
}

package cobol.commons.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    private String foodname;
    private String brandname;
    private String standname;
    private int amount;


    @Override
    public String toString() {
        return "OrderItem{" +
                "foodname='" + foodname + '\'' +
                ", brandname='" + brandname + '\'' +
                ", standname='" + standname + '\'' +
                ", amount=" + amount +
                '}';
    }
}

package cobol.dataset.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Food {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private long id;

    private String name;

    private String brandName;

    private String description;

    private float price;

    private int preparationTime;

    private int stock;

    @ElementCollection(fetch=FetchType.EAGER)
    @Column(name = "category_category")
    private List<String> category = new ArrayList<>();

}


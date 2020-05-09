package cobol.services.authentication.domain.entity;


import cobol.commons.domain.CommonUser;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Entity class that represents the user table in the database
 */
@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @NotNull
    @Column
    private String username;

    @NotNull
    @Column
    private String password;

    @Column
    private String email;

    @Column
    private String surname;

    @Column
    private String name;

    @Column
    private String customerId;

    @Builder.Default
    private BigDecimal balance = new BigDecimal(0);

    @Builder.Default
    private BigDecimal unconfirmedPayment = new BigDecimal(0);

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    @JoinTable(name = "user_role")
    @Column(name = "role_role")
    private List<String> roles = new ArrayList<>();

    public CommonUser asCommonUser() {
        return new CommonUser(username, password, email, surname, name, roles);
    }
}

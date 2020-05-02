package cobol.services.authentication.domain.entity;


import cobol.commons.security.CommonUser;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;


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

    @Column(nullable = false)
    private double balance = 0;


    private double unconfirmedPayment = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    @JoinTable(name = "user_role")
    @Column(name = "role_role")
    private List<String> roles = new ArrayList<>();

    public CommonUser asCommonUser() {
        return new CommonUser(username, password, email, surname, name, roles);
    }
}

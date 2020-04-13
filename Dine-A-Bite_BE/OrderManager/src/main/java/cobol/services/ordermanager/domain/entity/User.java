package cobol.services.ordermanager.domain.entity;


import cobol.commons.security.CommonUser;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;


/**
 * Entity class that represents the user table in the database
 */
@Entity
@Table(name = "user")
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class User extends CommonUser {

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

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    @JoinTable(name = "user_role")
    @Column(name = "role_role")
    private List<String> roles = new ArrayList<>();

    @ManyToMany(mappedBy = "owners")
    private List<Stand> stands = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(toList());
    }
}

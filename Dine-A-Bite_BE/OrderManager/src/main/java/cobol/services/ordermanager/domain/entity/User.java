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
import java.util.Objects;

import static java.util.stream.Collectors.toList;


/**
 * Entity class that represents the user table in the database
 */
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @NotNull
    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String email;

    @Column
    private String surname;

    @Column
    private String name;

    @ManyToMany(mappedBy = "owners")
    private List<Stand> stands = new ArrayList<>();

    public User(CommonUser user){
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.surname = user.getSurname();
        this.name = user.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}

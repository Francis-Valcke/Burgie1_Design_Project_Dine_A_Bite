package cobol.services.ordermanager.domain.entity;


import cobol.commons.domain.CommonUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;


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

    @JsonIgnore
    @Column
    private String password;

    @JsonIgnore
    @Column
    private String email;

    @JsonIgnore
    @Column
    private String surname;

    @JsonIgnore
    @Column
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "owners")
    private Set<Stand> stands = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Order> orders = new HashSet<>();

    public User(CommonUser user){
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.surname = user.getSurname();
        this.name = user.getName();
    }

    public User(String username) {
        this.username = username;
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

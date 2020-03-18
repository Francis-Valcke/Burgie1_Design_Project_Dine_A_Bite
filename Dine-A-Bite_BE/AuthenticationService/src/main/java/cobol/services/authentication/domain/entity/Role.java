package cobol.services.authentication.domain.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entity class that represents the role table in the database
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Role {

    @Id
    private String role;
}

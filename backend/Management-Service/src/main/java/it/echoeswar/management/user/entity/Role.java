package it.echoeswar.management.user.entity;

import it.echoeswar.management.user.enumerated.RoleName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles")
public class Role {

    @Id
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false, unique = true, name = "role")
    private RoleName roleName;

    @Builder.Default
    private boolean roleDefault = false;

    public Role(RoleName roleName) {
        this.roleName = roleName;
    }
}

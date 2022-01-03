package org.kexon5.recipes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="USERS")
public class User {
    @Id
    @Column(name = "user_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;

    @Column(name = "email")
    @Pattern(regexp = ".+@.+\\..+")
    private String email;

    @Column(name = "password")
    @NotBlank
    @Size(min = 8)
    private String password;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Recipe> recipesId = new ArrayList<>();
}

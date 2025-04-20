package shepelev.handmadeMarketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.awt.*;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    @Column(nullable = false)
    @Size(min = 2, message = "Ім'я користувача має бути не менше 2 символів")
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Size(min = 5, message = "Пароль має бути не менше 5 символів")
    private String password;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    private ERole role = ERole.USER_ROLE;

    private String city;

    private String country;
}
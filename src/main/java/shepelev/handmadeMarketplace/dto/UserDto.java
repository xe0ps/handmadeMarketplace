package shepelev.handmadeMarketplace.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String profileImageUrl;
    private String profileImageType;
}
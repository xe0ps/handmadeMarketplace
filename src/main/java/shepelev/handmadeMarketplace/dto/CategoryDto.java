package shepelev.handmadeMarketplace.dto;

import lombok.Data;
import shepelev.handmadeMarketplace.entity.Category;

@Data
public class CategoryDto {
    private Category category;
    private String base64Image;
    private String fileType;
}
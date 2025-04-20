package shepelev.handmadeMarketplace.dto;
import lombok.Data;

import java.util.List;

@Data
public class CitiesResponse {
    private List<String> cities;
}
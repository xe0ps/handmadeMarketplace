package shepelev.handmadeMarketplace.dto;
import lombok.Data;
import java.util.List;

@Data
public class CountryData {
    private String country;
    private List<String> cities;
}
package shepelev.handmadeMarketplace.dto;

import lombok.Data;

import java.util.List;

@Data
public class CountriesResponse {
    private boolean error;
    private String msg;
    private List<CountryData> data;
}
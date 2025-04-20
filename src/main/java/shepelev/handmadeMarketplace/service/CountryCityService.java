package shepelev.handmadeMarketplace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import shepelev.handmadeMarketplace.dto.CountriesResponse;

import java.util.List;

@Service
public class CountryCityService {

    private final RestTemplate restTemplate;
    @Value("${countriesApiUrl}")
    private String countriesApiUrl;

    public CountryCityService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CountriesResponse getCountriesAndCities() {
        ResponseEntity<CountriesResponse> response = restTemplate.getForEntity(countriesApiUrl, CountriesResponse.class);
        return response.getBody();
    }

    public ResponseEntity<List<String>> getCitiesByCountry(CountriesResponse countriesResponse, String country) {
        return countriesResponse.getData().stream()
                .filter(countryData -> countryData.getCountry().equals(country))
                .findFirst()
                .map(countryData -> new ResponseEntity<>(countryData.getCities(), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
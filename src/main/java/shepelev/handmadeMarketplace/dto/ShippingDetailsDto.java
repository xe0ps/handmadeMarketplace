package shepelev.handmadeMarketplace.dto;

import lombok.Data;

@Data
public class ShippingDetailsDto {
    private String firstName;
    private String lastName;
    private String phone;
    private String country;
    private String city;
    private String deliveryType;
    private String novaposhtaWarehouse; // Може бути null
    private String meestWarehouse; // Може бути null
}
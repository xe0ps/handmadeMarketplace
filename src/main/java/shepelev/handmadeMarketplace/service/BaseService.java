package shepelev.handmadeMarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.Product;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BaseService {
    @Autowired
    private ProductService productService;

    public void addLogoToModel(Model model) throws IOException, URISyntaxException {
        URI logoUri = Objects.requireNonNull(getClass().getClassLoader().getResource("images/logo.jpg")).toURI();
        Path logoPath = Paths.get(logoUri);
        byte[] logoBytes = Files.readAllBytes(logoPath);
        String logoBase64 = Base64.getEncoder().encodeToString(logoBytes);
        model.addAttribute("logoBase64", logoBase64);
        model.addAttribute("logoType", "image/jpeg");
    }

    public List<ProductDto> addProductsToModel() {
        return productService.getAllProducts().stream()
                .map(product -> {
                    ProductDto productDto = new ProductDto();
                    productDto.setProduct(product);
                    if (product.getImages() != null) {
                        List<String> base64Images = product.getImages().stream()
                                .map(image -> Base64.getEncoder().encodeToString(image.getData()))
                                .collect(Collectors.toList());
                        productDto.setBase64Images(base64Images);
                    }
                    return productDto;
                })
                .collect(Collectors.toList());
    }
}
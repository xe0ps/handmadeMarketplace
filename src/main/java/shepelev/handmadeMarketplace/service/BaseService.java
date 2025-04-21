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

// Анотація позначає клас як сервісний компонент у Spring-контейнері
@Service
public class BaseService {

    // Інʼєкція залежності сервісу для роботи з продуктами
    @Autowired
    private ProductService productService;

    /**
     * Додає логотип до моделі у вигляді Base64-рядка для відображення на сторінці.
     *
     * @param model модель, до якої додається логотип
     * @throws IOException якщо виникла помилка при зчитуванні файлу
     * @throws URISyntaxException якщо шлях до ресурсу некоректний
     */
    public void addLogoToModel(Model model) throws IOException, URISyntaxException {
        // Отримання URI до зображення логотипу
        URI logoUri = Objects.requireNonNull(getClass().getClassLoader().getResource("images/logo.jpg")).toURI();
        // Перетворення URI у шлях до файлу
        Path logoPath = Paths.get(logoUri);
        // Зчитування байтів зображення
        byte[] logoBytes = Files.readAllBytes(logoPath);
        // Кодування зображення у формат Base64
        String logoBase64 = Base64.getEncoder().encodeToString(logoBytes);
        // Додавання атрибутів логотипу до моделі
        model.addAttribute("logoBase64", logoBase64);
        model.addAttribute("logoType", "image/jpeg");
    }

    /**
     * Повертає список DTO-продуктів з зображеннями у форматі Base64.
     *
     * @return список DTO-обʼєктів для подальшого відображення
     */
    public List<ProductDto> addProductsToModel() {
        // Отримання всіх продуктів та перетворення їх у DTO
        return productService.getAllProducts().stream()
                .map(product -> {
                    ProductDto productDto = new ProductDto();
                    productDto.setProduct(product);
                    // Якщо у продукту є зображення — кодуємо їх у Base64
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

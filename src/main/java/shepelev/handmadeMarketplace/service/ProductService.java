package shepelev.handmadeMarketplace.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.Category;
import shepelev.handmadeMarketplace.entity.FavoriteProduct;
import shepelev.handmadeMarketplace.entity.Image;
import shepelev.handmadeMarketplace.entity.OrderItem;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.CategoryRepo;
import shepelev.handmadeMarketplace.repo.FavoriteProductRepo;
import shepelev.handmadeMarketplace.repo.OrderItemRepo;
import shepelev.handmadeMarketplace.repo.ProductRepo;
import shepelev.handmadeMarketplace.entity.Order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private FavoriteProductRepo favoriteProductRepo;
    @Autowired
    private OrderItemRepo orderItemRepo;

    @Transactional
    public Product saveProduct(Product product, String username, MultipartFile[] files) throws IOException {
        User user = userService.getUserByEmail(username);
        product.setUser(user);
        Category category = categoryRepo.findById(product.getCategory().getID()).orElse(null);
        product.setCategory(category);
        if (files != null && files.length > 0) {
            List<Image> images = new ArrayList<>();
            for (MultipartFile file : files) {
                Image image = imageService.saveImage(file);
                images.add(image);
            }
            product.setImages(images);

        }
        return productRepo.save(product);
    }

    @Transactional
    public List<Product> getUserProducts(String username) {
        User user = userService.getUserByEmail(username);
        List<Product> products = productRepo.findByUser(user);
        products.forEach(product -> {
            if (product.getImages() != null) {
                product.getImages().size();
            }
        });
        return products;
    }

    @Transactional
    public List<ProductDto> getUserProductDtosById(Long userId) {
        User user = userService.getUserById(userId);
        return productRepo.findByUser(user).stream()
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

    @Transactional
    public List<ProductDto> getUserProductDtos(String username) {
        return getUserProducts(username).stream()
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

    @Transactional
    public void deleteProductById(Long id) {
        Product product = productRepo.findById(id).orElse(null);
        if (product != null) {
            List<FavoriteProduct> favoriteProducts = favoriteProductRepo.findByProduct(product);
            favoriteProductRepo.deleteAll(favoriteProducts);
            List<OrderItem> orderItems = orderItemRepo.findByProduct(product);
            orderItemRepo.deleteAll(orderItems);
            productRepo.delete(product);
        }

    }

    @Transactional
    public void deleteProduct(Long productId, RedirectAttributes redirectAttributes) {
        try {
            deleteProductById(productId);
            redirectAttributes.addFlashAttribute("successMessage", "Товар успішно видалено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка видалення товару: " + e.getMessage());
        }
    }

    @Transactional
    public List<Product> getAllProducts() {
        List<Product> products = productRepo.findByIsVisibleTrue();
        products.forEach(product -> {
            if (product.getImages() != null) {
                product.getImages().size();
            }
            if (product.getUser() != null) {
                product.getUser().getUsername();
                product.getUser().getCity();
                product.getUser().getCountry();
            }

        });
        return products;
    }

    @Transactional
    public List<ProductDto> getAllProductDtos() {
        return getAllProducts().stream()
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

    @Transactional
    public Product getProductById(Long id) {
        Product product = productRepo.findById(id).orElse(null);
        if (product != null && product.getImages() != null) {
            product.getImages().size();
        }
        if (product != null && product.getUser() != null) {
            product.getUser().getUsername();
            product.getUser().getCity();
            product.getUser().getCountry();
        }
        return product;
    }

    @Transactional
    public boolean isProductOwner(Long productId, String username) {
        Product product = productRepo.findById(productId).orElse(null);
        if (product != null) {
            User user = userService.getUserByEmail(username);
            return product.getUser().getID().equals(user.getID());
        }
        return false;
    }

    @Transactional
    public ProductDto getProductDtoById(Long id) {
        Product product = productRepo.findById(id).orElse(null);
        if (product == null) return null;
        ProductDto productDto = new ProductDto();
        productDto.setProduct(product);
        if (product.getImages() != null) {
            List<String> base64Images = product.getImages().stream()
                    .map(image -> Base64.getEncoder().encodeToString(image.getData()))
                    .collect(Collectors.toList());
            productDto.setBase64Images(base64Images);
        }
        if (product.getUser() != null && product.getUser().getProfileImage() != null) {
            String base64ProfileImage = Base64.getEncoder().encodeToString(product.getUser().getProfileImage().getData());
            productDto.setBase64ProfileImage(base64ProfileImage);
            productDto.setProfileImageType(product.getUser().getProfileImage().getFileType());
        }
        return productDto;
    }

    @Transactional
    public List<Product> getProductsByCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId).orElse(null);
        List<Product> products = productRepo.findByCategoryAndIsVisibleTrue(category);
        products.forEach(product -> {
            if (product.getImages() != null) {
                product.getImages().size();
            }
            if (product.getUser() != null) {
                product.getUser().getUsername();
                product.getUser().getCity();
                product.getUser().getCountry();
            }
        });
        return products;
    }

    @Transactional
    public List<ProductDto> getProductsByCategoryDto(Long categoryId) {
        return getProductsByCategory(categoryId).stream()
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

    @Transactional
    public List<Product> searchProducts(String query) {
        List<Product> products = productRepo.findByNameContainingIgnoreCaseAndIsVisibleTrue(query);
        products.forEach(product -> {
            if (product.getImages() != null) {
                product.getImages().size();
            }
            if (product.getUser() != null) {
                product.getUser().getUsername();
                product.getUser().getCity();
                product.getUser().getCountry();
            }
        });
        return products;
    }

    @Transactional
    public List<ProductDto> searchProductDtos(String query) {
        return searchProducts(query).stream()
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

    @Transactional(readOnly = true)
    public List<ProductDto> getUserSoldProductDtos(String username) {
        User user = userService.getUserByEmail(username);
        if (user == null) return Collections.emptyList();

        // Знаходимо всі товари цього користувача, які позначені як невидимі (продані)
        List<Product> soldProducts = productRepo.findByUser(user).stream()
                .filter(product -> !product.isVisible())
                .collect(Collectors.toList());

        return soldProducts.stream()
                .map(product -> {
                    ProductDto productDto = new ProductDto();
                    productDto.setProduct(product);

                    // Завантажуємо зображення товару
                    if (product.getImages() != null) {
                        product.getImages().size(); // Trigger lazy loading
                        List<String> base64Images = product.getImages().stream()
                                .map(image -> Base64.getEncoder().encodeToString(image.getData()))
                                .collect(Collectors.toList());
                        productDto.setBase64Images(base64Images);
                    }

                    // Знаходимо деталі замовлення
                    // Шукаємо ОСТАННЄ замовлення для цього товару (припускаємо, що товар продається 1 раз)
                    OrderItem orderItem = orderItemRepo.findLastOrderItemByProduct(product).orElse(null); // Шукаємо останній OrderItem

                    if (orderItem != null && orderItem.getOrder() != null) {
                        Order order = orderItem.getOrder();
                        // Примусово завантажуємо дані покупця
                        productDto.setShippingDetailsFromOrder(order); // Використовуємо хелпер метод в DTO
                        logger.debug("Знайдено деталі замовлення ID {} для проданого товару ID {}", order.getId(), product.getID());
                    } else {
                        logger.warn("Не знайдено деталей замовлення для проданого товару ID {}", product.getID());
                    }

                    return productDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ProductDto> getUserActiveProductDtos(String username) {
        User user = userService.getUserByEmail(username);
        List<Product> products = productRepo.findByUserAndIsVisibleTrue(user);
        return products.stream()
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
package shepelev.handmadeMarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.FavoriteProduct;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.FavoriteProductRepo;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteProductService {
    @Autowired
    private FavoriteProductRepo favoriteProductRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;

    @Transactional
    public FavoriteProduct addProductToFavorite(Long productId, String username, RedirectAttributes redirectAttributes) {
        User user = userService.getUserByEmail(username);
        Product product = productService.getProductById(productId);
        FavoriteProduct favoriteProduct = favoriteProductRepo.findByUserAndProduct(user, product);
        if (favoriteProduct == null) {
            FavoriteProduct newFavoriteProduct = new FavoriteProduct();
            newFavoriteProduct.setUser(user);
            newFavoriteProduct.setProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Товар додано в обране.");
            return favoriteProductRepo.save(newFavoriteProduct);
        }
        return favoriteProduct;
    }

    @Transactional
    public List<FavoriteProduct> getUserFavoriteProducts(String username) {
        User user = userService.getUserByEmail(username);
        List<FavoriteProduct> favoriteProducts = favoriteProductRepo.findByUser(user);
        favoriteProducts.forEach(favoriteProduct -> {
            if (favoriteProduct.getProduct().getImages() != null) {
                favoriteProduct.getProduct().getImages().size();
            }

        });
        return favoriteProducts;
    }

    @Transactional
    public List<ProductDto> getUserFavoriteProductDtos(String username) {
        return getUserFavoriteProducts(username).stream()
                .map(favoriteProduct -> {
                    Product product = favoriteProduct.getProduct();
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
    public void deleteFromFavorites(Long productId, String username, RedirectAttributes redirectAttributes) {
        User user = userService.getUserByEmail(username);
        Product product = productService.getProductById(productId);
        FavoriteProduct favoriteProduct = favoriteProductRepo.findByUserAndProduct(user, product);
        if (favoriteProduct != null) {
            favoriteProductRepo.delete(favoriteProduct);
            redirectAttributes.addFlashAttribute("successMessage", "Товар видалено з обраного.");
        }
    }

    @Transactional
    public boolean isProductInFavorites(Long productId, String username) {
        User user = userService.getUserByEmail(username);
        Product product = productService.getProductById(productId);
        FavoriteProduct favoriteProduct = favoriteProductRepo.findByUserAndProduct(user, product);
        return favoriteProduct != null;
    }
}
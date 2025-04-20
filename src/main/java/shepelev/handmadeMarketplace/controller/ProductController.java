package shepelev.handmadeMarketplace.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.Category;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.service.CategoryService;
import shepelev.handmadeMarketplace.service.ProductService;
import shepelev.handmadeMarketplace.service.CartService;
import shepelev.handmadeMarketplace.service.FavoriteProductService;

import java.io.IOException;
import java.util.List;


@Controller
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private FavoriteProductService favoriteProductService;
    @Autowired
    private CartService cartService;


    @GetMapping("/product/add")
    public String showAddProductForm(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "addProduct";
    }

    @PostMapping("/product/add")
    public String addProduct(Product product,
                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                             Model model) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        productService.saveProduct(product, currentUsername, files);
        return "redirect:/profile";
    }

    @GetMapping("/product/my")
    public String showMyProducts(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        List<ProductDto> productDtos = productService.getUserActiveProductDtos(currentUsername);
        model.addAttribute("products", productDtos);
        return "myProducts";
    }

    @PostMapping("/product/delete")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String deleteProduct(@RequestParam("id") Long productId, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(productId, redirectAttributes);
        return "redirect:/product/my";
    }

    @GetMapping("/product/edit")
    public String showEditProductForm(@RequestParam("id") Long productId, Model model) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return "redirect:/profile";
        }
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "editProduct";
    }

    @GetMapping("/product/view")
    @Transactional
    public String showProductDetails(@RequestParam("id") Long productId, Model model, HttpSession session) {
        ProductDto productDto = productService.getProductDtoById(productId);
        if (productDto == null) {
            return "redirect:/";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isOwner = productService.isProductOwner(productId, currentUsername);
        model.addAttribute("isOwner", isOwner);
        boolean isInFavorites = favoriteProductService.isProductInFavorites(productId, currentUsername);
        model.addAttribute("isInFavorites", isInFavorites);
        boolean isInCart = cartService.isProductInCart(session, productId);
        model.addAttribute("isInCart", isInCart);
        if (productDto.getProduct().getUser() != null) {
            model.addAttribute("sellerId", productDto.getProduct().getUser().getID());
        }
        model.addAttribute("product", productDto);
        return "productDetails";
    }

    @GetMapping("/category/products")
    public String showProductsByCategory(@RequestParam("id") Long categoryId, Model model) {
        Category category = categoryService.getAllCategories().stream().filter(cat -> cat.getID().equals(categoryId)).findFirst().orElse(null);
        if (category == null) {
            return "redirect:/";
        }
        List<ProductDto> productDtos =  productService.getProductsByCategoryDto(categoryId);
        model.addAttribute("products", productDtos);
        model.addAttribute("categoryName", category.getName());
        return "categoryProducts";
    }

    @GetMapping("/product/search")
    @ResponseBody
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam("query") String query, Model model) {
        List<ProductDto> productDtos = productService.searchProductDtos(query);
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

}
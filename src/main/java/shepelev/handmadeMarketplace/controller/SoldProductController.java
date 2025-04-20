package shepelev.handmadeMarketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.service.ProductService;

import java.util.List;

@Controller
public class SoldProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/product/my/sold")
    public String showMySoldProducts(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        List<ProductDto> productDtos = productService.getUserSoldProductDtos(currentUsername);
        model.addAttribute("products", productDtos);
        return "mySoldProducts";
    }
}
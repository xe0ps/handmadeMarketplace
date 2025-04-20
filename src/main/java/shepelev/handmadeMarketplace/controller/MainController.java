package shepelev.handmadeMarketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.service.ProductService;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String showMainPage(Model model) {
        model.addAttribute("title", "HandMade Store");
        return "main";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
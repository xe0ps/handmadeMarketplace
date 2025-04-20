package shepelev.handmadeMarketplace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.service.AdminService;
import shepelev.handmadeMarketplace.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Base64;
@Controller
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private AdminService adminService;
    @Autowired
    private ProductService productService;


    @GetMapping("/admin")
    public String showAdminPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        logger.info("Admin user {} is attempting to access admin page", currentUsername);
        List<User> users = adminService.getAllUsers();
        model.addAttribute("users", users);
        List<ProductDto> productDtos = productService.getAllProductDtos();
        model.addAttribute("products", productDtos);
        return "admin";
    }
    @PostMapping("/admin/deleteUser")
    public String deleteUser(@RequestParam("id") Long userId, RedirectAttributes redirectAttributes) {
        adminService.deleteUser(userId, redirectAttributes);
        return "redirect:/admin";
    }
    @PostMapping("/admin/deleteProduct")
    public String deleteProduct(@RequestParam("id") Long productId, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(productId, redirectAttributes);
        return "redirect:/admin";
    }
}
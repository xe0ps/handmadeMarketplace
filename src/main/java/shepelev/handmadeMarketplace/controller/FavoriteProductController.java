package shepelev.handmadeMarketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.service.FavoriteProductService;

import java.util.List;

@Controller
public class FavoriteProductController {

    @Autowired
    private FavoriteProductService favoriteProductService;

    @GetMapping("/favorites")
    public String showFavoriteProducts(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        List<ProductDto> productDtos = favoriteProductService.getUserFavoriteProductDtos(currentUsername);
        model.addAttribute("products", productDtos);
        return "favoriteProducts";
    }

    @PostMapping("/product/addFavorite")
    public String addProductToFavorites(@RequestParam("id") Long productId, RedirectAttributes redirectAttributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        favoriteProductService.addProductToFavorite(productId, currentUsername, redirectAttributes);
        return "redirect:/product/view?id=" + productId;
    }
    @PostMapping("/favorites/delete")
    public String deleteFromFavorites(@RequestParam("id") Long productId, RedirectAttributes redirectAttributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        favoriteProductService.deleteFromFavorites(productId, currentUsername, redirectAttributes);
        return "redirect:/favorites";
    }
}
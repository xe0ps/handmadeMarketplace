package shepelev.handmadeMarketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller; // Переконайтесь, що це імпортовано
import org.springframework.transaction.annotation.Transactional; // Імпорт Transactional
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.User; // Імпорт User
import shepelev.handmadeMarketplace.service.BaseService;
import shepelev.handmadeMarketplace.service.ProductService; // Додано, бо використовується в BaseService
import shepelev.handmadeMarketplace.service.UserService; // Імпорт UserService

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64; // Імпорт Base64
import java.util.List;

@ControllerAdvice
public class BaseController {

    @Autowired
    private BaseService baseService;

    @Autowired
    private UserService userService;

    @ModelAttribute
    @Transactional(readOnly = true)
    public void addCommonAttributes(Model model) throws IOException, URISyntaxException {
        baseService.addLogoToModel(model);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);

        model.addAttribute("isAuthenticated", isAuthenticated);

        if (isAuthenticated) {
            String userEmail = authentication.getName();
            User currentUser = userService.getUserByEmail(userEmail);

            if (currentUser != null) {
                model.addAttribute("headerUsername", currentUser.getUsername());

                if (currentUser.getProfileImage() != null) {
                    byte[] avatarBytes = currentUser.getProfileImage().getData();
                    if (avatarBytes != null && avatarBytes.length > 0) {
                        String base64Avatar = Base64.getEncoder().encodeToString(avatarBytes);
                        model.addAttribute("headerUserAvatar", base64Avatar);
                        model.addAttribute("headerUserAvatarType", currentUser.getProfileImage().getFileType());
                    }
                }
            }
        }
    }

    @ModelAttribute("products")
    @Transactional(readOnly = true)
    public List<ProductDto> addProducts(Model model) {
        return baseService.addProductsToModel();
    }
}
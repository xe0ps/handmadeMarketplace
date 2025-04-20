package shepelev.handmadeMarketplace.controller;

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
import shepelev.handmadeMarketplace.dto.CountriesResponse;
import shepelev.handmadeMarketplace.entity.Category;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.service.CategoryService;
import shepelev.handmadeMarketplace.service.CountryCityService;
import shepelev.handmadeMarketplace.service.UserService;

import java.io.IOException;
import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private CountryCityService countryCityService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/profile")
    @Transactional
    public String profile(Model model) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User user = userService.getUserByEmail(currentUsername);
        userService.addProfileImageToModel(user, model);

        CountriesResponse countriesResponse = countryCityService.getCountriesAndCities();
        model.addAttribute("countries", countriesResponse.getData());
        if (user != null) {
            model.addAttribute("selectedCity", user.getCity());
            model.addAttribute("selectedCountry", user.getCountry());
        }
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "profile";
    }

    @GetMapping("/profile/cities")
    @ResponseBody
    public ResponseEntity<List<String>> getCities(@RequestParam("country") String country) {
        CountriesResponse countriesResponse = countryCityService.getCountriesAndCities();
        return countryCityService.getCitiesByCountry(countriesResponse, country);
    }

    @PostMapping("/profile/update")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String updateUserProfile(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "country", required = false) String country,
            RedirectAttributes redirectAttributes) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        userService.updateUserProfile(currentUsername, profileImage, password, city, country, redirectAttributes);
        return "redirect:/profile";
    }
}
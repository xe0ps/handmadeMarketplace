package shepelev.handmadeMarketplace.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.entity.ERole;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.entity.Image;
import shepelev.handmadeMarketplace.repo.UserRepo;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@AllArgsConstructor
@Data
public class UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ImageService imageService;

    private final BCryptPasswordEncoder encoder;

    @Transactional
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    @Transactional
    public User getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    @Transactional
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User saveUser(User user) {
        if (user.getID() == null && !user.getPassword().startsWith("$2a$")) {
            String encodedPassword = encoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        }

        return userRepo.save(user);
    }

    public User createAdmin(User user) {
        String encodedPassword = encoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole(ERole.ADMIN_ROLE);
        return userRepo.save(user);
    }

    public void updateUserProfile(String currentUsername, MultipartFile file, String password, String city, String country, RedirectAttributes redirectAttributes) throws IOException {
        try {
            User user = getUserByEmail(currentUsername);
            if (user != null) {
                updateUserProfile(user.getID(), file, password, city, country);
                redirectAttributes.addFlashAttribute("successMessage", "Профіль успішно оновлено.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Користувача не знайдено.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка оновлення профілю: " + e.getMessage());
        }
    }

    public User updateUserProfile(Long userId, MultipartFile file, String password, String city, String country) throws IOException {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (password != null && !password.isEmpty() && !password.startsWith("$2a$")) {
            String encodedPassword = encoder.encode(password);
            user.setPassword(encodedPassword);
        }
        if (file != null && !file.isEmpty()) {
            Image image = imageService.saveImage(file);
            user.setProfileImage(image);
        }
        user.setCity(city);
        user.setCountry(country);
        return userRepo.save(user);
    }

    public void addProfileImageToModel(User user, Model model) {
        if (user != null && user.getProfileImage() != null) {
            Image image = user.getProfileImage();
            String base64Image = Base64.getEncoder().encodeToString(image.getData());
            model.addAttribute("profileImage", base64Image);
            model.addAttribute("fileType", image.getFileType());
        }
    }
}
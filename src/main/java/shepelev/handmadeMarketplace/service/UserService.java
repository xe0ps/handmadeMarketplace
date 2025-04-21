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

/**
 * Сервіс для управління користувачами, включаючи їх створення, оновлення профілю та отримання даних.
 * Використовується для взаємодії з репозиторієм користувачів та обробки профільних зображень.
 */
@Service
@AllArgsConstructor
@Data
public class UserService {

    /** Репозиторій для доступу до даних користувачів. */
    @Autowired
    private UserRepo userRepo;

    /** Сервіс для збереження та обробки зображень. */
    @Autowired
    private ImageService imageService;

    /** Кодувальник паролів для забезпечення безпеки. */
    private final BCryptPasswordEncoder encoder;

    /**
     * Отримує користувача за його email.
     *
     * @param email Електронна пошта користувача.
     * @return Об'єкт користувача або null, якщо користувача не знайдено.
     */
    @Transactional
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    /**
     * Отримує користувача за його ідентифікатором.
     *
     * @param id Ідентифікатор користувача.
     * @return Об'єкт користувача або null, якщо користувача не знайдено.
     */
    @Transactional
    public User getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    /**
     * Отримує список усіх користувачів.
     *
     * @return Список усіх користувачів у системі.
     */
    @Transactional
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    /**
     * Зберігає користувача в базі даних, кодуючи пароль, якщо він ще не закодований.
     *
     * @param user Об'єкт користувача для збереження.
     * @return Збережений об'єкт користувача.
     */
    public User saveUser(User user) {
        // Кодування пароля для нових користувачів, якщо він не закодований
        if (user.getID() == null && !user.getPassword().startsWith("$2a$")) {
            String encodedPassword = encoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        }
        return userRepo.save(user);
    }

    /**
     * Створює адміністратора з закодованим паролем і роллю ADMIN_ROLE.
     *
     * @param user Об'єкт користувача для створення адміністратора.
     * @return Збережений об'єкт адміністратора.
     */
    public User createAdmin(User user) {
        // Кодування пароля та встановлення ролі адміністратора
        String encodedPassword = encoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole(ERole.ADMIN_ROLE);
        return userRepo.save(user);
    }

    /**
     * Оновлює профіль користувача за його email, обробляючи помилки та додаючи повідомлення.
     *
     * @param currentUsername Поточне ім'я користувача (email).
     * @param file            Файл із новим профільним зображенням (може бути null).
     * @param password        Новий пароль (може бути порожнім).
     * @param city            Нове місто користувача.
     * @param country         Нова країна користувача.
     * @param redirectAttributes Об'єкт для додавання повідомлень про успіх або помилку.
     * @throws IOException У разі помилки обробки файлу зображення.
     */
    public void updateUserProfile(String currentUsername, MultipartFile file, String password, String city, String country, RedirectAttributes redirectAttributes) throws IOException {
        try {
            // Пошук користувача за email
            User user = getUserByEmail(currentUsername);
            if (user != null) {
                // Оновлення профілю за ідентифікатором користувача
                updateUserProfile(user.getID(), file, password, city, country);
                redirectAttributes.addFlashAttribute("successMessage", "Профіль успішно оновлено.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Користувача не знайдено.");
            }
        } catch (Exception e) {
            // Обробка помилок із повідомленням для користувача
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка оновлення профілю: " + e.getMessage());
        }
    }

    /**
     * Оновлює профіль користувача за його ідентифікатором.
     *
     * @param userId   Ідентифікатор користувача.
     * @param file     Файл із новим профільним зображенням (може бути null).
     * @param password Новий пароль (може бути порожнім).
     * @param city     Нове місто користувача.
     * @param country  Нова країна користувача.
     * @return Оновлений об'єкт користувача.
     * @throws IOException      У разі помилки обробки файлу зображення.
     * @throws RuntimeException Якщо користувача не знайдено.
     */
    public User updateUserProfile(Long userId, MultipartFile file, String password, String city, String country) throws IOException {
        // Пошук користувача за ідентифікатором
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Оновлення пароля, якщо він не порожній і не закодований
        if (password != null && !password.isEmpty() && !password.startsWith("$2a$")) {
            String encodedPassword = encoder.encode(password);
            user.setPassword(encodedPassword);
        }

        // Оновлення профільного зображення, якщо файл надано
        if (file != null && !file.isEmpty()) {
            Image image = imageService.saveImage(file);
            user.setProfileImage(image);
        }

        // Оновлення міста та країни
        user.setCity(city);
        user.setCountry(country);

        // Збереження оновленого користувача
        return userRepo.save(user);
    }

    /**
     * Додає профільне зображення користувача до моделі для відображення.
     *
     * @param user Об'єкт користувача.
     * @param model Модель для передачі даних у представлення.
     */
    public void addProfileImageToModel(User user, Model model) {
        // Перевірка наявності користувача та його профільного зображення
        if (user != null && user.getProfileImage() != null) {
            Image image = user.getProfileImage();
            // Кодування зображення в Base64 для відображення у браузері
            String base64Image = Base64.getEncoder().encodeToString(image.getData());
            model.addAttribute("profileImage", base64Image);
            model.addAttribute("fileType", image.getFileType());
        }
    }
}
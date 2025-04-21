package shepelev.handmadeMarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.UserRepo;

import java.util.List;

// Анотація вказує, що клас є сервісом і може бути використаний як компонент Spring
@Service
public class AdminService {

    // Інʼєкція залежності — репозиторій для роботи з користувачами
    @Autowired
    private UserRepo userRepo;

    // Метод повертає список усіх користувачів з бази даних
    @Transactional
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // Метод видаляє користувача за ID у новій транзакції
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteUserById(Long id) {
        userRepo.deleteById(id);
    }

    // Метод видаляє користувача з повідомленням про успіх або помилку
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteUser(Long userId, RedirectAttributes redirectAttributes) {
        try {
            // Спроба видалити користувача
            deleteUserById(userId);
            // Додавання флеш-повідомлення про успішне видалення
            redirectAttributes.addFlashAttribute("successMessage", "Користувача успішно видалено.");
        } catch (Exception e) {
            // У випадку помилки — додавання флеш-повідомлення з текстом помилки
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка видалення користувача: " + e.getMessage());
        }
    }
}

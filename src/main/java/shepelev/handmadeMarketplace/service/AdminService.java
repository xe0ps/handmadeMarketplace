package shepelev.handmadeMarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.UserRepo;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepo userRepo;

    @Transactional
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteUserById(Long id) {
        userRepo.deleteById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteUser(Long userId, RedirectAttributes redirectAttributes) {
        try {
            deleteUserById(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Користувача успішно видалено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка видалення користувача: " + e.getMessage());
        }
    }
}
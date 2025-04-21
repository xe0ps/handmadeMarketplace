package shepelev.handmadeMarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shepelev.handmadeMarketplace.dto.CategoryDto;
import shepelev.handmadeMarketplace.entity.Category;
import shepelev.handmadeMarketplace.repo.CategoryRepo;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

// Анотація вказує, що клас є сервісом Spring
@Service
public class CategoryService {

    // Інʼєкція залежності — репозиторій для категорій
    @Autowired
    private CategoryRepo categoryRepo;

    /**
     * Отримує всі категорії з бази даних.
     * Якщо категорія має зображення — викликається метод отримання його байтів (необов’язково).
     *
     * @return список усіх категорій
     */
    @Transactional
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepo.findAll();
        // Ітерація по кожній категорії для перевірки наявності зображення
        categories.forEach(category -> {
            if (category.getImage() != null) {
                category.getImage().getData(); // Можливо використовується для ініціалізації proxy-обʼєкта
            }
        });
        return categories;
    }

    /**
     * Перетворює всі категорії у DTO-обʼєкти, включаючи зображення у форматі Base64.
     *
     * @return список DTO категорій
     */
    @Transactional
    public List<CategoryDto> getAllCategoryDtos(){
        return getAllCategories().stream()
                .map(category -> {
                    CategoryDto categoryDto = new CategoryDto();
                    categoryDto.setCategory(category);
                    // Якщо у категорії є зображення — кодуємо його у Base64
                    if (category.getImage() != null) {
                        String base64Image = Base64.getEncoder().encodeToString(category.getImage().getData());
                        categoryDto.setBase64Image(base64Image);
                        categoryDto.setFileType(category.getImage().getFileType());
                    }
                    return categoryDto;
                }).collect(Collectors.toList());
    }

    /**
     * Зберігає список категорій до бази даних.
     *
     * @param categories список категорій для збереження
     */
    @Transactional
    public void saveAllCategories(List<Category> categories){
        categoryRepo.saveAll(categories);
    }
}

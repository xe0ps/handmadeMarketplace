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

@Service
public class CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Transactional
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepo.findAll();
        categories.forEach(category -> {
            if (category.getImage() != null) {
                category.getImage().getData();
            }
        });
        return categories;
    }

    @Transactional
    public List<CategoryDto> getAllCategoryDtos(){
        return   getAllCategories().stream()
                .map(category -> {
                    CategoryDto categoryDto = new CategoryDto();
                    categoryDto.setCategory(category);
                    if (category.getImage() != null) {
                        String base64Image = Base64.getEncoder().encodeToString(category.getImage().getData());
                        categoryDto.setBase64Image(base64Image);
                        categoryDto.setFileType(category.getImage().getFileType());
                    }
                    return categoryDto;
                }).collect(Collectors.toList());
    }
    @Transactional
    public void saveAllCategories(List<Category> categories){
        categoryRepo.saveAll(categories);
    }
}
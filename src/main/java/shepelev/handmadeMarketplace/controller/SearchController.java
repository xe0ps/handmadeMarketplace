package shepelev.handmadeMarketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import shepelev.handmadeMarketplace.dto.CategoryDto;
import shepelev.handmadeMarketplace.service.CategoryService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/search")
    public String showSearchPage(Model model) throws IOException, URISyntaxException {
        List<CategoryDto> categoryDtos = categoryService.getAllCategoryDtos();
        model.addAttribute("categories", categoryDtos);

        return "search";
    }
}
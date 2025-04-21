package shepelev.handmadeMarketplace.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
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
import shepelev.handmadeMarketplace.dto.ProductDto;
import shepelev.handmadeMarketplace.entity.Category;
import shepelev.handmadeMarketplace.entity.Product;
import shepelev.handmadeMarketplace.service.CategoryService;
import shepelev.handmadeMarketplace.service.ProductService;
import shepelev.handmadeMarketplace.service.CartService;
import shepelev.handmadeMarketplace.service.FavoriteProductService;

import java.io.IOException;
import java.util.List;

/**
 * Контролер для управління товарами.
 * Відповідає за додавання, редагування, видалення, перегляд і пошук товарів, а також відображення товарів за категоріями.
 */
@Controller
@Slf4j
public class ProductController {

    /** Сервіс для роботи з товарами, зокрема їх додаванням, видаленням і пошуком. */
    @Autowired
    private ProductService productService;

    /** Сервіс для роботи з категоріями товарів. */
    @Autowired
    private CategoryService categoryService;

    /** Сервіс для роботи з улюбленими товарами користувача. */
    @Autowired
    private FavoriteProductService favoriteProductService;

    /** Сервіс для роботи з кошиком користувача. */
    @Autowired
    private CartService cartService;

    /**
     * Відображає форму для додавання нового товару.
     *
     * @param model Модель для передачі даних у представлення.
     * @return Назва шаблону сторінки ("addProduct").
     */
    @GetMapping("/product/add")
    public String showAddProductForm(Model model) {
        // Отримання списку всіх категорій для вибору при додаванні товару
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // Повернення шаблону для форми додавання товару
        return "addProduct";
    }

    /**
     * Додає новий товар.
     *
     * @param product Об'єкт товару, що додається.
     * @param files Масив файлів (зображень) для товару.
     * @param model Модель для передачі даних у представлення.
     * @return URL для перенаправлення на сторінку профілю.
     * @throws IOException У разі помилки під час обробки файлів.
     */
    @PostMapping("/product/add")
    public String addProduct(Product product,
                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                             Model model) throws IOException {
        // Отримання поточного користувача з контексту безпеки
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Збереження товару через сервіс
        productService.saveProduct(product, currentUsername, files);

        // Перенаправлення на сторінку профілю після додавання товару
        return "redirect:/profile";
    }

    /**
     * Відображає список товарів поточного користувача.
     *
     * @param model Модель для передачі даних у представлення.
     * @return Назва шаблону сторінки ("myProducts").
     */
    @GetMapping("/product/my")
    public String showMyProducts(Model model) {
        // Отримання поточного користувача з контексту безпеки
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Отримання списку активних товарів користувача у форматі DTO
        List<ProductDto> productDtos = productService.getUserActiveProductDtos(currentUsername);
        model.addAttribute("products", productDtos);

        // Повернення шаблону для відображення товарів користувача
        return "myProducts";
    }

    /**
     * Видаляє товар за його ідентифікатором.
     *
     * @param productId Ідентифікатор товару, який потрібно видалити.
     * @param redirectAttributes Об'єкт для додавання повідомлень після перенаправлення.
     * @return URL для перенаправлення на сторінку з товарами користувача.
     */
    @PostMapping("/product/delete")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String deleteProduct(@RequestParam("id") Long productId, RedirectAttributes redirectAttributes) {
        // Виклик сервісу для видалення товару
        productService.deleteProduct(productId, redirectAttributes);

        // Перенаправлення на сторінку з товарами користувача після видалення
        return "redirect:/product/my";
    }

    /**
     * Відображає форму для редагування товару.
     *
     * @param productId Ідентифікатор товару, який потрібно редагувати.
     * @param model Модель для передачі даних у представлення.
     * @return Назва шаблону сторінки ("editProduct") або перенаправлення на профіль, якщо товар не знайдено.
     */
    @GetMapping("/product/edit")
    public String showEditProductForm(@RequestParam("id") Long productId, Model model) {
        // Отримання товару за ідентифікатором
        Product product = productService.getProductById(productId);

        // Якщо товар не знайдено, перенаправляємо на профіль
        if (product == null) {
            return "redirect:/profile";
        }

        // Отримання списку всіх категорій для вибору при редагуванні
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);

        // Повернення шаблону для форми редагування товару
        return "editProduct";
    }

    /**
     * Відображає деталі товару.
     *
     * @param productId Ідентифікатор товару, деталі якого потрібно відобразити.
     * @param model Модель для передачі даних у представлення.
     * @param session Сесія для перевірки наявності товару в кошику.
     * @return Назва шаблону сторінки ("productDetails") або перенаправлення на головну, якщо товар не знайдено.
     */
    @GetMapping("/product/view")
    @Transactional
    public String showProductDetails(@RequestParam("id") Long productId, Model model, HttpSession session) {
        // Отримання товару у форматі DTO за ідентифікатором
        ProductDto productDto = productService.getProductDtoById(productId);

        // Якщо товар не знайдено, перенаправляємо на головну сторінку
        if (productDto == null) {
            return "redirect:/";
        }

        // Отримання поточного користувача з контексту безпеки
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Перевірка, чи є користувач власником товару
        boolean isOwner = productService.isProductOwner(productId, currentUsername);
        model.addAttribute("isOwner", isOwner);

        // Перевірка, чи є товар у списку улюблених
        boolean isInFavorites = favoriteProductService.isProductInFavorites(productId, currentUsername);
        model.addAttribute("isInFavorites", isInFavorites);

        // Перевірка, чи є товар у кошику
        boolean isInCart = cartService.isProductInCart(session, productId);
        model.addAttribute("isInCart", isInCart);

        // Додавання ID продавця, якщо товар має власника
        if (productDto.getProduct().getUser() != null) {
            model.addAttribute("sellerId", productDto.getProduct().getUser().getID());
        }

        // Додавання даних товару до моделі
        model.addAttribute("product", productDto);

        // Повернення шаблону для відображення деталей товару
        return "productDetails";
    }

    /**
     * Відображає товари за категорією.
     *
     * @param categoryId Ідентифікатор категорії, товари якої потрібно відобразити.
     * @param model Модель для передачі даних у представлення.
     * @return Назва шаблону сторінки ("categoryProducts") або перенаправлення на головну, якщо категорію не знайдено.
     */
    @GetMapping("/category/products")
    public String showProductsByCategory(@RequestParam("id") Long categoryId, Model model) {
        // Пошук категорії за ідентифікатором
        Category category = categoryService.getAllCategories().stream()
                .filter(cat -> cat.getID().equals(categoryId))
                .findFirst()
                .orElse(null);

        // Якщо категорію не знайдено, перенаправляємо на головну сторінку
        if (category == null) {
            return "redirect:/";
        }

        // Отримання списку товарів категорії у форматі DTO
        List<ProductDto> productDtos = productService.getProductsByCategoryDto(categoryId);
        model.addAttribute("products", productDtos);
        model.addAttribute("categoryName", category.getName());

        // Повернення шаблону для відображення товарів категорії
        return "categoryProducts";
    }

    /**
     * Виконує пошук товарів за запитом.
     *
     * @param query Рядок запиту для пошуку товарів.
     * @param model Модель для передачі даних у представлення.
     * @return Список товарів у форматі DTO у вигляді HTTP-відповіді.
     */
    @GetMapping("/product/search")
    @ResponseBody
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam("query") String query, Model model) {
        // Пошук товарів за запитом у форматі DTO
        List<ProductDto> productDtos = productService.searchProductDtos(query);

        // Повернення результатів пошуку у вигляді HTTP-відповіді
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }
}
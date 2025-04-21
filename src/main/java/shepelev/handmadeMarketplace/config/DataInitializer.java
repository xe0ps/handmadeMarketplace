package shepelev.handmadeMarketplace.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shepelev.handmadeMarketplace.entity.Category;
import shepelev.handmadeMarketplace.entity.ERole;
import shepelev.handmadeMarketplace.entity.Image;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.CategoryRepo;
import shepelev.handmadeMarketplace.repo.ImageRepo;
import shepelev.handmadeMarketplace.repo.UserRepo;
import shepelev.handmadeMarketplace.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Компонент для ініціалізації початкових даних у базі даних.
 * Виконує завантаження зображень для категорій та створення категорій і адміністратора під час запуску додатка.
 */
@Component
public class DataInitializer {

    /** Репозиторій для роботи з категоріями. */
    @Autowired
    private CategoryRepo categoryRepo;

    /** Репозиторій для роботи із зображеннями. */
    @Autowired
    private ImageRepo imageRepo;

    /** Репозиторій для роботи з користувачами. */
    @Autowired
    private UserRepo userRepo;

    /** Сервіс для роботи з користувачами, зокрема для створення адміністратора. */
    @Autowired
    private UserService userService;

    /**
     * Ініціалізує початкові дані після створення біна.
     * Завантажує зображення для категорій, створює категорії та адміністратора, якщо вони ще не існують у базі даних.
     *
     * @throws IOException У разі помилки під час читання файлів зображень.
     * @throws URISyntaxException У разі помилки під час обробки URI ресурсів.
     */
    @PostConstruct
    public void initializeData() throws IOException, URISyntaxException {
        // Ініціалізація категорій, якщо вони ще не створені
        if (categoryRepo.count() == 0) {
            // Завантаження зображень для категорій із ресурсів
            URI uri1 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat1.jpg")).toURI();
            Path path1 = Paths.get(uri1);
            byte[] image1Bytes = Files.readAllBytes(path1);

            URI uri2 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat2.jpg")).toURI();
            Path path2 = Paths.get(uri2);
            byte[] image2Bytes = Files.readAllBytes(path2);

            URI uri3 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat3.jpg")).toURI();
            Path path3 = Paths.get(uri3);
            byte[] image3Bytes = Files.readAllBytes(path3);

            URI uri4 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat4.jpg")).toURI();
            Path path4 = Paths.get(uri4);
            byte[] image4Bytes = Files.readAllBytes(path4);

            URI uri5 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat5.jpg")).toURI();
            Path path5 = Paths.get(uri5);
            byte[] image5Bytes = Files.readAllBytes(path5);

            URI uri6 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat6.jpg")).toURI();
            Path path6 = Paths.get(uri6);
            byte[] image6Bytes = Files.readAllBytes(path6);

            URI uri7 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat7.jpg")).toURI();
            Path path7 = Paths.get(uri7);
            byte[] image7Bytes = Files.readAllBytes(path7);

            URI uri8 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat8.jpg")).toURI();
            Path path8 = Paths.get(uri8);
            byte[] image8Bytes = Files.readAllBytes(path8);

            URI uri9 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat9.jpg")).toURI();
            Path path9 = Paths.get(uri9);
            byte[] image9Bytes = Files.readAllBytes(path9);

            URI uri10 = Objects.requireNonNull(getClass().getClassLoader().getResource("images/cat10.jpg")).toURI();
            Path path10 = Paths.get(uri10);
            byte[] image10Bytes = Files.readAllBytes(path10);

            // Збереження зображень у базі даних
            Image image1 = imageRepo.save(new Image(null, "cat1.jpg", "image/jpeg", image1Bytes));
            Image image2 = imageRepo.save(new Image(null, "cat2.jpg", "image/jpeg", image2Bytes));
            Image image3 = imageRepo.save(new Image(null, "cat3.jpg", "image/jpeg", image3Bytes));
            Image image4 = imageRepo.save(new Image(null, "cat4.jpg", "image/jpeg", image4Bytes));
            Image image5 = imageRepo.save(new Image(null, "cat5.jpg", "image/jpeg", image5Bytes));
            Image image6 = imageRepo.save(new Image(null, "cat6.jpg", "image/jpeg", image6Bytes));
            Image image7 = imageRepo.save(new Image(null, "cat7.jpg", "image/jpeg", image7Bytes));
            Image image8 = imageRepo.save(new Image(null, "cat8.jpg", "image/jpeg", image8Bytes));
            Image image9 = imageRepo.save(new Image(null, "cat9.jpg", "image/jpeg", image9Bytes));
            Image image10 = imageRepo.save(new Image(null, "cat10.jpg", "image/jpeg", image10Bytes));

            // Створення списку категорій із відповідними зображеннями
            List<Category> categories = Arrays.asList(
                    new Category(null, "В'язаний одяг та ін.", image1),
                    new Category(null, "Шитий одяг і аксесуари", image2),
                    new Category(null, "Вироби з дерева та металу", image3),
                    new Category(null, "Декупаж, малювання, скрапбукінг", image4),
                    new Category(null, "Вироби зі шкіри, смоли та інш.", image5),
                    new Category(null, "Вироби з бісеру, каміння та скла", image6),
                    new Category(null, "Текстильні іграшки та подарунки", image7),
                    new Category(null, "Домашня косметика", image8),
                    new Category(null, "Вишивка", image9),
                    new Category(null, "Плетіння", image10)
            );

            // Збереження всіх категорій у базі даних
            categoryRepo.saveAll(categories);
        }

        // Ініціалізація адміністратора, якщо користувачів ще немає
        if (userRepo.count() == 0) {
            // Створення об'єкта адміністратора з початковими даними
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword("admin");

            // Створення адміністратора через сервіс
            userService.createAdmin(admin);
        }
    }
}
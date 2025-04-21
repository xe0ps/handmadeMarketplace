package shepelev.handmadeMarketplace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import shepelev.handmadeMarketplace.dto.ChatMessage;
import shepelev.handmadeMarketplace.entity.ChatMessageEntity;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.ChatMessageRepo;
import shepelev.handmadeMarketplace.service.UserService;

import java.time.LocalDateTime;

/**
 * Контролер для обробки повідомлень чату через WebSocket.
 * Відповідає за надсилання повідомлень між користувачами та підключення користувачів до чату.
 */
@Controller
public class ChatController {

    /** Логер для відстеження дій у чаті. */
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    /** Репозиторій для збереження повідомлень чату. */
    @Autowired
    private ChatMessageRepo messageRepo;

    /** Сервіс для роботи з користувачами, зокрема для отримання даних відправника та отримувача. */
    @Autowired
    private UserService userService;

    /** Шаблон для надсилання повідомлень через WebSocket. */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Обробляє надсилання повідомлення в чаті.
     * Зберігає повідомлення в базі даних і надсилає його відправнику та отримувачу через WebSocket.
     *
     * @param chatMessage Об'єкт повідомлення, що містить дані про відправника, отримувача та вміст.
     * @param headerAccessor Аксесор для роботи з заголовками WebSocket-сесії.
     */
    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Логування надходження повідомлення
        logger.info("sendMessage викликано! Повідомлення: {}", chatMessage);

        // Отримання відправника за email
        String senderUsername = chatMessage.getSender();
        User sender = userService.getUserByEmail(senderUsername);

        // Отримання отримувача за ID
        User receiver = userService.getUserById(chatMessage.getReceiverId());
        String receiverUsername = receiver.getEmail();

        // Створення об'єкта повідомлення для збереження в базі даних
        ChatMessageEntity messageEntity = new ChatMessageEntity();
        messageEntity.setSender(sender);
        messageEntity.setReceiver(receiver);
        messageEntity.setContent(chatMessage.getContent());
        messageEntity.setTimestamp(LocalDateTime.now());
        messageEntity.setType(chatMessage.getType());

        // Збереження повідомлення в базі даних
        messageRepo.save(messageEntity);
        logger.info("Повідомлення збережено в БД.");

        // Оновлення часу в DTO повідомлення
        chatMessage.setTimestamp(messageEntity.getTimestamp());

        // Надсилання повідомлення отримувачу через WebSocket
        messagingTemplate.convertAndSendToUser(receiverUsername, "/queue/messages", chatMessage);

        // Надсилання повідомлення відправнику (для відображення в його чаті)
        messagingTemplate.convertAndSendToUser(senderUsername, "/queue/messages", chatMessage);

        // Логування успішного надсилання повідомлення
        logger.info("Повідомлення відправлено отримувачу {} -> {}", senderUsername, receiverUsername);
    }

    /**
     * Обробляє підключення користувача до чату.
     * Зберігає ім'я користувача в атрибутах сесії WebSocket.
     *
     * @param chatMessage Об'єкт повідомлення, що містить ім'я користувача.
     * @param headerAccessor Аксесор для роботи з заголовками WebSocket-сесії.
     */
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Збереження імені користувача в атрибутах сесії
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        // Логування підключення користувача до чату
        logger.info("Користувач {} приєднався до чату", chatMessage.getSender());
    }
}
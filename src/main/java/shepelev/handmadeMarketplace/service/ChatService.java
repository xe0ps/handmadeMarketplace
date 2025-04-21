package shepelev.handmadeMarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shepelev.handmadeMarketplace.dto.ChatMessage;
import shepelev.handmadeMarketplace.dto.UserDto;
import shepelev.handmadeMarketplace.entity.ChatMessageEntity;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.ChatMessageRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервіс для управління чатами, включаючи отримання повідомлень та списку співрозмовників.
 * Використовує репозиторій для доступу до повідомлень та сервіс користувачів для отримання даних.
 */
@Service
public class ChatService {

    /** Репозиторій для доступу до повідомлень чату. */
    @Autowired
    private ChatMessageRepo messageRepo;

    /** Сервіс для роботи з даними користувачів. */
    @Autowired
    private UserService userService;

    /**
     * Отримує список повідомлень між двома користувачами.
     *
     * @param senderId   Ідентифікатор відправника.
     * @param receiverId Ідентифікатор отримувача.
     * @return Список DTO повідомлень чату або порожній список, якщо користувачів не знайдено.
     */
    @Transactional
    public List<ChatMessage> getChatMessages(Long senderId, Long receiverId) {
        // Отримання відправника та отримувача за їхніми ідентифікаторами
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);

        // Повернення порожнього списку, якщо хоча б один із користувачів не знайдений
        if (sender == null || receiver == null) {
            return java.util.Collections.emptyList();
        }

        // Отримання повідомлень між користувачами з репозиторію
        List<ChatMessageEntity> messageEntities = messageRepo.findChatMessagesBetweenUsers(sender, receiver);

        // Конвертація повідомлень у DTO
        return messageEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Перетворює об'єкт повідомлення чату з сутності в DTO.
     *
     * @param entity Сутність повідомлення чату.
     * @return DTO повідомлення чату.
     */
    private ChatMessage convertToDto(ChatMessageEntity entity) {
        // Створення DTO та заповнення його даними з сутності
        ChatMessage dto = new ChatMessage();
        dto.setSender(entity.getSender().getEmail());
        dto.setReceiverId(entity.getReceiver().getID());
        dto.setContent(entity.getContent());
        dto.setType(entity.getType());
        dto.setTimestamp(entity.getTimestamp());
        return dto;
    }

    /**
     * Отримує список користувачів, з якими поточний користувач мав чат.
     *
     * @param currentUserId Ідентифікатор поточного користувача.
     * @return Список DTO співрозмовників або порожній список, якщо користувача не знайдено.
     */
    @Transactional
    public List<UserDto> getUsersWithChats(Long currentUserId) {
        // Отримання поточного користувача за ідентифікатором
        User currentUser = userService.getUserById(currentUserId);
        if (currentUser == null) {
            return java.util.Collections.emptyList();
        }

        // Отримання надісланих повідомлень та їхніх отримувачів
        List<ChatMessageEntity> sentMessages = messageRepo.findBySender(currentUser);
        Set<User> receivers = sentMessages.stream()
                .map(ChatMessageEntity::getReceiver)
                .filter(receiver -> !receiver.equals(currentUser)) // Виключення поточного користувача
                .collect(Collectors.toSet());

        // Отримання отриманих повідомлень та їхніх відправників
        List<ChatMessageEntity> receivedMessages = messageRepo.findByReceiver(currentUser);
        Set<User> senders = receivedMessages.stream()
                .map(ChatMessageEntity::getSender)
                .filter(sender -> !sender.equals(currentUser)) // Виключення поточного користувача
                .collect(Collectors.toSet());

        // Об'єднання списків отримувачів і відправників у множину унікальних співрозмовників
        Set<User> uniqueInterlocutors = new HashSet<>();
        uniqueInterlocutors.addAll(receivers);
        uniqueInterlocutors.addAll(senders);

        // Конвертація співрозмовників у DTO із додаванням профільних зображень у Base64
        return uniqueInterlocutors.stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getID());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    if (user.getProfileImage() != null) {
                        // Кодування профільного зображення у Base64
                        dto.setProfileImageUrl(java.util.Base64.getEncoder().encodeToString(user.getProfileImage().getData()));
                        dto.setProfileImageType(user.getProfileImage().getFileType());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
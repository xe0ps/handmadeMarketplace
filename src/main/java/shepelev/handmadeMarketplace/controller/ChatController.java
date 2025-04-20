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

@Controller
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatMessageRepo messageRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("sendMessage викликано! Повідомлення: {}", chatMessage);
        String senderUsername = chatMessage.getSender();
        User sender = userService.getUserByEmail(senderUsername);
        User receiver = userService.getUserById(chatMessage.getReceiverId());
        String receiverUsername = receiver.getEmail();
        ChatMessageEntity messageEntity = new ChatMessageEntity();
        messageEntity.setSender(sender);
        messageEntity.setReceiver(receiver);
        messageEntity.setContent(chatMessage.getContent());
        messageEntity.setTimestamp(LocalDateTime.now());
        messageEntity.setType(chatMessage.getType());
        messageRepo.save(messageEntity);
        logger.info("Повідомлення збережено в БД.");
        chatMessage.setTimestamp(messageEntity.getTimestamp());
        messagingTemplate.convertAndSendToUser(receiverUsername, "/queue/messages", chatMessage);
        messagingTemplate.convertAndSendToUser(senderUsername, "/queue/messages", chatMessage);
        logger.info("Повідомлення відправлено отримувачу {} -> {}" , senderUsername, receiverUsername);

    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        logger.info("Користувач {} приєднався до чату", chatMessage.getSender());
    }
}
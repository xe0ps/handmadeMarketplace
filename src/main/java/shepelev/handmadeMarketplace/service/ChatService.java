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

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepo messageRepo;
    @Autowired
    private UserService userService;

    @Transactional
    public List<ChatMessage> getChatMessages(Long senderId, Long receiverId) {
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);
        if (sender == null || receiver == null) {
            return java.util.Collections.emptyList();
        }
        List<ChatMessageEntity> messageEntities = messageRepo.findChatMessagesBetweenUsers(sender, receiver);
        return messageEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ChatMessage convertToDto(ChatMessageEntity entity) {
        ChatMessage dto = new ChatMessage();
        dto.setSender(entity.getSender().getEmail());
        dto.setReceiverId(entity.getReceiver().getID());
        dto.setContent(entity.getContent());
        dto.setType(entity.getType());
        dto.setTimestamp(entity.getTimestamp());
        return dto;
    }
    @Transactional
    public List<UserDto> getUsersWithChats(Long currentUserId) {
        User currentUser = userService.getUserById(currentUserId);
        if (currentUser == null) {
            return java.util.Collections.emptyList();
        }

        List<ChatMessageEntity> sentMessages = messageRepo.findBySender(currentUser);
        Set<User> receivers = sentMessages.stream()
                .map(ChatMessageEntity::getReceiver)
                .filter(receiver -> !receiver.equals(currentUser))
                .collect(Collectors.toSet());
        List<ChatMessageEntity> receivedMessages = messageRepo.findByReceiver(currentUser);
        Set<User> senders = receivedMessages.stream()
                .map(ChatMessageEntity::getSender)
                .filter(sender -> !sender.equals(currentUser))
                .collect(Collectors.toSet());

        Set<User> uniqueInterlocutors = new HashSet<>();
        uniqueInterlocutors.addAll(receivers);
        uniqueInterlocutors.addAll(senders);


        return uniqueInterlocutors.stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getID());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    if (user.getProfileImage() != null) {
                        dto.setProfileImageUrl(java.util.Base64.getEncoder().encodeToString(user.getProfileImage().getData()));
                        dto.setProfileImageType(user.getProfileImage().getFileType());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
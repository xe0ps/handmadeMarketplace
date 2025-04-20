package shepelev.handmadeMarketplace.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import shepelev.handmadeMarketplace.entity.ChatMessageEntity;
import shepelev.handmadeMarketplace.entity.User;

import java.util.List;

public interface ChatMessageRepo extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findBySenderAndReceiverOrderByTimestampAsc(User sender, User receiver);
    List<ChatMessageEntity> findByReceiverAndSenderOrderByTimestampAsc(User receiver, User sender);

    default List<ChatMessageEntity> findChatMessagesBetweenUsers(User user1, User user2) {
        List<ChatMessageEntity> messages1 = findBySenderAndReceiverOrderByTimestampAsc(user1, user2);
        List<ChatMessageEntity> messages2 = findByReceiverAndSenderOrderByTimestampAsc(user1, user2);
        List<ChatMessageEntity> allMessages = new java.util.ArrayList<>();
        allMessages.addAll(messages1);
        allMessages.addAll(messages2);
        allMessages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        return allMessages;
    }

    List<ChatMessageEntity> findBySender(User sender);
    List<ChatMessageEntity> findByReceiver(User receiver);
}
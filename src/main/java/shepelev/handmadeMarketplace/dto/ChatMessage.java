package shepelev.handmadeMarketplace.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String sender;
    private String content;
    private MessageType type;
    private Long receiverId;
    private LocalDateTime timestamp;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
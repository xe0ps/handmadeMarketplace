package shepelev.handmadeMarketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import shepelev.handmadeMarketplace.dto.ChatMessage;
import shepelev.handmadeMarketplace.dto.UserDto;
import shepelev.handmadeMarketplace.service.ChatService;
import shepelev.handmadeMarketplace.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChatPageController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserService userService;

    @GetMapping("/chat")
    public String showChatPage(@RequestParam(value = "receiverId", required = false) Long receiverId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long senderId = userService.getUserByEmail(currentUsername).getID();

        List<ChatMessage> chatHistory = null;
        if (receiverId != null) {
            chatHistory = chatService.getChatMessages(senderId, receiverId);
        }
        List<UserDto> chatUsers = chatService.getUsersWithChats(senderId);

        model.addAttribute("chatHistory", chatHistory);
        model.addAttribute("receiverId", receiverId);
        model.addAttribute("chatUsers", chatUsers);
        return "chat";
    }

    @GetMapping("/chat/history")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getChatHistory(@RequestParam("receiverId") Long receiverId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long senderId = userService.getUserByEmail(currentUsername).getID();
        List<ChatMessage> chatHistory = chatService.getChatMessages(senderId, receiverId);
        return ResponseEntity.ok(chatHistory);
    }

    @GetMapping("/chat/users")
    @ResponseBody
    @Transactional
    public ResponseEntity<List<UserDto>> getUsersForChat() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Long currentUserId = userService.getUserByEmail(currentUsername).getID();
        List<UserDto> userDtos = chatService.getUsersWithChats(currentUserId);
        return ResponseEntity.ok(userDtos);
    }
}
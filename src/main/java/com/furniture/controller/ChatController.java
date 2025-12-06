package com.furniture.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.furniture.modal.Conversation;
import com.furniture.modal.Order;
import com.furniture.modal.Seller;
import com.furniture.modal.User;
import com.furniture.request.ChatMessageRequest;
import com.furniture.service.ChatService;
import com.furniture.service.SellerService;
import com.furniture.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final SellerService sellerService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages from WebSocket
     * Endpoint: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        try {
            String email = principal.getName();
            System.out.println("[Chat] sendMessage called with email: " + email + ", conversationId: " + request.getConversationId());
            
            // Determine if sender is User or Seller
            User user = null;
            Seller seller = null;
            String senderType;
            Long senderId;
            
            try {
                user = userService.findUserByEmail(email);
                senderType = "USER";
                senderId = user.getId();
                System.out.println("[Chat] Sender identified as USER: " + senderId);
            } catch (Exception userEx) {
                System.out.println("[Chat] Not a user, checking if seller: " + userEx.getMessage());
                try {
                    seller = sellerService.getSellerByEmail(email);
                    senderType = "SELLER";
                    senderId = seller.getId();
                    System.out.println("[Chat] Sender identified as SELLER: " + senderId);
                } catch (Exception sellerEx) {
                    System.err.println("[Chat] Failed to identify sender with email: " + email);
                    System.err.println("[Chat] User error: " + userEx.getMessage());
                    System.err.println("[Chat] Seller error: " + sellerEx.getMessage());
                    throw new RuntimeException("Could not identify sender: " + email);
                }
            }

            // Get or create conversation
            Conversation conversation;
            if (request.getConversationId() != null) {
                conversation = chatService.findConversationById(request.getConversationId());
            } else {
                // Create new conversation
                if (user != null) {
                    // User is messaging seller
                    seller = sellerService.getSellerById(request.getSellerId());
                } else {
                    // Seller is messaging user - need to fetch user by some means
                    // Since UserService doesn't have findUserById, we'll need the full user object passed
                    // For now, this case won't work properly until findUserById is implemented
                    throw new RuntimeException("Cannot create conversation: UserService.findUserById not implemented");
                }
                
                Order order = null;
                // Note: Order handling can be added here if needed in the future
                
                conversation = chatService.getOrCreateConversation(user, seller, order, null);
            }

            // Save message to database (this broadcasts to /topic/conversation/{id} for all participants)
            chatService.saveMessage(
                conversation,
                senderId,
                senderType,
                request.getContent(),
                request.getMessageType()
            );
            // Message is already broadcast via topic in saveMessage(), no need to send again

        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle typing indicator
     * Endpoint: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload ChatMessageRequest request, Principal principal) {
        try {
            String email = principal.getName();
            Conversation conversation = chatService.findConversationById(request.getConversationId());
            
            // Determine sender type
            boolean isUser = false;
            try {
                userService.findUserByEmail(email);
                isUser = true;
            } catch (Exception ignored) {}

            // Send typing indicator to the other participant
            String recipientEmail = isUser 
                ? conversation.getSeller().getEmail() 
                : conversation.getUser().getEmail();

            messagingTemplate.convertAndSendToUser(
                recipientEmail,
                "/queue/typing",
                request.getConversationId()
            );
        } catch (Exception e) {
            System.err.println("Error handling typing indicator: " + e.getMessage());
        }
    }
}

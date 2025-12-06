package com.furniture.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.furniture.modal.Conversation;
import com.furniture.modal.Message;
import com.furniture.modal.Order;
import com.furniture.modal.Seller;
import com.furniture.modal.User;
import com.furniture.request.ChatMessageRequest;
import com.furniture.response.ChatMessageResponse;
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
            
            // Determine if sender is User or Seller
            User user = null;
            Seller seller;
            String senderType;
            Long senderId;
            
            try {
                user = userService.findUserByEmail(email);
                senderType = "USER";
                senderId = user.getId();
            } catch (Exception e) {
                seller = sellerService.getSellerByEmail(email);
                senderType = "SELLER";
                senderId = seller.getId();
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

            // Save message to database (this also broadcasts to /topic/conversation/{id})
            Message message = chatService.saveMessage(
                conversation,
                senderId,
                senderType,
                request.getContent(),
                request.getMessageType()
            );

            // Get sender name
            String senderName = chatService.getSenderName(senderId, senderType);

            // Create response
            ChatMessageResponse response = ChatMessageResponse.fromMessage(message, senderName);

            // Also send back to sender for confirmation
            messagingTemplate.convertAndSendToUser(
                email,
                "/queue/messages",
                response
            );

        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
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

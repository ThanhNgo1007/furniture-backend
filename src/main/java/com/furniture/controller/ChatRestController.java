package com.furniture.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.modal.Conversation;
import com.furniture.modal.Message;
import com.furniture.modal.Order;
import com.furniture.modal.Product;
import com.furniture.modal.Seller;
import com.furniture.modal.User;
import com.furniture.response.ChatMessageResponse;
import com.furniture.response.ConversationResponse;
import com.furniture.service.ChatService;
import com.furniture.service.OrderService;
import com.furniture.service.ProductService;
import com.furniture.service.SellerService;
import com.furniture.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;
    private final UserService userService;
    private final SellerService sellerService;
    private final OrderService orderService;
    private final ProductService productService;

    /**
     * Get all conversations for the current user
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(@RequestHeader("Authorization") String jwt) {
        try {
            String email = getUserEmailFromJwt(jwt);
            List<Conversation> conversations;
            
            // Determine if user or seller
            try {
                User user = userService.findUserByEmail(email);
                conversations = chatService.getUserConversations(user.getId());
            } catch (Exception e) {
                Seller seller = sellerService.getSellerByEmail(email);
                conversations = chatService.getSellerConversations(seller.getId());
            }

            // Convert to response DTOs
            List<ConversationResponse> response = conversations.stream()
                    .map(conv -> {
                        // Get last message preview
                        List<Message> messages = chatService.getAllMessages(conv.getId());
                        String lastMessagePreview = messages.isEmpty() ? "" : 
                                messages.get(messages.size() - 1).getContent();
                        
                        // Truncate if too long
                        if (lastMessagePreview.length() > 50) {
                            lastMessagePreview = lastMessagePreview.substring(0, 47) + "...";
                        }
                        
                        return ConversationResponse.fromConversation(conv, lastMessagePreview);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching conversations: " + e.getMessage());
        }
    }

    /**
     * Get chat history for a specific conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> getChatHistory(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestHeader("Authorization") String jwt
    ) {
        try {
            // Verify user has access to this conversation
            String email = getUserEmailFromJwt(jwt);
            Conversation conversation = chatService.findConversationById(conversationId);
            
            boolean hasAccess = conversation.getUser().getEmail().equals(email) ||
                              conversation.getSeller().getEmail().equals(email);
            
            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have access to this conversation");
            }

            // Get messages
            Page<Message> messagesPage = chatService.getChatHistory(conversationId, page, size);
            
            // Convert to response DTOs
            List<ChatMessageResponse> messages = messagesPage.getContent().stream()
                    .map(msg -> {
                        String senderName = chatService.getSenderName(msg.getSenderId(), msg.getSenderType());
                        return ChatMessageResponse.fromMessage(msg, senderName);
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages);
            response.put("currentPage", messagesPage.getNumber());
            response.put("totalPages", messagesPage.getTotalPages());
            response.put("totalMessages", messagesPage.getTotalElements());
            response.put("hasMore", messagesPage.hasNext());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching chat history: " + e.getMessage());
        }
    }

    /**
     * Create a new conversation
     */
    @PostMapping("/conversations")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ConversationResponse> createConversation(
            @RequestHeader("Authorization") String jwt,
            @RequestParam Long sellerId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long productId
    ) {
        try {
            User user = getUserFromToken(jwt);
            Seller seller = sellerService.getSellerById(sellerId);
            Order order = null;
            Product product = null;

            if (orderId != null) {
                order = orderService.findOrderById(orderId);
                
                // Verify order belongs to user
                if (!order.getUser().getId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            if (productId != null) {
                product = productService.findProductById(productId);
            }

            Conversation conversation = chatService.getOrCreateConversation(user, seller, order, product);
            ConversationResponse response = new ConversationResponse(conversation, user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark messages as read
     */
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long conversationId,
            @RequestHeader("Authorization") String jwt
    ) {
        try {
            String email = getUserEmailFromJwt(jwt);
            
            // Determine user type and ID
            Long recipientId;
            String recipientType;
            
            try {
                User user = userService.findUserByEmail(email);
                recipientId = user.getId();
                recipientType = "USER";
            } catch (Exception e) {
                Seller seller = sellerService.getSellerByEmail(email);
                recipientId = seller.getId();
                recipientType = "SELLER";
            }

            chatService.markMessagesAsRead(conversationId, recipientId, recipientType);

            return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error marking messages as read: " + e.getMessage());
        }
    }

    /**
     * Get unread message count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("Authorization") String jwt) {
        try {
            String email = getUserEmailFromJwt(jwt);
            Long unreadCount;
            
            try {
                User user = userService.findUserByEmail(email);
                unreadCount = chatService.getUserUnreadCount(user.getId());
            } catch (Exception e) {
                Seller seller = sellerService.getSellerByEmail(email);
                unreadCount = chatService.getSellerUnreadCount(seller.getId());
            }

            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching unread count: " + e.getMessage());
        }
    }

    /**
     * Get user's orders from a specific seller (for order selection)
     */
    @GetMapping("/seller/{sellerId}/orders")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<?> getUserOrdersFromSeller(
            @PathVariable Long sellerId,
            @RequestHeader("Authorization") String jwt
    ) {
        try {
            User user = userService.findUserByJwtToken(jwt);
            List<Order> orders = orderService.usersOrderHistory(user.getId());
            
            // Filter orders from this seller
            List<Order> sellerOrders = orders.stream()
                    .filter(order -> order.getSellerId().equals(sellerId))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(sellerOrders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching orders: " + e.getMessage());
        }
    }

    private String getUserEmailFromJwt(String jwt) throws Exception {
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        
        // Try to find user by JWT
        try {
            User user = userService.findUserByJwtToken("Bearer " + jwt);
            return user.getEmail();
        } catch (Exception e) {
            // If not user, try seller
            Seller seller = sellerService.getSellerProfile("Bearer " + jwt);
            return seller.getEmail();
        }
    }
    
    private User getUserFromToken(String jwt) throws Exception {
        return userService.findUserByJwtToken(jwt);
    }
}

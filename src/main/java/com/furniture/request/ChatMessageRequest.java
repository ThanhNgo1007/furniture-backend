package com.furniture.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageRequest {
    private Long conversationId;
    private Long sellerId;
    private Long orderId;  // Optional: link to specific order
    private String content;
    @lombok.Builder.Default
    private String messageType = "TEXT";  // "TEXT", "IMAGE", "ORDER_LINK"
}

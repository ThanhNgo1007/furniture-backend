package com.furniture.response;

import java.time.LocalDateTime;

import com.furniture.modal.Message;

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
public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderType;
    private String senderName;  // Name of sender (User fullName or Seller sellerName)
    private String content;
    private String messageType;
    private Boolean isRead;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static ChatMessageResponse fromMessage(Message message, String senderName) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType())
                .senderName(senderName)
                .content(message.getContent())
                .messageType(message.getMessageType())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

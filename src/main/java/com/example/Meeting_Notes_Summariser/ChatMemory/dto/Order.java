package com.example.Meeting_Notes_Summariser.ChatMemory.dto;


public record Order(
        String orderId,
        String carrier,
        OrderStatus status,
        String userId,
        String userName
) {
}

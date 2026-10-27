package com.example.Meeting_Notes_Summariser.MultiModality.TextToSpeech;

public record TicketResponse(
        String ticketId,
        TicketStatus status,
        String message
) {
}


package com.example.Meeting_Notes_Summariser.MultiModality.TextToSpeech;

import java.time.ZonedDateTime;

public record Ticket(
        String ticketId,
        ZonedDateTime ticketDate,
        TicketStatus status,
        String userId,
        String userName
) {
}


package com.example.Meeting_Notes_Summariser;

import com.example.Meeting_Notes_Summariser.Rag.service.RagIngestionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MeetingNotesSummariserApplication implements CommandLineRunner {

    private  final RagIngestionService ragingestionservice;

    public MeetingNotesSummariserApplication(RagIngestionService ragingestionservice) {
        this.ragingestionservice = ragingestionservice;
    }

    public static void main(String[] args) {
		SpringApplication.run(MeetingNotesSummariserApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception{
        ragingestionservice.initializePgVectorStore();
    }
}

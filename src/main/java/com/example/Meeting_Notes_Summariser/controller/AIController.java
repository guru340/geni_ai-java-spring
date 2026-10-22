package com.example.Meeting_Notes_Summariser.controller;

import com.example.Meeting_Notes_Summariser.Service.AIService;
import com.example.Meeting_Notes_Summariser.dto.SummarizationResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/openai/chat")
public class AIController {

    private String SYSTEM_PROPMT = "You are a helpful assistant that summarize any given contact " +
            "Ensure the summary is concise, informative, and captures the key points. " +
            "Do not answer anything other than summarization .If the question is about summarization" +
            "respond with 'I can only help with summarization tasks.' ";
    private final ChatClient chatClient;
    private final AIService aiService;

    @Value("classpath:/templates/summarize-prompt.st")
    private Resource summarizeprompt;

    public AIController(@Qualifier("openAIGeneralChatClient") ChatClient chatClient, AIService aiService) {
        this.chatClient = chatClient;
        this.aiService = aiService;
    }

    @PostMapping("/summarizer")
    public ChatClientResponse summarsize(@RequestBody String message) {
        return chatClient.prompt()
                .system(SYSTEM_PROPMT)
                .user(message)
                .call()
                .chatClientResponse();

    }


    @PostMapping("/summarizer-meeting-notes")
    public String summarizingmeetingnotes(@RequestBody String meetingNotes) {
        return chatClient.prompt()
                .system(SYSTEM_PROPMT)
                .user(u -> u.text("""
                        You are an expert meeting-notes summarization assistant. Your task is to summarize the following meeting notes: {meetingNotes} Use the following format while creating the summary: Meeting Summary: - Objective: - Key Discussion Points: - Decisions Made: - Action Items: - Next Steps: Example: Input: In today's sales strategy meeting, we revised Q3 targets and identified performance gaps. The team discussed improving customer acquisition, increasing sales conversion rates, and assigning new targets to each sales representative. Output: Meeting Summary: - Objective: Review Q3 sales targets and identify performance gaps. - Key Discussion Points: Customer acquisition, sales conversion, and individual sales targets were discussed. - Decisions Made: Q3 targets were revised and new targets were assigned to sales representatives. - Action Items: Sales representatives need to work on improving customer acquisition and conversion rates. - Next Steps: Track performance against the revised Q3 targets. Important Instructions: 1. Keep the summary concise and easy to understand. 2. Do not add information that is not present in the meeting notes.
                        3. Clearly distinguish between discussions, decisions, and action items.
                        4. Preserve important names, dates, numbers, and deadlines when provided. 5. If a section has no relevant information, write "Not specified". """).param("meetingNotes", meetingNotes)).call().content();

    }

    @PostMapping("/summarizer-meeting-notes-structed")
    public SummarizationResponse summarizingmeetingnotesStructedOuput(@RequestBody String meetingNotes) {
        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROPMT)
                    .user(u -> u.text("""
                            You are an expert meeting-notes summarization assistant. Your task is to summarize the following meeting notes: {meetingNotes} Use the following format while creating the summary: Meeting Summary: - Objective: - Key Discussion Points: - Decisions Made: - Action Items: - Next Steps: Example: Input: In today's sales strategy meeting, we revised Q3 targets and identified performance gaps. The team discussed improving customer acquisition, increasing sales conversion rates, and assigning new targets to each sales representative. Output: Meeting Summary: - Objective: Review Q3 sales targets and identify performance gaps. - Key Discussion Points: Customer acquisition, sales conversion, and individual sales targets were discussed. - Decisions Made: Q3 targets were revised and new targets were assigned to sales representatives. - Action Items: Sales representatives need to work on improving customer acquisition and conversion rates. - Next Steps: Track performance against the revised Q3 targets. Important Instructions: 1. Keep the summary concise and easy to understand. 2. Do not add information that is not present in the meeting notes.
                            3. Clearly distinguish between discussions, decisions, and action items.
                            4. Preserve important names, dates, numbers, and deadlines when provided. 5. If a section has no relevant information, write "Not specified". """).param("meetingNotes", meetingNotes)).call().entity(SummarizationResponse.class);
        } catch (Exception e) {
            return new  SummarizationResponse(null,null,e.getMessage());
        }

    }


    @PostMapping("/summarizer-meeting-notes-with-prompt-template")
    public SummarizationResponse summarizingmeetingnotesStructedOuputAndPromptTemplate(@RequestBody String meetingNotes) {
        PromptTemplate promptTemplate=new PromptTemplate(summarizeprompt);
        Prompt prompt=promptTemplate.create(Map.of("meetingnotes",meetingNotes));
        return  chatClient.prompt(prompt)
                .system(SYSTEM_PROPMT)
                .call()
                .entity(SummarizationResponse.class);

    }


    @PostMapping("/summarizer-meeting-notes-structed-list")
    public List<SummarizationResponse> summarizingmeetingnotesStructedOuputList(@RequestBody String meetingNotes) {

        return chatClient.prompt()
                .system(SYSTEM_PROPMT)
                .user(u -> u.text("""
                        You are an expert meeting-notes summarization assistant. Your task is to summarize the following meeting notes: {meetingNotes} Use the following format while creating the summary: Meeting Summary: - Objective: - Key Discussion Points: - Decisions Made: - Action Items: - Next Steps: Example: Input: In today's sales strategy meeting, we revised Q3 targets and identified performance gaps. The team discussed improving customer acquisition, increasing sales conversion rates, and assigning new targets to each sales representative. Output: Meeting Summary: - Objective: Review Q3 sales targets and identify performance gaps. - Key Discussion Points: Customer acquisition, sales conversion, and individual sales targets were discussed. - Decisions Made: Q3 targets were revised and new targets were assigned to sales representatives. - Action Items: Sales representatives need to work on improving customer acquisition and conversion rates. - Next Steps: Track performance against the revised Q3 targets. Important Instructions: 1. Keep the summary concise and easy to understand. 2. Do not add information that is not present in the meeting notes.
                        3. Clearly distinguish between discussions, decisions, and action items.
                        4. Preserve important names, dates, numbers, and deadlines when provided. 5. If a section has no relevant information, write "Not specified". """).param("meetingNotes", meetingNotes)).call().entity(new ParameterizedTypeReference<List<SummarizationResponse>>() {
                });
    }



    @PostMapping("/summarizer-with-http")
    public String SummarizerwithJavaClient(@RequestBody String message) throws Exception {
        return aiService.chat(message);
    }



//    Implmenting the Streaming
    @PostMapping(value="/summarizer-with-streaming",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamsummarsize(@RequestBody String message){
        return chatClient.prompt()
                .system(SYSTEM_PROPMT)
                .user(message)
                .stream()
                .content()
                .bufferTimeout(40, Duration.ofMillis(200))//40 tokens or every 200ms
                .map(tokenList->String.join(",",tokenList));

    }

    @PostMapping("/system-with-adivisor")
    public String summarisewithadivisor(@RequestBody String  message){
        return chatClient.prompt()

                .user(message).call().content();
    }
}

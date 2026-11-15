//package com.example.Meeting_Notes_Summariser.controller;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.prompt.ChatOptions;
//import org.springframework.ai.converter.ListOutputConverter;
//import org.springframework.ai.converter.MapOutputConverter;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/openai/chat")
//public class OpenAIStructuredOutputController {
//
//    private final  ChatClient chatclient;
//
//    public OpenAIStructuredOutputController(@Qualifier("openAIChatClient") ChatClient chatclient) {
//        this.chatclient = chatclient;
//    }
//
//    @PostMapping("/structured-list")
//    public List<String> liststructured(@RequestBody String message){
//        return chatclient.prompt()
//                .user(message)
//                .call()
//                .entity(new ListOutputConverter());
//    }
//
//    @PostMapping("/structured-map")
//    public Map<String,Object> structuredmap(@RequestBody String message){
//        return chatclient.prompt()
//                .user(message)
//                .call()
//                .entity(new MapOutputConverter());
//    }
//
//
//}

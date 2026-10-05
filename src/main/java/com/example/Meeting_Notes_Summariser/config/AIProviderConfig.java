package com.example.Meeting_Notes_Summariser.config;

import com.example.Meeting_Notes_Summariser.advisor.errorWrappingAdvisor;
import com.example.Meeting_Notes_Summariser.advisor.systemPromptAdvisor;
import com.example.Meeting_Notes_Summariser.advisor.ValidationAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AIProviderConfig {

    @Bean("openAIChatClient")
    ChatClient openAIchatClient(OpenAiChatModel openAiChatModel
    , SimpleLoggerAdvisor simpleLoggerAdvisor, SafeGuardAdvisor safeGuardAdvisor, errorWrappingAdvisor errorWrappingAdvisor,
                                systemPromptAdvisor systemPromptAdvisior, ValidationAdvisor validationAdvisor){

        return ChatClient.builder(openAiChatModel).defaultAdvisors(safeGuardAdvisor,simpleLoggerAdvisor, errorWrappingAdvisor,systemPromptAdvisior,validationAdvisor).build();
    }

//    @Bean("openAIGeneralChatClient")
//    ChatClient openAIGeneralchatClient(OpenAiChatModel openAiChatModel, BookingTools bookingTools){
//
//        return ChatClient.builder(openAiChatModel)
//                .defaultTools(bookingTools).build();
//    }
    @Bean("openAIGeneralChatClient")
    ChatClient openAIGeneralchatClient(OpenAiChatModel openAiChatModel){

        return ChatClient.builder(openAiChatModel)
                .build();
    }

    @Bean("openAIChatClientWithMemory")
    ChatClient openAIChatClientWithMemory(OpenAiChatModel openAiChatModel, ChatMemory chatMemory) {

        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())


                .build();

    }


//    @Bean("huggingfaceChatClient")
//    ChatClient huggingfaceChatClient(HuggingfaceChatModel huggingfaceChatModel){
//        return ChatClient.builder(huggingfaceChatModel).build();
//    }

    @Bean
    SimpleLoggerAdvisor simpleLoggerAdvisor(){
        return new SimpleLoggerAdvisor();
    }

    @Bean
    SafeGuardAdvisor safeGuardAdvisor(){
        return new SafeGuardAdvisor(
                List.of("password","ssn","credit card","hack","system prompt")
        );
    }
}

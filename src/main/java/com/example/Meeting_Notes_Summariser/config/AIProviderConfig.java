package com.example.Meeting_Notes_Summariser.config;

import com.example.Meeting_Notes_Summariser.advisor.errorWrappingAdvisor;
import com.example.Meeting_Notes_Summariser.advisor.systemPromptAdvisor;
import com.example.Meeting_Notes_Summariser.advisor.ValidationAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import com.pgvector.PGvector;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Configuration
public class AIProviderConfig {
    @Bean
    public PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate,
                                       @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel){
        return PgVectorStore.builder(jdbcTemplate,
                embeddingModel).initializeSchema(true).build();


    }

    @Bean
    ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(5)
                .build();
    }

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
    ChatClient openAIChatClientWithMemory(OpenAiChatModel openAiChatModel, ChatMemory chatMemory,
                                          PgVectorStore pgVectorStore){
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(VectorStoreChatMemoryAdvisor.builder(pgVectorStore).build())
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())


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

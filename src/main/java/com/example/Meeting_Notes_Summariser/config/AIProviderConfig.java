package com.example.Meeting_Notes_Summariser.config;

import com.example.Meeting_Notes_Summariser.advisor.errorWrappingAdvisor;
import com.example.Meeting_Notes_Summariser.advisor.systemPromptAdvisor;
import com.example.Meeting_Notes_Summariser.advisor.ValidationAdvisor;
import com.google.genai.Client;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.PromptTemplateChatActions;
import org.springframework.ai.embedding.EmbeddingModel;



import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;

import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;


import java.util.List;
import java.util.UUID;

@Configuration
public class AIProviderConfig {

    @Value("classpath:/templates/vector-store-memory-system-prompt.st")
    private Resource VectorStoreMemorySystemPrompt;
//
    private static final int TOP_K=10;

    @Value("classpath:/templates/prompt-chat-memory-system-prompt.st")
    private Resource promptChatMemorySystemPrompt;

    @Bean
    public PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate,
                                     @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel){
        return PgVectorStore.builder(jdbcTemplate,
                embeddingModel)
                .dimensions(1024).initializeSchema(true).build();


    }


    @Bean
    ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(3)
                .build();
    }

    @Bean("openAIChatClient")
    ChatClient openAIchatClient(OpenAiChatModel openAiChatModel
    , SimpleLoggerAdvisor simpleLoggerAdvisor, SafeGuardAdvisor safeGuardAdvisor, errorWrappingAdvisor errorWrappingAdvisor,
                                systemPromptAdvisor systemPromptAdvisior, ValidationAdvisor validationAdvisor){

        return ChatClient.builder(openAiChatModel).defaultAdvisors(safeGuardAdvisor,simpleLoggerAdvisor, errorWrappingAdvisor,systemPromptAdvisior,validationAdvisor).build();
    }
//
//    @Bean("openAIGeneralChatClient")
//    ChatClient openAIGeneralchatClient(OpenAiChatModel openAiChatModel, BookingTools bookingTools){
//
//        return ChatClient.builder(openAiChatModel)
//                .defaultTools(bookingTools).build();
//    }
    @Bean("openAIGeneralChatClient")
    ChatClient openAIGeneralchatClient(GoogleGenAiChatModel googleGenAiChatModel){

        return ChatClient.builder(googleGenAiChatModel)
                .build();
    }




    @Bean("openAIChatClientWithMemory")
    ChatClient openAIChatClientWithMemory(OpenAiChatModel openAiChatModel, ChatMemory chatMemory
                                        ){
        return ChatClient.builder(openAiChatModel)


//                .defaultAdvisors(VectorStoreChatMemoryAdvisor.builder(pgVectorStore)
//                        .systemPromptTemplate(new PromptTemplate(VectorStoreMemorySystemPrompt))
//
//                        .defaultTopK(TOP_K).build()
//                ,MessageChatMemoryAdvisor.builder(chatMemory).build())

                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())


                .build();

    }
//    @Bean("googlegenAIChatClientWithMemory")
//    ChatClient googlegenAIChatClientWithMemory(GoogleGenAiChatModel vertexAiGeminiChatModel,ChatMemory chatMemory,PgVectorStore vectorStore) {
//        return ChatClient.builder(vertexAiGeminiChatModel)
//                .defaultAdvisors(VectorStoreChatMemoryAdvisor.builder(vectorStore).systemPromptTemplate(new PromptTemplate(VectorStoreMemorySystemPrompt))
//                        .defaultTopK(TOP_K).build())
////                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//                        .build();
//    }
//    @Bean("ollamaAiChatWithMemory")
//    ChatClient ollamaAiChatWithMemory(GoogleGenAiChatModel ollamaChatModel, ChatMemory chatMemory, PgVectorStore vectorStore) {
//        return ChatClient.builder(ollamaChatModel)
//                .defaultAdvisors(VectorStoreChatMemoryAdvisor.builder(vectorStore).systemPromptTemplate(new PromptTemplate(VectorStoreMemorySystemPrompt))
//                        .defaultTopK(TOP_K)
//                        .order(Ordered.HIGHEST_PRECEDENCE + 200).build())
////                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//                .build();
//    }

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

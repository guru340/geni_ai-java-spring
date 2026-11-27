package com.example.Meeting_Notes_Summariser.config;

import com.example.Meeting_Notes_Summariser.Rag.config.data.PgVectorStoreConfigData;
import com.example.Meeting_Notes_Summariser.Rag.config.data.RagConfigData;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;



import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;

import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;


import java.util.List;

@Configuration
public class AIProviderConfig {

    @Value("classpath:/templates/vector-store-memory-system-prompt.st")
    private Resource VectorStoreMemorySystemPrompt;
//
    private static final int TOP_K=10;

    @Value("classpath:/templates/prompt-chat-memory-system-prompt.st")
    private Resource promptChatMemorySystemPrompt;

    @Bean("chatMemoryVectorStore")
    public VectorStore chatMemoryVectorStore(JdbcTemplate jdbcTemplate,
                                             @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel, PgVectorStoreConfigData pgVectorStoreConfigData){
        return PgVectorStore.builder(jdbcTemplate,
                embeddingModel)
                .vectorTableName(pgVectorStoreConfigData.getTableNameForChatMemory())
                .dimensions(pgVectorStoreConfigData.getDimensions()).initializeSchema(pgVectorStoreConfigData.isInitializeSchema())
                .distanceType(PgVectorStore.PgDistanceType.valueOf(pgVectorStoreConfigData.getDistanceType()))
                .indexType(PgVectorStore.PgIndexType.valueOf(pgVectorStoreConfigData.getIndexType()))
                .maxDocumentBatchSize(pgVectorStoreConfigData.getMaxDocumentBatchSize())
                .build();



    }

    @Bean("ragVectorStore")
    public VectorStore ragVectorStore(JdbcTemplate jdbcTemplate,
                                             @Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel, PgVectorStoreConfigData pgVectorStoreConfigData){
        return PgVectorStore.builder(jdbcTemplate,
                        embeddingModel)
                .vectorTableName(pgVectorStoreConfigData.getTableNameForRag())
                .dimensions(pgVectorStoreConfigData.getDimensions()).initializeSchema(pgVectorStoreConfigData.isInitializeSchema())
                .distanceType(PgVectorStore.PgDistanceType.valueOf(pgVectorStoreConfigData.getDistanceType()))
                .indexType(PgVectorStore.PgIndexType.valueOf(pgVectorStoreConfigData.getIndexType()))
                .maxDocumentBatchSize(pgVectorStoreConfigData.getMaxDocumentBatchSize())
                .build();



    }

    @Bean
    RetrievalAugmentationAdvisor ragAdvisor(@Qualifier("ragVectorStore") VectorStore vectorStore,
                                            RagConfigData ragConfigData){
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .topK(ragConfigData.getTopK())
                        .similarityThreshold(ragConfigData.getSimilarityThreshold())
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(false)
                        .build())


                .build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter(RagConfigData ragConfigData) {
        return TokenTextSplitter.builder()
                .withChunkSize(ragConfigData.getChunk().getSize())
                .withMinChunkSizeChars(ragConfigData.getChunk().getMinChunkSize())
                .withMinChunkLengthToEmbed(ragConfigData.getChunk().getMinChunkToEmbed())
                .withMaxNumChunks(ragConfigData.getChunk().getMaxNumChunks())
                .withKeepSeparator(ragConfigData.getChunk().isKeepSeprator())
                .build();
    }

    @Bean("genAIRAGChatClient")
    ChatClient genAIRAGChatClient(GoogleGenAiChatModel googleGenAiChatModel,
                                  SimpleVectorStore simpleVectorStore,
                                  SimpleLoggerAdvisor simpleLoggerAdvisor,
                                  RagConfigData ragConfigData){
        return ChatClient.builder(googleGenAiChatModel)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(simpleVectorStore)
                        .searchRequest(SearchRequest.builder().topK(ragConfigData.getTopK())
                                .similarityThreshold(ragConfigData.getSimilarityThreshold())
                                .build()).build(),simpleLoggerAdvisor).build();


    }
    @Bean("openAIAdvancedRAGChatClient")
    ChatClient openAIAdvancedRagChatClient(GoogleGenAiChatModel googleGenAiChatModel, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor){
        return ChatClient.builder(googleGenAiChatModel)
                .defaultAdvisors(retrievalAugmentationAdvisor).build();

    }
    @Bean
    @Primary
    EmbeddingModel primaryEmbedding(@Qualifier("ollamaEmbeddingModel")EmbeddingModel embeddingModel){
        return embeddingModel;
    }

//    @Bean("ollamaChatClient")
//    ChatClient ollamaChatclient(OllamaChatModel ollamaChatModel){
//        return ChatClient.builder(ollamaChatModel).build();
//    }

    @Bean
    ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(3)
                .build();
    }

//    @Bean("openAIChatClient")
//    ChatClient openAIchatClient(OpenAiChatModel openAiChatModel
//    , SimpleLoggerAdvisor simpleLoggerAdvisor, SafeGuardAdvisor safeGuardAdvisor, errorWrappingAdvisor errorWrappingAdvisor,
//                                systemPromptAdvisor systemPromptAdvisior, ValidationAdvisor validationAdvisor){
//
//        return ChatClient.builder(openAiChatModel).defaultAdvisors(safeGuardAdvisor,simpleLoggerAdvisor, errorWrappingAdvisor,systemPromptAdvisior,validationAdvisor).build();
//    }
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




//    @Bean("openAIChatClientWithMemory")
//    ChatClient openAIChatClientWithMemory(OpenAiChatModel openAiChatModel, ChatMemory chatMemory
//                                        ){
//        return ChatCliento.builder(openAiChatModel)
//
//
////                .defaultAdvisors(VectorStoreChatMemoryAdvisor.builder(pgVectorStore)
////                        .systemPromptTemplate(new PromptTemplate(VectorStoreMemorySystemPrompt))
////
////                        .defaultTopK(TOP_K).build()
////                ,MessageChatMemoryAdvisor.builder(chatMemory).build())
//
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//
//
//                .build();

//    }
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

package com.example.Meeting_Notes_Summariser.Rag.config;

import com.example.Meeting_Notes_Summariser.Rag.config.data.RagConfigData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SimpleRagConfurigation {

    private static final String FILES_PATH="classpath:rag/mini-employee-handbook/files/*.md";
    private static final String   VECTOR_STORE_PATH="src/main/resources/rag/mini-employee-handbook/vectore-store/vectorstore.json";

    @Bean
    public TokenTextSplitter tokenTextSplitter(RagConfigData ragConfigData){
        return TokenTextSplitter.builder().
                withChunkSize(ragConfigData.getChunk().getSize())
                .withMinChunkSizeChars(ragConfigData.getChunk().getMinChunkSize())
                .withMinChunkLengthToEmbed(ragConfigData.getChunk().getMinChunkToEmbed())
                .withMaxNumChunks(ragConfigData.getChunk().getMaxNumChunks())
                .withKeepSeparator(ragConfigData.getChunk().isKeepSeprator())
                .build();
    }

    @Bean
    public SimpleVectorStore simpleVectorStore(@Qualifier("ollamaAiChatWithMemory") EmbeddingModel embeddingModel,
                                                TokenTextSplitter tokenTextSplitter,
                                                RagConfigData ragConfigData){



    }
}

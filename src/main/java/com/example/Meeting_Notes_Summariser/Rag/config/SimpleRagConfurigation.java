package com.example.Meeting_Notes_Summariser.Rag.config;

import com.example.Meeting_Notes_Summariser.Rag.config.data.RagConfigData;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
                                                RagConfigData ragConfigData) throws Exception {

    var simpleVectorStore=SimpleVectorStore.builder(embeddingModel).build();
    var vectorStoreFile=new File(VECTOR_STORE_PATH);

    if(vectorStoreFile.exists() && !ragConfigData.isForceRebuild()){
        log.info("Vector store file exists, loading it from: {}",VECTOR_STORE_PATH);
        simpleVectorStore.load(vectorStoreFile);
        return simpleVectorStore;
    }
    log.info("Creating a new vector Store file at: {}",VECTOR_STORE_PATH);

//    ingest & Build new Vectore Store
    var documents=loadDocuments();
    var chunks=getChunks(documents,tokenTextSplitter);
    simpleVectorStore.add(chunks);
    simpleVectorStore.save(vectorStoreFile);
    return simpleVectorStore;
    }

    private List<Document> getChunks(List<Document> documents, TokenTextSplitter tokenTextSplitter) {
        return tokenTextSplitter.apply(documents);
    }

    private List<Document> loadDocuments() throws Exception {
        PathMatchingResourcePatternResolver resolver=new PathMatchingResourcePatternResolver();
      Resource[] resources=  resolver.getResources(FILES_PATH);

      List<Document> documents=new ArrayList<>();
      for(Resource resource:resources){
          TextReader reader=new TextReader(resource);
          reader.getCustomMetadata().put("category",resource.getFilename().replace(".md",""));
          reader.getCustomMetadata().put("access_level","public");
          reader.getCustomMetadata().put("version","2026.01");
          documents.addAll(reader.read());
      }
      return documents;
    }
}

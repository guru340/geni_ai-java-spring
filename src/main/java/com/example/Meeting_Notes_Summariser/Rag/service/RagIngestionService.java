package com.example.Meeting_Notes_Summariser.Rag.service;

import com.example.Meeting_Notes_Summariser.Rag.config.data.PgVectorStoreConfigData;
import com.example.Meeting_Notes_Summariser.Rag.config.data.RagConfigData;
import com.example.Meeting_Notes_Summariser.Rag.exception.RagException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.swing.text.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class RagIngestionService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final RagConfigData ragConfigData;
    private final PdfDocumentReaderConfig pdfDocumentReaderConfig;
    private final TokenTextSplitter tokenTextSplitter;
    private final String ragvectorTableName;

    public RagIngestionService(VectorStore vectorStore,
                               JdbcTemplate jdbcTemplate,
                               RagConfigData ragConfigData,
                                TokenTextSplitter tokenTextSplitter,
                               PgVectorStoreConfigData pgVectorStoreConfigData) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.ragConfigData = ragConfigData;
        RagConfigData.PdfProperties pdfProperties=ragConfigData.getPdf();
        this.pdfDocumentReaderConfig = PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(pdfProperties.getPagesPerDocument())
                .withPageExtractedTextFormatter(
                        ExtractedTextFormatter.builder()
                                .withLeftAlignment(pdfProperties.isLeftAlignment())
                                .withNumberOfTopTextLinesToDelete(pdfProperties.getNumberOfTopTextLinesToDelete())
                                .withNumberOfBottomTextLinesToDelete(pdfProperties.getNumberOfBottomTextLinesToDelete())
                                .build()
                )
                .build();
        this.tokenTextSplitter = tokenTextSplitter;
        this.ragvectorTableName = pgVectorStoreConfigData.getTableNameForRag();
    }

    public void initializePgVectorStore() throws RagException {
        if (skipIngest(jdbcTemplate)) {
            return;
        }
        var pdfResources = getResources();
        if (pdfResources == null) {
            return;
        }
        ingestDocumentChunksToVectorStore(pdfResources);
    }

    private Resource[] getResources() {
        try {
            var resolver = new PathMatchingResourcePatternResolver();
            String pdfPath = ragConfigData.getPdf().getPath();
            Resource[] pdfResources = resolver.getResources(pdfPath);
            if (pdfResources.length == 0) {
                log.warn("No PDFs found at {}", pdfPath);
                return null;
            }
            return pdfResources;
        } catch (IOException e) {
            throw new RagException("Could not get pdf resources!", e);
        }
    }

    private void ingestDocumentChunksToVectorStore(Resource[] pdfResources) {
        var documents = getDocuments(pdfResources);
        var chunks = getChunks(documents);
        addChunkIndex(chunks);
        vectorStore.add(chunks);
        log.info("Ingested {} chunks into PgVector", chunks.size());
    }

    private boolean skipIngest(JdbcTemplate jdbcTemplate) {
        if (ragConfigData.isForceRebuild()) {
            log.info("force-rebuild=true -> truncating {}", ragvectorTableName);
            jdbcTemplate.update("TRUNCATE TABLE " + ragvectorTableName);
        } else {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + ragvectorTableName, Integer.class);
            if (count != null && count > 0) {
                log.info("Vector store already populated ({} rows). Skipping ingest. Set app.rag.force-rebuild=true to rebuild", count);
                return true;
            }
        }
        return false;
    }

    private List<Document> getDocuments(Resource[] pdfResources) {
        List<Document> documents = new ArrayList<>();
        for (Resource pdfResource: pdfResources) {
            List<Document> parts = getDocumentParts(pdfResource);
            addMetadata(pdfResource, parts);
            documents.addAll(parts);
        }
        return documents;
    }

}

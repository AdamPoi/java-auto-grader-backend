package io.adampoi.java_auto_grader.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DocumentLoader {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private Logger logger = LoggerFactory.getLogger(DocumentLoader.class);

    public DocumentLoader(EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingStore = embeddingStore;
    }

    public void loadDocument() {
        logger.info("Loading document");
        Document document = FileSystemDocumentLoader.loadDocument("<LOCATION OF YOUR DOCUMENT WHICH WE USE TO TRAIN OUR MODEL>");
        EmbeddingStoreIngestor.ingest(document, embeddingStore);
        logger.info("Document loaded");
    }
}
package io.adampoi.java_auto_grader.rest;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/")
public class ChatResource {

    private final ChatLanguageModel chatLanguageModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private Logger logger = LoggerFactory.getLogger(ChatResource.class);

    public ChatResource(ChatLanguageModel chatLanguageModel, EmbeddingStore<TextSegment> embeddingStore) {
        this.chatLanguageModel = chatLanguageModel;
        this.embeddingStore = embeddingStore;
    }


    @GetMapping("/chat")
    public ApiSuccessResponse<String> getLLMResponse(@RequestParam("userQuery") String userQuery) {
        ChatService chatService = AiServices.builder(ChatService.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build();
        String response = chatService.chat(userQuery);
        return ApiSuccessResponse.<String>builder()
                .data(response)
                .statusCode(HttpStatus.OK)
                .build();
    }


    @GetMapping("/load")
    public void loadDocument() {
        logger.info("Loading document");
        Document document = FileSystemDocumentLoader.loadDocument("rag-data.txt");
        EmbeddingStoreIngestor.ingest(document, embeddingStore);
        logger.info("Document loaded");
    }
}
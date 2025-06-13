package io.adampoi.java_auto_grader.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelConfig {

    @Bean
    ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .modelName("llama3.2:3b")
                .baseUrl("http://localhost:11434")
                .build();
    }
}
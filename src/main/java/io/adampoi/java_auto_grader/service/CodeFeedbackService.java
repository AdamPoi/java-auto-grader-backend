package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.model.dto.CodeFeedbackDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CodeFeedbackService {

    private final org.springframework.ai.chat.client.ChatClient chatClient;

    public CodeFeedbackService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public CodeFeedbackDTO generateFeedback(String code) {
        BeanOutputConverter<CodeFeedbackDTO> converter = new BeanOutputConverter<>(CodeFeedbackDTO.class);

        String promptTemplate = """
                Analyze the following Java code and provide feedback in JSON format.
                {format}
                
                Code:
                {code}
                """;

        PromptTemplate template = new PromptTemplate(promptTemplate);
        Prompt prompt = template.create(Map.of(
                "code", code,
                "format", converter.getFormat()
        ));

        String response = chatClient.prompt(prompt).toString();
        return converter.convert(response);
    }
}

package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.model.response.CodeAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient.mutate()
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    public String generateText(String prompt) {
        return chatClient.prompt(prompt).call().content();
    }

    public CodeAnalysisResponse gemerateCodeFeedback(String javaCode) {
        log.info("Generating code analysis for: {}", javaCode);

        String jsonInstructions =
                "Your response should be in JSON format.\n" +
                        "Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.\n" +
                        "Do not include markdown code blocks in your response.\n" +
                        "Remove the ```json markdown from the output.\n";

        String analysisPrompt =
                "Analyze the following Java code and provide feedback on code quality, potential issues, and suggestions for improvement: " +
                        javaCode;

        PromptTemplate pt = new PromptTemplate(analysisPrompt);
        return chatClient
                .prompt(pt.create())
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }

    public List<Person> getPerson() {
        PromptTemplate pt = new PromptTemplate("""
                Return a current list of 10 persons if exists or generate a new list with random values.
                Each object should contain an auto-incremented id field.
                Do not include any explanations or additional text.
                """);

        return chatClient.prompt(pt.create())
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }


}
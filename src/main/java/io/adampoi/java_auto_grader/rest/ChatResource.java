package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.CodeAnalysisResponse;
import io.adampoi.java_auto_grader.service.ChatService;
import io.adampoi.java_auto_grader.service.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/")
@Slf4j
public class ChatResource {


    private final ChatService chatService;

    public ChatResource(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public ApiSuccessResponse<String> generateText(@RequestBody String prompt) {
        return ApiSuccessResponse.<String>builder()
                .data(chatService.generateText(prompt))
                .statusCode(HttpStatus.OK)
                .build();

    }

    @GetMapping("/analyze-code")
    public ApiSuccessResponse<CodeAnalysisResponse> generateCodeAnalysis(@RequestBody String javaCode) {
        CodeAnalysisResponse response = chatService.gemerateCodeFeedback(javaCode);
        log.info("Generated code analysis: {}", response);
        return ApiSuccessResponse.<CodeAnalysisResponse>builder()
                .data(response)
                .statusCode(HttpStatus.OK)
                .build();

    }

    @GetMapping("/person")
    public ApiSuccessResponse<List<Person>> generateText() {
        return ApiSuccessResponse.<List<Person>>builder()
                .data(chatService.getPerson())
                .statusCode(HttpStatus.OK)
                .build();

    }


}
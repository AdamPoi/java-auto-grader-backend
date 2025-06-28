package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.repository.SubmissionRepository;
import io.adampoi.java_auto_grader.service.ChatService;
import io.adampoi.java_auto_grader.service.SubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/")
@Slf4j
public class ChatResource {


    private final ChatService chatService;
    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;

    public ChatResource(ChatService chatService, SubmissionService submissionService, SubmissionRepository submissionRepository) {
        this.chatService = chatService;
        this.submissionService = submissionService;
        this.submissionRepository = submissionRepository;
    }

    @GetMapping("/chat")
    public ApiSuccessResponse<String> generateText(@RequestBody String prompt) {
        return ApiSuccessResponse.<String>builder()
                .data(chatService.generateText(prompt))
                .statusCode(HttpStatus.OK)
                .build();

    }


}
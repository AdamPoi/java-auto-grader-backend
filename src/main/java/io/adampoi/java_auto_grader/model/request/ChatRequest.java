package io.adampoi.java_auto_grader.model.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.ai.chat.messages.Message;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
public class ChatRequest {

    @NotBlank(message = "Model is required")
    private String model = "llama3.1:8b";

    private String prompt;
    private List<Message> messages;
    private double temperature = 0.7f;
    private boolean stream = false;

    @AssertTrue(message = "'prompt' oder 'messages' are required")
    private boolean isPromptOrMessages() {
        return prompt != null || messages != null;
    }

}
package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.model.request.ChatCodeAnalyzerRequest;
import io.adampoi.java_auto_grader.model.type.CodeFile;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodeFeedbackService {

    private final ChatClient chatClient;

    public CodeFeedbackService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateFeedback(ChatCodeAnalyzerRequest request) {
        String formattedStudentCodes = formatCodeFiles(request.getStudentCodes());
        String formattedRubrics = formatRubrics(request.getRubrics());
        String promptTemplate = """
                Anda adalah seorang instruktur Java yang sedang meninjau kode mahasiswa. Berikan feedback yang jelas dan membantu dalam bahasa Indonesia.
                
                Tinjau kode terhadap persyaratan dan berikan saran spesifik untuk perbaikan.
                Bersikaplah konstruktif dan tunjukkan apa yang sudah baik dan apa yang perlu diperbaiki.
                
                Format respons Anda dalam markdown dengan bagian-bagian berikut:
                - **Ringkasan:** Gambaran singkat tentang kualitas dan pendekatan submission secara keseluruhan
                - **Kelebihan:** Apa yang sudah dikerjakan dengan baik oleh mahasiswa
                - **Masalah:** Permasalahan spesifik yang perlu diperbaiki
                - **Saran:** Bagaimana cara meningkatkan kode
                
                ---
                **Persyaratan:**
                {instructions}
                
                **Rubrik Penilaian:**
                {rubrics}
                
                **Kode Mahasiswa:**
                ```java
                {codes}
                ```
                
                Feedback Anda:
                """;


        return chatClient.prompt()
                .user(p -> p.text(promptTemplate)
                        .param("rubrics", formattedRubrics)
                        .param("instructions", request.getInstructions())
                        .param("codes", formattedStudentCodes))
                .call()
                .content();
    }

    private String formatCodeFiles(List<CodeFile> codeFiles) {
        return codeFiles.stream()
                .map(file ->
                        "--- File: " + file.getFileName() + " ---\n" +
                                "```java\n" +
                                file.getContent() + "\n" +
                                "```"
                )
                .collect(Collectors.joining("\n\n"));
    }

    private String formatRubrics(List<ChatCodeAnalyzerRequest.SimpleRubric> rubrics) {
        if (rubrics == null || rubrics.isEmpty()) {
            return "";
        }
        String rubricDetails = rubrics.stream()
                .map(r -> "- " + r.getName() + ": " + r.getDescription())
                .collect(Collectors.joining("\n"));
        return "\n--- Grading Rubrics ---\n" + rubricDetails;
    }
}
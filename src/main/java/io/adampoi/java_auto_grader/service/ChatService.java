package io.adampoi.java_auto_grader.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

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

    public String generateCodeFeedback(String javaCode) {
        String analysisPrompt = """
                Anda adalah dosen Java berpengalaman yang sedang membantu mahasiswa memperbaiki kode mereka. 
                Silakan analisis kode Java berikut dan berikan feedback yang membantu:
                
                **Kode yang akan dianalisis:**
                %s
                
                
                
                **Format jawaban Anda sebagai berikut:**
                - **Kesalahan yang Ditemukan:** [list semua error, bugs, dan masalah yang teridentifikasi]
                - **Saran Perbaikan:** [rekomendasi spesifik dan actionable untuk memperbaiki setiap masalah]
                - **Aspek Positif:** [hal-hal yang sudah baik dan benar dalam kode mahasiswa]
                - **Tips Tambahan:** [best practices atau saran pengembangan lebih lanjut]
                """.formatted(javaCode);

        PromptTemplate pt = new PromptTemplate(analysisPrompt);
        return chatClient
                .prompt(pt.create())
                .call()
                .content();
    }


}
package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.evaluation.rq3.Rq3ModelGateway;
import io.adampoi.java_auto_grader.evaluation.rq3.Rq3ModelSpec;
import io.adampoi.java_auto_grader.evaluation.rq3.StructuredFeedbackJsonSchema;
import io.adampoi.java_auto_grader.model.request.ChatCodeAnalyzerRequest;
import io.adampoi.java_auto_grader.model.type.CodeFile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CodeFeedbackService {

    private static final String SYSTEM_PROMPT = """
            Anda adalah instruktur Java yang memberikan feedback berbasis bukti.
            Evaluasi hanya instruksi, rubrik, dan kode bernomor yang diberikan.
            Jangan menciptakan identifier, nomor baris, persyaratan, hasil eksekusi,
            atau aturan style. Gunakan PRAISE untuk observasi positif dan UNCERTAINTY
            jika bukti tidak cukup. Berikan saran konseptual tanpa membocorkan program
            atau metode lengkap yang sudah diperbaiki.
            Kembalikan tepat satu objek JSON tanpa Markdown fence atau teks tambahan.
            """;
    private static final String USER_PROMPT_TEMPLATE = """
            Tinjau kode terhadap persyaratan dan rubrik berikut.
            
            PERSYARATAN:
            {instructions}
            
            RUBRIK:
            {rubrics}
            
            KODE MAHASISWA DENGAN NOMOR BARIS:
            {codes}
            
            ATURAN CLAIM:
            - type harus tepat satu dari: DEFECT, STYLE, PRAISE, UNCERTAINTY.
            - category harus tepat satu dari: {categories}.
            - Setiap identifier harus muncul verbatim pada kode.
            - Setiap lineNumbers harus merujuk ke nomor baris yang diberikan.
            - Gunakan claims kosong jika tidak ada claim yang didukung bukti.
            - Gunakan CORRECTNESS untuk pujian tentang perilaku yang benar.
            - Gunakan STYLE hanya untuk masalah maintainability yang negatif.
            
            Kembalikan tepat bentuk JSON ini:
            {
              "summary": "ringkasan singkat berbasis bukti",
              "claims": [
                {
                  "type": "DEFECT",
                  "category": "LOGIC",
                  "message": "claim singkat",
                  "evidence": "bukti langsung dari kode atau rubrik",
                  "identifiers": ["identifierDalamKode"],
                  "lineNumbers": [1]
                }
              ],
              "completeSolutionProvided": false
            }
            """;

    private final Rq3ModelGateway modelGateway;
    private final CodeFeedbackProperties properties;

    public CodeFeedbackService(Rq3ModelGateway modelGateway,
                               CodeFeedbackProperties properties) {
        this.modelGateway = modelGateway;
        this.properties = properties;
    }

    public String generateFeedback(ChatCodeAnalyzerRequest request) {
        String formattedStudentCodes = formatCodeFiles(request.getStudentCodes());
        String formattedRubrics = formatRubrics(request.getRubrics());
        String userPrompt = USER_PROMPT_TEMPLATE
                .replace("{rubrics}", formattedRubrics)
                .replace("{instructions}", request.getInstructions())
                .replace("{codes}", formattedStudentCodes)
                .replace("{categories}",
                        String.join(", ", StructuredFeedbackJsonSchema.CATEGORIES));
        Rq3ModelSpec model = properties.resolveModel(request.getModel());
        Rq3ModelGateway.GenerationOptions options = new Rq3ModelGateway.GenerationOptions(
                properties.getTemperature(), properties.getTopP(), properties.getSeed());
        return modelGateway.generate(model, SYSTEM_PROMPT, userPrompt, options);
    }

    private String formatCodeFiles(List<CodeFile> codeFiles) {
        return codeFiles.stream()
                .map(file ->
                        "--- File: " + file.getFileName() + " ---\n" +
                                withLineNumbers(file.getContent())
                )
                .collect(Collectors.joining("\n\n"));
    }

    private String withLineNumbers(String source) {
        String[] lines = source.split("\\R", -1);
        return IntStream.range(0, lines.length)
                .mapToObj(index -> "%4d | %s".formatted(index + 1, lines[index]))
                .collect(Collectors.joining("\n"));
    }

    private String formatRubrics(List<ChatCodeAnalyzerRequest.SimpleRubric> rubrics) {
        if (rubrics == null || rubrics.isEmpty()) {
            return "";
        }
        String rubricDetails = rubrics.stream()
                .map(r -> "- " + r.getName() + ": " + r.getDescription())
                .collect(Collectors.joining("\n"));
        return rubricDetails;
    }
}

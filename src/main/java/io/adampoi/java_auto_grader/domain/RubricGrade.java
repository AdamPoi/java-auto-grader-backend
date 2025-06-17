package io.adampoi.java_auto_grader.domain;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.arguments.*;
import io.adampoi.java_auto_grader.model.type.GradeArguments;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "rubric_grades")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class RubricGrade {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 200)
    private String functionName;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal points;

    @Column(nullable = false)
    private Integer displayOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "arguments", columnDefinition = "jsonb")
    private Map<String, Object> arguments = new HashMap<>();


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GradeType gradeType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rubric_id", referencedColumnName = "id", nullable = false)
    private Rubric rubric;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", referencedColumnName = "id", nullable = false)
    private Assignment assignment;

    @OneToMany(mappedBy = "rubricGrade", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<GradeExecution> gradeExecutions;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, updatable = true)
    private OffsetDateTime updatedAt;

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public String getStringArgument(String key) {
        return arguments != null ? (String) arguments.get(key) : null;
    }

    public Integer getIntegerArgument(String key) {
        Object value = arguments != null ? arguments.get(key) : null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    public Boolean getBooleanArgument(String key) {
        return arguments != null ? (Boolean) arguments.get(key) : null;
    }

    public Double getDoubleArgument(String key) {
        Object value = arguments != null ? arguments.get(key) : null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public java.util.List<Object> getListArgument(String key) {
        return arguments != null ? (java.util.List<Object>) arguments.get(key) : null;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMapArgument(String key) {
        return arguments != null ? (Map<String, Object>) arguments.get(key) : null;
    }

    public void setArgument(String key, Object value) {
        if (arguments == null) {
            arguments = new HashMap<>();
        }
        arguments.put(key, value);
    }


    public <T extends GradeArguments> T getTypedArguments(Class<T> clazz, ObjectMapper objectMapper) {
        if (arguments == null) return null;

        try {
            String json = objectMapper.writeValueAsString(arguments);
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize arguments", e);
        }
    }

    public void setTypedArguments(GradeArguments typedArguments, ObjectMapper objectMapper) {
        if (typedArguments == null) {
            this.arguments = null;
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(typedArguments);
            this.arguments = objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize arguments", e);
        }
    }


    public CompilationArguments getCompilationArguments(ObjectMapper objectMapper) {
        return getTypedArguments(CompilationArguments.class, objectMapper);
    }

    public ErrorHandlingArguments getErrorHandlingArguments(ObjectMapper objectMapper) {
        return getTypedArguments(ErrorHandlingArguments.class, objectMapper);
    }

    public FunctionalityArguments getFunctionalityArguments(ObjectMapper objectMapper) {
        return getTypedArguments(FunctionalityArguments.class, objectMapper);
    }

    public InputOutputArguments getInputOutputArguments(ObjectMapper objectMapper) {
        return getTypedArguments(InputOutputArguments.class, objectMapper);
    }

    public DataTypeArguments getDataTypeArguments(ObjectMapper objectMapper) {
        return getTypedArguments(DataTypeArguments.class, objectMapper);
    }

    public RegexMatchArguments getRegexMatchArguments(ObjectMapper objectMapper) {
        return getTypedArguments(RegexMatchArguments.class, objectMapper);
    }

    public FullMatchArguments getFullMatchArguments(ObjectMapper objectMapper) {
        return getTypedArguments(FullMatchArguments.class, objectMapper);
    }

    public SubstringMatchArguments getSubstringMatchArguments(ObjectMapper objectMapper) {
        return getTypedArguments(SubstringMatchArguments.class, objectMapper);
    }

    public TimeoutArguments getTimeoutArguments(ObjectMapper objectMapper) {
        return getTypedArguments(TimeoutArguments.class, objectMapper);
    }

    public FileTestingArguments getFileTestingArguments(ObjectMapper objectMapper) {
        return getTypedArguments(FileTestingArguments.class, objectMapper);
    }

    public ScriptTestingArguments getScriptTestingArguments(ObjectMapper objectMapper) {
        return getTypedArguments(ScriptTestingArguments.class, objectMapper);
    }

    public CodeStyleArguments getCodeStyleArguments(ObjectMapper objectMapper) {
        return getTypedArguments(CodeStyleArguments.class, objectMapper);
    }

    public CodeStructureArguments getCodeStructureArguments(ObjectMapper objectMapper) {
        return getTypedArguments(CodeStructureArguments.class, objectMapper);
    }

    public ManualGradingArguments getManualGradingArguments(ObjectMapper objectMapper) {
        return getTypedArguments(ManualGradingArguments.class, objectMapper);
    }

    public OtherArguments getOtherArguments(ObjectMapper objectMapper) {
        return getTypedArguments(OtherArguments.class, objectMapper);
    }


    public enum GradeType {
        AUTOMATIC,
        COMPILATION,
        ERROR_HANDLING,
        FUNCTIONALITY,
        INPUT_OUTPUT,
        DATA_TYPE,
        REGEX_MATCH,
        FULL_MATCH,
        SUBSTRING_MATCH,
        TIMEOUT,
        FILE_TESTING,
        SCRIPT_TESTING,
        CODE_STYLE,
        CODE_STRUCTURE,
        MANUAL_GRADING,
        OTHER
    }
}
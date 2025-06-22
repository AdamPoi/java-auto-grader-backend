package io.adampoi.java_auto_grader.rest;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.assertj.core.api.Assertions.assertThat;

class TestClass1 {


    private static final String SOURCE_PATH = "src/main/java";
    private static List<CompilationUnit> allCompilationUnits;

    @BeforeAll
    public static void setup() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get(SOURCE_PATH));

        // Parse all files and collect the successful results into our static list.
        allCompilationUnits = sourceRoot.tryToParse("").stream()
                .filter(result -> result.isSuccessful() && result.getResult().isPresent())
                .map(result -> result.getResult().get())
                .toList();

        // A simple check to ensure parsing happened correctly.
        assertThat(allCompilationUnits).isNotEmpty();
        System.out.printf("Successfully parsed %d Java files for testing.%n", allCompilationUnits.size());
    }

    @TestFactory
    Stream<DynamicTest> testNamingConventions() {

        return allCompilationUnits.stream()
                .map(cu -> DynamicTest.dynamicTest("class 'Main' exists in " + cu.getStorage().get().getFileName(), () -> {
                    boolean exists = cu.getTypes().stream()
                            .filter(TypeDeclaration::isClassOrInterfaceDeclaration)
                            .map(td -> ((ClassOrInterfaceDeclaration) td).getNameAsString())
                            .anyMatch(n -> n.equals("Main"));
                    assertThat(exists).as("Expect class named 'Main'").isTrue();
                }));

    }

}
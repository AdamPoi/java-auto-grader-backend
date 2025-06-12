package io.adampoi.java_auto_grader.util;

import io.adampoi.java_auto_grader.model.type.TestCaseResult;
import io.adampoi.java_auto_grader.model.type.TestSuiteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TestReportParser {

    public List<TestSuiteResult> parseTestReports(Path testResultsDir) {
        if (!Files.exists(testResultsDir)) {
            log.warn("Test results directory not found: {}", testResultsDir);
            return Collections.emptyList();
        }

        List<TestSuiteResult> testSuites = new ArrayList<>();

        try {
            Files.walk(testResultsDir)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .forEach(xmlPath -> parseXmlReport(xmlPath).ifPresent(testSuites::add));
        } catch (IOException e) {
            log.error("Failed to walk test results directory", e);
        }

        return testSuites;
    }

    private Optional<TestSuiteResult> parseXmlReport(Path xmlPath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlPath.toFile());

            return Optional.of(parseTestSuite(document.getDocumentElement(), document));
        } catch (Exception e) {
            log.warn("Failed to parse XML report: {}", xmlPath, e);
            return Optional.empty();
        }
    }

    private TestSuiteResult parseTestSuite(Element testSuite, Document document) {
        TestSuiteResult suiteResult = new TestSuiteResult();
        suiteResult.setName(testSuite.getAttribute("name"));
        suiteResult.setTotalTests(parseIntAttribute(testSuite, "tests", 0));
        suiteResult.setFailures(parseIntAttribute(testSuite, "failures", 0));
        suiteResult.setErrors(parseIntAttribute(testSuite, "errors", 0));
        suiteResult.setSkipped(parseIntAttribute(testSuite, "skipped", 0));
        suiteResult.setExecutionTime(parseDoubleAttribute(testSuite, "time", 0.0));

        List<TestCaseResult> testCases = parseTestCases(document);
        suiteResult.setTestCases(testCases);

        return suiteResult;
    }

    private List<TestCaseResult> parseTestCases(Document document) {
        List<TestCaseResult> testCases = new ArrayList<>();
        NodeList testCaseNodes = document.getElementsByTagName("testcase");

        for (int i = 0; i < testCaseNodes.getLength(); i++) {
            Element testCase = (Element) testCaseNodes.item(i);
            testCases.add(parseTestCase(testCase));
        }

        return testCases;
    }

    private TestCaseResult parseTestCase(Element testCase) {
        TestCaseResult result = new TestCaseResult();
        result.setClassName(testCase.getAttribute("classname"));
        result.setMethodName(testCase.getAttribute("name"));
        result.setExecutionTime(parseDoubleAttribute(testCase, "time", 0.0));

        if (hasChildElement(testCase, "failure")) {
            setFailureInfo(result, testCase);
        } else if (hasChildElement(testCase, "error")) {
            setErrorInfo(result, testCase);
        } else if (hasChildElement(testCase, "skipped")) {
            result.setStatus("SKIPPED");
        } else {
            result.setStatus("PASSED");
        }

        return result;
    }

    private void setFailureInfo(TestCaseResult result, Element testCase) {
        Element failure = (Element) testCase.getElementsByTagName("failure").item(0);
        result.setStatus("FAILED");
        result.setFailureMessage(failure.getAttribute("message"));
        result.setStackTrace(failure.getTextContent());
    }

    private void setErrorInfo(TestCaseResult result, Element testCase) {
        Element error = (Element) testCase.getElementsByTagName("error").item(0);
        result.setStatus("ERROR");
        result.setErrorMessage(error.getAttribute("message"));
        result.setStackTrace(error.getTextContent());
    }

    private boolean hasChildElement(Element parent, String tagName) {
        return parent.getElementsByTagName(tagName).getLength() > 0;
    }

    private int parseIntAttribute(Element element, String attribute, int defaultValue) {
        String value = element.getAttribute(attribute);
        return value.isEmpty() ? defaultValue : Integer.parseInt(value);
    }

    private double parseDoubleAttribute(Element element, String attribute, double defaultValue) {
        String value = element.getAttribute(attribute);
        return value.isEmpty() ? defaultValue : Double.parseDouble(value);
    }
}
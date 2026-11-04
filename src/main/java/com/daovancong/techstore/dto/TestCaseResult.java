package com.daovancong.techstore.dto;

import java.util.List;

public class TestCaseResult {
    private String testCaseId;
    private String scenarioName;
    private String basisPath;
    private String inputDescription;
    private String expectedResult;
    private String actualResult;
    private boolean passed;
    private long executionTimeMs;
    private List<Integer> nodesCovered;

    public TestCaseResult() {}

    public TestCaseResult(String testCaseId, String scenarioName, String basisPath,
                          String inputDescription, String expectedResult, String actualResult,
                          boolean passed, long executionTimeMs, List<Integer> nodesCovered) {
        this.testCaseId = testCaseId;
        this.scenarioName = scenarioName;
        this.basisPath = basisPath;
        this.inputDescription = inputDescription;
        this.expectedResult = expectedResult;
        this.actualResult = actualResult;
        this.passed = passed;
        this.executionTimeMs = executionTimeMs;
        this.nodesCovered = nodesCovered;
    }

    public String getTestCaseId() { return testCaseId; }
    public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }

    public String getScenarioName() { return scenarioName; }
    public void setScenarioName(String scenarioName) { this.scenarioName = scenarioName; }

    public String getBasisPath() { return basisPath; }
    public void setBasisPath(String basisPath) { this.basisPath = basisPath; }

    public String getInputDescription() { return inputDescription; }
    public void setInputDescription(String inputDescription) { this.inputDescription = inputDescription; }

    public String getExpectedResult() { return expectedResult; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }

    public String getActualResult() { return actualResult; }
    public void setActualResult(String actualResult) { this.actualResult = actualResult; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public List<Integer> getNodesCovered() { return nodesCovered; }
    public void setNodesCovered(List<Integer> nodesCovered) { this.nodesCovered = nodesCovered; }
}

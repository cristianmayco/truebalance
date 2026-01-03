package com.truebalance.truebalance.application.dto.output;

public class ImportErrorDTO {

    private Integer lineNumber;
    private String field;
    private String message;
    private String value;

    public ImportErrorDTO() {
    }

    public ImportErrorDTO(Integer lineNumber, String field, String message, String value) {
        this.lineNumber = lineNumber;
        this.field = field;
        this.message = message;
        this.value = value;
    }

    // Getters and Setters
    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

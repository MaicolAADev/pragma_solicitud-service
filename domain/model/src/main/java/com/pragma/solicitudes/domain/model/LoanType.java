package com.pragma.solicitudes.domain.model;


public enum LoanType {
    PERSONAL("Personal"),
    HIPOTECARIO("Hipotecario"),
    AUTOMOTRIZ("Automotriz"),
    EDUCATIVO("Educativo");

    private final String description;

    LoanType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static String[] getValidTypes() {
        LoanType[] types = values();
        String[] validTypes = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            validTypes[i] = types[i].getDescription();
        }
        return validTypes;
    }
}
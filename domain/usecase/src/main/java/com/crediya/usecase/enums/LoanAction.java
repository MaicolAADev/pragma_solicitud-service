package com.crediya.usecase.enums;

public enum LoanAction {
    APPROVE("approve", "Aprobado", "2"),
    REFUSE("refuse", "Rechazado", "3");

    private final String action;
    private final String status;
    private final String stateId;

    LoanAction(String action, String status, String stateId) {
        this.action = action;
        this.status = status;
        this.stateId = stateId;
    }

    public String getAction() {
        return action;
    }

    public String getStatus() {
        return status;
    }

    public String getStateId() {
        return stateId;
    }

    public static LoanAction fromAction(String action) {
        for (LoanAction loanAction : values()) {
            if (loanAction.action.equalsIgnoreCase(action)) {
                return loanAction;
            }
        }
        throw new IllegalArgumentException("Acción no válida: " + action);
    }

    public static boolean isValidAction(String action) {
        for (LoanAction loanAction : values()) {
            if (loanAction.action.equalsIgnoreCase(action)) {
                return true;
            }
        }
        return false;
    }
}
package com.mtp.config.center.model;

public enum ClientStatus {

    ONLINE("ONLINE"),
    OFFLINE("OFFLINE");

    private final String status;

    ClientStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}

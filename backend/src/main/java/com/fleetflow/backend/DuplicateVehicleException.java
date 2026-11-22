package com.fleetflow.backend;

public class DuplicateVehicleException extends RuntimeException {

    public DuplicateVehicleException(String registrationNumber) {
        super("A vehicle with registration number "
                + registrationNumber + " already exists.");
    }
}
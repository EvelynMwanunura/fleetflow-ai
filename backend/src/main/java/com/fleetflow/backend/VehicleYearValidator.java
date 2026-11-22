package com.fleetflow.backend;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class VehicleYearValidator
        implements ConstraintValidator<ValidVehicleYear, Integer> {

    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        return year == null
                || (year >= 1900 && year <= Year.now().getValue() + 1);
    }
}

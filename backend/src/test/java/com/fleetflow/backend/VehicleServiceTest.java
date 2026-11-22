package com.fleetflow.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.lang.reflect.Field;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehicleServiceTest {

    private VehicleRepository vehicleRepository;
    private VehicleService vehicleService;
    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        vehicleRepository = mock(VehicleRepository.class);
        vehicleService = new VehicleService(vehicleRepository);
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void createVehicleSavesAndReturnsVehicleWhenRegistrationIsAvailable() {
        Vehicle vehicle = createVehicle("REG-001");
        when(vehicleRepository.existsByRegistrationNumberIgnoreCase("REG-001"))
                .thenReturn(false);
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);

        assertSame(vehicle, result);
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void createVehicleThrowsDuplicateVehicleExceptionWhenRegistrationExists() {
        Vehicle vehicle = createVehicle("REG-001");
        when(vehicleRepository.existsByRegistrationNumberIgnoreCase("REG-001"))
                .thenReturn(true);

        assertThrows(
                DuplicateVehicleException.class,
                () -> vehicleService.createVehicle(vehicle));

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicleTrimsAndUppercasesRegistrationBeforeCheckingAndSaving() {
        Vehicle vehicle = createVehicle("  reg-001  ");
        when(vehicleRepository.existsByRegistrationNumberIgnoreCase("REG-001"))
                .thenReturn(false);
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);

        assertSame(vehicle, result);
        assertEquals("REG-001", vehicle.getRegistrationNumber());
        verify(vehicleRepository).existsByRegistrationNumberIgnoreCase("REG-001");
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void getAllVehiclesReturnsVehiclesFromRepository() {
        List<Vehicle> vehicles = List.of(
                createVehicle("REG-001"),
                createVehicle("REG-002"));
        when(vehicleRepository.findAll()).thenReturn(vehicles);

        List<Vehicle> result = vehicleService.getAllVehicles();

        assertSame(vehicles, result);
    }

    @Test
    void getVehicleByIdReturnsVehicleWhenItExists() {
        Vehicle vehicle = createVehicle("REG-001");
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle result = vehicleService.getVehicleById(1L);

        assertSame(vehicle, result);
    }

    @Test
    void getVehicleByIdThrowsVehicleNotFoundExceptionWhenItDoesNotExist() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                VehicleNotFoundException.class,
                () -> vehicleService.getVehicleById(1L));
    }

    @Test
    void updateVehicleCopiesFieldsAndSavesExistingVehicle() throws Exception {
        Vehicle existingVehicle = createVehicle(1L, "REG-001");
        Vehicle updatedVehicle = createVehicle("REG-002");
        updatedVehicle.setMake("Updated Make");
        updatedVehicle.setModel("Updated Model");
        updatedVehicle.setYearOfManufacture(2025);
        updatedVehicle.setVehicleType("TRUCK");
        updatedVehicle.setCapacity(12.5);
        updatedVehicle.setCurrentMileage(1000.0);
        updatedVehicle.setStatus("IN_USE");

        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.findByRegistrationNumberIgnoreCase("REG-002"))
                .thenReturn(Optional.empty());
        when(vehicleRepository.save(existingVehicle)).thenReturn(existingVehicle);

        Vehicle result = vehicleService.updateVehicle(1L, updatedVehicle);

        assertSame(existingVehicle, result);
        assertEquals("REG-002", existingVehicle.getRegistrationNumber());
        assertEquals("Updated Make", existingVehicle.getMake());
        assertEquals("Updated Model", existingVehicle.getModel());
        assertEquals(2025, existingVehicle.getYearOfManufacture());
        assertEquals("TRUCK", existingVehicle.getVehicleType());
        assertEquals(12.5, existingVehicle.getCapacity());
        assertEquals(1000.0, existingVehicle.getCurrentMileage());
        assertEquals("IN_USE", existingVehicle.getStatus());
        verify(vehicleRepository).save(existingVehicle);
    }

    @Test
    void updateVehicleAllowsVehicleToKeepItsOwnRegistrationNumber() throws Exception {
        Vehicle existingVehicle = createVehicle(1L, "REG-001");
        Vehicle updatedVehicle = createVehicle(" reg-001 ");
        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.findByRegistrationNumberIgnoreCase("REG-001"))
                .thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.save(existingVehicle)).thenReturn(existingVehicle);

        Vehicle result = vehicleService.updateVehicle(1L, updatedVehicle);

        assertSame(existingVehicle, result);
        assertEquals("REG-001", existingVehicle.getRegistrationNumber());
        verify(vehicleRepository).save(existingVehicle);
    }

    @Test
    void updateVehicleRejectsCaseInsensitiveDuplicateRegistration()
            throws Exception {
        Vehicle existingVehicle = createVehicle(1L, "REG-001");
        Vehicle otherVehicle = createVehicle(2L, "reg-002");
        Vehicle updatedVehicle = createVehicle(" reg-002 ");
        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.findByRegistrationNumberIgnoreCase("REG-002"))
                .thenReturn(Optional.of(otherVehicle));

        assertThrows(
                DuplicateVehicleException.class,
                () -> vehicleService.updateVehicle(1L, updatedVehicle));

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void vehicleValidationAcceptsApprovedTypesAndStatuses() {
        List<String> approvedTypes = List.of(
                "TRUCK", "PICKUP", "VAN", "BUS", "TRAILER", "CAR", "OTHER");
        List<String> approvedStatuses = List.of(
                "AVAILABLE", "IN_USE", "IN_MAINTENANCE", "INACTIVE");

        for (String type : approvedTypes) {
            Vehicle vehicle = createVehicle("REG-001");
            vehicle.setVehicleType(type);
            assertFalse(invalidFields(vehicle).contains("vehicleType"), type);
        }

        for (String status : approvedStatuses) {
            Vehicle vehicle = createVehicle("REG-001");
            vehicle.setStatus(status);
            assertFalse(invalidFields(vehicle).contains("status"), status);
        }
    }

    @Test
    void vehicleValidationRejectsUnapprovedTypesAndStatuses() {
        Vehicle vehicle = createVehicle("REG-001");
        vehicle.setVehicleType("Truck");
        vehicle.setStatus("IN_SERVICE");

        Set<String> invalidFields = invalidFields(vehicle);

        assertTrue(invalidFields.contains("vehicleType"));
        assertTrue(invalidFields.contains("status"));
    }

    @Test
    void vehicleValidationEnforcesYearBoundaries() {
        int currentYear = Year.now().getValue();

        Vehicle vehicle = createVehicle("REG-001");
        vehicle.setYearOfManufacture(1899);
        assertTrue(invalidFields(vehicle).contains("yearOfManufacture"));

        vehicle.setYearOfManufacture(1900);
        assertFalse(invalidFields(vehicle).contains("yearOfManufacture"));

        vehicle.setYearOfManufacture(currentYear + 1);
        assertFalse(invalidFields(vehicle).contains("yearOfManufacture"));

        vehicle.setYearOfManufacture(currentYear + 2);
        assertTrue(invalidFields(vehicle).contains("yearOfManufacture"));
    }

    @Test
    void vehicleValidationRejectsNegativeCapacityAndMileage() {
        Vehicle vehicle = createVehicle("REG-001");
        vehicle.setCapacity(-0.1);
        vehicle.setCurrentMileage(-0.1);

        Set<String> invalidFields = invalidFields(vehicle);

        assertTrue(invalidFields.contains("capacity"));
        assertTrue(invalidFields.contains("currentMileage"));
    }

    @Test
    void vehicleValidationAllowsNullCapacityAndMileage() {
        Vehicle vehicle = createVehicle("REG-001");
        vehicle.setCapacity(null);
        vehicle.setCurrentMileage(null);

        Set<String> invalidFields = invalidFields(vehicle);

        assertFalse(invalidFields.contains("capacity"));
        assertFalse(invalidFields.contains("currentMileage"));
    }

    @Test
    void vehicleValidationRejectsBlankRegistrationMakeAndModel() {
        Vehicle vehicle = createVehicle("REG-001");
        vehicle.setRegistrationNumber("  ");
        vehicle.setMake(" ");
        vehicle.setModel("");

        Set<String> invalidFields = invalidFields(vehicle);

        assertTrue(invalidFields.contains("registrationNumber"));
        assertTrue(invalidFields.contains("make"));
        assertTrue(invalidFields.contains("model"));
    }

    @Test
    void deleteVehicleDeletesVehicleWhenItExists() throws Exception {
        Vehicle vehicle = createVehicle(1L, "REG-001");
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository).delete(vehicle);
    }

    @Test
    void deleteVehicleThrowsVehicleNotFoundExceptionWhenItDoesNotExist() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                VehicleNotFoundException.class,
                () -> vehicleService.deleteVehicle(1L));

        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }

    private static Vehicle createVehicle(String registrationNumber) {
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber(registrationNumber);
        vehicle.setMake("FleetFlow");
        vehicle.setModel("Transit");
        vehicle.setYearOfManufacture(2024);
        vehicle.setVehicleType("VAN");
        vehicle.setCapacity(5.0);
        vehicle.setCurrentMileage(100.0);
        vehicle.setStatus("AVAILABLE");
        return vehicle;
    }

    private Set<String> invalidFields(Vehicle vehicle) {
        return validator.validate(vehicle).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private static Vehicle createVehicle(Long id, String registrationNumber)
            throws Exception {
        Vehicle vehicle = createVehicle(registrationNumber);
        Field idField = Vehicle.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(vehicle, id);
        return vehicle;
    }
}

package com.fleetflow.backend;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

   public Vehicle getVehicleById(Long id) {
    return vehicleRepository.findById(id)
            .orElseThrow(() -> new VehicleNotFoundException(id));
}

    public Vehicle createVehicle(Vehicle vehicle) {
    String registrationNumber = normalizeRegistrationNumber(
            vehicle.getRegistrationNumber());
    vehicle.setRegistrationNumber(registrationNumber);

    if (vehicleRepository.existsByRegistrationNumberIgnoreCase(
            registrationNumber)) {
        throw new DuplicateVehicleException(
                registrationNumber);
    }

    return vehicleRepository.save(vehicle);
}
    public Vehicle updateVehicle(Long id, Vehicle updatedVehicle) {
    Vehicle existingVehicle = getVehicleById(id);
    String registrationNumber = normalizeRegistrationNumber(
            updatedVehicle.getRegistrationNumber());
    updatedVehicle.setRegistrationNumber(registrationNumber);

    vehicleRepository.findByRegistrationNumberIgnoreCase(registrationNumber)
            .filter(vehicle -> !vehicle.getId().equals(existingVehicle.getId()))
            .ifPresent(vehicle -> {
                throw new DuplicateVehicleException(
                        registrationNumber);
            });

    existingVehicle.setRegistrationNumber(
            registrationNumber);
    existingVehicle.setMake(updatedVehicle.getMake());
    existingVehicle.setModel(updatedVehicle.getModel());
    existingVehicle.setYearOfManufacture(
            updatedVehicle.getYearOfManufacture());
    existingVehicle.setVehicleType(updatedVehicle.getVehicleType());
    existingVehicle.setCapacity(updatedVehicle.getCapacity());
    existingVehicle.setCurrentMileage(updatedVehicle.getCurrentMileage());
    existingVehicle.setStatus(updatedVehicle.getStatus());

    return vehicleRepository.save(existingVehicle);
}

    private String normalizeRegistrationNumber(String registrationNumber) {
        return registrationNumber == null
                ? null
                : registrationNumber.trim().toUpperCase(Locale.ROOT);
    }

public void deleteVehicle(Long id) {
    Vehicle vehicle = getVehicleById(id);
    vehicleRepository.delete(vehicle);
}
}
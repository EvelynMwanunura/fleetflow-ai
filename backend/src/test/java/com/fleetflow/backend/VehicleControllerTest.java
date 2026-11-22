package com.fleetflow.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class VehicleControllerTest {

    private VehicleService vehicleService;
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        vehicleService = mock(VehicleService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = standaloneSetup(new VehicleController(vehicleService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.destroy();
    }

    @Test
    void createVehicleReturnsCreatedForValidRequest() throws Exception {
        when(vehicleService.createVehicle(any(Vehicle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validVehicleJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void createVehicleReturnsBadRequestWithFieldMessagesForInvalidRequest()
            throws Exception {
        String invalidVehicle = validVehicleJson()
                .replace("\"vehicleType\": \"VAN\"", "\"vehicleType\": \"Truck\"");

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidVehicle))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"error\":\"Bad Request\"")))
                .andExpect(content().string(containsString("\"message\":\"Validation failed\"")))
                .andExpect(content().string(containsString("\"vehicleType\"")));
    }

    @Test
    void getMissingVehicleReturnsNotFoundWithExistingErrorFormat()
            throws Exception {
        when(vehicleService.getVehicleById(99L))
                .thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(get("/api/v1/vehicles/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("\"error\":\"Not Found\"")))
                .andExpect(content().string(containsString(
                        "\"message\":\"Vehicle not found with ID: 99\"")));
    }

    @Test
    void createDuplicateVehicleReturnsConflictWithExistingErrorFormat()
            throws Exception {
        when(vehicleService.createVehicle(any(Vehicle.class)))
                .thenThrow(new DuplicateVehicleException("REG-001"));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validVehicleJson()))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("\"error\":\"Conflict\"")))
                .andExpect(content().string(containsString(
                        "\"message\":\"A vehicle with registration number REG-001 already exists.\"")));
    }

    @Test
    void deleteVehicleReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/1"))
                .andExpect(status().isNoContent());
    }

    private static String validVehicleJson() {
        return """
                {
                  "registrationNumber": "REG-001",
                  "make": "FleetFlow",
                  "model": "Transit",
                  "yearOfManufacture": 2024,
                  "vehicleType": "VAN",
                  "capacity": null,
                  "currentMileage": null,
                  "status": "AVAILABLE"
                }
                """;
    }
}

package com.metamapa.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de configuración de seguridad
 * 
 * Verifica que:
 * - Endpoints públicos son accesibles sin autenticación
 * - Endpoints protegidos requieren autenticación
 * - Endpoints admin solo son accesibles para ADMIN
 * 
 * @author MetaMapa Team
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test: Endpoint público debe ser accesible sin autenticación
     */
    @Test
    void testEndpointPublico_sinAutenticacion_debeSerAccesible() throws Exception {
        mockMvc.perform(get("/api/publica/colecciones"))
                .andExpect(status().isOk());
    }

    /**
     * Test: Endpoint protegido sin autenticación debe devolver 401
     */
    @Test
    void testEndpointProtegido_sinAutenticacion_debeDevolver401() throws Exception {
        mockMvc.perform(get("/api/interna/hechos"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: Endpoint protegido con autenticación debe ser accesible
     */
    @Test
    @WithMockUser(roles = "USER")
    void testEndpointProtegido_conAutenticacion_debeSerAccesible() throws Exception {
        mockMvc.perform(get("/api/interna/hechos"))
                .andExpect(status().isOk());
    }

    /**
     * Test: Endpoint admin con rol USER debe devolver 403
     */
    @Test
    @WithMockUser(roles = "USER")
    void testEndpointAdmin_conRolUser_debeDevolver403() throws Exception {
        mockMvc.perform(get("/api/admin/hechos/pendientes"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test: Endpoint admin con rol ADMIN debe ser accesible
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testEndpointAdmin_conRolAdmin_debeSerAccesible() throws Exception {
        mockMvc.perform(get("/api/admin/hechos/pendientes"))
                .andExpect(status().isOk());
    }

    /**
     * Test: Health endpoint debe ser público
     */
    @Test
    void testHealthEndpoint_sinAutenticacion_debeSerAccesible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}

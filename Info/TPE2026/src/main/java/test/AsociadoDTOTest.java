package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import persistencia.AsociadoDTO;

/**
 * Suite de pruebas unitarias para validar de forma estrictamente aislada
 * la clase AsociadoDTO (asignación, recuperación y validaciones internas).
 */
public class AsociadoDTOTest {

    private AsociadoDTO asociado;

    @Before
    public void setUp() {
        // Instancia limpia antes de cada prueba
        asociado = new AsociadoDTO();
    }

    // =========================================================================
    // 1. PRUEBAS DE ASIGNACIÓN Y RECUPERACIÓN (Getters / Setters Válidos)
    // =========================================================================

    @Test
    public void testSetAndGetNombre() {
        asociado.setNombre("Juan");
        assertEquals("El nombre recuperado debe ser igual al asignado", "Juan", asociado.getNombre());
    }

    @Test
    public void testSetAndGetApellido() {
        asociado.setApellido("Pérez");
        assertEquals("El apellido recuperado debe ser igual al asignado", "Pérez", asociado.getApellido());
    }

    @Test
    public void testSetAndGetDni() {
        asociado.setDni("12345678");
        assertEquals("El DNI recuperado debe ser igual al asignado", "12345678", asociado.getDni());
    }

    @Test
    public void testSetAndGetCiudad() {
        asociado.setCiudad("Mar del Plata");
        assertEquals("La ciudad recuperada debe ser igual a la asignada", "Mar del Plata", asociado.getCiudad());
    }

    @Test
    public void testSetAndGetCalle() {
        asociado.setCalle("San Martín");
        assertEquals("La calle recuperada debe ser igual a la asignada", "San Martín", asociado.getCalle());
    }

    @Test
    public void testSetAndGetNumeroValido() {
        asociado.setNumero(1234);
        assertEquals("El número recuperado debe ser igual al asignado", 1234, asociado.getNumero());
    }

    @Test
    public void testSetAndGetTelefono() {
        asociado.setTelefono("2231234567");
        assertEquals("El teléfono recuperado debe ser igual al asignado", "2231234567", asociado.getTelefono());
    }

    @Test
    public void testConstructorParametrizado() {
        AsociadoDTO dto = new AsociadoDTO("Ana", "Gómez", "87654321", "Mar del Plata", "Belgrano", 456, "2239876543");
        assertEquals("Ana", dto.getNombre());
        assertEquals("Gómez", dto.getApellido());
        assertEquals("87654321", dto.getDni());
        assertEquals("Mar del Plata", dto.getCiudad());
        assertEquals("Belgrano", dto.getCalle());
        assertEquals(456, dto.getNumero());
        assertEquals("2239876543", dto.getTelefono());
    }

    // =========================================================================
    // 2. PRUEBAS DE INTEGRIDAD Y Detección de Inconsistencias (AssertionError)
    // =========================================================================

    @Test(expected = AssertionError.class)
    public void testSetNombreNuloFail() {
        asociado.setNombre(null);
    }

    @Test(expected = AssertionError.class)
    public void testSetApellidoNuloFail() {
        asociado.setApellido(null);
    }

    @Test(expected = AssertionError.class)
    public void testSetDniNuloFail() {
        asociado.setDni(null);
    }

    @Test(expected = AssertionError.class)
    public void testSetCiudadNulaFail() {
        asociado.setCiudad(null);
    }

    @Test(expected = AssertionError.class)
    public void testSetCalleNulaFail() {
        asociado.setCalle(null);
    }

    @Test(expected = AssertionError.class)
    public void testSetTelefonoNuloFail() {
        asociado.setTelefono(null);
    }

    @Test(expected = AssertionError.class)
    public void testSetNumeroNegativoFail() {
        asociado.setNumero(-5);
    }

    /**
     * Prueba límite orientada a detectar discrepancia con la SRS:
     * La SRS especifica que el número debe ser >= 0. El DTO actual usa 'numero > 0'.
     */
    @Test
    public void testSetNumeroCeroValidoSegunSRS() {
        asociado.setNumero(0);
        assertEquals("El número 0 debería ser aceptado según la SRS", 0, asociado.getNumero());
    }
}
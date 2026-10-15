package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import persistencia.BaseDeDatos;
import persistencia.DataAccessObject;
import persistencia.AsociadoDTO;
import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.util.Domicilio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Suite de pruebas de integración para evaluar la interacción real entre
 * AsociadoDTO, DataAccessObject y BaseDeDatos conectando contra MySQL.
 */
public class IntegracionPersistenciaTest {

    private static BaseDeDatos bd;
    private DataAccessObject dao;

    @BeforeClass
    public static void setUpClass() {
        // Obtenemos la instancia Singleton de BaseDeDatos
        bd = BaseDeDatos.getInstancia();
    }

    @Before
    public void setUp() throws SQLException {
        // Reiniciamos el esquema y poblamos datos semilla antes de cada test
        bd.iniciarBD();
        dao = new DataAccessObject();
    }

    // =========================================================================
    // 1. PRUEBAS DEL PATRÓN SINGLETON Y CONEXIÓN A BASE DE DATOS
    // =========================================================================

    @Test
    public void testPatronSingleton() {
        BaseDeDatos otraInstancia = BaseDeDatos.getInstancia();
        assertNotNull("La instancia de BaseDeDatos no debe ser nula", bd);
        assertSame("getInstancia debe retornar la misma referencia en memoria (Singleton)", bd, otraInstancia);
    }

    @Test
    public void testConexionActiva() throws SQLException {
        Connection conn = bd.getConexion();
        assertNotNull("La conexión recuperada no debe ser nula", conn);
        assertFalse("La conexión a la base de datos debe estar abierta", conn.isClosed());
    }

    // =========================================================================
    // 2. PRUEBAS DE INICIALIZACIÓN DE TABLAS Y DATOS SEMILLA
    // =========================================================================

    @Test
    public void testCargarDatosSemilla() throws SQLException {
        ArrayList<AsociadoDTO> asociados = dao.cargarBD();
        assertNotNull("La lista devuelta por cargarBD no debe ser nula", asociados);
        assertEquals("Deben haberse cargado exactamente 20 registros semilla", 20, asociados.size());
        
        // Verificamos el primer asociado semilla (Juan State)
        AsociadoDTO primero = asociados.get(0);
        assertEquals("Juan", primero.getNombre());
        assertEquals("12345678", primero.getDni());
    }

    // =========================================================================
    // 3. PRUEBAS DE OPERACIONES CRUD CON PARÁMETROS
    // =========================================================================

    @Test
    public void testGuardarYRecuperarAsociadoValido() throws SQLException {
        Domicilio dom = new Domicilio("San Martín", 1234, "Mar del Plata");
        Asociado nuevo = new Asociado("Roberto", "Gomez", "99887766", "2239998877", dom);

        dao.guardar(nuevo);

        ArrayList<AsociadoDTO> asociados = dao.cargarBD();
        assertEquals("El total de asociados debe incrementarse a 21", 21, asociados.size());

        // Eliminación/Limpieza para verificar persitencia
        dao.eliminar("99887766");
    }

    @Test
    public void testCargarConLimite() throws SQLException {
        ArrayList<AsociadoDTO> limiteCinco = dao.cargarConLimite(5);
        assertNotNull(limiteCinco);
        assertEquals("Se deben recuperar exactamente 5 registros", 5, limiteCinco.size());
    }

    @Test
    public void testCargarPorId() throws SQLException {
        AsociadoDTO dto = dao.cargar(1);
        assertNotNull("El asociado con ID 1 debe existir", dto);
        assertEquals("Juan", dto.getNombre());
        assertEquals("12345678", dto.getDni());
    }

    @Test
    public void testEliminarAsociado() throws SQLException {
        // Eliminamos el registro semilla con DNI 12345678
        dao.eliminar("12345678");

        ArrayList<AsociadoDTO> todos = dao.cargarBD();
        assertEquals("La lista debe tener 19 elementos tras la eliminación", 19, todos.size());

        AsociadoDTO eliminado = dao.cargar(1);
        assertNull("El registro recuperado debe ser nulo al ya no existir en BD", eliminado);
    }

    // =========================================================================
    // 4. DETECCIÓN DE EXCEPCIONES SQL Y VALIDACIONES DEFENSIVAS
    // =========================================================================

    @Test(expected = SQLException.class)
    public void testDniDuplicadoLanzaSQLException() throws SQLException {
        // Intentar guardar un asociado con el DNI del primer registro semilla ("12345678")
        Domicilio dom = new Domicilio("Colón", 500, "Mar del Plata");
        Asociado duplicado = new Asociado("Pedro", "Duplicado", "12345678", "2230000000", dom);

        // Debe violar la restricción UNIQUE del campo DNI en MySQL
        dao.guardar(duplicado);
    }

    @Test(expected = AssertionError.class)
    public void testGuardarAsociadoNuloLanzaAssertionError() throws SQLException {
        dao.guardar(null);
    }

    @Test(expected = AssertionError.class)
    public void testCargarConLimiteInvalidoLanzaAssertionError() throws SQLException {
        dao.cargarConLimite(0);
    }

    @Test(expected = AssertionError.class)
    public void testCargarPorIdInvalidoLanzaAssertionError() throws SQLException {
        dao.cargar(-1);
    }

    @Test(expected = AssertionError.class)
    public void testEliminarDniNuloLanzaAssertionError() throws SQLException {
        dao.eliminar(null);
    }
}
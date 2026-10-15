package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.ambulancia.Ambulancia;
import modelo.modeloDominio.ambulancia.State;
import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.util.Domicilio;

/**
 * Doble de prueba (Mock) manual de la interfaz State para aislar las pruebas de Ambulancia.
 */
class StateMock implements State {
    public boolean solicitoTraslado = false;
    public boolean solicitoAtencionDomicilio = false;
    public boolean solicitoMantenimiento = false;
    public boolean solicitoRetornoClinica = false;

    @Override
    public void SolicitudDeTraslado() {
        solicitoTraslado = true;
    }

    @Override
    public void SolicitudDeAtencionDomicilio() {
        solicitoAtencionDomicilio = true;
    }

    @Override
    public void SolicitudMantenimiento() {
        solicitoMantenimiento = true;
    }

    @Override
    public void RetornoClinica() {
        solicitoRetornoClinica = true;
    }
}

public class AmbulanciaStateMockTest {

    private Ambulancia ambulancia;
    private StateMock mockState;
    private Asociado asociadoDummy;

    @Before
    public void setUp() {
        ambulancia = new Ambulancia();
        mockState = new StateMock();
        ambulancia.setState(mockState); // Inyectamos el Mock en la ambulancia

        Domicilio dom = new Domicilio("Jujuy", 100, "Mar del Plata");
        asociadoDummy = new Asociado("Test", "User", "11111111", "2230001111", dom);
    }

    @Test
    public void testAtenderDomicilioDelegaEnStateMock() {
        ambulancia.atenderDomicilio(asociadoDummy);
        assertTrue("atenderDomicilio debe invocar SolicitudDeAtencionDomicilio en el State activo",
                mockState.solicitoAtencionDomicilio);
        assertTrue("La ambulancia debe marcarse como ocupada", ambulancia.isOcupado());
    }

    @Test
    public void testTrasladarALaClinicaDelegaEnStateMock() {
        ambulancia.trasladarALaClinica(asociadoDummy);
        assertTrue("trasladarALaClinica debe invocar SolicitudDeTraslado en el State activo",
                mockState.solicitoTraslado);
        assertTrue("La ambulancia debe marcarse como ocupada", ambulancia.isOcupado());
    }

    @Test
    public void testRegresarSinPacienteDelegaEnStateMockYLiberaOcupado() {
        ambulancia.setOcupado(true);
        ambulancia.regresarSinPaciente();

        assertTrue("regresarSinPaciente debe invocar RetornoClinica en el State activo",
                mockState.solicitoRetornoClinica);
        assertFalse("La ambulancia debe quedar desocupada tras regresar sin paciente", ambulancia.isOcupado());
    }
}
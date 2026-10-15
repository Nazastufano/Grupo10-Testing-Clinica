package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.ambulancia.Ambulancia;
import modelo.modeloDominio.ambulancia.RegresandoDelTallerState;
import modelo.modeloDominio.ambulancia.RetornoAutomatico;
import modelo.modeloDominio.ambulancia.TrasladandoPacienteClinicaState;
import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.util.Domicilio;

public class AmbulanciaConcurrenciaTest {

    private Ambulancia ambulancia;

    @Before
    public void setUp() {
        ambulancia = new Ambulancia();
    }

    // =========================================================================
    // 1. DETECCIÓN DE BUGS EN MÁQUINA DE ESTADOS Y RETORNO AUTOMÁTICO
    // =========================================================================

    @Test
    public void testDetectarBugRetornoAutomaticoBucleInvertido() throws InterruptedException {
        // Inicialmente la simulación está activa (true)
        assertTrue(ambulancia.isSimulacionActiva());

        RetornoAutomatico retorno = new RetornoAutomatico(ambulancia);
        Thread hiloRetorno = new Thread(retorno);
        hiloRetorno.start();

        // Esperamos un instante para permitir la ejecución del hilo
        Thread.sleep(100);

        // DEMOSTRACIÓN DEL BUG: El hilo finalizó inmediatamente debido a 'while (!isSimulacionActiva())'
        assertFalse("DEMOSTRACIÓN DE BUG: El hilo RetornoAutomatico finaliza de inmediato por tener la condición del while invertida",
                hiloRetorno.isAlive());
    }

    @Test
    public void testDetectarTransicionIncorrectaEnTrasladandoPaciente() {
        // Forzamos el estado a TrasladandoPacienteClinicaState
        ambulancia.setState(new TrasladandoPacienteClinicaState(ambulancia));

        // Ejecutamos la transición de retorno
        ambulancia.regresarSinPaciente();

        // DEMOSTRACIÓN DEL BUG: En lugar de pasar a Disponible, pasa a RegresandoDelTallerState
        assertTrue("DEMOSTRACIÓN DE BUG: TrasladandoPacienteClinicaState conmuta erróneamente a RegresandoDelTallerState",
                ambulancia.getEstadoActual() instanceof RegresandoDelTallerState);
    }

    // =========================================================================
    // 2. PRUEBA DE CONCURRENCIA, BLOQUEO SYNCHRONIZED Y WAIT / NOTIFY
    // =========================================================================

    @Test
    public void testSincronizacionYEsperaAmbulanciaOcupada() throws InterruptedException {
        ambulancia.setOcupado(true); // Simulamos ambulancia ocupada por otro proceso

        Domicilio dom = new Domicilio("Colón", 2000, "Mar del Plata");
        Asociado asociadoEspecial = new Asociado("Socio1", "Perez", "22222222", "2234445555", dom);

        // Hilo secundario que intentará pedir atención a domicilio
        Thread hiloSocio = new Thread(() -> {
            ambulancia.atenderDomicilio(asociadoEspecial);
        });

        hiloSocio.start();
        Thread.sleep(150); // Tiempo para que entre al bucle wait() en Ambulancia

        // Verificamos que el hilo del socio quedó bloqueado esperando (TIMED_WAITING o WAITING)
        assertTrue("El hilo del asociado debe quedar bloqueado esperando a que se libere la ambulancia",
                hiloSocio.getState() == Thread.State.WAITING || hiloSocio.getState() == Thread.State.TIMED_WAITING);

        // Liberamos la ambulancia y notificamos
        ambulancia.regresarSinPaciente(); // Ejecuta notifyAll() y setOcupado(false)
        hiloSocio.join(1000); // Esperamos a que el hilo complete

        assertFalse("El hilo del asociado debe haber desbloqueado y finalizado correctamente", hiloSocio.isAlive());
    }
}
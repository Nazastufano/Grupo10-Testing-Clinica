package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.ambulancia.Ambulancia;
import modelo.modeloDominio.ambulancia.AtendiendoEnDomicilioState;
import modelo.modeloDominio.ambulancia.DisponibleState;
import modelo.modeloDominio.ambulancia.EnTallerState;
import modelo.modeloDominio.ambulancia.RegresandoClinicaSinPacienteState;
import modelo.modeloDominio.ambulancia.RegresandoDelTallerState;
import modelo.modeloDominio.ambulancia.RetornoAutomatico;
import modelo.modeloDominio.ambulancia.TrasladandoPacienteClinicaState;
import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.personas.operario.Operario;
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
    public void testRetornoAutomaticoSigueActivoMientrasLaSimulacionEstaActiva() throws InterruptedException {
        // Precondición: la simulación está activa
        assertTrue(ambulancia.isSimulacionActiva());

        RetornoAutomatico retorno = new RetornoAutomatico(ambulancia);
        Thread hiloRetorno = new Thread(retorno);
        hiloRetorno.start();

        // Esperamos un instante para permitir la ejecución del hilo
        Thread.sleep(100);

        // Comportamiento esperado: mientras la simulación está activa, el hilo debe seguir ejecutándose.
        // Si la condición del while está invertida (while (!isSimulacionActiva())), el hilo termina de inmediato y el test falla.
        boolean hiloVivo = hiloRetorno.isAlive();

        // Limpieza: finalizamos la simulación y detenemos el hilo para no afectar a otros tests
        ambulancia.setSimulacionActiva(false);
        hiloRetorno.interrupt();
        hiloRetorno.join(1000);

        assertTrue("El hilo RetornoAutomatico debe seguir activo mientras la simulación está activa",
                hiloVivo);
    }

    @Test
    public void testTrasladandoPacienteRetornaADisponible() {
        // Forzamos el estado a TrasladandoPacienteClinicaState
        ambulancia.setState(new TrasladandoPacienteClinicaState(ambulancia));

        // Ejecutamos la transición de retorno
        ambulancia.regresarSinPaciente();

        // Comportamiento esperado (según la documentación de la clase): al retornar a la clínica pasa a DisponibleState.
        // Si conmuta a otro estado (por ejemplo RegresandoDelTallerState), el test falla.
        assertTrue("Al retornar a la clínica, la ambulancia debe quedar en DisponibleState",
                ambulancia.getEstadoActual() instanceof DisponibleState);
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
    
    // =========================================================================
    // 3. FLUJO COMPLETO DE LA MÁQUINA DE ESTADOS ACOPLADA A AMBULANCIA
    // =========================================================================

    @Test
    public void testFlujoCompletoMaquinaDeEstados() {
        Domicilio dom = new Domicilio("Colón", 2000, "Mar del Plata");
        Asociado asociado = new Asociado("Socio2", "Lopez", "33333333", "2231112222", dom);
        Operario operario = new Operario("Op", "Garcia", "44444444", "2233334444", dom, ambulancia);

        // Estado inicial
        assertTrue("Estado inicial: Disponible", ambulancia.getEstadoActual() instanceof DisponibleState);
        assertFalse(ambulancia.isOcupado());

        // Ciclo de atención a domicilio: Disponible -> AtendiendoEnDomicilio -> RegresandoSinPaciente -> Disponible
        ambulancia.atenderDomicilio(asociado);
        assertTrue("Debe pasar a AtendiendoEnDomicilio", ambulancia.getEstadoActual() instanceof AtendiendoEnDomicilioState);
        assertTrue(ambulancia.isOcupado());

        ambulancia.regresarSinPaciente();
        assertTrue("Debe pasar a RegresandoClinicaSinPaciente", ambulancia.getEstadoActual() instanceof RegresandoClinicaSinPacienteState);
        assertFalse(ambulancia.isOcupado());

        ambulancia.regresarSinPaciente();
        assertTrue("Debe volver a Disponible", ambulancia.getEstadoActual() instanceof DisponibleState);

        // Ciclo de mantenimiento: Disponible -> EnTaller -> RegresandoDelTaller -> Disponible
        ambulancia.solicitarMantenimiento(operario);
        assertTrue("Debe pasar a EnTaller", ambulancia.getEstadoActual() instanceof EnTallerState);
        assertTrue(ambulancia.isOcupado());

        ambulancia.volviendoDelTaller();
        assertTrue("Debe pasar a RegresandoDelTaller", ambulancia.getEstadoActual() instanceof RegresandoDelTallerState);
        assertFalse(ambulancia.isOcupado());

        ambulancia.regresarSinPaciente(); // ejecuta RetornoClinica() sobre el estado actual
        assertTrue("Debe volver a Disponible tras el taller", ambulancia.getEstadoActual() instanceof DisponibleState);
    }
}
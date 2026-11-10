# Grupo10-Testing-Clinica

## Trabajo Práctico Final: Estrategia de Testing
**Aplicación:** Clínica

**Integrantes:** Ganduglia, Ciro · Rosal, Fernando Daniel · Sosa, Lucas · Stufano, Nazareno

Trabajo práctico de testing sobre la aplicación Clínica, diferenciando dos niveles:
- **Testing atómico (unitario)** sobre componentes independientes.
- **Testing de integración** sobre componentes acoplados con recursos externos (base de datos MySQL y concurrencia).

Los casos de prueba se diseñaron con técnicas de caja negra (particiones de equivalencia y valores límite) a partir de la SRS, y se implementaron con JUnit 4.

📄 **Informe:** [Trabajo Práctico Final - Estrategia de Testing (Aplicación Clínica) V3.0.pdf](<Trabajo Práctico Final - Estrategia de Testing (Aplicación Clínica) V3.0.pdf>)
📋 **Enunciado:** [Info/Enunciado_Trabajo_final.pdf](Info/Enunciado_Trabajo_final.pdf) · **SRS:** [Info/SRS_Persistencia_Clinica.pdf](Info/SRS_Persistencia_Clinica.pdf)

---

## Criterios de Entrega

- [x] **Código fuente completo** de las suites de pruebas implementadas.
- [x] **Capturas de ejecución** mostrando las pruebas en **verde** (funcionamiento correcto) y en **rojo** (detección de los errores).
- [x] **Informe formal** con el diseño de escenarios, los casos de prueba (clases de equivalencia y valores límite), las conclusiones y los resultados obtenidos.

---

## Contenido del Repositorio

| Ubicación | Contenido |
| :--- | :--- |
| `Trabajo Práctico Final - ... V3.0.pdf` | Informe final. |
| `Info/TPE2026/src/main/java/test/` | Suites de prueba: `AsociadoDTOTest`, `IntegracionPersistenciaTest`, `AmbulanciaStateMockTest` (incluye `StateMock`) y `AmbulanciaConcurrenciaTest`. |
| `Clases Test/java/` | Copia de las suites para leerlas directamente desde GitHub. |
| `Img/` | Capturas de ejecución en JUnit. |
| `Info/TPE2026/` | Proyecto Maven de la aplicación Clínica (código provisto + correcciones). |
| `Info/` | Enunciado y SRS. |

---

## Resultados

| Captura | Suite | Resultado |
| :--- | :--- | :--- |
| `Inciso1.1` / `Inciso1.2` | AsociadoDTOTest (código provisto) | 🔴 16 pruebas, 1 falla: `numero > 0` vs SRS `numero >= 0` |
| `Inciso1.3` | AsociadoDTOTest (tras la corrección) | 🟢 16/16 |
| `Inciso2` | IntegracionPersistenciaTest | 🔴 13 pruebas, 3 detectan defectos: `cargar()` sin `next()` y `actualizar()` sobre id fijo |
| `Inciso3.1` | AmbulanciaStateMockTest | 🟢 3/3 |
| `Inciso3.2` | AmbulanciaConcurrenciaTest | 🔴 4 pruebas, 2 detectan defectos: bucle invertido en `RetornoAutomatico` y transición errónea en `TrasladandoPacienteClinicaState` |

---

## Cómo Ejecutar las Pruebas

**Requisitos**
- JDK 8 o superior.
- MySQL 8 en `localhost:3306` con el usuario `progra_c` / contraseña `progra_c` (credenciales definidas en `BaseDeDatos.java`). Solo lo requiere `IntegracionPersistenciaTest`:
  ```sql
  CREATE USER 'progra_c'@'localhost' IDENTIFIED BY 'progra_c';
  GRANT ALL PRIVILEGES ON *.* TO 'progra_c'@'localhost';
  FLUSH PRIVILEGES;
  ```

**Pasos (Eclipse)**
1. `File → Import → Maven → Existing Maven Projects` y seleccionar la carpeta `Info/TPE2026`. Las dependencias (JUnit 4 y el driver de MySQL) están declaradas en el `pom.xml`.
2. Verificar en `Window → Preferences → Java → JUnit` que esté habilitado *Add '-ea' to VM arguments*: las validaciones de `AsociadoDTO` usan `assert` y requieren aserciones activas.
3. Clic derecho sobre cada clase de `src/main/java/test` → `Run As → JUnit Test`.

> `IntegracionPersistenciaTest` crea la base `Grupo_7` y recrea la tabla `Asociados` en cada ejecución.

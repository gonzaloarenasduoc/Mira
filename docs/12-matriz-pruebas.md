# Matriz de casos de prueba

Estado al 14-11-2026 de cada caso de `docs/10-casos-de-prueba.md`, con la prueba automatizada o la
evidencia que lo acredita. La suite completa (`./mvnw clean verify`) tiene 219 pruebas, de las cuales 18 son de
integración contra PostgreSQL 16 con Testcontainers. Pasa sin fallas ni pruebas omitidas.

**Resumen:** los 14 casos funcionales se cumplen. De los 16 no funcionales, 12 se cumplen, 2 se cumplen
en parte (PNF-03 y PNF-11) y 2 están pendientes, porque requieren personas ajenas al proyecto
(PNF-09 y PNF-16).

## Casos funcionales

| Caso | Prueba automatizada | Evidencia | Resultado |
|---|---|---|---|
| CF-01 · Arrendar un equipo disponible | `ArriendoServiceTest`; `MiraApplicationTests.arriendo_equipoDisponible_quedaArrendadoConSuMovimiento` | `evidencias/semana-10` | Cumple |
| CF-02 · Rechazar el arriendo de un equipo no disponible | `ArriendoServiceTest`; `EquipoServiceTest` | — | Cumple |
| CF-03 · Atomicidad del registro de arriendo | `MiraApplicationTests.arriendo_segundaLineaNoDisponible_noGuardaNada` | `evidencias/semana-10` | Cumple |
| CF-04 · Registrar el retorno de un arriendo | `EquipoServiceTest`; `MiraApplicationTests.retorno_enOtraBodega_reasignaElEquipoYCierraLaLinea` | `evidencias/semana-11` | Cumple |
| CF-05 · Impedir número de serie duplicado | `EquipoServiceTest.registrar_numeroSerieRepetido_lanzaExcepcionYNoGuardaNada`; `MiraApplicationTests.equipo_numeroSerieRepetidoEnLaMismaEmpresa_esRechazado` | `evidencias/semana-08` | Cumple |
| CF-06 · Mismo número de serie en otra empresa | `MiraApplicationTests.equipo_numeroSerieRepetidoEnOtraEmpresa_esAceptado` | — | Cumple |
| CF-07 · Disponibilidad consolidada | `DisponibilidadServiceTest` | `evidencias/semana-09` | Cumple |
| CF-08 · Traslado entre bodegas | `TrasladoServiceTest`; `MiraApplicationTests.traslado_salidaYLlegada_dejaElEquipoEnElDestinoConSusMovimientos` | `evidencias/semana-09` | Cumple |
| CF-09 · Venta de un equipo | `VentaServiceTest`; `MiraApplicationTests.venta_equipoDisponible_quedaVendidoYDejaDeEstarDisponible` | `evidencias/semana-12`; `Demo/Demo_v5.mp4` | Cumple |
| CF-10 · Alerta de retorno vencido | `ArriendoServiceTest`; `MiraApplicationTests.alertasDeRetorno_soloLineasPendientesQueVencenPronto` | `evidencias/semana-11` y `semana-13` | Cumple |
| CF-11 · Restricción de acceso por rol | `SeguridadTest` (matriz de permisos por rol y ruta); `MenuGlobalTest` | `evidencias/semana-08`; `Demo/Demo_v5.mp4` | Cumple |
| CF-12 · Advertencia por técnico ocupado | `CapacitacionServiceTest` | `evidencias/semana-13`, capturas 26 a 28 | Cumple |
| CF-13 · Documento vinculado a la operación y al equipo | `DocumentoServiceTest`; `MiraApplicationTests.documento_deUnArriendoConDosEquipos_apareceEnAmbos` | `evidencias/semana-13` | Cumple |
| CF-14 · Aislamiento entre empresas | Pruebas `…DeOtraEmpresa…` en los servicios de usuarios, equipos, clientes, arriendos, ventas, historial, documentos, técnicos y capacitaciones; CF-06 | — | Cumple |

## Casos no funcionales

| Caso | Cómo se verificó | Evidencia | Resultado |
|---|---|---|---|
| PNF-01 · Consulta de disponibilidad < 2 s | Medición con 500 equipos: 0,20 s en el percentil 95 | `evidencias/semana-09` | Cumple |
| PNF-02 · Panel < 3 s con un año de datos | Medición con un año de operación: 0,052 s en el percentil 95 | `evidencias/semana-13` | Cumple |
| PNF-03 · 10 usuarios sin degradar más de 50 % | 1 frente a 10 usuarios simultáneos, cada uno con su sesión, sobre cinco pantallas (50 y 500 solicitudes), en una sola máquina de 4 núcleos que corre la base, la aplicación y el cliente de carga. Con una pausa promedio de 1 s entre acciones, el p95 no se degrada (−10 %). Sin pausas (saturación) se degrada 371 %: de 0,036 a 0,171 s | `evidencias/semana-14/pnf` | Cumple en parte: cumple con usuarios que hacen pausas; en saturación no cumple el criterio relativo, aunque toda respuesta queda bajo 0,25 s |
| PNF-04 · Contraseñas con BCrypt | `MiraApplicationTests.datosIniciales_administrador_quedaConContrasenaCifradaConBCrypt` | — | Cumple |
| PNF-05 · Acceso sin sesión | `SeguridadTest` | — | Cumple |
| PNF-06 · Referencia directa a objetos (IDOR) | Pruebas `…DeOtraEmpresa…`: un recurso de otra empresa responde como inexistente | — | Cumple |
| PNF-07 · Inyección SQL | `BusquedaTest`; `MiraApplicationTests.buscar_textoConComodinesOInyeccion_noAlteraElResultado` | `evidencias/semana-09` | Cumple |
| PNF-08 · Cookies de sesión | `HttpOnly` siempre; `Secure` con el perfil `demo`, que se sirve por HTTPS | `evidencias/semana-09` | Cumple con el perfil `demo`. En local y en Docker Compose (HTTP) la cookie no lleva `Secure`, a propósito |
| PNF-09 · Registro de un arriendo por un usuario sin entrenamiento | Requiere 3 personas ajenas al proyecto | — | Pendiente |
| PNF-10 · Mensajes de error en lenguaje del negocio | Los rechazos de negocio muestran el mensaje de `NegocioException` y las pruebas verifican su texto. Una falla inesperada (5xx) muestra una página propia. Se corrigió el 14-11: antes mostraba la página Whitelabel de Spring | `evidencias/semana-14/pnf/pnf13.txt` | Cumple |
| PNF-11 · Chrome, Firefox y Edge | Todos los recorridos y videos se hicieron en Chromium | `evidencias/semana-08` a `semana-14` | Cumple en parte: faltan Firefox y Edge |
| PNF-12 · 1366×768 y 390 px | Inspección visual en ambas resoluciones | `evidencias/semana-09` (captura 16) y `semana-13` (captura 32) | Cumple |
| PNF-13 · Caída de la base durante un arriendo | Se congeló la base, se envió un arriendo de dos equipos y se detuvo la base a la fuerza a mitad de la operación. Al restablecerla no había ningún registro parcial | `evidencias/semana-14/pnf/pnf13.txt` | Cumple |
| PNF-14 · Respaldo y restauración | `pg_dump` y `pg_restore` en un contenedor limpio: las 14 tablas tienen el mismo conteo y los movimientos, la misma huella MD5 | `evidencias/semana-14/pnf/pnf14.txt` | Cumple |
| PNF-15 · Cobertura ≥ 70 % en servicios | JaCoCo en `./mvnw clean verify`: 84,6 % de líneas | `Demo/Demo_v5.mp4` | Cumple |
| PNF-16 · Levantar el sistema en < 15 min con el README | Requiere una persona del curso. La opción C del README (`docker compose up --build`) deja todo en un solo comando | — | Pendiente |

Los scripts de PNF-03 y PNF-13 están en `docs/evidencias/semana-14/pnf/`. Se ejecutan con Node sobre la aplicación
levantada con Docker Compose y los datos de `docs/demo`.

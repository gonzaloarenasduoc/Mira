# Bitácora de decisiones

Registro de las decisiones técnicas tomadas, con su motivo. Existe para que ninguno de los
dos revierta sin querer algo que ya se discutió, y para poder responder en la defensa.

Formato: fecha, decisión, motivo, alternativas descartadas.

---

## D01 · Java 21 con Spring Boot 4.1

**Motivo.** Las operaciones del sistema son transaccionales: registrar un arriendo cambia
el estado del equipo y registra su movimiento, y debe ocurrir todo o nada. El tipado
estático detecta errores en un dominio con muchos estados antes de ejecutar. Spring Data
JPA aporta auditoría nativa, que es el requisito central del proyecto. Ambas son versiones
con soporte de largo plazo.

**Descartado.** Python con Django, que habría dado mayor velocidad inicial por su panel de
administración incorporado. Se asumió el mayor volumen de código a cambio de adecuación al
problema y de valor formativo.

---

## D02 · Sin interfaz de página única (SPA)

**Motivo.** Una SPA implica construir y mantener dos aplicaciones separadas, con estado
duplicado. El sistema son formularios, listados y búsquedas, sin interactividad que lo
justifique. Con renderizado en servidor se mantiene un solo proyecto.

**Descartado.** React o Angular con una API completa.

**Nota.** Si una pantalla concreta necesita actualización parcial, se resuelve con HTMX
sobre las mismas plantillas de Thymeleaf, sin frontend separado.

---

## D03 · Monolito modular en capas

**Motivo.** Ningún módulo tiene un perfil de carga distinto al resto, por lo que el
escalado independiente no aporta. La organización por paquetes de dominio da la separación
necesaria sin la complejidad operacional de servicios distribuidos, y permite extraer un
módulo más adelante si hiciera falta.

**Descartado.** Microservicios.

---

## D04 · Multi-tenencia desde el inicio

**Motivo.** Toda entidad de negocio lleva `empresa_id` y las claves únicas lo son por
empresa. Incorporarlo desde el comienzo cuesta días; hacerlo después obligaría a tocar cada
entidad, cada consulta y a migrar todos los datos. Deja abierta la posibilidad de un
modelo multi-empresa sin comprometer el alcance actual.

---

## D05 · La disponibilidad es calculada, no almacenada

**Motivo.** Un campo almacenado puede quedar desincronizado del estado real, que es
exactamente el problema que el proyecto resuelve. Se determina a partir del estado del
equipo y de los arriendos vigentes.

---

## D06 · Documentos adjuntos en sistema de archivos

**Motivo.** Simplicidad para el alcance comprometido. En la base de datos se guarda la
referencia, no el binario. Migrar a almacenamiento de objetos más adelante es un cambio de
configuración.

---

## D07 · Filtro por empresa mediante parámetro explícito

**Motivo.** El identificador de empresa se pasa como parámetro explícito en cada método de
servicio y de repositorio: `findByEmpresaIdAndNumeroSerie(...)`. El controlador lo obtiene
del usuario autenticado.

Es más verboso que las alternativas, pero **el compilador obliga a pasarlo**: no se puede
olvidar en silencio. Con un contexto implícito, una consulta escrita sin el filtro compila
igual y la fuga de datos entre empresas aparece recién en producción.

**Descartado.** Un `TenantContext` con variable de hilo, y los filtros de Hibernate
activados por sesión. Ambos son implícitos y se rompen con facilidad en las pruebas.

**Verificado por** los casos CF-14 y PNF-06.

---

## D08 · Lombok sí, MapStruct no

**Motivo.** Lombok elimina el código repetitivo de entidades y DTOs, que es real en Java.

MapStruct se descarta: los DTOs son `record` y el mapeo se escribe a mano en la capa de
servicio. Agregar un segundo procesador de anotaciones complica la construcción y la
depuración a cambio de ahorrar unas pocas líneas.

---

## D09 · Cuatro roles de usuario

**Motivo.** La documentación de negocio describía cinco perfiles de uso mientras el
enumerado definía tres. Se unifica en cuatro roles: `ADMINISTRADOR`, `ENCARGADO_BODEGA`,
`VENDEDOR` y `JEFATURA`.

Las tareas administrativas de documentación quedan cubiertas por el encargado de bodega y
el administrador, en lugar de crear un rol propio que en una empresa de este tamaño
recaería en la misma persona.

**Matriz de permisos** en `docs/01-contexto-negocio.md`.

---

## D10 · Bootstrap como webjar, no por CDN

**Fecha.** 10-09-2026.

**Motivo.** La defensa es presencial y proyectada en una sala de Duoc: si el wifi falla, con
un CDN la interfaz se ve sin estilos. El webjar viaja dentro del `.jar` y no depende de la
red. Además, todas las vistas se construyen sobre esta base; cambiar el origen de Bootstrap
más adelante obligaría a tocar cada plantilla.

Se agrega `org.webjars:bootstrap` con `org.webjars:webjars-locator-lite`, que resuelve la
ruta sin la versión: `@{/webjars/bootstrap/css/bootstrap.min.css}`. Así, subir la versión de
Bootstrap es cambiar una propiedad del `pom.xml` y ninguna plantilla.

**Descartado.** Bootstrap por CDN, más simple pero dependiente de la red en la demostración.

**Zona de propiedad.** Las plantillas son de Gonzalo. Queda avisado antes de que construya
las vistas.

---

> Agrega acá toda decisión nueva que afecte al otro integrante. Si Claude Code propone algo
> que contradice una decisión registrada, debe advertirlo en vez de aplicarlo.

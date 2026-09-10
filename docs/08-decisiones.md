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

> Agrega acá toda decisión nueva que afecte al otro integrante. Si Claude Code propone algo
> que contradice una decisión registrada, debe advertirlo en vez de aplicarlo.

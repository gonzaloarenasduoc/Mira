# Arquitectura

## Forma general

Aplicación web **monolítica modular, estructurada en capas**, desplegada como una sola
unidad. Se descartaron los microservicios porque ningún módulo tiene un perfil de carga
distinto al resto y agregarían complejidad operacional sin beneficio.

Se descartó una interfaz de página única (SPA): las vistas se renderizan en el servidor
con Thymeleaf. Existe una API REST **acotada**, solo para alimentar los gráficos del panel
de indicadores.

## Capas

```
@Controller / @RestController   ← HTTP: recibe, valida formato, elige vista o respuesta
            ↓
@Service                        ← reglas de negocio, transacciones
            ↓
@Repository (Spring Data JPA)   ← acceso a datos
            ↓
@Entity                         ← modelo de dominio
```

**Regla de dependencia: cada capa solo llama a la inmediatamente inferior.** Un controlador
nunca inyecta un repositorio. Si aparece esa tentación, falta un método de servicio.

## Estructura de paquetes

```
cl.duoc.mira
├── config          Configuración de Spring Security, Web, beans
├── usuarios        Usuarios, roles y permisos
├── inventario      Equipos, categorías, estados, bodegas, traslados
├── operaciones     Ventas, arriendos, disponibilidad, retornos
├── capacitaciones  Agendamiento y asignación de técnicos
├── documentos      Órdenes de compra, guías de despacho, facturas
├── indicadores     Panel de indicadores y alertas
└── common          Excepciones, auditoría, utilidades compartidas
```

Cada paquete de dominio contiene sus propias clases `entity`, `repository`, `service`,
`controller` y `dto`. Se agrupa **por área de negocio, no por tipo técnico**.

Un paquete puede llamar a los servicios de otro, pero **nunca a sus repositorios ni a sus
entidades directamente**. Si `operaciones` necesita datos de `inventario`, llama a
`EquipoService`, no a `EquipoRepository`.

## Multi-tenencia

Toda entidad de negocio lleva una referencia a `Empresa`. Hoy existe una sola empresa
cargada, pero el modelo queda preparado para varias.

```java
@ManyToOne(optional = false)
private Empresa empresa;
```

Las claves únicas del negocio son únicas **por empresa**, no globalmente:

```java
@Table(uniqueConstraints =
    @UniqueConstraint(columnNames = {"empresa_id", "numero_serie"}))
```

Toda consulta debe filtrar por empresa. Esto se agrega ahora porque incorporarlo después
obligaría a tocar cada entidad, cada consulta y a migrar todos los datos.

## Transaccionalidad

Las operaciones que modifican el estado de un equipo son atómicas. Registrar un arriendo
implica tres escrituras: crear el arriendo, cambiar el estado del equipo y registrar el
movimiento en su historial. **Ocurren las tres o ninguna.**

```java
@Transactional
public Arriendo registrar(...) { ... }
```

Si esto falla parcialmente, queda un arriendo sobre un equipo que el sistema sigue mostrando
como disponible, que es exactamente el problema que el proyecto viene a resolver.

`@Transactional` va en la capa de servicio, nunca en el controlador ni en el repositorio.

## Auditoría

Se usa la auditoría nativa de Spring Data JPA (`@CreatedBy`, `@CreatedDate`,
`@LastModifiedBy`, `@LastModifiedDate`) sobre una clase base común en `common`. La
trazabilidad es el requisito central del proyecto: ninguna entidad de negocio queda sin
auditar.

## Manejo de archivos

Los documentos adjuntos se guardan en el sistema de archivos, en la ruta configurada por
`APP_UPLOAD_DIR`. En la base de datos se guarda la referencia, no el binario.

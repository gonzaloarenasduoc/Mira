# Estado del proyecto

Este archivo se actualiza al terminar cada sesión de trabajo. Es lo primero que lee el
asistente del otro integrante para saber qué está en curso y no pisar nada.

**Última actualización:** 10-09-2026

## Fase actual

Fase 1, definición del proyecto. El desarrollo comienza en la semana 5.

## Terminado

- Definición del proyecto, alcance y objetivos.
- Nueve épicas dimensionadas.
- Stack tecnológico decidido y justificado.
- Documentación base del repositorio.
- Modelo de datos y diccionario de datos (`docs/03-modelo-datos.md`).
- Casos de prueba funcionales y no funcionales (`docs/10-casos-de-prueba.md`).

## En curso

| Qué | Quién | Rama | Notas |
|---|---|---|---|
| _(nada aún)_ | | | |

## Siguiente

1. Configuración del proyecto y del repositorio: Spring Boot, PostgreSQL en Docker, Flyway,
   dependencias de pruebas y tablero en GitHub Projects. **No depende de nada pendiente.**
2. Migración `V1__esquema_inicial.sql` a partir del modelo de datos.
3. Épica 001, acceso y roles.
4. Reunión con la contraparte cuando sea posible, para confirmar los supuestos de la
   sección 6 del modelo de datos.

## Bloqueado

| Qué | Por qué | Quién lo destraba |
|---|---|---|
| Cifras reales en la documentación | No se ha concretado la reunión con la empresa | Joaquín |
| Confirmar los supuestos 3, 7, 8 y 10 del modelo de datos | Requiere consultar a la contraparte o a la Eve | Joaquín |

## Advertencias vigentes

- El modelo de datos está definido y se puede implementar. Respeta el diccionario tal como
  está escrito: nombres de tabla, de columna y enumerados.
- Los supuestos de la sección 6 del modelo aún no se validan con la contraparte. Si alguno
  cambia, se resuelve con una migración nueva, nunca editando una existente.
- Las áreas de desempeño del Plan de Estudio están pendientes de confirmar.

---

### Cómo actualizar este archivo

Al terminar de trabajar, deja registrado: qué terminaste, qué quedó a medias y en qué rama,
y cualquier cosa que el otro deba saber antes de tocar código. Sé específico: "quedó a
medias el servicio de arriendos, falta la validación de fechas, rama feature/E005" sirve;
"avancé en arriendos" no sirve.

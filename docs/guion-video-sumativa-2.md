# Guion del video de demostración · Evaluación sumativa 2

Duración máxima: **3 minutos**. Muestra toda la funcionalidad construida hasta la semana 8
(Sprint 2, historias HU-01 a HU-08). Cada escena indica el tiempo, lo que se ve en pantalla y la
narración. La narración está repartida para que ambos integrantes participen.

## Antes de grabar

1. Base limpia y aplicación arriba:
   ```
   docker compose up -d db
   .\mvnw.cmd spring-boot:run
   ```
   Si la base ya tiene datos de pruebas anteriores, se puede partir de cero con
   `docker compose down -v` y volver a levantarla. **Ese comando borra todos los datos de la base local.**
2. Para no gastar tiempo del video, cargar antes de grabar, con el usuario `admin@mira.cl` / `admin123`:
   - Bodegas: **Bodega Central** (Av. Providencia 1234, Providencia) y **Bodega Norte** (Antofagasta).
   - Equipos: **GS18-778201** (GPS RTK, Leica GS18 T, Bodega Norte), **M300-1A2B3C** (Dron, DJI Matrice 300 RTK,
     Bodega Central) y **NA2-55021** (Nivel, Leica NA2, Bodega Norte).
   - El equipo **TS16-334512** (Estación total, Leica TS16) **no** se carga: se registra en el video.
3. Navegador en ventana completa a 1366×768 o más, zoom al 110 %, sin otras pestañas ni marcadores visibles.
4. Grabar con Xbox Game Bar (`Win + Alt + R`) u OBS. Se recomienda grabar la pantalla primero y la
   narración después, o subtitular, para que el ritmo no dependa de hablar y operar a la vez.

## Escenas

| Tiempo | En pantalla | Narración | Voz |
|---|---|---|---|
| 0:00 – 0:15 | Abrir `localhost:8080/equipos` sin sesión: el sistema redirige al inicio de sesión. Ingresar una clave incorrecta: aparece "Correo o contraseña incorrectos". | "Mira protege todas sus pantallas: sin sesión, cualquier dirección lleva al inicio de sesión. Si la clave es incorrecta, el mensaje no revela si el error está en el correo o en la contraseña." | Gonzalo |
| 0:15 – 0:30 | Ingresar como administrador. Mostrar el inicio con su menú. | "Ingresamos como administrador. El menú se arma según el rol: el administrador ve equipos, bodegas y usuarios." | Gonzalo |
| 0:30 – 0:50 | Bodegas: mostrar las dos bodegas. Pulsar "Nueva bodega", escribir "bodega central" y guardar: aparece "Ya existe una bodega llamada Bodega Central". Cancelar. | "Las bodegas se administran desde aquí. El nombre es único dentro de la empresa, sin importar mayúsculas o minúsculas." | Gonzalo |
| 0:50 – 1:25 | Equipos → "Registrar equipo". Ingresar TS16-334512, Estación total, Leica, TS16, Bodega Central, valor 18.500.000, tarifa 95.000. Guardar: se abre la ficha con el estado "Disponible" y el movimiento de ingreso en el historial. | "Registramos una estación total con su número de serie. El equipo queda disponible en la bodega indicada y, en la misma transacción, se registra su movimiento de ingreso: quién, cuándo y hacia qué bodega. Ese historial es la base de la trazabilidad." | Joaquín |
| 1:25 – 1:40 | Volver a "Registrar equipo" e intentar el mismo número de serie, escrito en minúsculas: aparece "Ya existe un equipo con el número de serie…". | "El sistema impide registrar dos veces el mismo número de serie dentro de la empresa." | Joaquín |
| 1:40 – 2:00 | Listado de equipos: filtrar por "Bodega Norte" y luego buscar "gs18" por número de serie. | "El listado permite filtrar por categoría, estado y bodega, y buscar por número de serie. Cada usuario ve solo los equipos de su empresa." | Joaquín |
| 2:00 – 2:25 | Usuarios: crear a "Valentina Soto", vendedor@mira.cl, rol Vendedor. Mostrar la lista: el administrador no tiene botón para desactivarse a sí mismo. Cerrar sesión. | "El administrador crea usuarios y les asigna un rol. La contraseña se guarda cifrada y nadie puede desactivar su propio usuario." | Gonzalo |
| 2:25 – 2:50 | Ingresar como el vendedor: el menú no muestra Usuarios. En Equipos no aparece "Registrar equipo". Escribir a mano `localhost:8080/usuarios`: "Acceso denegado". | "El vendedor solo consulta el inventario: no ve la administración de usuarios ni puede registrar equipos. Y si intenta entrar escribiendo la dirección, el servidor le niega el acceso: el permiso no depende de ocultar un botón." | Joaquín |
| 2:50 – 3:00 | Terminal con `.\mvnw.cmd test` terminando en `BUILD SUCCESS`. | "Cada una de estas reglas está cubierta por pruebas automatizadas, que se ejecutan antes de cada integración." | Joaquín |

## Recomendaciones

- Ensayar una vez con cronómetro: si se pasa de 3 minutos, acortar la escena de usuarios.
- No mostrar la consola de Spring Boot con errores ni datos personales.
- Si el docente pide el video como archivo, exportarlo en MP4 con 1080p y guardarlo también en la carpeta `Demo/` del repositorio, como se hizo con `Demo_v2.mp4`.

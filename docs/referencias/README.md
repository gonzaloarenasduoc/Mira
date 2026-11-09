# Referencias

Material externo que sirve de referencia al proyecto. No es parte de los requisitos: lo que se
construye sigue definido en `docs/05-epicas.md`.

## catalogo-productos.xlsx

Catálogo de productos de una empresa chilena que vende equipos de geomensura, aportado por Gonzalo
Arenas el 09-11-2026. Tiene 219 filas con dos columnas: nombre del producto y código interno (SKU).
No incluye precios, stock ni números de serie.

Los códigos siguen el patrón `V-<tipo>-<marca>-<producto>`. El significado de cada tipo es una
interpretación nuestra a partir de los nombres, no un dato de la empresa:

| Tipo | Productos | Interpretación |
|---|---|---|
| AC | 78 | Accesorios: baterías, cargadores, jalones, bases, mochilas |
| SF | 61 | Software y licencias (Leica Cyclone, Global Mapper, ClearEdge3D, FJD Trion) |
| GN | 23 | Receptores GNSS (STEC SDi, SV1, SDC; bases CORS) |
| CU | 15 | Cursos asociados a un equipo o software |
| EL | 13 | Escáneres láser (Leica BLK360 G2, FJDynamics P1/P2) |
| LE | 8 | Lentes de seguridad |
| SD | 5 | Detección subterránea (Proceq GS8000) |
| CA | 4 | Cámaras 3D (Matterport) |
| ET | 4 | Estaciones totales (STEC AXIS) |
| SU | 3 | Suscripciones |
| NV | 2 | Niveles (STEC) |
| SERV, PRUEBA | 3 | Servicios y registros de prueba de facturación |

Marcas principales: STEC (47), FJDynamics (44), Leica (28), genéricos (19).

### Qué aporta al proyecto

- **Confirma el problema que resuelve Mira:** el catálogo identifica productos por modelo (SKU), no
  unidades por número de serie, que es lo que el sistema agrega (E002).
- **Datos realistas para demostraciones:** marcas y modelos reales de estaciones totales, GNSS y
  niveles para cargar equipos de prueba.
- **Diferencias con el alcance actual, a consultar con el docente antes de cambiar nada:** el
  catálogo incluye escáneres láser, cámaras 3D y equipos de detección subterránea, que no están entre
  las cuatro categorías de V2 (estación total, GPS RTK, dron y nivel), y no incluye drones. También
  vende cursos por equipo, relacionados con las capacitaciones de E007.

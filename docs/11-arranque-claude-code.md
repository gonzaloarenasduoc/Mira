# Arranque del desarrollo con Claude Code

Guía operativa para pasar de la documentación al código. Léela completa antes de empezar.

## Quién hace qué

El esqueleto del proyecto y la migración inicial **los genera una sola persona**. Los pasos
6 y 7 caen enteros en zona compartida: `pom.xml`, `config`, las migraciones y el paquete
`usuarios`. Si los dos generan el proyecto por separado, el merge no se arregla, se rehace.

Acordado por el equipo: **los genera Joaquín**. Gonzalo clona después, cuando el esqueleto
y la migración `V1` ya estén en `main`, y desde ahí ambos siguen el paso 10.

---

## Paso 0 · Requisitos en el equipo

Instalar y verificar:

```bash
java -version      # debe decir 21 o superior
docker --version
git --version
claude --version   # Claude Code
```

Si falta el JDK, instalar **Eclipse Temurin 21**. Java 17 no sirve: el proyecto compila
contra la versión 21 y el compilador se detiene.

Docker tiene que estar corriendo, no solo instalado. Después de instalarlo hay que reiniciar
el equipo; si `docker ps` responde con un error de conexión al demonio, todavía no está
listo.

---

## Paso 1 · Obtener el repositorio

El repositorio ya existe en GitHub, es público y lo creó Gonzalo. Clonarlo no requiere
autenticación:

```bash
git clone https://github.com/<usuario-gonzalo>/mira.git
cd mira
```

La documentación ya está adentro. La estructura debe verse así:

```
mira/
├── CLAUDE.md
├── README.md
└── docs/
    ├── 01-contexto-negocio.md
    ├── 02-arquitectura.md
    ├── 03-modelo-datos.md
    ├── 04-convenciones-codigo.md
    ├── 05-epicas.md
    ├── 06-flujo-trabajo.md
    ├── 07-pruebas.md
    ├── 08-decisiones.md
    ├── 09-glosario.md
    ├── 10-casos-de-prueba.md
    ├── 11-arranque-claude-code.md
    └── ESTADO.md
```

Si falta el `README.md`, agregarlo: está redactado y es requisito de la asignatura.

---

## Paso 2 · Identidad de Git en el equipo local

Esto importa: el historial del repositorio es la evidencia del indicador de colaboración.
Los commits tienen que quedar a nombre de quien los escribió, aunque después los suba otra
persona.

```bash
git config user.name "Joaquin Romero"
git config user.email "<el correo de la cuenta de GitHub>"
```

Verificar con `git config user.name`. Si esto queda mal, los commits aparecen como de otra
persona y no hay forma limpia de corregirlo después.

---

## Paso 3 · Verificar el repositorio remoto

Abrir el repositorio en GitHub y confirmar que el diagrama de `docs/03-modelo-datos.md` se
vea renderizado y no como texto. Si se ve como texto, el bloque Mermaid quedó mal cerrado.

Enviar el enlace al docente si no se ha hecho.

---

## Traspaso de trabajo cuando no se puede hacer push

Mientras Joaquín no recupere el acceso a su cuenta de GitHub, trabaja local y le entrega el
trabajo a Gonzalo para que lo suba. El mecanismo es un *bundle* de Git, que conserva los
commits tal cual: autor, fecha y mensaje.

**Joaquín, al terminar:**

```bash
git bundle create mira-<rama>.bundle main..<rama>
```

Manda ese archivo por el medio que sea. Pesa poco.

**Gonzalo, al recibirlo:**

```bash
git fetch ../mira-<rama>.bundle <rama>:<rama>
git push origin <rama>
```

Después abre el pull request como siempre. El commit sigue diciendo "Joaquin Romero", que
es lo que se evalúa.

**Esto es una solución temporal.** Recuperar el segundo factor de GitHub es tarea con
fecha: cada traspaso manual agrega una oportunidad de perder trabajo.

---

## Paso 4 · Abrir Claude Code

Desde la carpeta del proyecto:

```bash
claude
```

Al iniciar lee `CLAUDE.md` automáticamente.

---

## Paso 5 · Sesión 1 · Verificar que entendió el contexto

**No pidas código en esta sesión.** El objetivo es comprobar que la documentación se
sostiene sola. Copia esto:

> Lee CLAUDE.md y todos los archivos de docs/. No escribas código todavía.
>
> Respóndeme cuatro cosas:
> 1. Qué hace este proyecto, en tres frases.
> 2. Cuáles son las reglas que debes respetar sí o sí.
> 3. En qué estado está el proyecto según docs/ESTADO.md y qué corresponde hacer ahora.
> 4. Qué información te falta o qué te resulta ambiguo en la documentación.

La cuarta pregunta es la importante. Si algo no le queda claro a Claude Code, tampoco le
va a quedar claro a Gonzalo. Lo que aparezca ahí, arréglenlo en los documentos antes de
seguir.

---

## Antes del paso 6 · Lista de verificación

No empieces la sesión 2 sin esto resuelto. Cada punto que falte se transforma en un error
a mitad de camino, cuando ya hay archivos escritos.

| | Qué | Cómo se comprueba |
|---|---|---|
| ☐ | JDK 21 instalado y activo | `java -version` dice 21 |
| ☐ | Docker corriendo | `docker ps` responde sin error |
| ☐ | Repositorio clonado con la documentación adentro | existe `CLAUDE.md` en la raíz |
| ☐ | Identidad de Git configurada | `git config user.name` devuelve tu nombre |
| ☐ | Acordado quién genera el esqueleto | está escrito en `docs/ESTADO.md` |
| ☐ | Rama de trabajo creada, no `main` | `git status` dice `feature/esqueleto` |

Si Docker todavía no está listo, la sesión 2 igual se puede hacer completa: Claude Code
escribe los archivos y el proyecto compila con `./mvnw clean compile`. Lo único que no se
puede es levantar la aplicación, porque necesita la base de datos. Eso queda para cuando
Docker responda.

Crear la rama antes de abrir Claude Code:

```bash
git checkout -b feature/esqueleto
```

---

## Paso 6 · Sesión 2 · Configurar el proyecto base

> Vamos a configurar el proyecto base. Antes de crear nada, dime qué archivos vas a crear
> y espera mi confirmación.
>
> Proyecto Maven con Spring Boot 4.1 y Java 21. groupId `cl.duoc`, artifactId `mira`,
> paquete base `cl.duoc.mira`. Incluye el wrapper de Maven (`mvnw`, `mvnw.cmd` y
> `.mvn/wrapper`), porque los comandos de CLAUDE.md lo usan.
>
> Dependencias: Spring Web, Thymeleaf, Spring Data JPA, Spring Security, Validation, driver
> de PostgreSQL, Flyway (`flyway-core` y `flyway-database-postgresql`), Lombok y DevTools.
> Para pruebas: JUnit 5, Mockito y Testcontainers con PostgreSQL. Agrega el plugin JaCoCo
> para el informe de cobertura. **No agregues MapStruct**, ver D08 en docs/08-decisiones.md.
>
> Crea la estructura de paquetes según docs/02-arquitectura.md, vacía por ahora.
>
> Agrega docker-compose.yml con PostgreSQL 16 y volumen persistente, un .env.example con
> las variables del README, y un .gitignore para Java, Maven, IDE y .env.
>
> En `application.yml` deja `spring.flyway.enabled: true` y
> `spring.jpa.hibernate.ddl-auto: validate`. El esquema lo maneja Flyway, nunca Hibernate.
>
> No crees migraciones ni entidades todavía. Solo quiero que la aplicación levante y
> muestre una página de inicio simple con Thymeleaf y Bootstrap.

Al terminar, verificar en este orden:

```bash
./mvnw clean compile          # esto funciona aunque Docker no esté listo
docker compose up -d db
./mvnw spring-boot:run
```

Debe abrir en `http://localhost:8080`. Spring Security va a pedir usuario y contraseña: es
el comportamiento por omisión y está bien, la clave aparece en la consola al arrancar.

Commit. Si no puedes hacer push, genera el bundle y mándaselo a Gonzalo.

---

## Paso 7 · Sesión 3 · La migración del esquema

> Lee docs/03-modelo-datos.md completo.
>
> Crea `src/main/resources/db/migration/V1__esquema_inicial.sql` con las trece tablas del
> diccionario de datos.
>
> Requisitos que no puedes cambiar:
> - Respeta EXACTAMENTE los nombres de tabla, de columna y los valores de los enumerados
>   tal como están en el diccionario. Si crees que un nombre debería ser distinto, dímelo
>   antes en vez de cambiarlo.
> - Usa los tipos de dato indicados.
> - Incluye todas las claves foráneas, restricciones únicas e índices mencionados.
> - Agrega los cuatro campos de auditoría a todas las tablas.
> - `operacion`, `venta` y `arriendo` con estrategia de herencia JOINED.
> - `empresa_id` va en todas las entidades de negocio **salvo en `venta`, `arriendo` y
>   `operacion_detalle`**, que heredan la empresa de `operacion`. Está explicado en la
>   sección 1.2 del modelo. No se las agregues.
> - El enumerado `rol_usuario` tiene cuatro valores: `ADMINISTRADOR`, `ENCARGADO_BODEGA`,
>   `VENDEDOR` y `JEFATURA`.
>
> No crees entidades Java en esta sesión.
>
> Después crea `V2__datos_iniciales.sql` con una empresa, un usuario administrador y las
> categorías estación total, GPS RTK, dron y nivel. La contraseña del administrador va
> cifrada con BCrypt, nunca en texto plano, ni siquiera en datos de prueba.

Verificar que Flyway aplique las migraciones al levantar y revisar el esquema en la base.
Commit y, si no puedes hacer push, bundle para Gonzalo.

**A partir de aquí, esas migraciones no se editan nunca.**

---

## Paso 8 · Sesión 4 · Primera épica

> Vamos con la Épica 001 de docs/05-epicas.md, gestión de usuarios y control de acceso.
>
> Antes de escribir código, propón la lista de archivos que vas a crear y espera
> confirmación.
>
> Ten presente: la lógica va en la capa de servicios, los controladores reciben y devuelven
> DTOs y no entidades, las contraseñas se guardan con BCrypt, toda consulta filtra por
> empresa, y cada criterio de aceptación de la épica necesita su prueba automatizada.

Al terminar: `./mvnw test`, actualizar `docs/ESTADO.md`, commit y pull request.

---

## Paso 9 · Sumar a Gonzalo

Una vez que el esqueleto y la migración `V1` están en `main`:

```bash
git checkout main
git pull
cp .env.example .env
docker compose up -d db
./mvnw spring-boot:run
```

Si eso funciona sin ayuda de Joaquín y en menos de quince minutos, ya cumplieron la prueba
**PNF-16** de `docs/10-casos-de-prueba.md`. Déjenlo registrado con la hora, es evidencia.

Gonzalo abre Claude Code en su carpeta y hace el paso 5 antes de tocar nada. Recién ahí
empieza el trabajo en paralelo por zonas de propiedad.

---

## Paso 10 · Rutina de cada sesión de trabajo

**Al empezar:**

```bash
git checkout main
git pull
git checkout -b feature/E00X-descripcion
```

Y en Claude Code: *"Lee docs/ESTADO.md y dime qué está en curso."*

**Al terminar:**

```bash
./mvnw test
git add .
git commit -m "feat: descripcion en presente"
git push -u origin feature/E00X-descripcion
```

Quien no pueda hacer push reemplaza la última línea por el bundle:

```bash
git bundle create mira-E00X.bundle main..feature/E00X-descripcion
```

Actualizar `docs/ESTADO.md` con lo terminado, lo que quedó a medias y en qué rama. Abrir el
pull request y avisarle al otro para que lo revise.

---

## Cómo pedirle las cosas

**Sí funciona:**

- Una funcionalidad por vez, no una épica completa de una sola pasada.
- Pedir la lista de archivos antes de que escriba.
- Pedir la prueba junto con el código, no después.
- Cuando algo falle, pegar el error completo en vez de describirlo.
- Si propone algo que contradice la documentación, decírselo: *"eso rompe la regla X de
  CLAUDE.md"*.

**No funciona:**

- "Hazme la épica 005 completa." Sale mucho código de una vez, imposible de revisar, y si
  algo está mal hay que deshacer todo.
- "Arregla el error." Sin el mensaje no puede saber qué pasó.
- Aceptar cambios en archivos que no pediste tocar. Si aparecen, pregunta por qué.
- Dejar que modifique una migración ya aplicada. Si lo propone, se detiene ahí.

---

## Señales de que algo va mal

| Señal | Qué hacer |
|---|---|
| Propone editar una migración existente | Detenerlo. Crear una migración nueva |
| Un controlador llama a un repositorio | Falta un método de servicio |
| Aparece una consulta sin filtro por empresa | Corregir antes de continuar |
| Cambia nombres del diccionario de datos | Revertir. La documentación es la referencia |
| Toca archivos de la zona del otro integrante | Detenerlo y avisar al compañero |
| Las pruebas fallan y sugiere ignorarlas | No. Se arregla la causa |

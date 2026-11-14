#!/usr/bin/env bash
# Carga los datos de demostración sobre una base recién creada (V1 a V3 aplicadas, sin datos).
#
#   docs/demo/cargar-demo.sh docker   # sistema levantado con `docker compose up --build`
#   docs/demo/cargar-demo.sh local    # base local o `docker compose up -d db` + ./mvnw spring-boot:run
#
# Para empezar de cero: `docker compose down -v` (docker) o borrar y recrear la base (local), levantar
# la aplicación una vez para que Flyway cree el esquema, y volver a ejecutar este script.
set -euo pipefail

modo="${1:-docker}"
dir="$(cd "$(dirname "$0")" && pwd)"
archivos="$dir/archivos/1"

# psql recibe el tamaño de cada PDF para guardarlo como lo haría la aplicación.
tamano() { wc -c < "$archivos/$1" | tr -d ' '; }
variables=(
    -v bytes_oc131="$(tamano demo-oc-2026-131.pdf)"
    -v bytes_guia1045="$(tamano demo-guia-1045.pdf)"
    -v bytes_oc161="$(tamano demo-oc-2026-161.pdf)"
    -v bytes_fac88231="$(tamano demo-factura-88231.pdf)"
    -v bytes_guia1012="$(tamano demo-guia-1012.pdf)"
)

case "$modo" in
    docker)
        docker compose exec -T db psql -q -U "${DB_USER:-mira}" -d mira "${variables[@]}" -f - < "$dir/datos-demo.sql"
        docker compose exec -T app mkdir -p /data/uploads/1
        for f in "$archivos"/*.pdf; do
            docker compose cp "$f" "app:/data/uploads/1/$(basename "$f")"
        done
        ;;
    local)
        PGPASSWORD="${DB_PASSWORD:-mira}" psql -q -h "${DB_HOST:-localhost}" -U "${DB_USER:-mira}" -d mira \
            "${variables[@]}" -f "$dir/datos-demo.sql"
        destino="${APP_UPLOAD_DIR:-./uploads}/1"
        mkdir -p "$destino"
        cp "$archivos"/*.pdf "$destino/"
        ;;
    *)
        echo "Uso: $0 docker|local" >&2
        exit 1
        ;;
esac

echo "Datos de demostración cargados. Usuarios: admin@mira.cl/admin123, vendedor@mira.cl/vendedor123,"
echo "jefatura@mira.cl/jefatura123 y bodega@mira.cl/bodega123."

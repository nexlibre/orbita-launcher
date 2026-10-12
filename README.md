# ÓRBITA Launcher

Tu universo. Tus reglas.

Android/Kotlin + Compose. Primer incremento de desarrollo `0.1.0-dev.1`, **no Alfa 0.1 completa**. Nombre provisional. Repositorio público: [nexlibre/orbita-launcher](https://github.com/nexlibre/orbita-launcher).

## Incluido en el código

Actividad HOME real, bienvenida local, inventario del perfil personal, apertura de aplicaciones, cajón vertical alfabético y búsqueda sin red, cuadrícula de cinco columnas, fijar/quitar accesos y dock de hasta cinco favoritos, persistencia con DataStore, Essential claro/oscuro automático, etiquetas con guardar/descartar y controles táctiles accesibles. Inventario e iconos se cargan fuera del hilo principal; cambios de paquetes y regreso a primer plano actualizan el catálogo. No utiliza INTERNET ni QUERY_ALL_PACKAGES.

## Estado de validación

Ver `docs/VALIDATION.md`. No confundir pruebas del catálogo con validación de un launcher en dispositivo. El código permanece en desarrollo hasta superar compilación, lint, pruebas y escenarios Android.

## Descargar desde Android

Las versiones verificadas se publican como prereleases en [Releases](https://github.com/nexlibre/orbita-launcher/releases). En Assets, descargar el archivo APK e instalarlo autorizando esa descarga en Android. Después: Ajustes → Aplicaciones predeterminadas → Aplicación de inicio → ÓRBITA. Siempre puedes volver al launcher anterior desde los ajustes de Android.

El primer APK es de depuración. La firma debug no está garantizada entre runners: antes de distribuir actualizaciones instalables sobre la primera versión habrá que configurar una clave persistente protegida. No se publican claves en el repositorio. No desinstales una versión con datos importantes para instalar otra sin respaldo.

El flujo dev.1 permite publicar solo la primera prerelease verificada; no publica actualizaciones con otra firma automáticamente. Una compilación fallida no produce una release. Los artefactos de Actions también permiten inspeccionar informes y APK de cada compilación; caducan en 30 días.

## Compilar

JDK 17, Gradle 8.11.1, SDK Android 35 y Build Tools 35.0.0. Android mínimo 8 (API 26). Identificador propuesto: org.nexlibre.orbita.

`./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`

Las versiones están fijadas. Se incluye el wrapper oficial Gradle 8.11.1 con checksum SHA-256 de la distribución.

## Pendientes

Páginas, mover/reordenar, carpetas, gestos, restauración explícita de datos inválidos, prueba TalkBack/letra grande, animaciones adaptativas y medición de rendimiento. Liquid, Motion Engine avanzado, widgets y Atelier no están implementados. No soporta aún perfiles de trabajo.

## Licencia

Apache-2.0, confirmada por el propietario. Ver [LICENSE](LICENSE).

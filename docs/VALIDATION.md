# Resultados reales — 12 de octubre de 2026

## Ejecutado

- GitHub `get_user_login`: cuenta nexlibre (ID 134002775).
- Búsqueda `user:nexlibre orbita`: sin resultados. No demuestra por sí sola que no exista un repositorio inaccesible.
- Compilador Kotlin 2.1.20 + JDK 17: compilar Catalog.kt y CatalogTest.kt con JUnit 4.13.2.
- JUnitCore: **OK (4 tests)**, 0 fallos. Se ejecutó el código de producción del catálogo, no una reimplementación.
- Wrapper oficial Gradle 8.11.1 generado; distribución con checksum SHA-256.

## Fallos y límites de la etapa local inicial

- `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`: falló durante resolución de plugin `com.android.application:8.9.2`. Se instalaron SDK 35 y Build Tools; varios intentos con el proxy del entorno no resolvieron el plugin. No se alcanzó la compilación Android.
- Los POM del plugin y del marcador respondieron HTTP 200 mediante curl. Esto no demuestra que Gradle o GitHub CI puedan resolver todo el árbol de dependencias.
- En la etapa local inicial no se generó APK. Repositorio público creado y licencia Apache-2.0 confirmada posteriormente por el propietario. La verificación CI se documentará tras ejecutarse.
- No se ejecutaron pruebas instrumentadas, TalkBack, reinicio, cambio de paquete o rendimiento en dispositivo.
- Ninguno de los criterios P0 en Android se declara aprobado.

## Continuación autorizada

El propietario autorizó la creación por navegador de nexlibre/orbita-launcher y confirmó Apache-2.0. GitHub confirma repositorio público y permisos de escritura. Se prepara CI con pruebas unitarias, lint y smoke instrumentado en API 29 y 35 antes de publicar la primera prerelease dev.1.

Publicar código no valida funciones. El primer CI deberá superar compilación, pruebas y lint antes de ofrecer el APK como build de desarrollo; completar P0 requiere los escenarios de ACCEPTANCE_TESTS.md.

## Verificación completada en GitHub Actions

Commit probado: `8cb0c73bc1b5f75a848fda4613f6971563f2b8e7`. Ejecución: https://github.com/nexlibre/orbita-launcher/actions/runs/38264872332

- Compilación Kotlin/Android y assembleDebug: correctos.
- JUnit: 4 pruebas, 0 fallos, 0 ignoradas. Informe del catálogo: 0.121 segundos.
- Android lint: 0 errores, 13 advertencias. Tipos: OldTargetApi, GradleDependency (8), DataExtractionRules, ObsoleteSdkInt, MonochromeLauncherIcon y UseKtx. La revisión de dependencias/SDK y refinamientos de configuración sigue pendiente; no se ocultan las advertencias.
- Smoke instrumentado: 1 escenario en API 29 (Android 10) y el mismo en API 35 (Android 15), ambos correctos. Comprueba actividad HOME declarada, bienvenida, apertura del cajón, búsqueda sin coincidencias y entrar/descartar ajustes.
- Publicación de primera prerelease: correcta; APK de depuración y checksum disponibles en https://github.com/nexlibre/orbita-launcher/releases/tag/v0.1.0-dev.1
- APK publicado: 9,766,649 bytes. SHA-256: `cb28c9640d01f93fa4df632fc30c23831bd7a169d17a6f91e543126807c2f33b`.

Estas pruebas no seleccionan el launcher predeterminado ni abren otra app, no fuerzan cierre/reinicio de dispositivo, no validan TalkBack y no miden hardware real. Tampoco validan API 26 o perfiles de trabajo. Los criterios P0 completos siguen pendientes; dev.1 no se anuncia como Alfa 0.1 completa.

La primera ejecución CI falló por el paquete obsoleto `tools`; se corrigió a `platform-tools`. El segundo commit compiló y pasó lint/JUnit pero su ejecución se canceló al publicar el icono. La ejecución citada arriba prueba el commit definitivo de dev.1 y pasó todas sus etapas.

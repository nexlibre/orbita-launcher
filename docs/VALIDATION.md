# Resultados reales — 12 de octubre de 2026

## Ejecutado

- GitHub `get_user_login`: cuenta nexlibre (ID 134002775).
- Búsqueda `user:nexlibre orbita`: sin resultados. No demuestra por sí sola que no exista un repositorio inaccesible.
- Compilador Kotlin 2.1.20 + JDK 17: compilar Catalog.kt y CatalogTest.kt con JUnit 4.13.2.
- JUnitCore: **OK (4 tests)**, 0 fallos. Se ejecutó el código de producción del catálogo, no una reimplementación.
- Wrapper oficial Gradle 8.11.1 generado; distribución con checksum SHA-256.

## Fallos / no ejecutado

- `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`: falló durante resolución de plugin `com.android.application:8.9.2`. Se instalaron SDK 35 y Build Tools; varios intentos con el proxy del entorno no resolvieron el plugin. No se alcanzó la compilación Android.
- Los POM del plugin y del marcador respondieron HTTP 200 mediante curl. Esto no demuestra que Gradle o GitHub CI puedan resolver todo el árbol de dependencias.
- En la etapa local inicial no se generó APK. Repositorio público creado y licencia Apache-2.0 confirmada posteriormente por el propietario. La verificación CI se documentará tras ejecutarse.
- No se ejecutaron pruebas instrumentadas, TalkBack, reinicio, cambio de paquete o rendimiento en dispositivo.
- Ninguno de los criterios P0 en Android se declara aprobado.

## Continuación autorizada

El propietario autorizó la creación por navegador de nexlibre/orbita-launcher y confirmó Apache-2.0. GitHub confirma repositorio público y permisos de escritura. Se prepara CI con pruebas unitarias, lint y smoke instrumentado en API 29 y 35 antes de publicar la primera prerelease dev.1.

Publicar código no valida funciones. El primer CI deberá superar compilación, pruebas y lint antes de ofrecer el APK como build de desarrollo; completar P0 requiere los escenarios de ACCEPTANCE_TESTS.md.

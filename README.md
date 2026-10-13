# ÓRBITA Launcher

**Tu universo. Tus reglas.** Launcher Android original en Kotlin/Compose, local y de código abierto con Apache-2.0. Nombre provisional.

## Descargar en Android

[Descargar Alfa 0.1](https://github.com/nexlibre/orbita-launcher/releases/tag/v0.1.0-alpha.1). En Assets, elegir ORBITA-0.1.0-alpha.1.apk, instalar y seleccionar ÓRBITA en Ajustes Android → Aplicaciones predeterminadas → Aplicación de inicio. Puedes volver al launcher anterior.

Alfa incluye páginas de cinco columnas, dock transparente, movimiento por posiciones/intercambio con deshacer, carpetas con orden manual, cajón alfabético y búsqueda local, Essential automático/claro/oscuro, etiquetas, movimiento reducido y vibración. Cambios de apariencia con vista previa, Guardar y Descartar. Pulsación larga o acción accesible abre opciones. Subir/bajar sobre la barra inferior abre/cierra el cajón; los botones ofrecen alternativas.

Disposición y preferencias se guardan con DataStore. Ante datos dañados, bloquea la edición, conserva originales y permite exportar, recuperar una copia válida o confirmar restaurar vacío después de exportar. Las referencias a apps ausentes se conservan. Sin cuentas, anuncios, INTERNET ni QUERY_ALL_PACKAGES.

## Verificación real

[CI aprobado](https://github.com/nexlibre/orbita-launcher/actions/runs/38278990550): compilación, 12 pruebas unitarias, lint sin errores y 40 ejecuciones instrumentadas de aceptación entre Android 8/10/13/15. Incluye interfaz, carpetas, movimientos, deshacer, gestos, preferencias, cierre/reinicio, selección HOME, apertura accesible de apps, instalación/desinstalación en vivo, 500 actividades lanzables de una fixture, texto 200%, exportación y recuperación de corrupción.

**La aprobación total de P0 sigue pendiente de las comprobaciones manuales** de TalkBack hablado, fabricantes, contraste y rendimiento físico. Ver [VALIDATION](docs/VALIDATION.md) y [ACCEPTANCE_TESTS](docs/ACCEPTANCE_TESTS.md). Liquid, Atelier, física avanzada y widgets permanecen fuera de esta alfa.

## Firma y actualizaciones

Alfa utiliza una firma de desarrollo persistente, protegida con secretos cifrados de GitHub. No hay claves en código/artefactos/cachés. El APK es de depuración, no de producción.

Si instalaste dev.1, su firma temporal no se pudo recuperar: Android exige desinstalarla antes de instalar la alfa. Esto elimina sus accesos y ajustes; anótalos primero. Futuras versiones firmadas con la clave de la alfa pueden conservar los datos al actualizar.

## Arquitectura y desarrollo

El núcleo tiene catálogo/Layout/Codec puros, repositorio transaccional de persistencia/recuperación y un ViewModel compartido a nivel de aplicación. HOME y entrada ordinaria pueden ser tareas distintas; comparten catálogo, edición y deshacer. Inventario, búsqueda y serialización ocurren fuera del hilo UI. Estructura, apariencia y gestos permanecen separados del modelo persistido.

Android mínimo 8 (API 26), SDK 35, JDK 17, wrapper Gradle 8.11.1. Comando: ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug

La app fixture no se incluye en el APK de ÓRBITA. ci/android-acceptance.sh se usa solo en emuladores desechables: instala/desinstala la fixture, modifica ajustes, reinicia y daña datos deliberadamente para validar recuperación.

[Visión](docs/PRODUCT_VISION.md) · [Especificación](docs/SPEC_ALPHA_0_1.md) · [Arquitectura](docs/ARCHITECTURE.md) · [Versiones](CHANGELOG.md) · [Licencia](LICENSE)

## Ampliación 0.2.0-alpha.1

Tema AMOLED negro con monogramas blancos; reloj con tamaño y fecha editables, barra de búsqueda opcional, tamaño de iconos y perfiles de movimiento. Temas Pixel inspirado y Liquid inicial. Frecuencia preferida automática o compatible hasta 144 Hz. Android decide la frecuencia efectiva.

[Alcance y limitaciones](docs/RELEASE_ALPHA_0_2.md) · [Desarrollo de edición completa](docs/ROADMAP.md). Widgets externos, edición por elemento, paquetes de iconos y refracción Liquid quedan en siguientes entregas. La release 0.2 solo se publica después de aprobar el CI en main.

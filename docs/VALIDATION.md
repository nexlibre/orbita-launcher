# Resultados reales — 12 de octubre de 2026

## Alfa 0.1.0-alpha.1

Commit probado/publicado: 357a900bea603dc56448bd9bbcb6c3bd3e1b1285.

[Ejecutar y consultar informes](https://github.com/nexlibre/orbita-launcher/actions/runs/38278990550) · [Release](https://github.com/nexlibre/orbita-launcher/releases/tag/v0.1.0-alpha.1)

- Compilación Kotlin/Android, assembleDebug y lintDebug correctos. Lint conserva advertencias: consultar el artefacto verification-reports; no se declaran cero advertencias.
- 12 pruebas unitarias (8 Layout/Codec y 4 catálogo), incluidas identidad estable de migración, orden manual, swaps, datos inválidos, duplicados, capacidad y búsqueda de 1000 registros.
- Cuatro jobs Android aprobados: API 26/29/33/35 (Android 8/10/13/15).
- Cada dispositivo ejecutó un escenario Compose y nueve casos instrumentados en distintas fases/procesos: 40 ejecuciones de prueba en total, todas correctas. La verificación después de proceso/reinicio se ejecuta en dos momentos distintos.
- UI: bienvenida, búsqueda, barra de gestos, preferencias, carpeta (crear/añadir/renombrar/reordenar/sacar), movimiento por posición, quitar sin desinstalar y deshacer.
- Procesos/OS: force-stop, nuevo proceso y reboot real del emulador conservan disposición y apariencia.
- Paquetes: fixture instalada/desinstalada mientras HOME permanece visible; 500 aliases lanzables reales más apps del sistema actualizan el catálogo. No se afirma que sean 500 paquetes distintos.
- Accesibilidad Android: apertura real de fixture desde cajón/escritorio y texto 200% con controles alcanzables.
- Datos: ZIP incluye layout/originales; archivo protobuf dañado archiva originales, bloquea edición y recupera copia válida de layout/apariencia.
- Release protegida por todos los jobs: publicación correcta con APK debug y SHA256SUMS.txt.

APK: 9,864,977 bytes. Digest SHA-256 informado por GitHub: 71d33e107c12dcc1829ef4fa2199fc774aa2e1f58155442cb0500cd8e4af235b. Las instalaciones Android de la suite validaron firma durante las pruebas. La verificación local adicional del archivo descargado no se pudo ejecutar porque el entorno local se desconectó; no se afirma que se completó.

## Errores encontrados y resueltos

Configuración inicial de Actions con paquete SDK tools obsoleto; contexto runner no disponible en env de job; limpieza automática de APK de pruebas por AGP; selector de accesibilidad que coincidía con texto editable; núcleo independiente por actividad HOME/entrada estándar; IDs inestables de migración. Las correcciones están en el historial y el commit citado pasó la suite completa. Búsqueda y serialización se trasladaron fuera del hilo UI; accesos guardados pueden abrirse durante la carga del catálogo.

## Límites de aceptación

No se ejecutó TalkBack hablado, una prueba de fabricantes/teléfono físico, medición de frames en dispositivo básico, corte físico de energía o disco lleno. No se declara terminado ese trabajo ni todos los criterios P0. Liquid, Motion avanzado, Atelier, widgets/shortcuts y demás P1 siguen fuera.

La firma temporal de dev.1 no fue recuperable. La alfa inicia una firma persistente guardada en GitHub Secrets; para pasar de dev.1 a alfa se requiere retirar dev.1, perdiendo sus ajustes/accesos. La futura actualización con misma firma debe validarse al producir la siguiente alfa.

## Entrega anterior

0.1.0-dev.1: commit 8cb0c73bc1b5f75a848fda4613f6971563f2b8e7 y ejecución 38264872332. Cuatro pruebas unitarias y smoke API 29/35, primera release debug. No implementaba todos los P0.

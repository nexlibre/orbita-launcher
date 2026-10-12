# Arquitectura inicial

`core/Catalog.kt`: catálogo y búsqueda puros, sin APIs Android. Pruebas de orden estable, normalización y ausencias.

`core/LauncherState.kt`: integración PackageManager, modelos, persistencia DataStore y estado unidireccional. ComponentName identifica cada actividad; el catálogo actual no elimina registros persistentes de aplicaciones ausentes.

`feature/Orbita.kt`: presentación Compose de Essential, escritorio, cajón, bienvenida y borrador de etiquetas. `MainActivity` conecta ciclo de vida HOME y cambios del sistema.

Este es un módulo app con paquetes por responsabilidad, no una arquitectura multimódulo terminada. Antes de añadir motores, extraer repositorios e interfaces para Theme/Layout/Motion; no convertir LauncherState en un contenedor de todos los motores. Los módulos Widget y Atelier se crearán cuando exista implementación validada, evitando módulos vacíos.

No se sustituyen Recientes, pantalla de bloqueo ni animaciones del sistema. El incremento no añade animaciones complejas: reducción de movimiento e integración con preferencias Android deben validarse antes de introducirlas.

DataStore serializa ediciones de preferencias. Validar toda la disposición antes de exponer edición evita sobrescribir datos inválidos. Ante error de lectura la edición queda bloqueada y los datos se conservan; la UI de recuperación/exportación aún falta. No se escribe una disposición por defecto automáticamente sobre datos corruptos.

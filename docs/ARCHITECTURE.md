# Arquitectura del núcleo Alfa 0.1

Catalog.kt normaliza búsqueda y orden estable. Layout.kt implementa páginas, slots, dock, carpetas, codec versionado/acotado y migración con IDs deterministas. LauncherRepository mantiene una instancia DataStore por archivo, transacciones y respaldo AtomicFile de configuración válida; archiva originales dañados y exporta por ContentResolver/SAF. LauncherState publica estado unidireccional, inventario/cancelación y edición serializada con deshacer.

OrbitaApplication mantiene un ViewModelStore del núcleo compartido por HOME y la entrada normal. Android puede crear tareas/actividades diferentes; compartir catálogo, mutex y deshacer evita divergencias. MainActivity conecta ciclo de vida y cambios de paquetes. Orbita.kt presenta páginas, cajón, carpetas, posición/edición, preferencias, gestos y recuperación.

Inventario/iconos, búsqueda/ordenación y validación/serialización se ejecutan fuera del hilo de interfaz. Cambiar apariencia no reordena posiciones. El historial de 20 pasos es temporal; la disposición confirmada es persistente. La copia guarda layout, apariencia y bienvenida. Apps ausentes conservan referencias; intentar abrirlas muestra un aviso.

Una operación valida el modelo antes de confirmar. El codec rechaza versión desconocida, entradas extra y datos inválidos. La corrupción archiva originales y bloquea edición; no la sustituye silenciosamente por una disposición vacía. Recuperar vacío requiere exportar y confirmar. Exportar incluye referencias/configuración, no APK ni datos externos.

Es un módulo app separado por paquetes, no una arquitectura multimódulo terminada. El Layout Core puro prepara el futuro Layout Engine; Theme/Motion y Widget/Atelier deberán extraerse con contratos reales al añadir capacidades, sin módulos vacíos ni efectos gráficos costosos en el núcleo inicial.

Límites: cinco columnas, 25 slots/página, hasta 40 páginas, cinco slots de dock; carpetas sin anidación y perfil personal. No sustituye Recientes, bloqueo ni otras apps. La zona de gestos está dentro de safeDrawing sin excluir gestos Android.

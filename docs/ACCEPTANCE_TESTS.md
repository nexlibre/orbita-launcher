# Aceptación P0

Las funciones del núcleo están implementadas y la automatización pasó en API 26/29/33/35. No se declara aprobada toda la aceptación de P0 hasta ejecutar los casos manuales restantes.

| Criterio | Evidencia automatizada aprobada | Comprobación manual restante |
|---|---|---|
| HOME | Declaración y selección del sistema; Inicio abre ÓRBITA | Selector en fabricantes |
| Abrir apps | App real desde cajón y escritorio mediante accesibilidad Android | Apps del teléfono real |
| Persistencia | Páginas, slots, carpeta, dock, apariencia; proceso y reinicio del emulador | Reinicio físico y actualización alfa |
| Paquetes | Instalar/desinstalar con launcher visible, mismo proceso; 500 actividades de fixture | Tiendas/OEM |
| Carpetas | Crear, añadir, renombrar, ordenar, sacar; mover/deshacer por UI; invariantes | Uso prolongado |
| Tema | Guardar/descartar, persistencia y fuente 200% | Contraste y cambio Auto real |
| Gestos | Subir/bajar barra inferior, botones y HOME | Navegación gestual de fabricantes |
| Accesibilidad | Roles/semántica y acciones Android abren apps; controles con texto 200% | TalkBack hablado y lector real |
| Búsqueda | 1000 registros JUnit, 500 actividades reales de fixture y sin coincidencias | Scroll/rendimiento físico |
| Recuperación | Protobuf dañado, bloqueo de edición, archivo original, exportación y recuperación explícita | Energía/almacenamiento lleno |

P1 (widgets, shortcuts, cuadrícula variable e importación de ZIP) queda fuera. La exportación incluida no es un importador de respaldos. Emuladores y acciones de accesibilidad del framework no sustituyen TalkBack real.

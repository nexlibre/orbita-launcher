# Verificación P0 — todas pendientes en Android

| Criterio | Comprobación | Estado |
|---|---|---|
| HOME | Seleccionar ÓRBITA desde ajustes y pulsar Inicio | Pendiente |
| Abrir apps | Abrir app desde escritorio, dock y cajón | Pendiente |
| Persistencia | Fijar, quitar, forzar cierre y reiniciar | Pendiente |
| Paquetes | Instalar/desinstalar con launcher visible y en segundo plano | Pendiente |
| Carpetas | Crear, renombrar, ordenar, mover fuera | No implementado |
| Tema | Claro/oscuro del sistema, guardar/descartar etiquetas | Pendiente |
| Gestos | Subir/bajar sin bloquear navegación Android | No implementado |
| Accesibilidad | TalkBack, abrir app y configurar con letra 200% | Pendiente |
| Catálogo | Vacío, sin coincidencias, nombres duplicados y 1000 apps | Pruebas unitarias escritas; ejecución documentada aparte |
| Recuperación | Datos inválidos, fallo de disco, restauración sin sobrescritura | Bloqueo de edición implementado; recuperación pendiente |

Repetir en API 26/29/33/35. Medir tiempo de inicio y frames en dispositivo básico real. Comprobar perfil de trabajo por separado (fuera de dev.1). Ningún criterio de aceptación se considera aprobado por inspección estática.

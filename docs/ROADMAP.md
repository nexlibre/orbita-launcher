# Ruta progresiva

1. dev.1 publicado: núcleo inicial, HOME/catálogo y CI.
2. alpha.1 publicado: implementación del núcleo P0, firma persistente y aceptación automatizada en Android 8/10/13/15.
3. Siguiente validación: TalkBack real, fabricantes, teléfono básico/rendimiento, contraste, actualización alfa con misma firma y pruebas físicas de recuperación.
4. P1 solo después de estabilizar esos resultados: widgets/shortcuts, cuadrícula y tamaños con vista previa, importación de respaldos.
5. Theme/Layout/Motion Engines ampliados, Liquid y Atelier como fases posteriores, sin comprometer el núcleo.

No cerrar la aceptación manual por tener CI verde. Cada versión debe subir versionCode, mantener firma y publicar notas/evidencia. No reemplazar un tag publicado con otro APK.

## Ampliación solicitada: personalización completa

Primera entrega 0.2.0-alpha.1: AMOLED, monogramas blancos, búsqueda, reloj integrado, tamaño de iconos, perfiles de resorte y preferencia de refresco compatible. Pixel inspirado y Liquid inicial son temas opcionales.

Siguientes entregas, con validación en cada etapa:

1. Layout Engine: cuadrícula configurable, posición libre controlada, tamaños, márgenes, dock editable y previsualización con guardar/descartar.
2. Widget Engine: AppWidgetHost, selector de proveedores, consentimiento de vinculación, configuración/reconfiguración, redimensionado y eliminación con limpieza de IDs. Los widgets externos conservan los límites de su proveedor.
3. Widgets propios editables: reloj, fecha, calendario y batería; colores, tipografía, formas, alineación, contenido y posición. No solicitar ubicación para clima hasta activación expresa.
4. Iconos: etiquetas individuales, paquetes compatibles, selector de imágenes con permiso acotado, formas y tamaño por elemento; conservar claves de componentes y recuperación.
5. Motion Engine: arrastre con inercia, resistencia, retorno y reacción táctil; medir latencia y tiempo de cuadro en teléfonos reales, suspender trabajo en reposo y respetar accesibilidad.
6. Theme Engine y Atelier: temas exportables con parámetros independientes; Liquid con capas y desenfoque condicionado a capacidades, fallback económico; estilos adicionales sin copiar identidades.

No ofrecer un deslizador continuo 0–144 como frecuencia física: mostrar Automática y modos disponibles hasta 144 Hz. Nunca cambiar resolución ni ajustes globales del dispositivo para simular compatibilidad.

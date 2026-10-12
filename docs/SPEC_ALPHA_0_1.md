# ÓRBITA Launcher — Especificación técnica y traspaso

**Estado:** visión conceptual aprobada; especificación inicial para revisión técnica.  
**Nombre:** ÓRBITA (provisional, pendiente de comprobación de marca).  
**Objetivo:** launcher Android original, estable y profundamente personalizable, con física perceptiva y estilos independientes de la estructura y la navegación.  
**Fase:** Alfa 0.1 — núcleo funcional. No afirmar que hay APK o código implementado.

## 1. Principios no negociables

1. Identidad original; no clonar Pixel, iOS, Niagara o Smart Launcher.
2. La apariencia, estructura y comportamiento son sistemas independientes.
3. Calidad y estabilidad antes de sumar funciones; alfas y betas progresivas.
4. Animaciones naturales, táctiles, interrumpibles y adaptativas, con reducción de movimiento.
5. Privacidad local-first; sin cuentas obligatorias, publicidad invasiva o permisos masivos.
6. Accesibilidad desde la bienvenida, heredando ajustes compatibles de Android.
7. Cambios de configuración con vista previa y guardar; acciones directas de escritorio con deshacer.
8. Persistencia y recuperación: no perder disposición, carpetas ni preferencias.

## 2. Decisiones aprobadas

- Nombre provisional ÓRBITA; lema de trabajo: «Tu universo. Tus reglas».
- Apariencia inicial: **Automático + Essential**, siguiendo claro/oscuro del sistema; colores dinámicos opcionales.
- Estructura inicial: distribución recomendada, cinco columnas, dock sin fondo, Pixel Space equilibrado y discreto cuando haya contenido real.
- Cajón vertical continuo, orden alfabético, búsqueda local, sugerencias opcionales.
- Gestos con sensibilidad equilibrada configurable, navegación gestual con alternativas táctiles.
- Vibración sutil por defecto, desactivable.
- Transiciones adaptativas activadas por defecto, desactivables; vista previa de gestos integrada.
- Carpetas con orden manual y comportamiento común; presentaciones avanzadas después.
- Temas futuros: Essential, Liquid, Obsidian, Flow, Atelier; combinar materiales, layouts y física.
- Quick Canvas, Widget Studio y Atelier Studio: objetivos progresivos, no compromisos para Alfa 0.1.

## 3. Stack propuesto (a validar en Work)

- Kotlin; Android SDK actual compatible con dispositivos objetivo.
- Jetpack Compose para UI del launcher, combinando vistas/API Android cuando convenga (AppWidgetHost, LauncherApps, shortcuts, drag-and-drop, etc.).
- MVVM o arquitectura unidireccional por capas: `app`, `core/model`, `core/data`, `core/designsystem`, `feature/home`, `feature/drawer`, `feature/settings`, `feature/onboarding`.
- DataStore para preferencias; Room para disposiciones y relaciones de carpetas si el modelo lo justifica.
- Coroutines/Flow para datos reactivos, evitando trabajo costoso en el hilo principal.
- Gradle, Android lint, pruebas unitarias e instrumentadas; GitHub Actions para compilar APK de depuración descargable como artefacto.
- Licencia propuesta Apache-2.0: **requiere confirmación antes de publicar**.
- No prometer control completo de animaciones del sistema, recientes, pantalla de bloqueo o apps ajenas: Android/fabricante limitan estas capacidades.

## 4. Alfa 0.1: alcance

### P0 — Imprescindible para llamar a la alfa funcional

- Declarar actividad HOME y permitir seleccionar launcher por el mecanismo del sistema.
- Primera ejecución breve, sin registro; mostrar cómo elegir app de inicio; acceso a accesibilidad.
- Enumerar aplicaciones lanzables instaladas, manejar cambios de instalación/desinstalación y abrirlas correctamente.
- Escritorio con páginas y cuadrícula inicial de cinco columnas; colocar, mover y quitar accesos sin desinstalar aplicaciones.
- Dock transparente con favoritos editables.
- Cajón vertical alfabético con búsqueda por nombre y estado vacío correcto.
- Carpetas básicas: crear, abrir, renombrar, agregar, quitar y reordenar aplicaciones.
- Persistir distribución, carpetas, preferencias y orden tras cierre forzado/reinicio.
- Tema automático claro/oscuro con estilo Essential original, etiquetas ajustables.
- Gestos básicos: subir abre cajón, bajar lo cierra (sin interferir con gestos reservados de Android); alternativas táctiles accesibles.
- Soporte TalkBack básico, descripciones de iconos y controles, texto ampliado y reducción de movimiento.
- Manejar errores de datos sin destruir la disposición anterior; recuperación predeterminada.

### P1 — Si P0 ya está estable

- Reordenación con deshacer.
- Accesos directos de aplicaciones compatibles y widgets Android estándar, si la integración se valida.
- Ajuste de cuadrícula y tamaño de iconos con previsualización y confirmación.
- Pruebas de respaldo/exportación inicial, con aviso sobre elementos externos no recuperables.

### Fuera de Alfa 0.1

- Física avanzada Motion Engine 2.0; Liquid Glass de alto coste gráfico.
- Editor Atelier Studio, Widget Studio completo y Quick Canvas automático.
- Sugerencias basadas en hábitos, IA, automatizaciones, sincronización en nube.
- Sustitución de interfaz del sistema Android o de otras aplicaciones.

## 5. Contratos de experiencia

### Inicio

- Abrir el launcher presenta el escritorio guardado sin tutorial recurrente.
- Si no hay apps fijadas, presentar una distribución inicial discreta sin simular apps inexistentes.
- No duplicar saludo fuera y dentro de Pixel Space.
- Dock sin cápsula; nombres y búsqueda opcionales.

### Cajón

- Orden alfabético estable, con manejo de nombres duplicados.
- Buscar no necesita red; los resultados cambian con la consulta.
- El menú de ordenación y opciones no ocupa una fila permanente.

### Carpetas

- Apertura desde origen cuando la animación lo permita; cierre con toque fuera/atrás.
- Orden manual preservado; mover fuera no duplica el acceso.
- Si desaparece una app, preservar coherencia de la carpeta.

### Personalización

- Preferencias de apariencia: borrador -> previsualización -> guardar/descartar.
- Movimiento directo de iconos: cambio inmediato + deshacer.
- No aplicar cambios silenciosos de layout durante actualizaciones.

### Accesibilidad

- Preferencias compatibles del sistema tienen prioridad inicial.
- Reducción de movimiento simplifica transiciones.
- Controles esenciales disponibles sin gestos complejos.

## 6. Criterios de aceptación P0

1. Android permite seleccionar ÓRBITA como launcher predeterminado en dispositivos compatibles.
2. Abrir al menos una app instalada desde escritorio y cajón funciona correctamente.
3. Tras reiniciar dispositivo/proceso, se conservan posiciones, dock y carpetas.
4. Instalar o desinstalar apps actualiza el cajón sin fallos ni duplicados persistentes.
5. Crear/renombrar/reordenar carpeta no pierde aplicaciones.
6. Cambiar modo claro/oscuro y volver conserva preferencias y contraste legible.
7. Navegación por gestos del sistema no queda bloqueada por el launcher.
8. TalkBack identifica controles esenciales y se puede abrir una app sin gestos complejos.
9. El cajón responde a búsqueda vacía, sin coincidencias y cientos de apps.
10. Una configuración inválida se recupera sin bucles de cierre y sin sobrescribir datos válidos sin aviso.

**No declarar aprobados estos criterios hasta ejecutar pruebas reales.**

## 7. Escenarios de prueba

- Usuario nuevo: bienvenida -> elegir HOME -> abrir cajón -> abrir app.
- Usuario minimalista: tema oscuro/AMOLED futuro -> ocultar etiquetas -> mantener accesibilidad.
- Usuario intensivo: muchas apps, varias páginas y carpetas; búsquedas repetidas.
- Usuario accesibilidad: TalkBack, letra grande, movimiento reducido, controles alternativos.
- Dispositivo básico: desplazamiento sin bloqueos prolongados ni trabajo en segundo plano innecesario.
- Cambio de app instalada: icono que desaparece, renombre de paquete y datos persistentes.
- Recuperación: cierre forzado durante arrastre o guardado, actualización de app, restauración de datos.

## 8. Arquitectura evolutiva

- `Launcher Core`: integración Android, inventario de apps, estado y persistencia.
- `Layout Engine`: cuadrículas, listas y composiciones modulares (primero cuadrícula).
- `Theme Engine`: tokens visuales, materiales y estilos (primero Essential automático).
- `Motion Engine`: gestos y animaciones (primero comportamiento básico).
- `Widget Engine`: host de widgets del sistema; widgets propios en fases posteriores.
- `Atelier Studio`: editor visual futuro que orquesta motores sin acoplarlos.

## 9. Estructura de repositorio propuesta

```
orbita-launcher/
  README.md
  LICENSE                 # solo tras confirmar licencia
  docs/
    PRODUCT_VISION.md
    SPEC_ALPHA_0_1.md
    ACCEPTANCE_TESTS.md
    DESIGN_SYSTEM.md
    ROADMAP.md
  .github/workflows/
    android-ci.yml
  app/
  core/
  feature/
```

## 10. Entregables de ChatGPT Work / GitHub

1. Confirmar repositorio y propietario, nombre final del repositorio, visibilidad y licencia.
2. Revisar requisitos SDK mínimos, herramientas y matriz de dispositivos objetivo.
3. Crear proyecto Android compilable con CI; no incluir secretos en el repositorio.
4. Implementar P0 por incrementos pequeños y revisables.
5. Ejecutar compilación, pruebas y publicar APK de **depuración** como artefacto de GitHub Actions; un APK de publicación requerirá firma y configuración adicional.
6. Registrar problemas y criterios de aceptación por issue o checklist.
7. Entregar notas de instalación y limitaciones reales de cada alfa.

## 11. Instrucción de traspaso para Work

«Trabaja sobre el proyecto ÓRBITA Launcher, nombre provisional. Lee `docs/SPEC_ALPHA_0_1.md` y los documentos de visión. Primero verifica acceso a GitHub, repositorio y permisos; no sobrescribas proyectos existentes. Prepara la arquitectura Kotlin/Android y un plan incremental para Alfa 0.1, priorizando una actividad HOME real, cajón de apps, escritorio persistente y CI que compile un APK de pruebas. Mantén identidad visual original, evita clonar launchers existentes, respeta restricciones de Android, no afirmes que una función está lista sin compilarla y probarla. Confirma antes de publicar una licencia o realizar cambios destructivos». 

## 12. Pendientes de decisión

- Propietario y repositorio GitHub: confirmar, no suponer.
- Licencia open source exacta.
- Versión mínima de Android y modelos de prueba.
- Identificador definitivo de aplicación (`applicationId`) para evitar cambios posteriores.
- Política de firma y distribución de APK de lanzamiento.

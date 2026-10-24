# Plan de Mejora y Rediseño - Reiki Sound Therapy

Este documento recopila el análisis técnico y de diseño realizado conjuntamente para llevar la aplicación **Reiki Sound Therapy** al siguiente nivel (nivel 10/10).

---

## 🛠️ Mejoras Técnicas y Arquitecturales

1. **Sonidos Predeterminados (Out-of-the-Box)**
   - Integrar audios por defecto (campana y ondas binaurales) en recursos (`res/raw/`) para que la app funcione inmediatamente al instalarse, sin requerir selección manual obligatoria de archivos.
   - Mantener la capacidad de seleccionar archivos personalizados del dispositivo.

2. **Gestión de Foco de Audio (`AudioManager`)**
   - Implementar `AudioFocusRequest` en `SoundTherapyService` para manejar interrupciones (llamadas telefónicas, notificaciones, otras apps de audio).
   - Añadir `setOnErrorListener` a los reproductores multimedia para evitar bloqueos o cierres inesperados.

3. **Sincronización de Estado y Persistencia**
   - Conectar la interfaz con el ciclo de vida del servicio mediante un `ViewModel` y/o persistencia ligera (DataStore/SharedPreferences) para recordar volúmenes preferidos e intervalos de campana.
   - Reflejar correctamente el estado de reproducción tras cambios de configuración (rotación de pantalla) o minimización de la app.

4. **Temporizador de Sesión**
   - Añadir un contador de tiempo transcurrido y una cuenta atrás visual para el próximo toque de campana.

---

## 🎨 Rediseño Visual y UI/UX (Material 3)

1. **Paleta de Colores Meditativa**
   - Tonos serenos inspirados en cromoterapia y apps de bienestar (lavanda suave, verde salvia, crema cálido y acentos dorados).
   - Soporte completo para Modo Claro y Modo Oscuro.

2. **Estructura de Tarjetas (Cards)**
   - Agrupación modular para Música de Fondo, Ondas Binaurales y Campana.
   - Indicadores claros del archivo activo y controles de volumen minimalistas con porcentajes.

3. **Pantalla de Sesión Activa**
   - Vista inmersiva con animación de pulso suave/respiración cuando la terapia está en curso.
   - Controles de parada rápida accesibles.

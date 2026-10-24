# 🧘‍♀️ Reiki Sound Therapy

**Reiki Sound Therapy** es una aplicación Android moderna, serena y elegante diseñada para la creación de sesiones de armonización Reiki, meditación guiada y relajación profunda mediante la mezcla personalizada de sonidos de la naturaleza, frecuencias armónicas y avisos de atención plena.

---

## ✨ Características Principales

* 🌿 **Entornos Naturales Out-of-the-Box:** Incluye 3 paisajes sonoros naturales predeterminados de alta calidad:
  * 🌲 **Bosque y Aves** (Aire y ambiente natural)
  * 💧 **Agua y Lluvia** (Fluidez y calma)
  * 🔥 **Fuego de Leña** (Calidez)
* 🧠 **Frecuencias Reiki & Solfeggio:** Acompaña tus sesiones con ondas binaurales Theta (1Hz) e integra tus propios archivos de audio de frecuencias Solfeggio o música personalizada.
* 🔔 **Campana de Meditación e Intervalos:** Reproducción periódica de cuencos tibetanos y campanas de atención plena para marcar los cambios de posición de manos en Reiki o lapsos de meditación.
* ⏱️ **Temporizador de Duración de Sesión:** Elige la duración total de tu práctica (15, 30, 45, 60, 90 minutos o modo Infinito). Al finalizar el tiempo, suena un toque final de campana suave y la sesión se detiene automáticamente.
* 🎚️ **Mezclador de Volumen Independiente:** Ajusta el volumen de cada pista por separado para lograr la atmósfera perfecta.
* 🎨 **Diseño Diurno M3 en Píldora (Zen UI):** Interfaz declarativa en Jetpack Compose con paleta de colores serena, tarjetas elevadas en cápsula y botones táctiles limpios y simétricos.
* 📲 **Reproducción en Segundo Plano Estabilizada:** Funciona mediante un `Foreground Service` optimizado para Android 14+ con gestión de **Audio Focus** (`AudioManager`), pausando o atenuando el audio ante llamadas entrantes o interrupciones.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** 100% [Kotlin](https://kotlinlang.org/).
* **UI Declarativa:** [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material Design 3.
* **Audio:** `MediaPlayer` API con gestión avanzada de atributos de audio y foco.
* **Servicio:** Android `Foreground Service` con tipo `mediaPlayback` y soporte para notificaciones de control.
* **Compatibilidad:** Android 8.0 (API 26) hasta Android 14+ (API 34).

---

## 📜 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.

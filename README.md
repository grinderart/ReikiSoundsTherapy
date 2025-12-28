# Reiki Sounds Therapy

Una aplicación Android sencilla y personalizable para crear sesiones de terapia de sonido y meditación. Permite a los usuarios mezclar sus propios archivos de audio (música de fondo, ondas binaurales y una campana periódica) para una experiencia relajante única.

El proyecto fue desarrollado originalmente hace un tiempo y ha sido **completamente modernizado** para seguir las mejores prácticas actuales de desarrollo en Android.

## ✨ Características Principales

*   **Mezcla de Sonido Personalizada:** Selecciona y combina hasta tres pistas de audio desde el almacenamiento de tu dispositivo:
    *   Música de fondo.
    *   Ondas binaurales.
    *   Sonido de campana periódico.
*   **Control de Volumen Independiente:** Ajusta el volumen de cada pista por separado para crear la mezcla perfecta.
*   **Intervalos de Campana Configurables:** Elige cada cuántos minutos quieres que suene la campana para marcar tus sesiones.
*   **Reproducción en Segundo Plano:** La sesión de sonido continúa activa gracias a un `Foreground Service`, incluso si la aplicación está cerrada o la pantalla bloqueada.
*   **Interfaz Moderna:** La interfaz de usuario ha sido migrada de XML a **Jetpack Compose**, ofreciendo un código más limpio, reactivo y fácil de mantener.
*   **Gestión de Permisos Actualizada:** Implementa la solicitud de permisos en tiempo de ejecución para Android 13 y versiones superiores, incluyendo `POST_NOTIFICATIONS`.

## 🛠️ Tecnologías Utilizadas

*   **Lenguaje:** 100% [Kotlin](https://kotlinlang.org/).
*   **Interfaz de Usuario:** [Jetpack Compose](https://developer.android.com/jetpack/compose) para una UI declarativa y moderna.
*   **Audio:** `MediaPlayer` API para la reproducción de las pistas.
*   **Servicio en Segundo Plano:** `Foreground Service` para garantizar la reproducción de audio ininterrumpida.
*   **Gestión de Estado:** Manejo de estado de Compose (`remember`, `mutableStateOf`) para una UI reactiva.

## 📜 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.

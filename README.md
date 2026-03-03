# Ruleta (Android Native)

Aplicación móvil nativa para Android desarrollada en **Kotlin** con **Jetpack Compose**.  
Proyecto de la asignatura *Desarrollo de aplicaciones móviles nativas*.

---

## Descripción

Juego tipo ruleta simplificada (0–36) con saldo de monedas. El jugador selecciona apuestas, define cantidad, gira la ruleta (con animación) y el sistema evalúa ganancias/pérdidas. La partida puede finalizar por retirada o por quedarse sin monedas.

---

## Funcionalidades

### Juego
- Ruleta 0–36 y evaluación de:
  - Rojo / Negro
  - Par / Impar
  - Passe (19–36) / Manque (1–18)
- Apuestas múltiples simultáneas
- Bloqueo de combinaciones incompatibles (p. ej. Rojo + Negro)
- Cantidad de apuesta configurable
- Finalización manual (**Retirarse**) y automática (saldo 0)
- Animación de giro de ruleta

### Persistencia (SQLite)
- Guardado de partidas en SQLite usando APIs nativas:
  - `SQLiteOpenHelper`
  - `ContentValues`
  - `Cursor`
  - Consultas con `query()`
- Historial de partidas
- Top 3 mejores puntuaciones en la pantalla principal

### UI / Navegación
- UI completa en Jetpack Compose
- Navegación con Navigation Compose
- Pantallas con scroll cuando el contenido excede el alto disponible
- Fondo estilo casino en la pantalla de juego

---

## Requisitos

- Android Studio (recomendado: versión reciente)
- **minSdk**: API 30 (Android 11)
- Dispositivo o emulador Android compatible

---

## Cómo ejecutar

1. Clona el repositorio.
2. Abre el proyecto en Android Studio.
3. Sincroniza Gradle.
4. Ejecuta en un emulador o dispositivo.

---

## Mecánica del juego (resumen)

1. El jugador inicia con **3 monedas**.
2. Selecciona apuestas y cantidad.
3. Se gira la ruleta (animación).
4. Se evalúan apuestas y se actualiza el saldo.
5. La partida finaliza al retirarse o al llegar a 0 monedas.
6. Se guarda el resultado en SQLite.

---

## Posibles mejoras futuras

- MVVM completo con ViewModel
- Sonido y efectos multimedia
- Guardado de ubicación (latitud/longitud) para cada partida
- Modo multijugador online

---

## Autor

APKtados



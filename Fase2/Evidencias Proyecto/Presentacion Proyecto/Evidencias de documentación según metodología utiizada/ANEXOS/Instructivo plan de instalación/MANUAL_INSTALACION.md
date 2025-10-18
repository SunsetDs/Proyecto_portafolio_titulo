# Manual de Instalación y Configuración del Entorno de Desarrollo: Kachate

## 1. Introducción

Este documento detalla el procedimiento para la configuración del entorno de desarrollo, compilación y ejecución de la aplicación Android "Kachate". El proyecto está desarrollado en Kotlin, utiliza el sistema de compilación Gradle y se integra con servicios de Firebase.

El cumplimiento de esta guía es fundamental para garantizar una base de desarrollo homogénea y funcional.

## 2. Arquitectura de Software y Dependencias

- **Lenguaje de Programación:** Kotlin
- **Sistema de Compilación:** Gradle
- **Versión de JDK Requerida:** Java 21
- **Versión del SDK de Android (compileSdk):** 34
- **Componentes Principales:**
  - **AndroidX:** Biblioteca de componentes base.
  - **Firebase:** Autenticación (Auth) y base de datos (Firestore).
  - **CameraX:** Interacción con la cámara del dispositivo.
  - **Google ML Kit:** Reconocimiento de texto en imágenes.
  - **Retrofit:** Cliente HTTP para el consumo de APIs REST.
  - **Glide:** Carga y almacenamiento en caché de imágenes.

## 3. Prerrequisitos de Software

La instalación de los siguientes componentes es un requisito mandatorio antes de proceder con la clonación y compilación del proyecto.

| Software                   | Versión Mínima/Requerida | Propósito                                       |
| -------------------------- | ------------------------ | ----------------------------------------------- |
| Git                        | 2.30+                    | Sistema de Control de Versiones Distribuido.    |
| Java Development Kit (JDK) | 21                       | Entorno de ejecución y compilación para Gradle. |
| Android Studio             | Iguana 2023.2.1+         | IDE principal, SDK de Android y emuladores.     |

## 4. Guía de Instalación de Prerrequisitos por Sistema Operativo

A continuación, se describen los pasos de instalación para los entornos soportados.

### 4.1. Windows (10/11)

Se recomienda el uso del gestor de paquetes `winget`, disponible por defecto en versiones modernas de Windows.

1. **Instalar Git:** `winget install -e --id Git.Git`
2. **Instalar JDK 21:** `winget install -e --id Microsoft.OpenJDK.21`
3. **Instalar Android Studio:** Descargar desde el [sitio web oficial](https://developer.android.com/studio).

### 4.2. Arch Linux

Se utilizará el gestor de paquetes `pacman` y un _AUR helper_ (ej. `yay`).

1. **Instalar Git y JDK 21:** `sudo pacman -S git jdk-openjdk`
2. **Instalar Android Studio:** `yay -S android-studio`

## 5. Obtención del Código Fuente

```bash
git clone https://github.com/Bulmak12/Proyecto_KachateApp.git
cd Proyecto_KachateApp/Kachate
```

## 6. Configuración del Entorno de Desarrollo (IDE)

1. **Abrir Android Studio.**
2. Seleccione **"Open"** y navegue hasta el directorio `Kachate` clonado.
3. Permita que la **sincronización de Gradle** finalice.
4. Configure un **Dispositivo Virtual (AVD)** a través de **Tools > Device Manager**.

## 7. Compilación y Ejecución

### 7.1. Desde Android Studio

1. **Seleccione el dispositivo:** Elija su AVD o un dispositivo físico conectado en la barra de herramientas superior.
2. **Ejecute la aplicación:** Haga clic en el botón **"Run 'app'"** (icono de reproducción verde) o use el atajo `Shift + F10`.

### 7.2. Desde la Línea de Comandos

Asegúrese de estar en el directorio raíz del proyecto (`Kachate`).

1. **Generar el APK de depuración:** Si solo desea crear el archivo de paquete sin instalarlo, utilice:

   ```bash
   ./gradlew assembleDebug
   ```

   El APK resultante se encontrará en `app/build/outputs/apk/debug/app-debug.apk`.

## 8. Ejecución de Pruebas

El proyecto contiene dos tipos de pruebas:

- **Unit Tests:** Pruebas lógicas que se ejecutan en la JVM local. Ubicadas en `app/src/test`.
- **Instrumented Tests:** Pruebas que requieren un entorno Android (emulador/dispositivo). Ubicadas en `app/src/androidTest`.

### 8.1. Desde Android Studio

- **Ejecutar todas las pruebas:** Haga clic derecho en el directorio `java` dentro de `app/src/test` o `app/src/androidTest` y seleccione **"Run 'Tests in ...'"**.
- **Ejecutar una clase o método específico:** Haga clic derecho sobre el nombre de la clase o método de prueba y seleccione la opción "Run".

### 8.2. Desde la Línea de Comandos

1. **Ejecutar Unit Tests:**

   ```bash
   ./gradlew test
   ```

   Los resultados se generan en `app/build/reports/tests/testDebugUnitTest/`.

2. **Ejecutar Instrumented Tests:**

   ```bash
   ./gradlew connectedAndroidTest
   ```

   Este comando requiere un emulador o dispositivo conectado. Los resultados se generan en `app/build/reports/androidTests/connected/`.

---

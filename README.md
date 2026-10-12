# 📜 Sistema de Automatización Notarial y Generación Documental

![Java 19+](https://img.shields.io/badge/Java-19%2B-orange.svg)
![JavaFX](https://img.shields.io/badge/UI-JavaFX-blue.svg)
![Apache POI](https://img.shields.io/badge/Docx-Apache%20POI-red.svg)
![License](https://img.shields.io/badge/License-GPLv3-green.svg)

Una solución moderna de escritorio para la gestión y redacción automatizada de instrumentos públicos, contratos y escrituras notariales en formato Word (`.docx`). 

Diseñada para optimizar la labor notarial eliminando errores de transcripción manual mediante el uso de **variables simples**, **bloques lógicos condicionales** y un **motor de procesamiento masivo por lotes vía CSV**.

---

## 🌟 Características Principales

- **Sustitución Dinámica de Variables:** Reemplazo automático de datos de clientes, fechas y valores dentro del documento.
- **Motor de Bloques Condicionales (`#if` / `/if`):** Inclusión o exclusión de cláusulas enteras de forma dinámica sin dejar líneas en blanco ni espacios orfanos en el archivo final.
- **Formularios Dinámicos en JavaFX:** La interfaz gráfica analiza la plantilla Word seleccionada y construye un formulario a medida en tiempo real (casillas de verificación para condiciones y campos de texto para datos).
- **Generación Masiva por Lotes (CSV):** Procesa decenas o cientos de escrituras simultáneas a partir de una planilla CSV.
- **Robustez y Limpieza:** Control de excepciones para ejecuciones concurrentes y parseo flexible de etiquetas con tildes o variaciones de codificación.

---

## 📝 Sintaxis de las Plantillas Word (`.docx`)

Diseña tus plantillas estándar en Microsoft Word o LibreOffice utilizando las siguientes convenciones:

### 1. Variables Simples
Utilizadas para insertar valores de texto directo:
```text
Por la presente, el Sr./Sra. ${COMPRADOR_NOMBRE}, DNI N° ${COMPRADOR_CEDULA}, adquiere el inmueble...

---

## ☕ Apoya el Proyecto

Si esta herramienta te ha sido útil para optimizar tu trabajo notarial o desarrollo, considera apoyar su mantenimiento y evolución con una donación:

* **Cuenta / Alias de donación:** `brea69`
* **Cuenta / Alias de donación:** `sonaja.organo.telar`

¡Muchas gracias por tu colaboración!

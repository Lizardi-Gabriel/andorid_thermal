# Thermal Monitoring - Aplicación Android

Sistema de monitoreo térmico para detección y gestión de eventos de fumadores mediante análisis de imágenes térmicas y monitoreo de calidad del aire.

## Descripción

Aplicación móvil Android que permite a administradores y operadores gestionar eventos de detección térmica, con capacidades de filtrado avanzado, generación de reportes PDF y notificaciones push cuando sucede un evento.

## Características Principales

### Autenticación y Seguridad
- Sistema de login con JWT
- Dos roles de usuario: Administrador y Operador
- Persistencia de sesión mediante DataStore
- Tokens de autenticación seguros

### Dashboard Administrador
- Vista general con estadísticas de eventos
- Filtros avanzados por estatus, operador y rango de fechas
- Carga optimizada con eventos del día por defecto
- Gestión completa de usuarios operadores
- Generación de reportes PDF personalizados

### Dashboard Operador
- Galería de eventos con navegación por fechas
- Botones de navegación rápida (día anterior/siguiente)
- Filtros: Todos, Pendientes, Mi Historial
- Confirmación y descarte de eventos
- Visualización de estadísticas personales

### Gestión de Eventos
- Visualización detallada con imágenes y detecciones
- Navegación entre múltiples imágenes
- Datos de calidad del aire (PM10, PM2.5, PM1.0, AQI)
- Marcado temporal de eventos

### Reportes PDF
- Resumen ejecutivo con estadísticas
- Gráficas de distribución de eventos
- Análisis de calidad del aire vs límites OMS
- Alertas visuales cuando se exceden límites
- Filtrado por rango de fechas personalizado
- Guardado automático en carpeta de Descargas

### Notificaciones Push
- Firebase Cloud Messaging integrado
- Notificaciones en tiempo real
- Navegación directa al detalle del evento
- Gestión de tokens FCM

### Perfil de Usuario
- Información personal completa
- Estadísticas de eventos gestionados (operadores)
- Visualización de rol y permisos
- Foto de perfil

## Requisitos Técnicos

### Requisitos del Sistema
- Android 8.0 (API 26) o superior
- Conexión a Internet
- Permisos de notificaciones

## Configuración

### 1. Clonar el Repositorio
```bash
git clone [URL_DEL_REPOSITORIO]
cd android_thermal
```

### 2. Configurar Firebase
1. Descargar `google-services.json` desde Firebase Console
2. Colocar en `app/google-services.json`
3. Configurar las claves del proyecto en Firebase

### 3. Configurar Backend
Editar `utils/Config.kt`:
```kotlin
private const val BASE_URL = "https://tu-backend.azurewebsites.net/"
```

### 4. Compilar y Ejecutar
1. abrir el proyecto en android studio



## Versión

Versión actual: 1.0.0
Última actualización: Noviembre 2025
# Hotel Colibrí · Sistema de reservas con servicios SOAP y una API REST

Proyecto final de **Arquitectura Orientada a Servicios** (prof. Jesús Leonardo López Hernández).
El cliente solo habla con la API REST; la API guarda los datos en MySQL y consume dos servicios
SOAP heredados (divisas y pagos) a partir de sus WSDL.

## Integrantes

| Nombre | Matrícula | Responsabilidad |
|---|---|---|
| Sergio Antonio Morales Mavil | 200910011 | Líder técnico: repositorio, servicio de divisas y API REST |
| Jaqueline López Álvarez | 202360086 | _(su parte la cubre Sergio)_ |
| Francisco Robles | _pendiente_ | Servicio SOAP de pagos y facturación |
| Magaly Sánchez | _pendiente_ | Documento de arquitectura y evidencias |

## Tecnologías

| Parte | Tecnología |
|---|---|
| Servicios SOAP | Python **3.11**, spyne, lxml (spyne no funciona con 3.12 o superior) |
| Pruebas del SOAP | zeep y Postman |
| API REST | Java 21, Spring Boot, Maven, MySQL; cliente SOAP generado con wsimport |

## Estructura

```
hotel-colibri/
├── soap/
│   ├── divisas_service.py          ← servicio de divisas (puerto 8001)
│   ├── cliente_prueba_divisas.py   ← pruebas de divisas con zeep
│   ├── pagos_service.py            ← servicio de pagos (puerto 8002) · en desarrollo
│   └── requirements.txt
├── api/                            ← proyecto Spring Boot (puerto 8080) · en desarrollo
├── docs/
│   ├── borrador_diseno.md          ← borrador del documento de arquitectura
│   └── evidencias/                 ← capturas de Postman
├── .gitignore
└── README.md
```

## Puertos

| Componente | Puerto |
|---|---|
| Servicio de divisas | 8001 |
| Servicio de pagos | 8002 |
| API REST | 8080 |
| MySQL | 3306 |

## Cómo ejecutar los servicios SOAP (Windows · PowerShell)

1. Crear el entorno virtual con Python 3.11 (solo la primera vez):
   ```powershell
   cd soap
   py -3.11 -m venv venv
   .\venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   ```
2. Arrancar el servicio de divisas (**Terminal 1**, se queda escuchando):
   ```powershell
   python divisas_service.py
   ```
   WSDL: <http://localhost:8001/?wsdl>
3. Probarlo (**Terminal 2**, con el venv activado):
   ```powershell
   python cliente_prueba_divisas.py
   ```
   Resultado esperado: `8 de 8 pruebas correctas`.

_(Pasos del servicio de pagos, de MySQL y de la API: se agregan conforme se terminan.)_

## Estado

- ✅ Servicio de divisas con WSDL, validación lxml y Faults (3 operaciones)
- ⏳ Servicio de pagos y facturación
- ⏳ API REST: habitaciones, reservas (no traslape) y cotización
- ⏳ Documento de arquitectura (borrador para el Avance 1)

## Uso de inteligencia artificial

Se usó Claude (Anthropic) como asesor: para explicar conceptos, organizar el trabajo del equipo y
revisar el código. Cada integrante puede explicar y modificar la parte que le tocó.

# Hotel Colibrí · Sistema de reservas con servicios SOAP y una API REST

Proyecto final de **Arquitectura Orientada a Servicios** (prof. Jesús Leonardo López Hernández).
El cliente solo habla con la API REST; la API guarda los datos en MySQL y consume dos servicios
SOAP heredados (divisas y pagos) a partir de sus WSDL.

## Integrantes

| Nombre | Matrícula | Responsabilidad |
|---|---|---|
| Sergio Antonio Morales Mavil | 200910011 | Líder técnico: repositorio, servicio de divisas y API REST (habitaciones, huéspedes y reservas) |
| Jaqueline López Álvarez | 202360086 | Integración SOAP de la API (cliente wsimport, monedas y cotización), colección de Postman y evidencias de la API |
| Francisco Robles | _pendiente_ | Servicio SOAP de pagos y facturación |
| Magaly Sánchez | 201120142 | Documento de arquitectura y evidencias |

## Tecnologías

| Parte | Tecnología |
|---|---|
| Servicios SOAP | Python **3.11**, spyne, lxml (spyne no funciona con 3.12 o superior) |
| Pruebas del SOAP | zeep y Postman |
| API REST | Java 21, Spring Boot 4.1.3, Maven, Spring Data JPA, Bean Validation |
| Base de datos | MySQL 8.4 |
| Cliente SOAP en la API | JAX-WS 4.0.5 (`jaxws-maven-plugin`, goal `wsimport`, + `jaxws-rt`) |

## Estructura

```
hotel-colibri/
├── soap/
│   ├── divisas_service.py          ← servicio de divisas (puerto 8001)
│   ├── cliente_prueba_divisas.py   ← pruebas de divisas con zeep
│   ├── pagos_service.py            ← servicio de pagos (puerto 8002)
│   └── requirements.txt
├── api/                            ← API REST Spring Boot (puerto 8080)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/api/
│       │   ├── model/       ← entidades JPA y enums (Habitacion, Huesped, Reserva...)
│       │   ├── repository/  ← acceso a MySQL (Spring Data JPA)
│       │   ├── service/     ← reglas de negocio
│       │   ├── controller/  ← endpoints REST
│       │   ├── dto/         ← peticiones y respuestas JSON-LD (schema.org)
│       │   ├── exception/   ← errores en formato ProblemDetail
│       │   ├── config/      ← carga inicial de habitaciones
│       │   └── soap/        ← cliente del servicio de divisas
│       └── resources/
│           ├── application.properties
│           └── wsdl/divisas.wsdl   ← WSDL local para wsimport
├── docs/
│   ├── arquitectura_y_diseno.pdf   ← documento del Avance 1
│   ├── borrador_diseno.md
│   └── evidencias/                 ← capturas de pruebas
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

## Cómo ejecutar (Windows · PowerShell)

### 1. Servicios SOAP

1. Crear el entorno virtual con Python 3.11 (solo la primera vez):
   ```powershell
   cd soap
   py -3.11 -m venv venv
   .\venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   ```
2. Arrancar cada servicio en su propia terminal (con el venv activado):
   ```powershell
   python divisas_service.py
   ```
   ```powershell
   python pagos_service.py
   ```
   WSDL: <http://localhost:8001/?wsdl> y <http://localhost:8002/?wsdl>
3. Probar divisas: `python cliente_prueba_divisas.py` → `8 de 8 pruebas correctas`.

### 2. API REST

Requisitos: Java 21 y MySQL encendido en el puerto 3306 (en Windows: servicio `MySQL84`).
La base `hotel_colibri` y sus tablas se crean solas al arrancar.

1. Definir la contraseña de MySQL **en la misma terminal** donde se arranca la API.
   La contraseña NO está en el repositorio: `application.properties` la lee de la variable `DB_PASSWORD`.
   ```powershell
   cd api
   $env:DB_PASSWORD='tu_contraseña'
   ```
   (Opcional: `$env:DB_USER='usuario'`; si no se define, se usa `root`.)
2. Compilar y arrancar:
   ```powershell
   .\mvnw clean compile
   .\mvnw spring-boot:run
   ```
   Debe aparecer `Started ApiApplication`. La primera vez se registran las habitaciones 101, 102 y 201.
3. La dirección del servicio de divisas se cambia en `api/src/main/resources/application.properties`
   (`divisas.soap.url=http://localhost:8001/`).

## Endpoints de la API (Avance 1)

| Método y URI | Respuestas |
|---|---|
| `GET /api/v1/habitaciones` | 200 |
| `GET /api/v1/habitaciones?entrada=2027-01-15&salida=2027-01-18&huespedes=2` | 200 (solo disponibles) · 400 |
| `GET /api/v1/habitaciones/{numero}` | 200 · 404 |
| `POST /api/v1/habitaciones` | 201 + Location · 400 · 409 |
| `PUT /api/v1/habitaciones/{numero}` | 200 · 400 · 404 |
| `PATCH /api/v1/habitaciones/{numero}` `{"activa": false}` | 200 · 400 · 404 |
| `POST /api/v1/huespedes` | 201 + Location · 400 · 409 |
| `GET /api/v1/huespedes/{id}` | 200 · 404 |
| `POST /api/v1/reservas` | 201 + Location (PENDIENTE) · 400 · 404 · 409 · 422 |
| `GET /api/v1/reservas/{id}` | 200 · 404 |
| `GET /api/v1/divisas` | 200 · 503 |
| `GET /api/v1/reservas/{id}/cotizacion?moneda=USD` | 200 · 400 · 404 · 502 · 503 |

Ejemplo para crear una reserva (el cliente nunca envía el precio):
```json
{ "huesped": 1, "habitacion": "102", "fechaEntrada": "2027-01-15", "fechaSalida": "2027-01-18", "numHuespedes": 2 }
```

Los errores se responden en formato **ProblemDetail** (RFC 9457), sin trazas internas.
Las respuestas usan **JSON-LD con schema.org**: `HotelRoom`, `Person`, `LodgingReservation` y `PriceSpecification`.

## Decisiones técnicas

| Decisión | Por qué | Alternativa descartada |
|---|---|---|
| Contraseña de MySQL en la variable de entorno `DB_PASSWORD` | Nunca se sube al repositorio | Escribirla en `application.properties` |
| Huésped como entidad propia (`POST` y `GET /huespedes`) y `@ManyToOne` desde la reserva | Un huésped tiene varias reservas sin repetir sus datos; se mapea a `Person` de schema.org | Guardar nombre y correo dentro de cada reserva |
| `id` interno y `numero` de habitación separados; la URI usa el número | El número es dato del negocio; la llave de la BD no cambia | Usar el número como llave primaria |
| Total calculado por el servidor (noches × precio) y guardado en la reserva | El cliente no puede alterar el precio; las reservas conservan el precio con que se hicieron | Recalcular el total en cada consulta |
| Regla de traslape con `<` y `>` estrictos, ignorando reservas canceladas | La salida de una reserva puede coincidir con la entrada de otra | `<=` y `>=` (prohibiría ese caso) |
| Bloqueo pesimista de la habitación al reservar (`SELECT ... FOR UPDATE`) | Dos reservas simultáneas de la misma habitación no se enciman | Sin bloqueo (posible doble reserva) |
| Orden de validación: 400 → 404 → 422 → 409 | De lo más barato a lo más costoso, y el cliente recibe el error más útil | Cualquier orden |
| Errores con una clase base `ApiException` y un solo `@RestControllerAdvice` | Un solo lugar para el formato ProblemDetail; controllers sin try/catch | try/catch en cada controller |
| Respuestas en JSON-LD desde el Avance 1 | Coincide con el glosario del documento; evita cambiar el JSON después | Agregarlo en la entrega final |
| Cliente SOAP con wsimport desde un WSDL **local** | Lo pide la rúbrica; la API arranca aunque divisas esté apagado y puede responder 503 | Leer el WSDL de `http://localhost:8001/?wsdl` al arrancar |

## Estado

- ✅ Servicio de divisas con WSDL, validación lxml y Faults (3 operaciones)
- ✅ Servicio de pagos: `autorizarPago` y `consultarPago` · ⏳ `reembolsarPago` y `emitirFactura` (entrega final)
- ✅ API REST: habitaciones (CRUD y carga inicial), huéspedes y reservas con todas las reglas
  (pruebas 2, 3, 4, 5 y 14 del enunciado)
- ⏳ API REST: cliente wsimport, `GET /api/v1/divisas` y cotización (pruebas 1, 6, 7 y 18)
- ✅ Documento de arquitectura del Avance 1 (`docs/arquitectura_y_diseno.pdf`)
- Entrega final (4 dic): pagar, facturar, cancelar con reembolso, llegada, salida, OpenAPI y colección de Postman

## Uso de inteligencia artificial

Se usó Claude (Anthropic) como asesor: para explicar conceptos, organizar el trabajo del equipo y
revisar el código. Cada integrante puede explicar y modificar la parte que le tocó.

# Revisión del proyecto Sabor Gourmet

## Alcance de esta revisión

Esta revisión fue diagnóstica. No se modificó el código fuente, el JSON de datos ni la documentación durante el diagnóstico. Tampoco se hizo commit ni push.

Se compararon:

- El enunciado y los cinco criterios de nivel **Sobresaliente**.
- El código actual.
- Las pruebas automatizadas.
- `data/sabor-gourmet.json`.
- `docs/informe-documentacion-codigo.md`.
- Las comprobaciones manuales informadas desde Postman.

## Resultado ejecutivo

| Criterio | Estado | Evidencia principal | Brecha para Sobresaliente |
|---|---|---|---|
| IL 2.1 Funcionalidad completa | **parcialmente verificado** | CRUD implementado; consultas HTTP reales respondieron; pruebas de solapamiento e invalidación correctas | Falta observar por HTTP una creación válida `201`, modificación y cancelación. También debe aclararse si la disponibilidad debe filtrar por `people`. |
| IL 2.2 Calidad del código | **parcialmente verificado** | Nombres claros, capas separadas y comentarios en español | `overlaps` está duplicado; el manejo de zona horaria no es uniforme; faltan algunas pruebas HTTP. |
| IL 2.3 Implementación CSR | **verificado** | El flujo Controller → Service → Repository está correctamente separado | No se detectaron responsabilidades mal ubicadas. |
| IL 2.4 Integración y uso de datos | **parcialmente verificado** | Referencias por ID, JSON único, pruebas con `@TempDir`; recuperación después del reinicio confirmada manualmente | Falta comprobar una escritura nueva, reinicio y lectura posterior del mismo registro. |
| Calidad del informe y documentación | **parcialmente verificado** | El informe cubre configuración, código, reglas, rutas, JSON y Postman | Hay una descripción incorrecta de `TableRequest.available`; faltan Word/PDF, logo y capturas. |

---

## 1. Funcionalidad completa — IL 2.1

### Clientes

Archivo: `src/main/java/cl/ipss/sabor_gourmet/controller/CustomerController.java`

Métodos:

- `getCustomers()`
- `getCustomer(Long id)`
- `createCustomer(CustomerRequest request)`
- `updateCustomer(Long id, CustomerRequest request)`
- `deleteCustomer(Long id)`

Rutas:

```text
GET    /customers
GET    /customers/{id}
POST   /customers
PUT    /customers/{id}
DELETE /customers/{id}
```

### Mesas

Archivo: `src/main/java/cl/ipss/sabor_gourmet/controller/TableController.java`

Métodos:

- `getTables()`
- `getAvailableTables(String date, String startTime)`
- `getReservedTables(String date, String startTime)`
- `getTable(Long id)`
- `createTable(TableRequest request)`
- `updateTable(Long id, TableRequest request)`
- `deleteTable(Long id)`

Rutas:

```text
GET    /tables
GET    /tables/available
GET    /tables/reserved
GET    /tables/{id}
POST   /tables
PUT    /tables/{id}
DELETE /tables/{id}
```

### Reservas

Archivo: `src/main/java/cl/ipss/sabor_gourmet/controller/ReservationController.java`

Métodos:

- `getCurrentReservations(String date)`
- `getReservation(Long id)`
- `createReservation(ReservationRequest request)`
- `updateReservation(Long id, ReservationRequest request)`
- `cancelReservation(Long id)`

Rutas:

```text
GET    /reservations
GET    /reservations/{id}
POST   /reservations
PUT    /reservations/{id}
DELETE /reservations/{id}
```

### Consultas HTTP observadas

La instancia existente en `localhost:8080` fue consultada sin iniciar otra instancia.

| Solicitud | Resultado observado |
|---|---|
| `GET /tables/available` | `200 OK`; excluyó la mesa 2. |
| `GET /tables/reserved` | `200 OK`; devolvió la mesa 2 una sola vez. |
| `GET /tables/available?date=2026-11-16` | `200 OK`; la mesa 1 quedó disponible y la mesa 2 quedó excluida. |
| `GET /tables/reserved?date=2026-11-20` | `200 OK`; devolvió la mesa 1. |
| `GET /tables/available?startTime=2026-11-16T20:00` | `200 OK`; excluyó la mesa 2. |
| `GET /reservations` | `200 OK`; devolvió las dos reservas confirmadas cuyo término aún no había pasado. |
| `GET /reservations?date=2026-11-20` | `200 OK`; devolvió la reserva de Ana. |
| `GET /tables/available?date=2026-11-16&startTime=2026-11-16T20:00` | `400 Bad Request`. |

Error observado para la combinación de parámetros:

```json
{
  "message": "date and startTime cannot be used together"
}
```

### Superposición de horarios

Solicitud probada:

```http
POST http://localhost:8080/reservations
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "tableId": 2,
  "startTime": "2026-11-16T20:30",
  "people": 2
}
```

Resultado observado:

```http
409 Conflict
```

```json
{
  "message": "Table is already reserved in that time range: 2"
}
```

La operación falló antes de escribir el archivo JSON.

### Datos inválidos

Solicitud probada:

```http
POST http://localhost:8080/reservations
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "tableId": 3,
  "startTime": "2026-12-01T18:00",
  "people": 0
}
```

Resultado observado:

```http
400 Bad Request
```

```json
{
  "message": "Request validation failed"
}
```

### Creación válida

La creación válida está cubierta en:

`src/test/java/cl/ipss/sabor_gourmet/ReservationServiceTest.java`

Método:

```text
rejectsOverlapAndPersistsCancellationWithoutLosingCollections
```

La prueba crea una reserva válida en un JSON temporal y confirma que se persiste. No se observó por HTTP el estado `201 Created` en esta revisión porque no se envió una creación válida al JSON de demostración activo.

Estados esperados según el código:

| Operación | Estado esperado |
|---|---|
| Consultas | `200 OK` |
| Crear reserva | `201 Created` |
| Modificar reserva | `200 OK` |
| Cancelar reserva | `200 OK` con estado `CANCELLED` |
| Actualizar mesa | `200 OK` |
| Cuerpo inválido | `400 Bad Request` |
| Fecha inválida o `date` + `startTime` | `400 Bad Request` |
| ID inexistente | `404 Not Found` |
| Solapamiento, capacidad o límite de mesas | `409 Conflict` |

### Brecha posible: cantidad de personas

El enunciado original mencionaba consultar disponibilidad por horario y cantidad de personas. El código actual de:

```text
TableController.getAvailableTables(String date, String startTime)
TableService.getAvailableTables(String date, String startTime)
```

no recibe `people`.

Se probó:

```http
GET http://localhost:8080/tables/available?startTime=2026-11-16T20:00&people=10
```

La respuesta fue `200 OK`, pero incluyó mesas con capacidad menor que 10, como la mesa 3 con capacidad 2.

Esto ocurre porque Spring ignora ese parámetro no declarado.

Si la rúbrica final exige filtrar disponibilidad por cantidad de personas, esta funcionalidad está incompleta. Si la decisión posterior fue eliminar `people` del contrato, debe quedar explícito en `AGENTS.md` y en el informe.

---

## 2. Calidad del código — IL 2.2

### Aspectos correctos

Los nombres de clases y métodos son claros y están en inglés:

- `ReservationController`
- `ReservationService`
- `RestaurantJsonRepository`
- `getAvailableTables`
- `validateTableCapacity`
- `validateOverlap`
- `cancelReservation`

La organización permite seguir el flujo mediante:

```text
controller/
service/
repository/
model/
exception/
```

Los comentarios en español explican las reglas principales en:

- `TableService.getAvailableTables`
- `TableService.getReservedTables`
- `TableService.overlaps`
- `ReservationService.validateOverlap`
- `ReservationService.cancelReservation`
- `RestaurantJsonRepository.update`
- `RestaurantJsonRepository.write`
- `TimeRange.resolve`

### Duplicaciones relevantes

#### Lógica `overlaps` duplicada

Existe una versión en:

```text
src/main/java/cl/ipss/sabor_gourmet/service/TableService.java
TableService.overlaps(...)
```

y otra en:

```text
src/main/java/cl/ipss/sabor_gourmet/service/ReservationService.java
ReservationService.overlaps(...)
```

Ambas aplican la misma fórmula de intervalos.

Efecto: si se cambia la duración o el límite de intervalos en un método y no en el otro, la disponibilidad podría aceptar una reserva que el Service rechaza, o al revés.

#### Constantes de estado repetidas

`CONFIRMED` y `CANCELLED` están declaradas por separado en los servicios.

#### Métodos auxiliares repetidos

La búsqueda de índices y generación de IDs se repite en:

- `CustomerService.indexOf`
- `TableService.indexOf`
- `ReservationService.indexOf`
- `nextId` de los tres servicios

No rompe el funcionamiento actual, pero aumenta el mantenimiento.

### Zona horaria inconsistente

Las consultas de mesas usan explícitamente:

```text
TimeRange.SANTIAGO_ZONE
ZoneId.of("America/Santiago")
```

en `TimeRange.resolve(...)`.

Pero las validaciones de hora actual usan `LocalDateTime.now()` en:

- `ReservationService.validateStartTime`
- `ReservationService.getCurrentReservations`
- `TableService.validateActiveReservationsCapacity`

Si la aplicación se ejecuta en una máquina con otra zona horaria, el criterio de “hora actual” podría diferir del criterio usado para consultar el día.

### Validaciones no implementadas

No se observó validación de:

- Números de mesa duplicados.
- Correos de clientes duplicados.
- Consistencia entre el `number` y el `id` de una mesa.

No necesariamente son requisitos de la rúbrica, pero deben considerarse si el informe afirma que la configuración está completamente validada.

---

## 3. Implementación CSR — IL 2.3

**Estado: verificado.**

### Controller

Archivo:

`src/main/java/cl/ipss/sabor_gourmet/controller/ReservationController.java`

Método:

```text
createReservation(ReservationRequest request)
```

Responsabilidades:

- Recibir `POST /reservations`.
- Deserializar el cuerpo JSON.
- Ejecutar `@Valid`.
- Delegar al Service.
- Construir `ResponseEntity`.
- Devolver `201 Created`.

### Service

Archivo:

`src/main/java/cl/ipss/sabor_gourmet/service/ReservationService.java`

Método:

```text
createReservation(ReservationRequest request)
```

Invoca:

```text
validateStartTime(request)
validateReferences(...)
validateTableCapacity(...)
validateOverlap(...)
```

Después construye una reserva con:

```java
new Reservation(
    nextId(...),
    request.customerId(),
    request.tableId(),
    range.start().toString(),
    request.people(),
    CONFIRMED
)
```

La reserva guarda solamente los IDs, no copia el cliente ni la mesa.

### Repository

Archivo:

`src/main/java/cl/ipss/sabor_gourmet/repository/RestaurantJsonRepository.java`

Métodos:

```text
update(Function<RestaurantData, T> operation)
read()
write(RestaurantData data)
```

Flujo real:

```text
ReservationController.createReservation
    → ReservationService.createReservation
        → RestaurantJsonRepository.update
            → read()
            → valida y modifica RestaurantData
            → write(data)
        → devuelve Reservation
    → ResponseEntity.created(...).body(reservation)
```

No se detectaron responsabilidades mal ubicadas:

- HTTP y `ResponseEntity`: Controller.
- Reglas de negocio: Service.
- Acceso al JSON: Repository.
- Forma de datos: Model.
- Conversión de errores: `ApiExceptionHandler`.

---

## 4. Integración y uso de datos — IL 2.4

### CRUD existente

| Recurso | Crear | Consultar | Actualizar | Eliminar o cancelar |
|---|---|---|---|---|
| Clientes | `POST /customers` | `GET /customers`, `GET /customers/{id}` | `PUT /customers/{id}` | `DELETE /customers/{id}` |
| Mesas | `POST /tables` | `GET /tables`, `/available`, `/reserved`, `/{id}` | `PUT /tables/{id}` | `DELETE /tables/{id}` |
| Reservas | `POST /reservations` | `GET /reservations`, `GET /reservations/{id}` | `PUT /reservations/{id}` | `DELETE /reservations/{id}` cambia a `CANCELLED` |

### Referencias

Archivo:

`src/main/java/cl/ipss/sabor_gourmet/model/Reservation.java`

La reserva contiene:

```java
Long customerId;
Long tableId;
```

No contiene objetos completos `Customer` ni `Table`.

El JSON actual confirma el diseño:

```json
{
  "id": 1,
  "customerId": 1,
  "tableId": 1,
  "startTime": "2026-11-20T20:00",
  "people": 2,
  "status": "CONFIRMED"
}
```

No se duplican los datos completos del cliente ni de la mesa.

### Persistencia

Archivo:

`data/sabor-gourmet.json`

Repositorio:

`RestaurantJsonRepository.read()`  
`RestaurantJsonRepository.update(...)`  
`RestaurantJsonRepository.write(...)`

Las pruebas automatizadas que escriben usan archivos temporales mediante `@TempDir`:

- `TableServiceTest`
- `ReservationServiceTest`

Esto evita modificar el JSON principal durante esas pruebas.

### Reinicio comprobado manualmente

Informaste que consultaste una reserva antes y después de reiniciar Spring Boot.

Resultado informado:

```http
200 OK
```

La API recuperó el mismo ID y los mismos datos.

Esta evidencia demuestra que el archivo se lee correctamente después del reinicio.

### Límite de la evidencia

La comprobación corresponde a una reserva ya existente. No demuestra todavía el flujo completo:

```text
crear o modificar
→ detener Spring Boot
→ iniciar Spring Boot
→ consultar el mismo ID
```

Por eso el criterio queda **parcialmente verificado**.

### Riesgo de escritura

`RestaurantJsonRepository.write` usa `TRUNCATE_EXISTING` y reemplaza directamente el archivo. Si el proceso se interrumpe durante la escritura, el JSON podría quedar incompleto. No fue observado durante esta revisión, pero es un riesgo de robustez.

---

## 5. Calidad del informe y documentación del código

Archivo revisado:

`docs/informe-documentacion-codigo.md`

### Cobertura correcta

El informe incluye:

- Portada editable.
- Espacio para logo IPSS.
- Índice.
- Introducción.
- Tecnologías verificadas desde `pom.xml`.
- Java 21.
- Spring Boot 4.1.2.
- Spring MVC.
- Validation.
- JSON/Jackson.
- Maven Wrapper 3.10.1.
- VS Code, Postman y Warp.
- Arquitectura CSR.
- Modelos.
- Controllers.
- Services.
- Repository.
- Excepciones.
- Archivo JSON.
- Pruebas existentes.
- Fragmentos de creación, validación, disponibilidad, cancelación y persistencia.
- Ejecución en Windows.
- Pruebas Postman.
- Estados HTTP.
- Espacios para capturas.
- Lista de pendientes.

### Discrepancia detectada

En la sección **5.2**, el informe describe `TableRequest` como si `@Min` se aplicara también a `available`.

Archivo real:

`src/main/java/cl/ipss/sabor_gourmet/model/TableRequest.java`

La definición real es:

```java
@NotNull Integer number
@Min(1) Integer number

@NotNull Integer capacity
@Min(1) Integer capacity

@NotNull Boolean available
```

`available` tiene `@NotNull`, pero no tiene `@Min`.

Esto debe corregirse antes de entregar el informe.

### Pendientes de entrega

El Markdown todavía no es el documento final:

- No existe todavía Word.
- No existe todavía PDF.
- El logo sigue siendo un marcador.
- Los datos de estudiante, docente, sección y fecha siguen editables.
- Faltan capturas reales de Postman.
- Debe aplicarse Arial 12.
- Debe aplicarse interlineado 1,15.
- Debe agregarse numeración de páginas.
- Deben revisarse saltos de página y tablas.

---

## 6. Pruebas ejecutadas por el asistente

### Suite Maven

La primera invocación literal falló por el intérprete de la herramienta, no por el proyecto:

```text
.mvnw.cmd no se reconoce
```

La orden se ejecutó correctamente mediante PowerShell, ejecutando el Wrapper Windows:

```powershell
& '.\mvnw.cmd' test
```

Resultado:

```text
Tests run: 12
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Distribución observada:

| Clase | Pruebas |
|---|---:|
| `ReservationServiceTest` | 3 |
| `SaborGourmetApplicationTests` | 1 |
| `TableControllerTest` | 4 |
| `TableServiceTest` | 4 |
| Total | 12 |

### HTTP sin escritura

Se ejecutaron consultas `GET` contra la instancia existente en `localhost:8080` y dos `POST` que fallaron antes de escribir:

- Disponibilidad actual: `200`.
- Mesas reservadas actuales: `200`.
- Disponibilidad por fecha: `200`.
- Mesas reservadas del 20 de noviembre: `200`.
- Disponibilidad por turno: `200`.
- Reservas por fecha: `200`.
- `date` y `startTime` juntos: `400`.
- Cuerpo inválido con `people: 0`: `400`.
- Superposición con la mesa 2: `409`.

No se envió una reserva válida al JSON principal.

---

## 7. Comprobaciones manuales informadas por la estudiante

Informaste que comprobaste en Postman:

1. Mesas disponibles del día en curso.
2. Mesas reservadas del día en curso.
3. Consultas de mesas por fecha.
4. Una reserva antes del reinicio de Spring Boot.
5. La misma reserva después del reinicio.
6. `200 OK` después del reinicio.
7. Mismo ID y mismos datos recuperados.

No se atribuyen a esta revisión pruebas manuales de creación, modificación o cancelación porque no fueron informadas como ejecutadas.

---

## 8. Correcciones y pruebas pendientes priorizadas

### Prioridad alta 1 — Confirmar el requisito `people`

Si la rúbrica exige capacidad en la disponibilidad, debe agregarse y probarse el filtro.

Prueba:

```http
GET http://localhost:8080/tables/available?startTime=2026-11-16T20:00&people=10
```

Resultado actual observado: `200 OK`, pero devuelve mesas con capacidad menor que 10.

Resultado esperado si el requisito sigue vigente:

- Solo mesas con `capacity >= 10`.
- La mesa 2 debe seguir excluida por estar reservada.

### Prioridad alta 2 — Flujo completo en una copia aislada

No usar el JSON principal. Copiar el proyecto a una carpeta de prueba y ejecutar la aplicación desde esa copia con otro puerto, por ejemplo `8081`.

#### Crear reserva válida

```http
POST http://localhost:8081/reservations
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "tableId": 3,
  "startTime": "2027-01-10T18:00",
  "people": 2
}
```

Esperado: `201 Created`. Guardar el ID devuelto.

#### Superposición

```http
POST http://localhost:8081/reservations
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "tableId": 3,
  "startTime": "2027-01-10T19:00",
  "people": 2
}
```

Esperado: `409 Conflict`.

#### Datos inválidos

```http
POST http://localhost:8081/reservations
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "tableId": 3,
  "startTime": "2027-01-10T18:00",
  "people": 0
}
```

Esperado: `400 Bad Request`.

#### Modificar

Usar el ID devuelto por la creación:

```http
PUT http://localhost:8081/reservations/{id}
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "tableId": 4,
  "startTime": "2027-01-10T18:30",
  "people": 2
}
```

Esperado: `200 OK` y el mismo ID.

#### Cancelar

```http
DELETE http://localhost:8081/reservations/{id}
```

Esperado: `200 OK` y `status: "CANCELLED"`.

#### Reiniciar y consultar

Después de detener y reiniciar la copia aislada:

```http
GET http://localhost:8081/reservations/{id}
```

Esperado: `200 OK`, mismo ID y `status: "CANCELLED"`.

### Prioridad media 3 — Actualizar una mesa

```http
PUT http://localhost:8081/tables/3
Content-Type: application/json
```

```json
{
  "number": 3,
  "capacity": 3,
  "available": true
}
```

Esperado: `200 OK`.

Después del reinicio:

```http
GET http://localhost:8081/tables/3
```

Debe conservar `capacity: 3`.

### Prioridad media 4 — Probar recursos inexistentes

```http
GET http://localhost:8081/reservations/9999
```

Esperado: `404 Not Found`.

```http
GET http://localhost:8081/tables/9999
```

Esperado: `404 Not Found`.

### Prioridad media 5 — Completar el informe

1. Corregir la descripción de `TableRequest.available`.
2. Confirmar si `/tables/available` soporta `people`.
3. Insertar el logo IPSS.
4. Completar los datos de portada.
5. Agregar capturas de Postman.
6. Exportar a Word.
7. Revisar Arial 12 e interlineado 1,15.
8. Exportar la versión final a PDF.

## Conclusión

La implementación CSR y la persistencia básica están bien encaminadas. La suite pasa completa y las correcciones de `people`, superposición y zona horaria ya están verificadas por pruebas automatizadas y por un flujo HTTP aislado. Para alcanzar el nivel **Sobresaliente** aún falta reunir la evidencia de Postman de las operaciones de escritura, completar la verificación de `TableRequest.available` y preparar la entrega en Word/PDF.

## Actualización posterior a la auditoría

La corrección implementó el filtro opcional `people` en `GET /tables/available`. El Service valida `people >= 1` y aplica `capacity >= people` en las consultas del día actual, por fecha y por turno. La superposición quedó centralizada en `TimeRange.overlaps`, con intervalos semiabiertos: 20:00–21:30 se cruza con un inicio a las 21:00 y permite otro a las 21:30. La edición excluye la reserva que está modificando.

Las consultas y validaciones de hora actual usan `America/Santiago`. Las pruebas automatizadas cubren una reserva que cruza medianoche y confirman que aparece en las consultas de ambos días. No se agregó unicidad para `Customer.email` ni `Table.number` porque el enunciado no la define; los IDs se siguen generando después del mayor ID guardado.

`mvnw.cmd test` terminó con 20 pruebas, 0 fallos, 0 errores y 0 omitidas. En una copia aislada del JSON, el flujo HTTP observó `201` al crear, `200` al modificar, `200` al recuperar después del reinicio y `200` al cancelar. También se observó `400` para `people=0` y para `date` combinado con `startTime`. La copia temporal se eliminó y los datos de demostración no cambiaron.

## Checklist de cierre hacia el 100 %

- [x] Revisar el contrato de `people` en Controller, Service y pruebas.
- [x] Aplicar el filtro `capacity >= people` en hoy, `date` y `startTime`.
- [x] Reutilizar una única regla de superposición en consultas, creación y modificación.
- [x] Excluir la reserva actual al modificarla.
- [x] Verificar turnos consecutivos y reservas que cruzan medianoche.
- [x] Usar `America/Santiago` para día actual y comparaciones con la hora actual.
- [x] Ejecutar `.\mvnw.cmd test`: 20 pasaron, 0 fallaron, 0 se omitieron.
- [x] Ejecutar el flujo de persistencia HTTP en una copia aislada y reiniciar la instancia.
- [ ] Repetir en Postman el flujo de crear, modificar, cancelar y consultar una reserva en la instancia de entrega; guardar capturas.
- [ ] Probar en Postman `available: true`, `available: false`, campo omitido y `available: null` al crear y actualizar mesas.
- [ ] Registrar en Postman un `404`, un `409` por superposición y un `400` por datos inválidos si la rúbrica exige cada estado.
- [ ] Completar portada, logo IPSS, Arial 12, interlineado 1,15 y exportar Word/PDF.

Comentario útil: para que la evidencia sea reproducible, usa una fecha futura de prueba, guarda el ID devuelto por `POST /reservations`, reinicia la aplicación antes del `GET` por ID y captura cada respuesta con su código HTTP.

# Auditoría actual de Sabor Gourmet

## Alcance y método

Esta revisión compara el código actual, las pruebas, `AGENTS.md`, `MEMORY.md`, la auditoría anterior (`docs/Revision-proyecto.md`) y `docs/informe-documentacion-codigo.md` con los cinco criterios de nivel **Sobresaliente**.

No se modificó el código ni los documentos existentes. Solo se creó este archivo. No se hizo commit ni push.

Se ejecutaron:

```powershell
.\mvnw.cmd test
```

También se levantó una copia aislada de la aplicación en el puerto `8081`, usando una copia temporal de `data/sabor-gourmet.json`. La copia se eliminó al terminar y el JSON de demostración no se modificó.

## Resultado ejecutivo

| Criterio | Estado | Evidencia | Brecha para llegar a Sobresaliente |
|---|---|---|---|
| **Funcionalidad completa (IL 2.1)** | **parcialmente verificado** | Se comprobó por HTTP aislado crear, consultar, modificar y cancelar una reserva; actualizar una mesa; consultar disponibilidad hoy, por fecha y por turno; usar `people`; recibir `400` por datos inválidos y `409` por superposición. | Falta repetir y capturar el flujo completo en Postman sobre la instancia de entrega. No se ejecutó en esta pasada por HTTP el listado `GET /reservations` sin fecha ni `GET /reservations?date=...`; está cubierto por código y pruebas anteriores. |
| **Calidad del código (IL 2.2)** | **verificado** | Nombres claros, capas separadas, comentarios en español, una única comparación en `TimeRange.overlaps` y uso de `America/Santiago` en las comparaciones con el momento actual. | La unicidad de `Customer.email` y `Table.number` no está definida por el enunciado; el comportamiento está documentado, pero requiere confirmación si la rúbrica exige una regla adicional. |
| **Implementación de CSR (IL 2.3)** | **verificado** | `ReservationController` recibe HTTP, `ReservationService` valida las reglas y `RestaurantJsonRepository` lee y escribe el documento completo. | No se detectó una responsabilidad ubicada en una capa incorrecta. |
| **Integración y uso de datos (IL 2.4)** | **parcialmente verificado** | Se conservaron referencias por `customerId` y `tableId`; la reserva no duplica clientes ni mesas; una reserva creada y modificada sobrevivió un reinicio en la copia aislada. | Faltan capturas de Postman y una comprobación HTTP específica de todas las operaciones CRUD de clientes y eliminaciones. En la prueba aislada se verificó escritura, modificación, reinicio, lectura y cancelación de una reserva. |
| **Calidad del informe y documentación** | **parcialmente verificado** | El informe explica configuración, modelos, controladores, servicios, repositorio, JSON, reglas de disponibilidad, `people`, `available` y pruebas Postman. | La tabla de pruebas del informe todavía no enumera todos los métodos de prueba agregados recientemente. Faltan completar portada, logo IPSS, capturas y exportación final a Word/PDF con Arial 12 e interlineado 1,15. |

## 1. Funcionalidad completa — IL 2.1

### 1.1 HTTP ejecutado en copia aislada

La prueba usó una copia de `data/sabor-gourmet.json` y el puerto `8081`. Los estados observados fueron:

| Solicitud | Estado esperado | Estado observado | Resultado relevante |
|---|---:|---:|---|
| `GET /tables/available` | `200` | `200` | Devolvió las mesas libres del día actual: `[1,3,4,5,6,7,8,9,10]`. |
| `GET /tables/reserved` | `200` | `200` | Devolvió una sola vez la mesa `[2]`. |
| `GET /tables/reserved?date=2026-11-20` | `200` | `200` | Devolvió la mesa `[1]`, donde está la reserva de Ana. |
| `GET /tables/available?date=2026-11-16` | `200` | `200` | La mesa 2 quedó fuera por su reserva confirmada. |
| `GET /tables/available?startTime=2026-11-16T20:00` | `200` | `200` | La mesa 2 quedó fuera del turno. |
| `GET /tables/available?date=2027-01-10&people=4` | `200` | `200` | La mesa 3, capacidad 2, quedó fuera; la mesa 4, capacidad 4, apareció. |
| `GET /tables/available?startTime=2027-01-10T18:00&people=4` | `200` | `200` | Aplicó el mismo filtro para el turno. |
| `GET /tables/available?people=0` | `400` | `400` | Mensaje: `people must be greater than or equal to 1`. |
| `GET /tables/available?date=2027-01-10&startTime=2027-01-10T18:00` | `400` | `400` | Rechazó la combinación ambigua de parámetros. |
| `PUT /tables/3` | `200` | `200` | Actualizó capacidad a 3 y mantuvo la mesa habilitada. |
| `GET /tables/3` | `200` | `200` | Recuperó la configuración actualizada. |
| `POST /reservations` válido | `201` | `201` | Creó la reserva `id=3`. |
| `GET /reservations/3` | `200` | `200` | Recuperó la reserva creada. |
| `POST /reservations` superpuesta | `409` | `409` | Rechazó el cruce en la mesa 3. |
| `POST /reservations` con `people=0` | `400` | `400` | Rechazó el cuerpo por validación. |
| `PUT /reservations/3` | `200` | `200` | Movió la reserva a la mesa 4 y a las 18:30. |
| `GET /reservations/3` después de reiniciar | `200` | `200` | Conservó el ID 3 y los datos modificados. |
| `DELETE /reservations/3` | `200` | `200` | Cambió el estado a `CANCELLED`. |
| `GET /tables/available?startTime=2027-01-10T18:30&people=4` | `200` | `200` | La mesa 4 volvió a aparecer disponible después de cancelar. |

### 1.2 Código que respalda la funcionalidad

- `src/main/java/cl/ipss/sabor_gourmet/controller/TableController.java:42-47` recibe `date`, `startTime` y `people`.
- `src/main/java/cl/ipss/sabor_gourmet/service/TableService.java:45-61` valida `people`, filtra capacidad, habilitación y reservas confirmadas.
- `src/main/java/cl/ipss/sabor_gourmet/service/TableService.java:66-76` lista mesas reservadas una sola vez y excluye canceladas.
- `src/main/java/cl/ipss/sabor_gourmet/controller/ReservationController.java:35-66` expone consulta, creación, modificación y cancelación.
- `src/main/java/cl/ipss/sabor_gourmet/service/ReservationService.java:64-110` implementa creación y modificación.
- `src/main/java/cl/ipss/sabor_gourmet/service/ReservationService.java:114-133` cancela conservando el registro.

### 1.3 Consultas de reservas no repetidas en esta pasada

El código sí contempla:

```http
GET /reservations
GET /reservations?date=YYYY-MM-DD
```

`ReservationService.getReservations(String date)` usa `TimeRange.fromDate(date)` cuando se envía fecha y devuelve reservas `CONFIRMED` que se cruzan con ese día. Sin fecha delega en `getCurrentReservations()`, que conserva reservas confirmadas cuyo término calculado aún no pasó.

Estas dos solicitudes no fueron repetidas en la ejecución HTTP aislada de esta auditoría. El informe anterior y las pruebas del proyecto las habían cubierto. Para cerrar la evidencia manual, se deben ejecutar en Postman.

## 2. Calidad del código — IL 2.2

### 2.1 Claridad y organización

La organización sigue una convención simple:

- `controller`: recibe solicitudes y construye `ResponseEntity`.
- `service`: concentra validaciones y reglas de negocio.
- `repository`: mantiene el acceso al JSON.
- `model`: separa entidades persistidas de solicitudes de entrada.
- `exception`: traduce errores de negocio a estados HTTP.

Los nombres son consistentes y expresan la intención: `getAvailableTables`, `validateTableCapacity`, `validateOverlap`, `updateReservation` y `cancelReservation`.

### 2.2 Regla única de superposición

La regla está en:

```text
src/main/java/cl/ipss/sabor_gourmet/service/TimeRange.java
Método: overlaps(TimeRange other)
```

Implementación observada:

```java
return start.isBefore(other.end) && end.isAfter(other.start);
```

La usan:

- `TableService.getAvailableTables`.
- `TableService.getReservedTables`.
- `ReservationService.validateOverlap`.
- `ReservationService.getReservations`.

La prueba `TimeRangeTest.detectsOverlapAndAllowsExactBoundary` verifica que un intervalo de 20:00 a 21:30 se cruce con 21:00 y no se cruce con otro que comienza exactamente a 21:30.

### 2.3 Zona horaria

`TimeRange.currentDate()` y `TimeRange.currentDateTime()` usan:

```java
ZoneId.of("America/Santiago")
```

Se usan para:

- Resolver el día actual sin parámetros.
- Validar que una reserva no comience en el pasado.
- Determinar si una reserva sin filtro de fecha sigue vigente.
- Validar si una reserva activa permite reducir la capacidad de una mesa.

La prueba `TimeRangeTest.resolvesCurrentDateAndTimeInSantiago` lo verifica directamente.

### 2.4 Unicidad

El proyecto garantiza IDs no reutilizados mediante los métodos `nextId` de `CustomerService`, `TableService` y `ReservationService`.

No existe una regla implementada para hacer únicos:

- `Customer.email`.
- `Table.number`.

Esto coincide con la decisión documentada en `AGENTS.md`, `MEMORY.md` y el informe técnico: el enunciado no define esos campos como únicos. Si la rúbrica o el docente exige esa regla, debe definirse antes de modificar el código.

### 2.5 Comentarios

Los comentarios en español explican las decisiones importantes sin comentar cada línea. Los puntos mejor cubiertos son:

- Capacidad y disponibilidad en `TableService`.
- Cancelación y exclusión de la reserva propia en `ReservationService`.
- Relaciones por ID en `ReservationService.validateReferences`.
- Escritura completa del documento en `RestaurantJsonRepository.update` y `write`.
- Duración fija y zona horaria en `TimeRange`.

## 3. Implementación Controller–Service–Repository — IL 2.3

### 3.1 Recorrido de una creación de reserva

Solicitud:

```http
POST http://localhost:8080/reservations
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

#### Controller

Archivo:

```text
src/main/java/cl/ipss/sabor_gourmet/controller/ReservationController.java
```

Método:

```text
createReservation(ReservationRequest request)
```

Responsabilidades:

1. Recibir el JSON con `@RequestBody`.
2. Ejecutar validación básica con `@Valid`.
3. Delegar a `reservationService.createReservation(request)`.
4. Devolver `ResponseEntity.created(...)` con `201 Created` y la reserva creada.

#### Service

Archivo:

```text
src/main/java/cl/ipss/sabor_gourmet/service/ReservationService.java
```

Método:

```text
createReservation(ReservationRequest request)
```

Reglas aplicadas:

1. `validateStartTime` analiza `startTime`, calcula 120 minutos y rechaza una hora pasada.
2. `validateReferences` confirma que existan `customerId` y `tableId`.
3. `validateTableCapacity` confirma que la mesa esté habilitada y tenga capacidad suficiente.
4. `validateOverlap` compara solo reservas `CONFIRMED` de la misma mesa.
5. Se crea una reserva con el nuevo ID, los dos IDs de referencia, `startTime`, `people` y estado `CONFIRMED`.

#### Repository

Archivo:

```text
src/main/java/cl/ipss/sabor_gourmet/repository/RestaurantJsonRepository.java
```

Método:

```text
update(Function<RestaurantData, T> operation)
```

Recorrido:

1. `read()` lee `data/sabor-gourmet.json` completo.
2. El Service modifica la colección `reservations` dentro de `RestaurantData`.
3. `write(data)` serializa nuevamente clientes, mesas y reservas.
4. `TRUNCATE_EXISTING` reemplaza el contenido del archivo con el documento actualizado.

La respuesta vuelve desde el Repository al Service, del Service al Controller y finalmente a Postman como JSON con `201 Created`.

## 4. Integración y uso de datos — IL 2.4

### 4.1 Operaciones CRUD disponibles

#### Clientes

Archivo Controller: `src/main/java/cl/ipss/sabor_gourmet/controller/CustomerController.java`

| Operación | Método y ruta | Método Controller |
|---|---|---|
| Listar | `GET /customers` | `getCustomers()` |
| Consultar | `GET /customers/{id}` | `getCustomer(Long id)` |
| Crear | `POST /customers` | `createCustomer(CustomerRequest request)` |
| Actualizar | `PUT /customers/{id}` | `updateCustomer(Long id, CustomerRequest request)` |
| Eliminar | `DELETE /customers/{id}` | `deleteCustomer(Long id)` |

`CustomerService.deleteCustomer` responde conflicto si alguna reserva conserva el `customerId`.

#### Mesas

Archivo Controller: `src/main/java/cl/ipss/sabor_gourmet/controller/TableController.java`

| Operación | Método y ruta | Método Controller |
|---|---|---|
| Listar | `GET /tables` | `getTables()` |
| Disponibles | `GET /tables/available` | `getAvailableTables(...)` |
| Reservadas | `GET /tables/reserved` | `getReservedTables(...)` |
| Consultar | `GET /tables/{id}` | `getTable(Long id)` |
| Crear | `POST /tables` | `createTable(TableRequest request)` |
| Actualizar | `PUT /tables/{id}` | `updateTable(Long id, TableRequest request)` |
| Eliminar | `DELETE /tables/{id}` | `deleteTable(Long id)` |

`TableService.createTable` impide una undécima mesa. `TableService.deleteTable` impide eliminar una mesa referenciada.

#### Reservas

Archivo Controller: `src/main/java/cl/ipss/sabor_gourmet/controller/ReservationController.java`

| Operación | Método y ruta | Método Controller |
|---|---|---|
| Listar vigentes | `GET /reservations` | `getCurrentReservations(String date)` |
| Listar por fecha | `GET /reservations?date=YYYY-MM-DD` | `getCurrentReservations(String date)` |
| Consultar | `GET /reservations/{id}` | `getReservation(Long id)` |
| Crear | `POST /reservations` | `createReservation(ReservationRequest request)` |
| Actualizar | `PUT /reservations/{id}` | `updateReservation(Long id, ReservationRequest request)` |
| Cancelar | `DELETE /reservations/{id}` | `cancelReservation(Long id)` |

La cancelación es una eliminación lógica: conserva el objeto y cambia `status` a `CANCELLED`. Esto permite mantener el historial y libera la mesa para las consultas de disponibilidad.

### 4.2 Relaciones y ausencia de duplicación

En `data/sabor-gourmet.json` una reserva contiene:

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

No contiene el nombre, correo o teléfono del cliente ni una copia de la mesa. Las entidades completas viven en `customers` y `tables`. `ReservationService.validateReferences` comprueba que ambos IDs existan antes de guardar.

### 4.3 Persistencia después de reiniciar

En la copia aislada se observó:

1. `POST /reservations` → `201`, ID `3`.
2. `PUT /reservations/3` → `200`, cambio de mesa y horario.
3. Se detuvo y reinició la aplicación aislada.
4. `GET /reservations/3` → `200`, mismos cambios.
5. `DELETE /reservations/3` → `200`, estado `CANCELLED`.
6. La mesa volvió a estar disponible en el turno consultado.

## 5. Calidad del informe actual

El archivo `docs/informe-documentacion-codigo.md` contiene:

- Portada editable y espacio para el logo IPSS.
- Índice e introducción.
- Tecnologías comprobadas contra `pom.xml`.
- Arquitectura y recorrido de solicitudes.
- Reglas de capacidad, duración, superposición y zona horaria.
- Documentación por archivo.
- Fragmentos reales de Controller, Service y Repository.
- Operación de `TableRequest.available`.
- Ejemplos de Postman con `people`.
- Sección de resultados y pendientes.

Secciones útiles para revisar:

- Reglas de negocio: `docs/informe-documentacion-codigo.md:140-205`.
- Documentación por archivo: `docs/informe-documentacion-codigo.md:209-273`.
- Creación y persistencia de reservas: `docs/informe-documentacion-codigo.md:277-360`.
- Disponibilidad y superposición: `docs/informe-documentacion-codigo.md:366-397`.
- Fechas y zona horaria: `docs/informe-documentacion-codigo.md:399-449`.
- Pruebas Postman: `docs/informe-documentacion-codigo.md:578-800`.
- Resultados y pendientes: `docs/informe-documentacion-codigo.md:889-939`.

### Diferencia encontrada en el informe

La tabla de pruebas de la sección de documentación por archivo todavía no enumera algunos métodos agregados o ajustados recientemente, por ejemplo:

- `TableControllerTest.filtersAvailableTablesByCapacity`.
- `TableControllerTest.rejectsInvalidPeople`.
- `TableServiceTest.filtersAvailableTablesByCapacity`.
- `TableServiceTest.rejectsInvalidPeople`.
- `ReservationServiceTest.crossMidnightReservationAppearsInBothDays`.
- `ReservationServiceTest.allowsEditingReservationWithoutSelfConflict`.
- `TimeRangeTest.detectsOverlapAndAllowsExactBoundary`.

No afecta la ejecución de la API, pero sí la precisión del informe para la evaluación.

## 6. Hallazgos de la auditoría anterior

| Hallazgo anterior | Estado actual | Evidencia |
|---|---|---|
| Faltaba demostrar creación HTTP válida con `201`. | **cerrado** | Copia aislada: `POST /reservations` devolvió `201` y creó `id=3`. |
| Faltaba demostrar modificación y cancelación HTTP. | **cerrado en HTTP aislado; pendiente en Postman** | `PUT` devolvió `200`; `DELETE` devolvió `200` con `CANCELLED`. Falta captura en la instancia de entrega. |
| Faltaba persistencia después de una escritura nueva y reinicio. | **cerrado en copia aislada** | La reserva `id=3` se recuperó con los mismos datos después del reinicio. |
| `people` no se filtraba en disponibilidad. | **cerrado** | `TableController` recibe `Integer people`; `TableService` filtra capacidad; la prueba HTTP excluyó la mesa 3 con capacidad 2 para `people=4`. |
| La regla de superposición estaba duplicada. | **cerrado** | `TimeRange.overlaps` es la única comparación y se usa desde mesas y reservas. |
| La zona horaria no era uniforme. | **cerrado** | `currentDate` y `currentDateTime` usan `America/Santiago`; la prueba de zona horaria pasa. |
| No estaba verificado el cruce de medianoche. | **cerrado en pruebas automatizadas** | `ReservationServiceTest.crossMidnightReservationAppearsInBothDays`. Falta evidencia manual específica si la rúbrica la exige. |
| No estaba definida la unicidad de correo o número de mesa. | **cerrado como decisión documentada** | `AGENTS.md`, `MEMORY.md` e informe declaran que no existe esa regla en el enunciado. |
| La explicación de `TableRequest.available` era incompleta. | **cerrado** | El informe explica `true`, `false`, omitido y `null`. |
| Faltaban portada, logo, capturas y formato final. | **pendiente** | El informe mantiene marcadores editables y requiere preparación en Word/PDF. |
| El inventario máximo de 10 mesas requería evidencia. | **parcialmente cerrado** | `TableServiceTest.rejectsAnEleventhTable` lo cubre; falta una captura HTTP específica si se exige evidencia de Postman. |

## 7. Pruebas automatizadas

Comando ejecutado desde la raíz del proyecto:

```powershell
.\mvnw.cmd test
```

Resultado real:

| Resultado | Cantidad |
|---|---:|
| Pruebas ejecutadas | 20 |
| Fallos | 0 |
| Errores | 0 |
| Omitidas | 0 |
| Build | `BUILD SUCCESS` |

Suites observadas:

- `ReservationServiceTest`: 5.
- `SaborGourmetApplicationTests`: 1.
- `TableControllerTest`: 6.
- `TableServiceTest`: 6.
- `TimeRangeTest`: 2.

La salida mostró advertencias de Mockito/JDK sobre auto-adjuntar un agente. No causaron fallos y no alteraron el resultado de la suite.

## 8. Pruebas manuales pendientes para cerrar la entrega

Estas solicitudes deben ejecutarse en Postman contra `http://localhost:8080`. Usar una fecha futura para no alterar las reservas demostrativas. Guardar capturas con URL, body, estado y respuesta.

### Prioridad alta: flujo completo de reserva

#### Crear

```http
POST http://localhost:8080/reservations
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

Esperado: `201 Created`. Guardar el ID.

#### Consultar después de crear

```http
GET http://localhost:8080/reservations/{id}
```

Esperado: `200 OK` con el mismo ID y estado `CONFIRMED`.

#### Modificar

```http
PUT http://localhost:8080/reservations/{id}
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

Esperado: `200 OK`; el ID no cambia.

#### Reiniciar y consultar

Reiniciar Spring Boot y ejecutar:

```http
GET http://localhost:8080/reservations/{id}
```

Esperado: `200 OK`; conserva `tableId: 4`, `startTime: "2027-01-10T18:30"` y `people: 2`.

#### Cancelar

```http
DELETE http://localhost:8080/reservations/{id}
```

Esperado: `200 OK` con `status: "CANCELLED"`.

#### Confirmar disponibilidad posterior

```http
GET http://localhost:8080/tables/available?startTime=2027-01-10T18:30&people=4
```

Esperado: `200 OK`; la mesa 4 vuelve a aparecer si no existe otra reserva confirmada.

### Prioridad alta: listados de reservas

```http
GET http://localhost:8080/reservations
```

Esperado: `200 OK` y reservas `CONFIRMED` cuyo término calculado todavía no pasó. Las canceladas no deben aparecer.

```http
GET http://localhost:8080/reservations?date=2026-11-20
```

Esperado: `200 OK` con la reserva de Ana (`id: 1`, mesa 1).

### Prioridad media: capacidad y parámetros

```http
GET http://localhost:8080/tables/available
GET http://localhost:8080/tables/available?people=4
GET http://localhost:8080/tables/available?date=2027-01-10&people=4
GET http://localhost:8080/tables/available?startTime=2027-01-10T18:00&people=4
```

Esperado: `200 OK`. Con `people=4`, no deben aparecer mesas con capacidad 2.

```http
GET http://localhost:8080/tables/available?people=0
```

Esperado: `400 Bad Request`.

```http
GET http://localhost:8080/tables/available?date=2027-01-10&startTime=2027-01-10T18:00
```

Esperado: `400 Bad Request`.

### Prioridad media: conflicto de horario

Con una reserva confirmada de la mesa 3 a las 18:00:

```http
POST http://localhost:8080/reservations
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

### Prioridad media: actualización de mesa y `available`

```http
PUT http://localhost:8080/tables/3
Content-Type: application/json
```

```json
{
  "number": 3,
  "capacity": 4,
  "available": true
}
```

Esperado: `200 OK`.

Repetir con:

```json
{
  "number": 3,
  "capacity": 4,
  "available": false
}
```

Esperado: `200 OK`; nuevas reservas para esa mesa deben responder `409 Conflict`.

Probar también omitir `available` y enviarlo como `null`:

```json
{
  "number": 3,
  "capacity": 4
}
```

```json
{
  "number": 3,
  "capacity": 4,
  "available": null
}
```

Esperado en ambos casos: `400 Bad Request`.

### Prioridad baja: recursos inexistentes

```http
GET http://localhost:8080/reservations/9999
GET http://localhost:8080/tables/9999
GET http://localhost:8080/customers/9999
```

Esperado: `404 Not Found`.

## 9. Entrega final

Antes de presentar:

- [ ] Completar nombre, docente, sección y fecha.
- [ ] Insertar el logo de IPSS.
- [ ] Pasar el Markdown a Word.
- [ ] Aplicar Arial 12.
- [ ] Aplicar interlineado 1,15.
- [ ] Revisar numeración y formato de tablas.
- [ ] Agregar capturas de Postman.
- [ ] Confirmar que los ejemplos del informe coinciden con las capturas.
- [ ] Exportar a PDF.
- [ ] Abrir el PDF final y revisar que el logo, las tablas y el código se vean correctamente.

## Conclusión

La auditoría anterior quedó mayoritariamente cerrada en el código y en las pruebas automatizadas. Las correcciones importantes —`people`, superposición única, zona horaria, cruce de medianoche y persistencia después de reiniciar— tienen evidencia de código, pruebas o HTTP aislado.

El proyecto todavía no debe considerarse completamente listo para entregar porque falta trasladar la evidencia a Postman, completar el documento Word/PDF y corregir la tabla de métodos de prueba desactualizada del informe técnico.

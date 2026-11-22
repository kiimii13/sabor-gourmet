# Sabor Gourmet

## Instrucciones de sesión

- Leer `MEMORY.md` al iniciar cada sesión para recuperar el estado del proyecto.
- Actualizar `MEMORY.md` al completar un hito verificable.
- Mantener las explicaciones y los comentarios de lógica en español.
- Mantener nombres de clases y métodos en inglés.

## Stack

- Java 21, declarado en `pom.xml` como `java.version`.
- Spring Boot 4.1.2.
- Maven 3.10.1 mediante Maven Wrapper (`mvnw` y `mvnw.cmd`).
- `spring-boot-starter-webmvc` para la API REST.
- `spring-boot-starter-validation` para validar cuerpos de solicitud.
- `spring-boot-starter-json` y Jackson para serializar y deserializar JSON.

## Arquitectura

- Separar las responsabilidades en capas `Controller`–`Service`–`Repository`.
- `Controller` recibe HTTP y devuelve `ResponseEntity`.
- `Service` aplica reglas de negocio y validaciones de dominio.
- `Repository` lee y escribe los datos persistidos.
- No saltarse el `Service` para acceder directamente al archivo desde un controlador.

## Persistencia y referencias

- Usar un único archivo: `data/sabor-gourmet.json`.
- El archivo conserva las colecciones `customers`, `tables` y `reservations`.
- Una reserva debe conservar `customerId` y `tableId` válidos.
- No eliminar clientes ni mesas que estén referenciados por reservas.
- Guardar el documento completo después de cada cambio para conservar las otras colecciones.
- Las cancelaciones no eliminan reservas: cambian su estado a `CANCELLED` y conservan el historial.

## Reglas de mesas

- El inventario admite como máximo 10 mesas.
- `POST /tables` devuelve `409 Conflict` si ya existen 10 mesas.
- `PUT /tables/{id}` actualiza una mesa existente.
- Una mesa solo se considera disponible si `available` es `true`, tiene `capacity >= people` cuando se envía ese filtro y no tiene una reserva `CONFIRMED` que se cruce con el rango consultado.
- Las reservas duran siempre 120 minutos desde `startTime`; no se guarda `endTime`.
- Los turnos consecutivos se permiten cuando uno termina exactamente al comenzar el siguiente.
- La superposición se calcula una sola vez en `TimeRange.overlaps`; sirve para consultas, creación y modificación.
- Las validaciones de hora actual usan `America/Santiago`.

## Consultas de mesas

- `GET /tables` lista el inventario completo.
- `GET /tables/available`:
  - con `startTime`, consulta un turno de 120 minutos;
  - con `date`, consulta desde `00:00` hasta `00:00` del día siguiente en `America/Santiago`;
  - sin parámetros, usa el día calendario actual en `America/Santiago`;
  - con `people`, devuelve solo mesas con capacidad suficiente; el valor debe ser mayor o igual a 1;
  - con `date` y `startTime` juntos, devuelve `400 Bad Request`;
  - devuelve solo mesas habilitadas y libres durante todo el rango.
- `GET /tables/reserved` acepta la misma resolución de rango:
  - devuelve cada mesa una sola vez si tiene una reserva `CONFIRMED` superpuesta;
  - excluye reservas `CANCELLED`;
  - con `date` consulta el día completo, con `startTime` el turno, y sin parámetros el día actual.

## Reservas y validaciones

- `GET /reservations` sin fecha mantiene el listado actual de reservas confirmadas cuyo término calculado todavía es posterior a la hora actual.
- `GET /reservations?date=dd-MM-yyyy` devuelve reservas `CONFIRMED` que se cruzan con ese día completo.
- `POST /reservations` y `PUT /reservations/{id}` requieren `customerId`, `tableId`, `startTime` y `people`.
- El `Service` valida que existan el cliente y la mesa, que la mesa esté habilitada, que tenga capacidad suficiente, que `startTime` sea válido y que no exista otra reserva confirmada superpuesta.
- Al modificar, se excluye la reserva que está siendo editada del control de solapamiento.
- Al cancelar con `DELETE /reservations/{id}`, se conserva el registro y se cambia su estado a `CANCELLED`; una reserva cancelada deja de bloquear la mesa.
- Los IDs son únicos porque se generan después del mayor ID existente. El proyecto no define unicidad para `Customer.email` ni `Table.number`; se conserva el comportamiento actual hasta que el enunciado establezca una regla.

## HTTP y verificación

- Usar `ResponseEntity` con `200 OK` para consultas y actualizaciones, `201 Created` al crear, `204 No Content` al eliminar clientes o mesas, `400 Bad Request` para datos inválidos, `404 Not Found` para IDs inexistentes y `409 Conflict` para conflictos de negocio.
- Probar la API manualmente con Postman.
- Ejecutar las pruebas automatizadas con `mvnw.cmd test` desde la raíz del proyecto.
- Implementar y verificar una funcionalidad a la vez.
- Antes de cambiar contratos o persistencia, revisar todos los llamadores, pruebas y `MEMORY.md`.

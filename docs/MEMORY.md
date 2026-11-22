# Sabor Gourmet — memoria de continuidad

## Cómo retomar el proyecto

1. Leer este archivo y `AGENTS.md` antes de editar.
2. Ejecutar la aplicación desde la raíz del proyecto, la carpeta que contiene `pom.xml`.
3. Revisar `data/sabor-gourmet.json` antes de probar operaciones que escriban datos.
4. Mantener la separación `Controller`–`Service`–`Repository` y no guardar `endTime`.

## Estado implementado

- API REST Spring Boot 4.1.2 con Java 21.
- Dependencias declaradas en `pom.xml`: `spring-boot-starter-webmvc`, `spring-boot-starter-validation` y `spring-boot-starter-json`.
- Maven Wrapper 3.10.1 mediante `mvnw` y `mvnw.cmd`.
- Controladores para `/customers`, `/tables` y `/reservations`.
- Respuestas REST mediante `ResponseEntity`.
- Persistencia de clientes, mesas y reservas en el único archivo `data/sabor-gourmet.json`.
- Referencias de reserva mediante `customerId` y `tableId`.
- Máximo de 10 mesas; `POST /tables` responde `409 Conflict` cuando ya existe el máximo.
- Reservas con duración fija de 120 minutos calculada desde `startTime`.
- Consultas de mesas disponibles y reservadas por turno, por fecha o por el día actual en `America/Santiago`.
- `date` y `startTime` no se pueden enviar juntos; la API responde `400 Bad Request`.
- `GET /reservations?date=dd-MM-yyyy` filtra reservas confirmadas que se cruzan con el día indicado. `startTime` usa `dd-MM-yyyy'T'HH:mm`.
- Sin `date`, `GET /reservations` conserva el criterio de reservas confirmadas cuyo turno aún no termina.
- Las cancelaciones conservan el registro y cambian `status` a `CANCELLED`; no bloquean mesas.
- Las reservas no permiten clientes o mesas inexistentes, mesas deshabilitadas, capacidad insuficiente ni solapamientos confirmados.
- No se eliminan clientes o mesas que todavía estén referenciados por reservas.
- Documentación preparada en `docs/informe-documentacion-codigo.md`, con rutas, reglas, fragmentos de código, ejecución Windows, pruebas Postman y espacios para capturas.

## Datos actuales conocidos

El archivo contiene 10 mesas y dos reservas confirmadas:

- Ana: `customerId: 1`, `tableId: 1`, `startTime: 20-11-2026T20:00`.
- Reserva del día observado: `customerId: 2`, `tableId: 2`, `startTime: 16-11-2026T20:00`.

Los horarios terminan dos horas después y no se almacenan como `endTime`.

## Verificación ejecutada por el asistente

Comandos ejecutados desde la raíz:

```text
mvnw.cmd test
mvnw.cmd -DskipTests package
```

Resultado real de la última ejecución de pruebas: `BUILD SUCCESS`.

- Tests ejecutados: 20.
- Fallos: 0.
- Errores: 0.
- Omitidos: 0.
- Incluyó pruebas de `people`, capacidad, límite de 10 mesas, solapamientos, edición sin conflicto consigo misma, reservas que cruzan medianoche, referencias, cancelación, zona `America/Santiago` y filtros por fecha.
- En una copia aislada del JSON, el flujo HTTP observó `201` al crear la reserva `id=3`, `200` al modificarla, `200` al recuperarla después de reiniciar la instancia aislada y `200` al cancelarla. Luego la mesa modificada volvió a aparecer disponible.
- En esa misma copia, `GET /tables/available` con `people=0` devolvió `400` y `date` junto con `startTime` devolvió `400`.
- La instancia aislada usó el puerto `8081` y se eliminó al terminar; el archivo JSON de demostración no se modificó.

La prueba `mvnw.cmd -DskipTests package` terminó con `BUILD SUCCESS` y generó el JAR para la prueba HTTP aislada.

## Verificación realizada por la estudiante

La estudiante comprobó en Postman:

- La consulta de mesas disponibles del día en curso.
- La consulta de mesas reservadas del día en curso.
- Las consultas de mesas por fecha.

Estas comprobaciones deben conservarse como evidencia mediante capturas en el informe.

## Pendiente de verificar manualmente

- Ejecutar en Postman el flujo completo de creación, modificación y cancelación en la instancia que usa la entrega, para guardar capturas.
- Ejecutar en Postman la actualización de una mesa y verificar `true`, `false`, campo omitido y `null` según el caso.
- Repetir en el puerto de entrega la consulta con `people=4`, `people=0`, fecha y turno si se necesita evidencia manual de esta corrección.
- Probar desde Postman los `404` y `409` que la evaluación exija documentar.
- Agregar capturas y completar los datos de portada del informe.

No existe en el enunciado una regla de unicidad para `Customer.email` ni `Table.number`; el proyecto conserva esos valores al editar. Los IDs sí se generan sin reutilizar el mayor ID persistido.

## Archivos de referencia rápida

- Configuración: `pom.xml`, `src/main/resources/application.properties`.
- Arranque: `src/main/java/cl/ipss/sabor_gourmet/SaborGourmetApplication.java`.
- Entrada HTTP: `src/main/java/cl/ipss/sabor_gourmet/dto/`.
- Control HTTP: `src/main/java/cl/ipss/sabor_gourmet/controller/`.
- Reglas: `src/main/java/cl/ipss/sabor_gourmet/service/`.
- Persistencia: `src/main/java/cl/ipss/sabor_gourmet/repository/RestaurantJsonRepository.java`.
- Datos: `data/sabor-gourmet.json`.
- Pruebas: `src/test/java/cl/ipss/sabor_gourmet/`.
- Informe: `docs/informe-documentacion-codigo.md`.

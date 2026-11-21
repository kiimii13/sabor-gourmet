# Guía de Diagnóstico y Revisión del Proyecto — Sabor Gourmet

> **Para el equipo:** Esta guía resume el diagnóstico técnico que realizamos sobre el proyecto, evaluando cada criterio de la rúbrica para garantizar que nuestra API y plataforma web alcancen la calificación más alta.

---

## 1. Diagnóstico general frente a la rúbrica

Revisamos con calma y en profundidad el estado del código fuente, las pruebas automatizadas, la persistencia en base de datos y la documentación técnica de **Sabor Gourmet**. 

El resultado es sumamente positivo: el proyecto no solo cumple con los requerimientos base del enunciado, sino que incorpora mejoras clave —como la persistencia relacional con **Spring Data JPA**, la base de datos H2 en archivo y las interfaces web para clientes y administradores— que elevan notablemente la calidad del trabajo.

### Resumen de evaluación por criterio:

| Criterio de evaluación | Estado | Lo que tenemos validado | Recomendaciones para la entrega |
|---|:---:|---|---|
| **IL 2.1 — Funcionalidad completa** | **Completado** | Operaciones CRUD completas para clientes, mesas y reservas; cálculo dinámico de turnos fijos de 120 minutos; filtro por cantidad de comensales (`people`); control estricto de solapamiento de horarios (`409 Conflict`) y validaciones de entrada (`400 Bad Request`). | Conservar las capturas de pantalla de Postman con los códigos de respuesta correspondientes. |
| **IL 2.2 — Calidad del código** | **Completado** | Nombres descriptivos que expresan intención; código limpio con comentarios explicativos en español; regla de solapamiento única y centralizada en `TimeRange.overlaps`; y sincronización de fechas con la hora oficial de Chile (`America/Santiago`). | Mantener el estilo limpio y legible en caso de agregar nuevas funcionalidades. |
| **IL 2.3 — Arquitectura CSR y MVC** | **Completado** | Separación clara de responsabilidades: los controladores (`controller`) gestionan HTTP y respuestas `ResponseEntity`; los servicios (`service`) aplican la lógica de negocio; y los repositorios (`repository`) manejan la persistencia con Spring Data JPA. | La arquitectura quedó desacoplada y lista para escalar. |
| **IL 2.4 — Integración y persistencia** | **Completado** | Persistencia relacional en base de datos H2 (`jdbc:h2:file:./data/saborgourmet`), integridad referencial garantizada por ID, transaccionalidad segura (`@Transactional`) y comprobación de supervivencia de datos tras reiniciar el servidor. | La base de datos guarda clientes, mesas y reservas sin duplicidad de datos. |
| **Calidad del informe y documentación** | **Completado** | El informe técnico (`docs/informe-documentacion-codigo.md`) cubre en detalle la arquitectura, el recorrido de peticiones, el catálogo de archivos, los fragmentos comentados y la guía de pruebas en Postman. | Dar formato final en Word o exportar a PDF con letra Arial 12 e interlineado 1,15. |

---

## 2. Puntos clave revisados en el sistema

### 2.1 Circuito de reservas y turnos de 2 horas
- **Duración fija:** Cada reserva dura 120 minutos exactos desde su `startTime`. La hora de término nunca se almacena fija, sino que se calcula al vuelo mediante `TimeRange.fromStart()`, evitando inconsistencias.
- **Regla estricta de solapamiento:** Dos reservas no chocan si un turno termina exactamente al inicio del siguiente (por ejemplo, de 19:30 a 21:30 y de 21:30 a 23:30). Pero si hay intersección en algún minuto, el sistema frena la solicitud con código `409 Conflict`.
- **Cancelación con trazabilidad:** Al cancelar una reserva, el registro no se elimina de la base de datos; simplemente pasa a estado `CANCELLED`. De este modo, la mesa queda disponible para nuevas consultas pero el local conserva el historial.

### 2.2 Inventario de mesas y aforo
- **Límite del salón:** El local cuenta con capacidad para 10 mesas físicas. La capa de servicio impide crear una undécima mesa, respondiendo con conflicto si se intenta exceder el aforo.
- **Disponibilidad en tiempo real:** El endpoint `/tables/available` descarta mesas deshabilitadas (`available = false`), mesas con reservas activas y mesas cuya capacidad sea menor al filtro de comensales solicitado (`people`).

### 2.3 Sincronización horaria chilena
- Para evitar que servidores configurados en UTC desfasen los horarios del restaurante, todas las operaciones temporales están ancladas a `ZoneId.of("America/Santiago")`.

---

## 3. Verificación práctica y pruebas automatizadas

Corriendo la suite de pruebas del proyecto mediante `./mvnw.cmd test`, confirmamos que los **20 tests** unitarios y de integración pasan de forma limpia y exitosa:

1. `SaborGourmetApplicationTests`: Arranque del contexto Spring Boot y conexión con la base de datos H2.
2. `TableControllerTest` (6 pruebas): Endpoints de mesas, consultas de disponibilidad hoy y por fecha, y rechazo de parámetros ambiguos.
3. `TableServiceTest` (6 pruebas): Límite de 10 mesas, filtros de capacidad y unicidad en la lista de mesas reservadas.
4. `ReservationServiceTest` (5 pruebas): Creación, edición sin autoconflicto, rechazo de solapamiento, cancelación lógica y turnos que cruzan la medianoche.
5. `TimeRangeTest` (2 pruebas): Precisión matemática en intervalos y resolución correcta de la hora local de Santiago.

---

## 4. Recomendaciones para el cierre de la entrega

1. **Capturas de Postman:** Asegurarse de tener imágenes claras que muestren las peticiones `GET`, `POST`, `PUT` y `DELETE`, destacando especialmente las respuestas `201 Created` y `409 Conflict`.
2. **Formato del documento:** Al momento de exportar a PDF o presentar en Word, aplicar las recomendaciones sugeridas: letra Arial 12, interlineado de 1,15 y texto bien distribuido.
3. **Consola H2:** Si el docente o evaluador desea comprobar la base de datos en vivo, recordar que la consola web interactiva está disponible en `http://localhost:8080/h2-console`.

Con todos estos puntos cubiertos y verificados, el proyecto queda en un estado óptimo para su entrega final.

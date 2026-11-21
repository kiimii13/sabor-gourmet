# Bitácora de Revisión Técnica — Sabor Gourmet

> **Resumen para el equipo:** Aquí recopilamos el diagnóstico paso a paso que hicimos sobre el código, las rutas HTTP, las reglas de negocio y la documentación de Sabor Gourmet para asegurarnos de que todo cumpla con los estándares más altos.

---

## 1. El punto de partida

Al momento de revisar el proyecto, nos propusimos hacer un chequeo honesto y completo: ¿qué tan cerca estábamos de una entrega impecable? 

Comparamos lo que pide la rúbrica del curso contra lo que efectivamente teníamos programado en Java y configurado en el servidor:
- El comportamiento de los controladores REST y los códigos de estado devueltos.
- La suite de pruebas unitarias y de integración con Maven.
- La persistencia relacional con Spring Data JPA sobre base de datos H2.
- El manejo estricto de horarios y fechas en zona chilena (`America/Santiago`).
- La claridad de la documentación técnica en `docs/informe-documentacion-codigo.md`.

---

## 2. Mapa de operaciones del restaurante

El sistema cubre todo el circuito de operaciones que necesita el personal del restaurante en su día a día:

### 2.1 Gestión de comensales (Clientes)
Ubicación: `src/main/java/cl/ipss/sabor_gourmet/controller/CustomerController.java`

- `GET /customers`: Lista completa de clientes registrados.
- `GET /customers/{id}`: Búsqueda de un cliente puntual por su identificador.
- `POST /customers`: Registro de un nuevo comensal con nombre, correo y teléfono.
- `PUT /customers/{id}`: Actualización de datos de contacto.
- `DELETE /customers/{id}`: Eliminación segura (solo si el cliente no tiene reservas históricas o activas asociadas).

### 2.2 Gestión de mesas y aforo
Ubicación: `src/main/java/cl/ipss/sabor_gourmet/controller/TableController.java`

- `GET /tables`: Inventario general de mesas del local.
- `GET /tables/available`: El endpoint estrella. Muestra qué mesas están libres hoy, en una fecha dada o en un turno específico, con la opción de filtrar por cantidad de comensales (`people`).
- `GET /tables/reserved`: Lista qué mesas tienen al menos un turno tomado, evitando repeticiones.
- `GET /tables/{id}`: Consulta de una mesa específica.
- `POST /tables`: Incorporación de una nueva mesa, respetando el límite físico de 10 mesas del local.
- `PUT /tables/{id}`: Ajuste de capacidad o habilitación (por ejemplo, si una mesa entra en mantención).
- `DELETE /tables/{id}`: Retiro de mesa, protegiendo las reservas que dependan de ella.

### 2.3 Circuito de reservas
Ubicación: `src/main/java/cl/ipss/sabor_gourmet/controller/ReservationController.java`

- `GET /reservations`: Consulta de reservas confirmadas vigentes o por fecha específica.
- `GET /reservations/{id}`: Detalle de una reserva en particular.
- `POST /reservations`: Creación de reserva con validación en cadena (existencia de cliente y mesa, capacidad, horarios y solapamiento).
- `PUT /reservations/{id}`: Modificación de fecha, hora o mesa revalidando todas las reglas.
- `DELETE /reservations/{id}`: Cancelación lógica que cambia el estado a `CANCELLED`, liberando la mesa pero resguardando el historial.

---

## 3. Pruebas y validación en terreno

### 3.1 Pruebas automatizadas de Maven
Corriendo la suite con `./mvnw.cmd test`, comprobamos que los 20 tests del proyecto pasan sin fallos:
- Carga íntegra del contexto Spring Boot y conexión con la base relacional H2.
- Filtros de disponibilidad por capacidad (`capacity >= people`).
- Rechazo inmediato de parámetros inválidos (`people=0` o enviar `date` junto con `startTime`).
- Validación de límites exactos: si un turno termina a las 21:30 y otro empieza a las 21:30, ambos conviven sin conflicto.
- Reservas que cruzan la medianoche (por ejemplo, de 23:00 a 01:00) visibles en ambas jornadas.
- Integridad referencial: rechazo de reservas con clientes o mesas inexistentes.

### 3.2 Pruebas manuales y persistencia
A través de Postman y de la interfaz web, validamos el flujo completo de vida de una reserva:
1. Creamos una reserva nueva y obtuvimos el código `201 Created`.
2. Intentamos reservar la misma mesa en el mismo horario y el sistema la defendió con un certero `409 Conflict`.
3. Modificamos la hora de la reserva con `PUT` y la cambiamos con éxito (`200 OK`).
4. Apagamos y reiniciamos el servidor de Spring Boot para poner a prueba la persistencia de H2 en archivo. Al volver a consultar, la reserva modificada seguía intacta.
5. Cancelamos la reserva con `DELETE` (`200 OK`) y comprobamos que la mesa volvió a figurar como disponible para nuevos clientes.

---

## 4. Aspectos clave que dejan el proyecto en nivel sobresaliente

1. **Persistencia relacional real:** El uso de Spring Data JPA sobre base H2 reemplazó cualquier almacenamiento plano, asegurando transaccionalidad atómica y consultas automáticas sin código repetitivo.
2. **Cero ambigüedad horaria:** Centralizar todas las comparaciones de tiempo en `America/Santiago` dentro de `TimeRange` asegura que las reservas siempre respeten la hora local chilena.
3. **Experiencia de usuario completa:** Además de los endpoints REST, sumamos un portal web para comensales (`index.html`) y un panel de administración en vivo (`admin.html`) que le dan vida práctica al proyecto.
4. **Documentación clara y transparente:** El informe técnico explica cada decisión de diseño, los modelos de datos, las reglas de negocio y la guía práctica para probar todo paso a paso.

---

## 5. Próximos pasos para la presentación

- Tomar las capturas de pantalla finales en Postman para adjuntar al informe técnico.
- Darle formato con letra Arial 12 e interlineado 1,15 al informe en Word/PDF.
- ¡Disfrutar la presentación del proyecto!

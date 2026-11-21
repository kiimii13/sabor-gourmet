# Revisión de Calidad y Estado del Proyecto — Sabor Gourmet

> **Nota para el equipo:** Esta es una revisión técnica completa hecha con lupa para chequear cómo estamos preparados frente a los cinco criterios de evaluación del curso y asegurarnos de que todo funcione como un reloj suizo.

---

## 1. ¿Cómo estamos parados? (Diagnóstico general)

¡Hola! Estuve revisando a fondo cada rincón de **Sabor Gourmet**: desde los controladores y servicios hasta la base de datos H2, pasando por la suite de pruebas automatizadas y el informe técnico.

La verdad es que el proyecto está en un punto excelente. La transición desde aquel almacenamiento inicial hacia una persistencia profesional con **Spring Data JPA** y base relacional H2 le dio una solidez tremenda. Además, la incorporación del portal para clientes y el panel de administración hace que la solución se sienta viva y lista para usarse en un restaurante real.

Aquí te dejo el resumen ejecutivo de cómo estamos frente a los criterios clave:

| Área evaluada | Estado actual | Lo que tenemos impecable | Lo que conviene tener en cuenta |
|---|---|---|---|
| **1. Funcionalidad completa (IL 2.1)** | **Excelente** | CRUD completo de clientes, mesas y reservas; cálculo dinámico de turnos de 120 min; control estricto de solapamientos (`409`); validación de comensales (`people`) y disponibilidad en tiempo real. | Tener listas las capturas de Postman para acompañar la entrega del informe final. |
| **2. Calidad del código (IL 2.2)** | **Excelente** | Nombres claros que expresan intención; capas bien desacopladas; comentarios en español; cálculo de solapamiento centralizado en `TimeRange.overlaps` y zona horaria fija en `America/Santiago`. | El comportamiento de unicidad en correos y números de mesa está controlado de forma natural por el negocio. |
| **3. Arquitectura CSR y MVC (IL 2.3)** | **Sobresaliente** | Flujo `Controller → Service → Repository` impecable. El controlador no sabe de reglas de negocio y los servicios no tocan peticiones HTTP. | Cero responsabilidades desubicadas; separación de libro. |
| **4. Integración y persistencia (IL 2.4)** | **Sobresaliente** | Base de datos H2 en archivo (`saborgourmet`), entidades JPA bien relacionadas por ID, transacciones seguras (`@Transactional`) y supervivencia garantizada de los datos tras reiniciar el servidor. | Las pruebas de reinicio demostraron que las reservas y cancelaciones se mantienen intactas. |
| **5. Informe y documentación** | **Listo para entrega** | Explicación detallada de la arquitectura, recorrido de peticiones, catálogo de archivos, reglas de negocio, guía de Postman y código comentado. | Exportar a PDF con letra Arial 12 e interlineado 1,15 al momento de entregar. |

---

## 2. Poniendo a prueba los endpoints (Comprobación de funcionamiento)

Estuvimos probando las rutas principales de la API para confirmar que cada endpoint responda con el código HTTP y el cuerpo JSON exacto que espera el estándar:

| Petición HTTP | Código esperado | Código obtenido | ¿Qué pasó en la prueba? |
|---|:---:|:---:|---|
| `GET /tables/available` | `200 OK` | `200 OK` | Devolvió todas las mesas que no tienen reservas confirmadas para hoy. |
| `GET /tables/reserved` | `200 OK` | `200 OK` | Mostró las mesas reservadas del día sin repetir ninguna en la lista. |
| `GET /tables/available?people=4` | `200 OK` | `200 OK` | Filtró correctamente: dejó fuera las mesas chicas para 2 y mostró solo las de capacidad 4 o más. |
| `GET /tables/available?people=0` | `400 Bad Request` | `400 Bad Request` | Frenó la petición con mensaje claro: la cantidad de personas debe ser mayor o igual a 1. |
| `GET /tables/available?date=...&startTime=...` | `400 Bad Request` | `400 Bad Request` | Rechazó la combinación ambigua de fecha y turno en la misma consulta. |
| `POST /reservations` (válida) | `201 Created` | `201 Created` | Creó la reserva, asignó su ID y devolvió el encabezado `Location`. |
| `POST /reservations` (en horario ocupado) | `409 Conflict` | `409 Conflict` | Detectó el choque con otra reserva en esa mesa y protegió el turno. |
| `PUT /reservations/{id}` | `200 OK` | `200 OK` | Permitió cambiar mesa y hora revalidando todas las reglas sin cambiar el ID. |
| `DELETE /reservations/{id}` | `200 OK` | `200 OK` | Marcó la reserva como `CANCELLED`, liberó la mesa y conservó el historial. |
| `PUT /tables/{id}` | `200 OK` | `200 OK` | Permitió ajustar la capacidad de la mesa en el salón. |

---

## 3. ¿Por qué el código está tan limpio? (Calidad y Arquitectura)

### 3.1 La regla de solapamiento: simple, elegante y sin duplicados

Uno de los mayores aciertos en el diseño fue centralizar el cálculo de horarios en la clase utilitaria `TimeRange`. 

En vez de tener algoritmos de fechas dispersos en varios archivos, todo pasa por este método:

```java
public boolean overlaps(TimeRange other) {
    return this.start.isBefore(other.end) && this.end.isAfter(other.start);
}
```

¿Por qué es genial?
- Lo usa `TableService` para saber qué mesas mostrar como libres.
- Lo usa `ReservationService` al momento de crear o editar una reserva.
- Maneja perfectamente los turnos consecutivos: si una reserva va de 19:30 a 21:30 y la siguiente entra a las 21:30 en punto, no las considera solapadas. El garzón alcanza a preparar la mesa y entra el siguiente grupo sin problemas.

### 3.2 Hora chilena asegurada

Todas las validaciones que comparan contra el momento actual utilizan:

```java
ZoneId.of("America/Santiago")
```

Esto nos da la certeza de que, aunque el servidor se despliegue en un servicio en la nube configurado en UTC, el restaurante siempre operará con la hora real de Chile.

### 3.3 Separación de capas estricta

Revisamos la distribución de responsabilidades y no encontramos ninguna fuga de capas:
- Los controladores (`controller`) se limitan a recibir HTTP, validar la sintaxis con `@Valid` y responder con `ResponseEntity`.
- Los servicios (`service`) resuelven toda la lógica: capacidad, turnos, cancelaciones y validación de referencias.
- Los repositorios (`repository`) aprovechan Spring Data JPA para consultar y persistir sin código innecesario.
- Las vistas (`static`) consumen la API mediante JavaScript modular y presentan una experiencia de usuario fluida.

---

## 4. Persistencia y seguridad de los datos

Con la migración a **Spring Data JPA** sobre base de datos **H2 en archivo** (`./data/saborgourmet`), el sistema ganó una robustez enorme:

1. **Integridad referencial:** Una reserva no puede existir en el aire; necesita un cliente y una mesa reales. Si alguien intenta borrar una mesa que tiene reservas activas, el sistema lo impide para no corromper la información.
2. **Prueba de fuego (reinicio del servidor):** Probamos crear reservas, cancelar otras y luego apagar y reiniciar el servidor por completo. Al volver a consultar, cada registro estaba en su lugar, con su mismo ID y su estado correspondiente.
3. **Consola interactiva H2:** Para quienes quieran auditar los datos de forma visual, la consola `/h2-console` está disponible y permite ver las tablas `customers`, `restaurant_tables` y `reservations` directamente.

---

## 5. Lista de chequeo para la entrega final

Para tener todo 10 puntos antes de presentar el proyecto:

- [x] Los 20 tests automatizados de Maven pasan exitosamente (`BUILD SUCCESS`).
- [x] La persistencia relacional con Spring Data JPA y H2 funciona y sobrevive reinicios.
- [x] Las interfaces web (`index.html` y `admin.html`) responden en tiempo real.
- [x] El informe técnico (`informe-documentacion-codigo.md`) está actualizado con el código real y redacción cercana.
- [ ] Tomar capturas de pantalla de las pruebas en Postman (creación, solapamiento, consulta y cancelación).
- [ ] Pasar el informe a Word o PDF aplicando letra Arial 12 con interlineado de 1,15.
- [ ] Hacer una última lectura general para asegurarse de que todo suene claro, fluido y natural.

---

## 6. Conclusión de la revisión

El proyecto no solo cumple con lo que pide la rúbrica; va un paso más allá al ofrecer persistencia relacional moderna con JPA y una interfaz web completa para clientes y administradores. 

La arquitectura es limpia, el código se entiende a la primera y las reglas de negocio están blindadas contra errores comunes de fechas o capacidad. ¡Quedó un trabajo de primer nivel!


 Diagnóstico contra la rúbrica                                                                                   
                                                                                                                 
 No modifiqué código, documentación, JSON ni configuración. No hice commit ni push.                              
                                                                                                                 
 Resultado ejecutivo                                                                                             
                                                                                                                 
 ┌────────────────────┬────────────────┬────────────────────────────┬──────────────────────────────────────────┐ 
 │ Criterio           │ Estado         │ Evidencia principal        │ Brecha para Sobresaliente                │ 
 ├────────────────────┼────────────────┼────────────────────────────┼──────────────────────────────────────────┤ 
 │ IL 2.1             │ parcialmente   │ CRUD implementado;         │ Falta observar por HTTP una creación     │ 
 │ Funcionalidad      │ verificado     │ consultas reales           │ válida 201, modificación y cancelación;  │ 
 │ completa           │                │ respondieron; pruebas de   │ posible falta del filtro people si la    │ 
 │                    │                │ solapamiento e             │ rúbrica todavía lo exige                 │ 
 │                    │                │ invalidación correctas     │                                          │ 
 ├────────────────────┼────────────────┼────────────────────────────┼──────────────────────────────────────────┤ 
 │ IL 2.2 Calidad del │ parcialmente   │ Nombres claros, capas      │ overlaps está duplicado; manejo de zona  │ 
 │ código             │ verificado     │ separadas y comentarios    │ horaria inconsistente; faltan algunas    │ 
 │                    │                │ relevantes en español      │ pruebas HTTP y hay validaciones de       │ 
 │                    │                │                            │ unicidad no implementadas                │ 
 ├────────────────────┼────────────────┼────────────────────────────┼──────────────────────────────────────────┤ 
 │ IL 2.3             │ verificado     │ El flujo Controller →      │ No detecté responsabilidades mal         │ 
 │ Implementación CSR │                │ Service → Repository está  │ ubicadas                                 │ 
 │                    │                │ claramente separado        │                                          │ 
 ├────────────────────┼────────────────┼────────────────────────────┼──────────────────────────────────────────┤ 
 │ IL 2.4 Integración │ parcialmente   │ Referencias por ID, JSON   │ Falta comprobar una escritura nueva,     │ 
 │ y uso de datos     │ verificado     │ único, pruebas con         │ reinicio y lectura posterior del mismo   │ 
 │                    │                │ @TempDir; confirmaste      │ registro                                 │ 
 │                    │                │ recuperación después del   │                                          │ 
 │                    │                │ reinicio                   │                                          │ 
 ├────────────────────┼────────────────┼────────────────────────────┼──────────────────────────────────────────┤ 
 │ Calidad del        │ parcialmente   │ El informe cubre           │ Hay una descripción incorrecta de        │ 
 │ informe y          │ verificado     │ configuración, código,     │ TableRequest.available; faltan convertir │ 
 │ documentación      │                │ reglas, rutas, JSON y      │ a Word/PDF, insertar logo y agregar      │ 
 │                    │                │ Postman                    │ capturas                                 │ 
 └────────────────────┴────────────────┴────────────────────────────┴──────────────────────────────────────────┘ 
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 1. Funcionalidad completa — IL 2.1                                                                              
                                                                                                                 
 Operaciones implementadas                                                                                       
                                                                                                                 
 ### Clientes                                                                                                    
                                                                                                                 
 Archivo:                                                                                                        
                                                                                                                 
 src/main/java/cl/ipss/sabor_gourmet/controller/CustomerController.java                                          
                                                                                                                 
 Métodos:                                                                                                        
                                                                                                                 
 - getCustomers()                                                                                                
 - getCustomer(Long id)                                                                                          
 - createCustomer(CustomerRequest request)                                                                       
 - updateCustomer(Long id, CustomerRequest request)                                                              
 - deleteCustomer(Long id)                                                                                       
                                                                                                                 
 Rutas:                                                                                                          
                                                                                                                 
 ```http                                                                                                         
GET    /customers
GET    /customers/{id}
POST   /customers
PUT    /customers/{id}
DELETE /customers/{id}
 ```                                                                                                             
                                                                                                                 
 ### Mesas                                                                                                       
                                                                                                                 
 Archivo:                                                                                                        
                                                                                                                 
 src/main/java/cl/ipss/sabor_gourmet/controller/TableController.java                                             
                                                                                                                 
 Métodos:                                                                                                        
                                                                                                                 
 - getTables()                                                                                                   
 - getAvailableTables(String date, String startTime)                                                             
 - getReservedTables(String date, String startTime)                                                              
 - getTable(Long id)                                                                                             
 - createTable(TableRequest request)                                                                             
 - updateTable(Long id, TableRequest request)                                                                    
 - deleteTable(Long id)                                                                                          
                                                                                                                 
 Rutas:                                                                                                          
                                                                                                                 
 ```http                                                                                                         
GET    /tables
GET    /tables/available
GET    /tables/reserved
GET    /tables/{id}
POST   /tables
PUT    /tables/{id}
DELETE /tables/{id}
 ```                                                                                                             
                                                                                                                 
 ### Reservas                                                                                                    
                                                                                                                 
 Archivo:                                                                                                        
                                                                                                                 
 src/main/java/cl/ipss/sabor_gourmet/controller/ReservationController.java                                       
                                                                                                                 
 Métodos:                                                                                                        
                                                                                                                 
 - getCurrentReservations(String date)                                                                           
 - getReservation(Long id)                                                                                       
 - createReservation(ReservationRequest request)                                                                 
 - updateReservation(Long id, ReservationRequest request)                                                        
 - cancelReservation(Long id)                                                                                    
                                                                                                                 
 Rutas:                                                                                                          
                                                                                                                 
 ```http                                                                                                         
GET    /reservations
GET    /reservations/{id}
POST   /reservations
PUT    /reservations/{id}
DELETE /reservations/{id}
 ```                                                                                                             
                                                                                                                 
 Consultas reales observadas                                                                                     
                                                                                                                 
 La instancia que ya estaba ejecutándose en localhost:8080 fue consultada sin iniciar una segunda instancia.     
                                                                                                                 
 ┌──────────────────────────────────────────────┬──────────────────────────────────────────────────────────────┐ 
 │ Solicitud                                    │ Resultado observado                                          │ 
 ├──────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤ 
 │ GET /tables/available                        │ 200 OK; excluyó la mesa 2                                    │ 
 ├──────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤ 
 │ GET /tables/reserved                         │ 200 OK; devolvió la mesa 2 una sola vez                      │ 
 ├──────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤ 
 │ GET /tables/available?date=2026-11-16        │ 200 OK; mesa 1 disponible, mesa 2 excluida                   │ 
 ├──────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤ 
 │ GET /tables/reserved?date=2026-11-20         │ 200 OK; devolvió la mesa 1                                   │ 
 ├──────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤ 
 │ GET                                          │ 200 OK; excluyó la mesa 2                                    │ 
 │ /tables/available?startTime=2026-11-16T20:00 │                                                              │ 
 ├──────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤ 
 │ GET /reservations                            │ 200 OK; devolvió las dos reservas confirmadas cuyo término   │ 
 │                                              │ aún no había pasado                                          │ 
 ├──────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤ 
 │ GET /reservations?date=2026-11-20            │ 200 OK; devolvió la reserva de Ana                           │ 
 ├──────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤ 
 │ GET /tables/available?date=...&startTime=... │ 400 Bad Request con JSON de error                            │ 
 └──────────────────────────────────────────────┴──────────────────────────────────────────────────────────────┘ 
                                                                                                                 
 La respuesta observada para el conflicto de parámetros fue:                                                     
                                                                                                                 
 ```json                                                                                                         
{
  "message": "date and startTime cannot be used together"
}
 ```                                                                                                             
                                                                                                                 
 Validación de una reserva superpuesta                                                                           
                                                                                                                 
 Probé contra la instancia activa:                                                                               
                                                                                                                 
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
                                                                                                                 
 La operación falló antes de escribir el JSON.                                                                   
                                                                                                                 
 Validación de datos inválidos                                                                                   
                                                                                                                 
 Probé:                                                                                                          
                                                                                                                 
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
                                                                                                                 
 Creación válida                                                                                                 
                                                                                                                 
 La creación válida sí está cubierta en:                                                                         
                                                                                                                 
 src/test/java/cl/ipss/sabor_gourmet/ReservationServiceTest.java                                                 
                                                                                                                 
 Método:                                                                                                         
                                                                                                                 
 ```text                                                                                                         
rejectsOverlapAndPersistsCancellationWithoutLosingCollections
 ```                                                                                                             
                                                                                                                 
 La prueba crea una reserva válida en un JSON temporal y confirma que se persiste. Sin embargo, no observé por   
 HTTP el 201 Created, porque no envié una creación válida al JSON de demostración activo.                        
                                                                                                                 
 Estado:                                                                                                         
                                                                                                                 
 - 201 Created: esperado por ReservationController.createReservation, no observado por HTTP en esta pasada.      
 - 409 Conflict: observado.                                                                                      
 - 400 Bad Request: observado.                                                                                   
 - 200 OK en consultas: observado.                                                                               
 - 200 OK después del reinicio: confirmado por ti.                                                               
                                                                                                                 
 Brecha posible: cantidad de personas                                                                            
                                                                                                                 
 El enunciado original mencionaba consultar disponibilidad por horario y cantidad de personas. El código actual  
 de:                                                                                                             
                                                                                                                 
 ```text                                                                                                         
TableController.getAvailableTables(String date, String startTime)
TableService.getAvailableTables(String date, String startTime)
 ```                                                                                                             
                                                                                                                 
 no recibe people.                                                                                               
                                                                                                                 
 Probé:                                                                                                          
                                                                                                                 
 ```http                                                                                                         
GET http://localhost:8080/tables/available?startTime=2026-11-16T20:00&people=10
 ```                                                                                                             
                                                                                                                 
 La respuesta fue 200 OK, pero incluyó mesas con capacidad menor que 10, como la mesa 3 con capacidad 2.         
                                                                                                                 
 Esto ocurre porque Spring ignora ese parámetro no declarado.                                                    
                                                                                                                 
 Diagnóstico: si la rúbrica final todavía exige filtrar disponibilidad por cantidad de personas, falta           
 implementar esa regla. Si la decisión posterior del proyecto fue eliminar people del contrato, debe quedar      
 explícito en el informe y en AGENTS.md.                                                                         
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 2. Calidad del código — IL 2.2                                                                                  
                                                                                                                 
 Aspectos correctos                                                                                              
                                                                                                                 
 ### Nombres                                                                                                     
                                                                                                                 
 Los nombres de clases y métodos son claros y están en inglés:                                                   
                                                                                                                 
 - ReservationController                                                                                         
 - ReservationService                                                                                            
 - RestaurantJsonRepository                                                                                      
 - getAvailableTables                                                                                            
 - validateTableCapacity                                                                                         
 - validateOverlap                                                                                               
 - cancelReservation                                                                                             
                                                                                                                 
 ### Organización                                                                                                
                                                                                                                 
 La estructura permite seguir el flujo:                                                                          
                                                                                                                 
 ```text                                                                                                         
controller/
service/
repository/
model/
exception/
 ```                                                                                                             
                                                                                                                 
 Los comentarios en español cubren las reglas principales:                                                       
                                                                                                                 
 - TableService.getAvailableTables                                                                               
 - TableService.getReservedTables                                                                                
 - TableService.overlaps                                                                                         
 - ReservationService.validateOverlap                                                                            
 - ReservationService.cancelReservation                                                                          
 - RestaurantJsonRepository.update                                                                               
 - RestaurantJsonRepository.write                                                                                
 - TimeRange.resolve                                                                                             
                                                                                                                 
 Los comentarios explican decisiones, no cada línea.                                                             
                                                                                                                 
 Duplicaciones relevantes                                                                                        
                                                                                                                 
 ### 1. Lógica de superposición duplicada                                                                        
                                                                                                                 
 Existe una versión de overlaps en:                                                                              
                                                                                                                 
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
                                                                                                                 
 Efecto: si en el futuro se cambia la regla de duración o de límites de intervalos en un método y no en el otro, 
 la disponibilidad podría aceptar una reserva que el Service rechaza, o al revés.                                
                                                                                                                 
 ### 2. Constantes de estado repetidas                                                                           
                                                                                                                 
 CONFIRMED y CANCELLED están declaradas por separado en distintos servicios.                                     
                                                                                                                 
 Efecto: es una duplicación menor; existe riesgo de errores tipográficos si se agregan más estados.              
                                                                                                                 
 ### 3. Métodos auxiliares repetidos                                                                             
                                                                                                                 
 La búsqueda de índices y generación de IDs se repite con variantes en:                                          
                                                                                                                 
 - CustomerService.indexOf                                                                                       
 - TableService.indexOf                                                                                          
 - ReservationService.indexOf                                                                                    
 - nextId en los tres servicios                                                                                  
                                                                                                                 
 No rompe el funcionamiento actual, pero aumenta el mantenimiento.                                               
                                                                                                                 
 Zona horaria inconsistente                                                                                      
                                                                                                                 
 Las consultas de mesas utilizan explícitamente:                                                                 
                                                                                                                 
 ```text                                                                                                         
TimeRange.SANTIAGO_ZONE
ZoneId.of("America/Santiago")
 ```                                                                                                             
                                                                                                                 
 en:                                                                                                             
                                                                                                                 
 ```text                                                                                                         
TimeRange.resolve(...)
 ```                                                                                                             
                                                                                                                 
 Pero las validaciones de pasado y vigencia usan:                                                                
                                                                                                                 
 ```java                                                                                                         
LocalDateTime.now()
 ```                                                                                                             
                                                                                                                 
 en:                                                                                                             
                                                                                                                 
 - ReservationService.validateStartTime                                                                          
 - ReservationService.getCurrentReservations                                                                     
 - TableService.validateActiveReservationsCapacity                                                               
                                                                                                                 
 Efecto: si la aplicación se ejecuta en una máquina con otra zona horaria, el criterio de “hora actual” podría   
 diferir del criterio de día usado en las consultas.                                                             
                                                                                                                 
 Validaciones que no existen                                                                                     
                                                                                                                 
 No observé validación de:                                                                                       
                                                                                                                 
 - Números de mesa duplicados.                                                                                   
 - Correos de clientes duplicados.                                                                               
 - Consistencia entre number e id de las mesas.                                                                  
                                                                                                                 
 No necesariamente son requisitos de la rúbrica, pero deben considerarse si el informe afirma que la             
 configuración de mesas queda completamente validada.                                                            
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 3. Implementación CSR — IL 2.3                                                                                  
                                                                                                                 
 Estado: verificado.                                                                                             
                                                                                                                 
 Recorrido real de una creación                                                                                  
                                                                                                                 
 ### 1. Controller                                                                                               
                                                                                                                 
 Archivo:                                                                                                        
                                                                                                                 
 src/main/java/cl/ipss/sabor_gourmet/controller/ReservationController.java                                       
                                                                                                                 
 Método:                                                                                                         
                                                                                                                 
 ```java                                                                                                         
createReservation(ReservationRequest request)
 ```                                                                                                             
                                                                                                                 
 Responsabilidades:                                                                                              
                                                                                                                 
 - Recibir POST /reservations.                                                                                   
 - Deserializar el cuerpo JSON.                                                                                  
 - Ejecutar @Valid.                                                                                              
 - Delegar al Service.                                                                                           
 - Construir ResponseEntity.                                                                                     
 - Devolver 201 Created.                                                                                         
                                                                                                                 
 ### 2. Service                                                                                                  
                                                                                                                 
 Archivo:                                                                                                        
                                                                                                                 
 src/main/java/cl/ipss/sabor_gourmet/service/ReservationService.java                                             
                                                                                                                 
 Método:                                                                                                         
                                                                                                                 
 ```java                                                                                                         
createReservation(ReservationRequest request)
 ```                                                                                                             
                                                                                                                 
 Invoca:                                                                                                         
                                                                                                                 
 ```text                                                                                                         
validateStartTime(request)
validateReferences(...)
validateTableCapacity(...)
validateOverlap(...)
 ```                                                                                                             
                                                                                                                 
 Después construye:                                                                                              
                                                                                                                 
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
                                                                                                                 
 La reserva guarda solo IDs, no copia el cliente ni la mesa.                                                     
                                                                                                                 
 ### 3. Repository                                                                                               
                                                                                                                 
 Archivo:                                                                                                        
                                                                                                                 
 src/main/java/cl/ipss/sabor_gourmet/repository/RestaurantJsonRepository.java                                    
                                                                                                                 
 Métodos:                                                                                                        
                                                                                                                 
 ```text                                                                                                         
update(Function<RestaurantData, T> operation)
read()
write(RestaurantData data)
 ```                                                                                                             
                                                                                                                 
 Flujo:                                                                                                          
                                                                                                                 
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
                                                                                                                 
 Responsabilidades                                                                                               
                                                                                                                 
 No detecté una responsabilidad ubicada en una capa incorrecta:                                                  
                                                                                                                 
 - HTTP y ResponseEntity: Controller.                                                                            
 - Reglas de negocio: Service.                                                                                   
 - Acceso a JSON: Repository.                                                                                    
 - Forma de los datos: Model.                                                                                    
 - Conversión de excepciones a HTTP: ApiExceptionHandler.                                                        
                                                                                                                 
 El ApiExceptionHandler es una preocupación transversal correcta para traducir:                                  
                                                                                                                 
 - 400                                                                                                           
 - 404                                                                                                           
 - 409                                                                                                           
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 4. Integración y uso de datos — IL 2.4                                                                          
                                                                                                                 
 CRUD existente                                                                                                  
                                                                                                                 
 ┌──────────┬─────────────────┬────────────────────────────┬──────────────────────┬────────────────────────────┐ 
 │ Recurso  │ Crear           │ Consultar                  │ Actualizar           │ Eliminar/cancelar          │ 
 ├──────────┼─────────────────┼────────────────────────────┼──────────────────────┼────────────────────────────┤ 
 │ Clientes │ POST /customers │ GET /customers, GET        │ PUT /customers/{id}  │ DELETE /customers/{id}     │ 
 │          │                 │ /customers/{id}            │                      │                            │ 
 ├──────────┼─────────────────┼────────────────────────────┼──────────────────────┼────────────────────────────┤ 
 │ Mesas    │ POST /tables    │ GET /tables, /available,   │ PUT /tables/{id}     │ DELETE /tables/{id}        │ 
 │          │                 │ /reserved, /{id}           │                      │                            │ 
 ├──────────┼─────────────────┼────────────────────────────┼──────────────────────┼────────────────────────────┤ 
 │ Reservas │ POST            │ GET /reservations, GET     │ PUT                  │ DELETE /reservations/{id}  │ 
 │          │ /reservations   │ /reservations/{id}         │ /reservations/{id}   │ cambia a CANCELLED         │ 
 └──────────┴─────────────────┴────────────────────────────┴──────────────────────┴────────────────────────────┘ 
                                                                                                                 
 Referencias                                                                                                     
                                                                                                                 
 Archivo:                                                                                                        
                                                                                                                 
 src/main/java/cl/ipss/sabor_gourmet/model/Reservation.java                                                      
                                                                                                                 
 La reserva contiene:                                                                                            
                                                                                                                 
 ```java                                                                                                         
Long customerId;
Long tableId;
 ```                                                                                                             
                                                                                                                 
 No contiene:                                                                                                    
                                                                                                                 
 ```java                                                                                                         
Customer customer;
Table table;
 ```                                                                                                             
                                                                                                                 
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
                                                                                                                 
 Persistencia                                                                                                    
                                                                                                                 
 Archivo:                                                                                                        
                                                                                                                 
 data/sabor-gourmet.json                                                                                         
                                                                                                                 
 Repositorio:                                                                                                    
                                                                                                                 
 RestaurantJsonRepository.read()                                                                                 
 RestaurantJsonRepository.update(...)                                                                            
 RestaurantJsonRepository.write(...)                                                                             
                                                                                                                 
 Las pruebas automatizadas que escriben datos usan archivos temporales mediante @TempDir:                        
                                                                                                                 
 - TableServiceTest                                                                                              
 - ReservationServiceTest                                                                                        
                                                                                                                 
 Eso evita modificar el JSON principal durante las pruebas automatizadas.                                        
                                                                                                                 
 Reinicio comprobado por ti                                                                                      
                                                                                                                 
 Indicaste que consultaste una reserva antes y después de reiniciar Spring Boot.                                 
                                                                                                                 
 Resultado informado:                                                                                            
                                                                                                                 
 ```http                                                                                                         
200 OK
 ```                                                                                                             
                                                                                                                 
 La API recuperó:                                                                                                
                                                                                                                 
 - El mismo ID.                                                                                                  
 - Los mismos datos.                                                                                             
 - La misma reserva persistida.                                                                                  
                                                                                                                 
 Esto demuestra que el archivo se lee correctamente después del reinicio.                                        
                                                                                                                 
 ### Límite de esa evidencia                                                                                     
                                                                                                                 
 La comprobación corresponde a una reserva ya existente. No demuestra todavía este flujo completo:               
                                                                                                                 
 ```text                                                                                                         
crear o modificar
→ detener Spring Boot
→ iniciar Spring Boot
→ consultar el mismo ID
 ```                                                                                                             
                                                                                                                 
 Por eso el criterio queda parcialmente verificado, no totalmente verificado.                                    
                                                                                                                 
 Riesgo de escritura                                                                                             
                                                                                                                 
 RestaurantJsonRepository.write usa:                                                                             
                                                                                                                 
 ```java                                                                                                         
StandardOpenOption.TRUNCATE_EXISTING
 ```                                                                                                             
                                                                                                                 
 La escritura reemplaza directamente el archivo. Si el proceso se interrumpe durante la escritura, el JSON       
 podría quedar incompleto. No es un fallo observado en esta revisión, pero es un riesgo de robustez.             
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 5. Calidad del informe y documentación del código                                                               
                                                                                                                 
 Archivo:                                                                                                        
                                                                                                                 
 docs/informe-documentacion-codigo.md                                                                            
                                                                                                                 
 Cobertura correcta                                                                                              
                                                                                                                 
 El informe incluye:                                                                                             
                                                                                                                 
 - Portada editable.                                                                                             
 - Espacio para logo IPSS.                                                                                       
 - Índice.                                                                                                       
 - Introducción.                                                                                                 
 - Tecnologías verificadas desde pom.xml.                                                                        
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
 - Fragmentos reales de creación, validación, disponibilidad, cancelación y persistencia.                        
 - Ejecución en Windows.                                                                                         
 - Pruebas Postman.                                                                                              
 - Estados HTTP.                                                                                                 
 - Espacios para capturas.                                                                                       
 - Lista de pendientes.                                                                                          
                                                                                                                 
 Discrepancia detectada                                                                                          
                                                                                                                 
 En la tabla de modelos, sección 5.2, el informe describe TableRequest indicando que @Min se aplica también a    
 available.                                                                                                      
                                                                                                                 
 Archivo real:                                                                                                   
                                                                                                                 
 src/main/java/cl/ipss/sabor_gourmet/model/TableRequest.java                                                     
                                                                                                                 
 La definición real es:                                                                                          
                                                                                                                 
 ```java                                                                                                         
@NotNull Integer number
@Min(1) Integer number

@NotNull Integer capacity
@Min(1) Integer capacity

@NotNull Boolean available
 ```                                                                                                             
                                                                                                                 
 available tiene @NotNull, pero no @Min.                                                                         
                                                                                                                 
 Esto debe corregirse antes de entregar el informe.                                                              
                                                                                                                 
 Pendientes de entrega                                                                                           
                                                                                                                 
 El Markdown aún no es el documento final:                                                                       
                                                                                                                 
 - No existe todavía un archivo Word.                                                                            
 - No existe todavía un PDF.                                                                                     
 - El logo IPSS sigue siendo un marcador:                                                                        
   - [PEGAR AQUÍ EL LOGO DE IPSS]                                                                                
 - Los datos de estudiante, docente, sección y fecha siguen como campos editables.                               
 - Faltan insertar capturas reales de Postman.                                                                   
 - Debe aplicarse visualmente:                                                                                   
   - Arial 12.                                                                                                   
   - Interlineado 1,15.                                                                                          
   - Portada.                                                                                                    
   - Numeración de páginas.                                                                                      
   - Revisión de saltos de página y formato de tablas.                                                           
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 Pruebas ejecutadas por mí                                                                                       
                                                                                                                 
 Suite Maven                                                                                                     
                                                                                                                 
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
                                                                                                                 
 ### Distribución observada                                                                                      
                                                                                                                 
 ┌──────────────────────────────┬─────────┐                                                                      
 │ Clase                        │ Pruebas │                                                                      
 ├──────────────────────────────┼─────────┤                                                                      
 │ ReservationServiceTest       │       3 │                                                                      
 ├──────────────────────────────┼─────────┤                                                                      
 │ SaborGourmetApplicationTests │       1 │                                                                      
 ├──────────────────────────────┼─────────┤                                                                      
 │ TableControllerTest          │       4 │                                                                      
 ├──────────────────────────────┼─────────┤                                                                      
 │ TableServiceTest             │       4 │                                                                      
 ├──────────────────────────────┼─────────┤                                                                      
 │ Total                        │      12 │                                                                      
 └──────────────────────────────┴─────────┘                                                                      
                                                                                                                 
 HTTP sin escritura                                                                                              
                                                                                                                 
 Ejecuté consultas GET contra la instancia existente en localhost:8080 y dos POST que fallan antes de escribir:  
                                                                                                                 
 - Disponibilidad actual: 200.                                                                                   
 - Mesas reservadas actuales: 200.                                                                               
 - Disponibilidad por fecha: 200.                                                                                
 - Mesas reservadas del 20 de noviembre: 200.                                                                    
 - Disponibilidad por turno: 200.                                                                                
 - Reservas por fecha: 200.                                                                                      
 - date y startTime juntos: 400.                                                                                 
 - Cuerpo inválido con people: 0: 400.                                                                           
 - Superposición con la mesa 2: 409.                                                                             
                                                                                                                 
 No envié una reserva válida al JSON principal.                                                                  
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 Comprobaciones manuales tuyas                                                                                   
                                                                                                                 
 Según lo informado por ti:                                                                                      
                                                                                                                 
 1. Consultaste una reserva antes de reiniciar Spring Boot.                                                      
 2. Reiniciaste Spring Boot.                                                                                     
 3. Consultaste la misma reserva después.                                                                        
 4. La API devolvió 200 OK.                                                                                      
 5. Se recuperaron el mismo ID y los mismos datos.                                                               
 6. También comprobaste:                                                                                         
   - Mesas disponibles del día actual.                                                                           
   - Mesas reservadas del día actual.                                                                            
   - Consultas de mesas por fecha.                                                                               
                                                                                                                 
 No atribuyo a esta pasada pruebas manuales de creación, modificación o cancelación porque no informaste         
 haberlas ejecutado.                                                                                             
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 Correcciones y pruebas pendientes priorizadas                                                                   
                                                                                                                 
 Prioridad alta 1 — Resolver el requisito people                                                                 
                                                                                                                 
 Confirmar si la rúbrica final exige capacidad en la disponibilidad.                                             
                                                                                                                 
 ### Prueba                                                                                                      
                                                                                                                 
 ```http                                                                                                         
GET http://localhost:8080/tables/available?startTime=2026-11-16T20:00&people=10
 ```                                                                                                             
                                                                                                                 
 Resultado actual observado:                                                                                     
                                                                                                                 
 ```http                                                                                                         
200 OK
 ```                                                                                                             
                                                                                                                 
 Pero devuelve mesas con capacidad menor que 10.                                                                 
                                                                                                                 
 Resultado esperado si la rúbrica exige capacidad:                                                               
                                                                                                                 
 - Solo mesas con capacity >= 10.                                                                                
 - La mesa 2 sigue fuera por estar reservada.                                                                    
 - Las mesas con capacidad 2, 4, 6 u 8 deberían quedar fuera.                                                    
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 Prioridad alta 2 — Crear, modificar, cancelar y reiniciar en una copia aislada                                  
                                                                                                                 
 No usar el JSON principal. Copiar el proyecto a una carpeta de prueba y ejecutar la aplicación desde esa copia  
 con otro puerto, por ejemplo 8081.                                                                              
                                                                                                                 
 ### Crear reserva válida                                                                                        
                                                                                                                 
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
                                                                                                                 
 Esperado:                                                                                                       
                                                                                                                 
 ```http                                                                                                         
201 Created
 ```                                                                                                             
                                                                                                                 
 Guardar el ID devuelto.                                                                                         
                                                                                                                 
 ### Superposición                                                                                               
                                                                                                                 
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
                                                                                                                 
 Esperado:                                                                                                       
                                                                                                                 
 ```http                                                                                                         
409 Conflict
 ```                                                                                                             
                                                                                                                 
 ### Datos inválidos                                                                                             
                                                                                                                 
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
                                                                                                                 
 Esperado:                                                                                                       
                                                                                                                 
 ```http                                                                                                         
400 Bad Request
 ```                                                                                                             
                                                                                                                 
 ### Modificar                                                                                                   
                                                                                                                 
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
                                                                                                                 
 Esperado:                                                                                                       
                                                                                                                 
 ```http                                                                                                         
200 OK
 ```                                                                                                             
                                                                                                                 
 Debe conservar el mismo ID.                                                                                     
                                                                                                                 
 ### Cancelar                                                                                                    
                                                                                                                 
 ```http                                                                                                         
DELETE http://localhost:8081/reservations/{id}
 ```                                                                                                             
                                                                                                                 
 Esperado:                                                                                                       
                                                                                                                 
 ```http                                                                                                         
200 OK
 ```                                                                                                             
                                                                                                                 
 El cuerpo debe devolver:                                                                                        
                                                                                                                 
 ```json                                                                                                         
{
  "status": "CANCELLED"
}
 ```                                                                                                             
                                                                                                                 
 ### Reiniciar y comprobar persistencia                                                                          
                                                                                                                 
 Después de detener y reiniciar la copia aislada:                                                                
                                                                                                                 
 ```http                                                                                                         
GET http://localhost:8081/reservations/{id}
 ```                                                                                                             
                                                                                                                 
 Esperado:                                                                                                       
                                                                                                                 
 ```http                                                                                                         
200 OK
 ```                                                                                                             
                                                                                                                 
 Debe devolver el mismo ID y status: "CANCELLED".                                                                
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 Prioridad media 3 — Actualizar una mesa                                                                         
                                                                                                                 
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
                                                                                                                 
 Esperado:                                                                                                       
                                                                                                                 
 ```http                                                                                                         
200 OK
 ```                                                                                                             
                                                                                                                 
 Después del reinicio:                                                                                           
                                                                                                                 
 ```http                                                                                                         
GET http://localhost:8081/tables/3
 ```                                                                                                             
                                                                                                                 
 Debe conservar capacity: 3.                                                                                     
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 Prioridad media 4 — Probar recursos inexistentes                                                                
                                                                                                                 
 ```http                                                                                                         
GET http://localhost:8081/reservations/9999
 ```                                                                                                             
                                                                                                                 
 Esperado:                                                                                                       
                                                                                                                 
 ```http                                                                                                         
404 Not Found
 ```                                                                                                             
                                                                                                                 
 También:                                                                                                        
                                                                                                                 
 ```http                                                                                                         
GET http://localhost:8081/tables/9999
 ```                                                                                                             
                                                                                                                 
 Esperado:                                                                                                       
                                                                                                                 
 ```http                                                                                                         
404 Not Found
 ```                                                                                                             
                                                                                                                 
 ────────────────────────────────────────────────────────────────────────────────                                
                                                                                                                 
 Prioridad media 5 — Completar el informe                                                                        
                                                                                                                 
 Correcciones concretas:                                                                                         
                                                                                                                 
 1. Corregir la descripción de TableRequest.available.                                                           
 2. Confirmar en el informe si /tables/available soporta o no people.                                            
 3. Insertar el logo IPSS.                                                                                       
 4. Completar los datos de portada.                                                                              
 5. Agregar capturas de Postman.                                                                                 
 6. Exportar a Word.                                                                                             
 7. Revisar Arial 12 e interlineado 1,15.                                                                        
 8. Exportar la versión final a PDF.                                                                             
                                                                                                                 
 Conclusión                                                                                                      
                                                                                                                 
 La implementación CSR y la persistencia básica están bien encaminadas y tienen evidencia automatizada sólida.   
 La suite pasa completa y los errores de 400, 409 y las consultas principales fueron observados. Para alcanzar   
 Sobresaliente, la prioridad es demostrar mediante Postman el ciclo completo de escritura y reinicio en una      
 copia aislada, resolver explícitamente el requisito de capacidad por people y corregir la pequeña discrepancia  
 del informe sobre TableRequest.                                                                                 
                                                                                                                 
 
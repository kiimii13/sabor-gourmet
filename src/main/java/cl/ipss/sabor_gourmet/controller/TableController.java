package cl.ipss.sabor_gourmet.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cl.ipss.sabor_gourmet.model.Table;
import cl.ipss.sabor_gourmet.model.TableRequest;
import cl.ipss.sabor_gourmet.service.TableService;

import java.net.URI;
import java.util.List;

/**
 * Expone el inventario de mesas y las consultas de disponibilidad.
 */
@RestController
@RequestMapping("/tables")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    // La consulta no aplica filtros: devuelve el inventario persistido completo.
    @GetMapping
    public ResponseEntity<List<Table>> getTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    // El Service decide si el rango es un turno, un día indicado o el día actual.
    @GetMapping("/available")
    public ResponseEntity<List<Table>> getAvailableTables(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) Integer people
    ) {
        return ResponseEntity.ok(tableService.getAvailableTables(date, startTime, people));
    }

    // Se devuelve cada mesa con alguna reserva confirmada superpuesta, una sola vez.
    @GetMapping("/reserved")
    public ResponseEntity<List<Table>> getReservedTables(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startTime
    ) {
        return ResponseEntity.ok(tableService.getReservedTables(date, startTime));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Table> getTable(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.getTable(id));
    }

    // El Service impide superar el máximo de diez mesas antes de escribir el JSON.
    @PostMapping
    public ResponseEntity<Table> createTable(@Valid @RequestBody TableRequest request) {
        Table table = tableService.createTable(request);
        return ResponseEntity.created(URI.create("/tables/" + table.id())).body(table);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Table> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody TableRequest request
    ) {
        return ResponseEntity.ok(tableService.updateTable(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}

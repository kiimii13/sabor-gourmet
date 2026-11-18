package cl.ipss.sabor_gourmet.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;
import cl.ipss.sabor_gourmet.model.Customer;
import cl.ipss.sabor_gourmet.model.Reservation;
import cl.ipss.sabor_gourmet.model.RestaurantData;
import cl.ipss.sabor_gourmet.model.Table;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * Repositorio para el archivo JSON del restaurante
 */
@Repository
public class RestaurantJsonRepository {

    private static final Path DEFAULT_DATA_FILE = Path.of("data", "sabor-gourmet.json");

    private final ObjectMapper objectMapper;
    private final Path dataFile;

    @Autowired
    public RestaurantJsonRepository(ObjectMapper objectMapper) {
        this(objectMapper, DEFAULT_DATA_FILE);
    }

    public RestaurantJsonRepository(ObjectMapper objectMapper, Path dataFile) {
        this.objectMapper = objectMapper;
        this.dataFile = dataFile;
    }

    // Lee el archivo JSON y lo convierte en datos
    public synchronized RestaurantData read() {
        try {
            return normalize(objectMapper.readValue(Files.readString(dataFile), RestaurantData.class));
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudieron leer los datos del restaurante", exception);
        }
    }

    // Lee el JSON, aplica el cambio y lo guarda.
    public synchronized <T> T update(Function<RestaurantData, T> operation) {
        RestaurantData data = read();
        T result = operation.apply(data);
        write(data);
        return result;
    }

    // Guarda el JSON con los datos ya modificados.
    private void write(RestaurantData data) {
        try {
            Files.createDirectories(dataFile.getParent());
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            Files.writeString(
                    dataFile,
                    json,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudieron leer los datos del restaurante", exception);
        }
    }

    // Completa los datos con listas vacías para evitar nulls en los Services.
    private RestaurantData normalize(RestaurantData data) {
        if (data == null) {
            return new RestaurantData();
        }

        if (data.getCustomers() == null) {
            data.setCustomers(new ArrayList<Customer>());
        }
        if (data.getTables() == null) {
            data.setTables(new ArrayList<Table>());
        }
        if (data.getReservations() == null) {
            data.setReservations(new ArrayList<Reservation>());
        }

        return data;
    }
}

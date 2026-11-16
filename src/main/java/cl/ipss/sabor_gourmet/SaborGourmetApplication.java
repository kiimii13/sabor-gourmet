package cl.ipss.sabor_gourmet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de Spring Boot. Desde aquí se crea el contexto que descubre
 * los controladores, servicios, repositorio y el manejador global de errores.
 */
@SpringBootApplication
public class SaborGourmetApplication {

	// Spring Boot inicia el servidor HTTP y registra los componentes de la aplicación.
	public static void main(String[] args) {
		SpringApplication.run(SaborGourmetApplication.class, args);
	}

}

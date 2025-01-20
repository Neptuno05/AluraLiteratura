package com.alura.literalura.principal;

import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.model.*;
import com.alura.literalura.repository.LibrosRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class Principal {
    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private final ConvierteDatos conversor = new ConvierteDatos();
    @Autowired
    private LibrosRepository librosRepositorio;
    @Autowired
    private AutorRepository autorRepositorio;

    public Principal(AutorRepository repository, LibrosRepository librosRepository){
        this.autorRepositorio = repository;
        this.librosRepositorio = librosRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            mostrarMenu();
            if (teclado.hasNextInt()) {
                opcion = teclado.nextInt();
                teclado.nextLine(); // Limpiar el buffer del scanner
                if (opcion >= 0 && opcion <= 5) {
                    seleccionarProceso(opcion);
                } else {
                    mostrarMensajeOpcionInvalida();
                }
            } else {
                mostrarMensajeOpcionInvalida();
                teclado.next(); // Limpiar la entrada no válida
            }
        }
        teclado.close();
    }
    private void mostrarMensajeOpcionInvalida(){
        System.out.println("Opción inválida. Por favor, ingrese un número entre 0 y 5.");
    }
    private static void mostrarMenu(){
        var menu = """
                    ---------->
                    Seleccione una opcíon numérica:
                    1 - Buscar libro por título.
                    2 - Listar libros por registrados.
                    3 - Listar autores registrados.
                    4 - Listar autores vivos en un determinado año.
                    5 - Listar libros por idioma.

                    0 - Salir <-|
                    """;
        System.out.println(menu);
    }
    private void seleccionarProceso(int numeroProceso){
        switch (numeroProceso) {
            case 1:
                buscarlibros();
                break;
            case 2:
                mostrarLibrosBuscados();
                break;
            case 3:
                mostrarAutoresRegistrados();
                break;
            case 4:
                mostrarPorAutoresVivosPorAnio();
                break;
            case 5:
                mostrarLibroPorIdioma();
                break;
            case 0:
                System.out.println("Cerrando la aplicación...\n" + "¡Gracias por usar el sistema! Hasta pronto.");
                break;
        }
    }

    private void buscarlibros() {
        try {
            System.out.println("\n---- BÚSCAR LIBRO ----");
            System.out.print("Escriba el título del libro: ");
            String nombreLibro = teclado.nextLine().trim();
            if (nombreLibro.isEmpty()) {
                System.out.println("El título no puede estar vacío.");
                return;
            }

            // Buscar primero en la base de datos local
            Optional<Libros> libroEnBaseDatos = librosRepositorio.findByTituloContainingIgnoreCase(nombreLibro);
            if (libroEnBaseDatos.isPresent()) {
                System.out.println("\nLibro encontrado en la base de datos:");
                System.out.println(libroEnBaseDatos.get());
                return;
            }
            // Si no está en la Base de Datos, buscar en la API
            buscarLibroEnApi(nombreLibro);
        } catch (Exception e) {
            System.out.println("Error durante la búsqueda: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void buscarLibroEnApi(String nombreLibro){
        System.out.println("Buscando en la API externa...");
        String json = consumoApi.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "%20"));

        if (json == null || json.isEmpty()) {
            System.out.println("No se recibió respuesta de la API");
            return;
        }

        ResultDatosLibros datosBusqueda = conversor.obtenerDatos(json, ResultDatosLibros.class);

        if (datosBusqueda.libros() == null || datosBusqueda.libros().isEmpty()) {
            System.out.println("No se encontraron resultados para: " + nombreLibro);
            return;
        }
        procesarResultadosBusqueda(datosBusqueda.libros(), nombreLibro);
    }

    private void procesarResultadosBusqueda(List<DatosLibros> resultados, String tituloLibro) {
        boolean encontrado = false;
        for (DatosLibros datosLibro : resultados) {
            if (datosLibro.titulo().toUpperCase().contains(tituloLibro.toUpperCase())) {
                guardarLibroYAutor(datosLibro);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            System.out.println("No se encontró ningún libro que coincida exactamente con: " + tituloLibro);
        }
    }

    private void guardarLibroYAutor(DatosLibros datosLibro) {
        try {
            if (datosLibro.autores() == null || datosLibro.autores().isEmpty()) {
                System.out.println("El libro no tiene autor registrado.");
                return;
            }

            // Guardar o recuperar el autor
            DatosAutor datosAutor = datosLibro.autores().get(0);
            Autor autor = autorRepositorio.findByNombre(datosAutor.nombreAutor())
                    .orElseGet(() -> {
                        Autor nuevoAutor = new Autor(datosAutor);
                        System.out.println("Guardando nuevo autor: " + datosAutor.nombreAutor());
                        return autorRepositorio.save(nuevoAutor);
                    });

            // Verificar si el libro ya existe
            if (librosRepositorio.findByTituloContainingIgnoreCase(datosLibro.titulo()).isPresent()) {
                System.out.println("El libro ya existe en la base de datos.");
                return;
            }

            // Guardar el nuevo libro
            Libros libro = new Libros(datosLibro, autor);
            Libros libroGuardado = librosRepositorio.save(libro);
            System.out.println("\nLibro guardado exitosamente:");
            System.out.println(libroGuardado);

        } catch (Exception e) {
            System.out.println("Guardar Libro: Error al guardar el libro y autor: " + e.getMessage());
        }
    }

    private void mostrarLibrosBuscados() {
        System.out.println("\n---- LIBROS REGISTRADOS ----");
        List<Libros> libros = librosRepositorio.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en la base de datos.");
            return;
        }
        libros.forEach(System.out::println);
    }

    private void mostrarAutoresRegistrados() {
        System.out.println("\n---- AUTORES REGISTRADOS ----");
        List<Autor> autores = autorRepositorio.findAutoresConLibros();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados en la base de datos.");
            return;
        }

        for (Autor autor : autores) {
            Integer fechaNacimiento = autor.getAnioNacimiento() != null ? autor.getAnioNacimiento() : 0;
            Integer fechaFallecimiento = autor.getAnioFallecimiento() != null ? autor.getAnioFallecimiento() : 0;

            System.out.printf("Autor: %s | Fecha de nacimiento: %s - Fecha de fallecimiento: %s%n",
                    autor.getNombre(), fechaNacimiento, fechaFallecimiento);

            System.out.println("Libros:");
            autor.getLibros().forEach(libro -> System.out.printf("- %s (Idioma: %s, Descargas: %s)%n",
                    libro.getTitulo(), libro.getIdioma(), libro.getNumeroDescargas()));
            System.out.println("--------------------------------------------------");
        }
    }
    private void mostrarPorAutoresVivosPorAnio() {
        try {
            System.out.println("\n---- AUTORES VIVOS POR AÑO ----");
            System.out.println("Ingrese el año para consultar: ");
            int anio = Integer.parseInt(teclado.nextLine().trim());

            if (anio < 0 || anio > 2024) {
                System.out.println("Por favor, ingrese un año válido.");
                return;
            }

            System.out.println("Autores vivos en " + anio + ":");
            List<Autor> autores = autorRepositorio.findAutoresVivosEnAnioConLibros(anio);
            if (autores.isEmpty()){
                System.out.println("No se encontraron autores vivos para el año especificado.");
            }else {
                autores.forEach(a -> {
                    System.out.println("- " + a.getNombre() +
                            " (Nacimiento: " + a.getAnioNacimiento() +
                            " | Fallecimiento: " + a.getAnioFallecimiento() + ")");
                    a.getLibros().forEach(libro ->
                            System.out.println("  * Libro: " + libro.getTitulo()));
                });
            }


        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingrese un año válido en formato numérico.");
        } catch (Exception e) {
            System.out.println("Error al procesar la consulta: " + e.getMessage());
        }
    }
    private void mostrarLibroPorIdioma() {
        System.out.println("\n---- LIBROS POR IDIOMA ----");
        System.out.println("Idiomas disponibles: ES (Español), EN (Inglés), FR (Francés), PT (Portugués)");
        System.out.print("Ingrese el código del idioma: ");

        String idioma = teclado.nextLine().trim().toUpperCase();
        if (!esIdiomaValido(idioma)) {
            System.out.println("Idioma no válido. Use: ES, EN, FR o PT");
            return;
        }

        List<Libros> librosFiltrados = buscarLibrosPorIdioma(idioma);
        if (librosFiltrados.isEmpty()) {
            System.out.println("No se encontraron libros en " + obtenerNombreIdioma(idioma));
            return;
        }

        System.out.println("\n -> Libros en " + obtenerNombreIdioma(idioma) + ":");
        librosFiltrados.forEach(libro ->
                System.out.printf("- %s (Autor: %s)%n",
                        libro.getTitulo(),
                        libro.getAutor().getNombre())
        );
    }

    private boolean esIdiomaValido(String idioma) {
        return idioma.matches("^(ES|EN|FR|PT)$");
    }

    private String obtenerNombreIdioma(String codigo) {
        return switch (codigo) {
            case "ES" -> "Español";
            case "EN" -> "Inglés";
            case "FR" -> "Francés";
            case "PT" -> "Portugués";
            default -> codigo;
        };
    }
    private List<Libros> buscarLibrosPorIdioma(String idioma){
        return librosRepositorio.findByIdiomaContainingIgnoreCase(idioma);
    }

}

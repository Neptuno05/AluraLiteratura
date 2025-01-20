package com.alura.literalura.repository;

import com.alura.literalura.model.Autor;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT a FROM Autor a JOIN FETCH a.libros")
    List<Autor> findAutoresConLibros(); //Obtener autor con sus libros

    Optional<Autor> findByNombre(String nombre);

    @Query("SELECT a FROM Autor a LEFT JOIN FETCH a.libros WHERE (a.anioFallecimiento IS NULL OR a.anioFallecimiento > :anio) AND a.anioNacimiento <= :anio")
    List<Autor> findAutoresVivosEnAnioConLibros(int anio);

}

package com.alura.literalura.repository;

import com.alura.literalura.model.Libros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibrosRepository extends JpaRepository<Libros, Long> {
    Optional<Libros> findByTituloContainingIgnoreCase(String titulo);

    List<Libros> findByIdiomaContainingIgnoreCase(String idioma);
}

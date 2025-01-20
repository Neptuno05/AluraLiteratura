package com.alura.literalura.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Integer anioNacimiento;
    private Integer anioFallecimiento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Libros> libros;

    public Autor(){}

    public Autor(DatosAutor datosAutor) {
        this.nombre = datosAutor.nombreAutor();
        this.anioNacimiento = datosAutor.anioNacimiento();
        this.anioFallecimiento = datosAutor.anioFallecimiento();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getAnioNacimiento() { return anioNacimiento; }
    public void setAnioNacimiento(Integer anioNacimiento) { this.anioNacimiento = anioNacimiento; }

    public Integer getAnioFallecimiento() {return anioFallecimiento; }
    public void setAnioFallecimiento(Integer anioFallecimiento) { this.anioFallecimiento = anioFallecimiento; }

    public List<Libros> getLibros() {return libros;}
    public void setLibros(List<Libros> libros){this.libros = libros;}
}

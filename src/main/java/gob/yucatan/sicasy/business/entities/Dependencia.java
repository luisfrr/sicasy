package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "dependencia")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Dependencia implements Cloneable, Serializable {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dependencia_id", nullable = false)
    private Integer idDependencia;

    @Column(name = "clave_presupuestal", nullable = false)
    private String clavePresupuestal;

    @Column(name = "abreviatura", nullable = false)
    private String abreviatura;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "director")
    private String director;

    @Column(name = "puesto")
    private String puesto;

    @Column(name = "no_oficio")
    private String noOficio;

    @Override
    public Dependencia clone() {
        try {
            return (Dependencia) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    //region Getters & Setters

    public String getClavePresupuestal() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(clavePresupuestal);
    }

    public void setClavePresupuestal(String clavePresupuestal) {
        this.clavePresupuestal = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(clavePresupuestal);
    }

    public String getAbreviatura() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(abreviatura);
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(abreviatura);
    }

    public String getNombre() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(nombre);
    }

    public void setNombre(String nombre) {
        this.nombre = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(nombre);
    }

    public String getDirector() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(director);
    }

    public void setDirector(String director) {
        this.director = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(director);
    }

    public String getPuesto() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(puesto);
    }

    public void setPuesto(String puesto) {
        this.puesto = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(puesto);
    }

    public String getNoOficio() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(noOficio);
    }

    public void setNoOficio(String noOficio) {
        this.noOficio = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(noOficio);
    }

    //endregion Getters & Setters
}

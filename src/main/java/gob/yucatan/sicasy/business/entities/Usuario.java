package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusUsuario;
import gob.yucatan.sicasy.utils.strings.HtmlEntityConverter;
import jakarta.persistence.*;
import lombok.*;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuario", schema = "sec")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Usuario implements UserDetails {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long idUsuario;

    @Setter
    @Getter
    @Column(name = "usuario", nullable = false)
    private String usuario;

    @Setter
    @Getter
    @Column(name = "contrasenia", nullable = false)
    private String contrasenia;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "email", nullable = false)
    private String email;

    @Setter
    @Getter
    @Column(name = "correo_confirmado")
    private Integer correoConfirmado;

    @Setter
    @Getter
    @Column(name = "token")
    private String token;

    @Setter
    @Getter
    @Column(name = "vigencia_token")
    private Date vigenciaToken;

    @Setter
    @Getter
    @Column(name = "token_type")
    private String tokenType;

    @Setter
    @Getter
    @OneToMany(cascade=CascadeType.ALL, mappedBy="usuario", fetch = FetchType.EAGER)
    private Set<UsuarioRol> usuarioRolSet;

    @Setter
    @Getter
    @Column(name = "estatus", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusUsuario estatus;

    @Setter
    @Getter
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por")
    private String modificadoPor;

    @Setter
    @Getter
    @Column(name = "fecha_borrado")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por")
    private String borradoPor;

    //region Transients

    @Setter
    @Getter
    @Transient
    private boolean rolOwner;

    @Setter
    @Getter
    @Transient
    private List<Long> idRolList;

    @Setter
    @Transient
    @Getter(AccessLevel.NONE)
    private Collection<? extends GrantedAuthority> authorities;

    //endregion Transients

    //region Getters & Setters

    public String getNombre() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(nombre);
    }

    public void setNombre(String nombre) {
        this.nombre = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(nombre);
    }

    public String getEmail() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(email);
    }

    public void setEmail(String email) {
        this.email = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(email);
    }

    public String getCreadoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(creadoPor);
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(creadoPor);
    }

    public String getModificadoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(modificadoPor);
    }

    public void setModificadoPor(String modificadoPor) {
        this.modificadoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(modificadoPor);
    }

    public String getBorradoPor() {
        return HtmlEntityConverter.convertHtmlEntitiesToSymbols(borradoPor);
    }

    public void setBorradoPor(String borradoPor) {
        this.borradoPor = HtmlEntityConverter.convertSymbolsAndReservedWordsToHtmlEntities(borradoPor);
    }

    //endregion Getters & Setters


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.contrasenia;
    }

    @Override
    public String getUsername() {
        return this.usuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.estatus != null && this.estatus == EstatusUsuario.ACTIVO;
    }

    public String getRoles() {
        String roles = Strings.EMPTY;
        if(this.usuarioRolSet != null && !this.usuarioRolSet.isEmpty()) {
            roles = this.usuarioRolSet.stream()
                    .map(usuarioRol -> usuarioRol.getRol().getNombre())
                    .collect(Collectors.joining(", "));
        }
        return roles;
    }

    public Usuario(Usuario original) {
        this.idUsuario = original.getIdUsuario();
        this.usuario = original.getUsuario();
        this.contrasenia = original.getContrasenia();
        this.nombre = original.getNombre();
        this.email = original.getEmail();
        this.correoConfirmado = original.getCorreoConfirmado();
        this.token = original.getToken();
        this.vigenciaToken  = original.getVigenciaToken();
        this.tokenType = original.getTokenType();
        this.usuarioRolSet  = original.getUsuarioRolSet();
        this.estatus = original.getEstatus();
        this.fechaCreacion = original.getFechaCreacion();
        this.creadoPor  = original.getCreadoPor();
        this.fechaModificacion = original.getFechaModificacion();
        this.modificadoPor  = original.getModificadoPor();
        this.fechaBorrado = original.getFechaBorrado();
        this.borradoPor  = original.getBorradoPor();
        this.rolOwner = original.isRolOwner();
        this.idRolList = original.getIdRolList();
        this.authorities = original.getAuthorities();
    }

}

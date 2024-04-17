package gob.yucatan.sicasy.business.entities;

import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "usuario", schema = "sec")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long idUsuario;

    @Column(name = "usuario", nullable = false)
    private String usuario;

    @Column(name = "contrasenia", nullable = false)
    private String contrasenia;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "correo_confirmado")
    private Integer correoConfirmado;

    @Column(name = "token")
    private String token;

    @Column(name = "vigencia_token")
    private Date vigenciaToken;

    @Column(name = "token_type")
    private String tokenType;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="usuario")
    private Set<UsuarioRol> usuarioRolSet;

    @Column(name = "estatus", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private EstatusRegistro estatus;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;

    @Column(name = "fecha_modificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaModificacion;

    @Column(name = "modificado_por")
    private String modificadoPor;

    @Column(name = "fecha_borrado")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaBorrado;

    @Column(name = "borrado_por")
    private String borradoPor;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if(this.usuarioRolSet != null && !this.usuarioRolSet.isEmpty()) {
            this.usuarioRolSet.forEach(usuarioRol -> authorities.add(new SimpleGrantedAuthority(usuarioRol.getRol().getCodigo())));
        }
        return authorities;
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
        return this.estatus != null && this.estatus == EstatusRegistro.ACTIVO;
    }

}

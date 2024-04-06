package gob.yucatan.sicasy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security Configuration.
 *
 * @author Luis Fernando Rogel Rosado
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationManager customAuthenticationManager;
    private final CustomAuthFailureHandler customAuthFailureHandler;

    /**
     * Configure security.
     * @param http security var.
     * @return the security filter chain.
     **/
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) {
        try {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests((authorize) -> authorize
                            .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                            //.requestMatchers(new AntPathRequestMatcher("/**.faces")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/jakarta.faces.resource/**")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                            .anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults())
                    .formLogin(formLogin -> formLogin
                            .loginPage("/login.faces").permitAll()
                            .usernameParameter("username")
                            .passwordParameter("password")
                            .loginProcessingUrl("/login.faces")
                            .defaultSuccessUrl("/views/home.faces", true)
                            .failureHandler(customAuthFailureHandler))
                    .authenticationManager(customAuthenticationManager)
                    .logout((logout) -> logout
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/login.faces?logout=true")
                            .deleteCookies("JSESSIONID")
                            .invalidateHttpSession(true))
                    .sessionManagement(sessionConfig -> {
                        sessionConfig.maximumSessions(3);
                        sessionConfig.invalidSessionUrl("/login.faces");
                    })
                    .build();
        }
        catch (Exception ex) {
            throw new BeanCreationException("Wrong spring security configuration", ex);
        }
    }
}

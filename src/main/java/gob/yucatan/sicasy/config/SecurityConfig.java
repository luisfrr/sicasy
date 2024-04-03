package gob.yucatan.sicasy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
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


    /**
     * Configure security.
     * @param http security var.
     * @param mvc to create request matchers not handled by faces.
     * @return the security filter chain.
     **/
    @Bean
    public SecurityFilterChain configure(HttpSecurity http, MvcRequestMatcher.Builder mvc) {
        try {
            http.csrf(AbstractHttpConfigurer::disable);
            http.authorizeHttpRequests((authorize) -> authorize
                            .requestMatchers(mvc.pattern("/")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/jakarta.faces.resource/**")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                            .anyRequest().authenticated())
                    .formLogin((formLogin) -> formLogin
                            .loginPage("/login.faces").permitAll()
                            .failureUrl("/login.faces?error=true")
                            .defaultSuccessUrl("/views/home.faces"))
                    .logout((logout) -> logout
                            .logoutSuccessUrl("/login.faces")
                            .deleteCookies("JSESSIONID"));
            return http.build();
        }
        catch (Exception ex) {
            throw new BeanCreationException("Wrong spring security configuration", ex);
        }
    }

    @Scope("prototype")
    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspect) {
        return new MvcRequestMatcher.Builder(introspect);
    }

    /**
     * UserDetailsService that configures an in-memory users store.
     * @return a manager that keeps all the users' info in the memory
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        InMemoryUserDetailsManager result = new InMemoryUserDetailsManager();
        Collection<GrantedAuthority> emptyAuthorities = Collections.emptyList();
        result.createUser(User.builder()
                .username("sicasy")
                .password(encoder.encode("12345678"))
                .authorities(emptyAuthorities)
                .build());
        return result;
    }


}

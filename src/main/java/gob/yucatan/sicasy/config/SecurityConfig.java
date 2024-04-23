package gob.yucatan.sicasy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;


/**
 * Spring Security Configuration.
 *
 * @author Luis Fernando Rogel Rosado
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
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
    public SecurityFilterChain configure(HttpSecurity http, MvcRequestMatcher.Builder mvc) {
        try {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests((authorize) -> authorize
                            .requestMatchers(mvc.pattern("/")).permitAll()
                            .requestMatchers(mvc.pattern("/views/auth/activate.faces")).permitAll()
                            .requestMatchers(mvc.pattern("/views/auth/forgotpassword.faces")).permitAll()
                            .requestMatchers(mvc.pattern("/views/auth/resetpassword.faces")).permitAll()
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

    @Scope("prototype")
    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }
}

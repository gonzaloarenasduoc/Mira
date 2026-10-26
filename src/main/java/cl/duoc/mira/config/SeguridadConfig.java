package cl.duoc.mira.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Reglas de acceso por rol, tomadas de la matriz de permisos de docs/01-contexto-negocio.md.
 * Se validan en el servidor: ocultar una opción del menú no basta (HU-03, CF-11).
 */
@Configuration
public class SeguridadConfig {

    /** Roles que crean y modifican inventario (E002 y E003). Vendedor y jefatura solo consultan. */
    static final String[] EDITAN_INVENTARIO = {"ADMINISTRADOR", "ENCARGADO_BODEGA"};

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(acceso -> acceso
                .requestMatchers("/login", "/error", "/webjars/**", "/css/**", "/img/**").permitAll()
                .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/bodegas/nueva", "/bodegas/*/editar", "/equipos/nuevo", "/traslados/nuevo")
                    .hasAnyRole(EDITAN_INVENTARIO)
                .requestMatchers(HttpMethod.POST, "/bodegas/**", "/equipos/**", "/traslados/**")
                    .hasAnyRole(EDITAN_INVENTARIO)
                .anyRequest().authenticated())
            .formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .logout(salida -> salida.logoutSuccessUrl("/login?salir"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

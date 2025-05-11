package com.isitempty.backend.oauthlogin.config.security;

import com.isitempty.backend.oauthlogin.api.repository.user.UserRefreshTokenRepository;
import com.isitempty.backend.oauthlogin.config.properties.AppProperties;
import com.isitempty.backend.oauthlogin.config.properties.CorsProperties;
import com.isitempty.backend.oauthlogin.oauth.entity.RoleType;
import com.isitempty.backend.oauthlogin.oauth.exception.RestAuthenticationEntryPoint;
import com.isitempty.backend.oauthlogin.oauth.filter.TokenAuthenticationFilter;
import com.isitempty.backend.oauthlogin.oauth.handler.OAuth2AuthenticationFailureHandler;
import com.isitempty.backend.oauthlogin.oauth.handler.OAuth2AuthenticationSuccessHandler;
import com.isitempty.backend.oauthlogin.oauth.handler.TokenAccessDeniedHandler;
import com.isitempty.backend.oauthlogin.oauth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.isitempty.backend.oauthlogin.oauth.service.CustomOAuth2UserService;
import com.isitempty.backend.oauthlogin.oauth.service.CustomUserDetailsService;
import com.isitempty.backend.oauthlogin.oauth.token.AuthTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final AppProperties appProperties;
    private final AuthTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final TokenAccessDeniedHandler tokenAccessDeniedHandler;
    private final UserRefreshTokenRepository userRefreshTokenRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((AuthenticationEntryPoint) new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(tokenAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers(
                                "/api/v1/users/signup",
                                "/api/v1/users/createadmin",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/parking-lots/**",
                                "/api/camera/**",
                                "/api/toilet/**",
                                "/api/reviews/**",
                                "/oauth2/**",
                                "/api/favorites/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/question").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/question").hasAuthority(RoleType.ADMIN.getCode())
                        .requestMatchers("/api/admin/**").hasAuthority(RoleType.ADMIN.getCode())
                        .requestMatchers("/api/v1/users/**").hasAnyAuthority(RoleType.ADMIN.getCode(), RoleType.USER.getCode())
                        .requestMatchers("/api/**").hasAnyAuthority(RoleType.ADMIN.getCode(), RoleType.USER.getCode())
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authEndpoint -> authEndpoint
                                .baseUri("/oauth2/authorization")
                                .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                        )
                        .redirectionEndpoint(redir -> redir.baseUri("/*/oauth2/code/*"))
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler())
                        .failureHandler(oAuth2AuthenticationFailureHandler())
                );

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(
                tokenProvider,
                appProperties,
                userRefreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository()
        );
    }

    @Bean
    public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler(oAuth2AuthorizationRequestBasedOnCookieRepository());
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedHeaders(Arrays.asList(corsProperties.getAllowedHeaders().split(",")));
        corsConfig.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods().split(",")));
        corsConfig.setAllowedOrigins(Arrays.asList(corsProperties.getAllowedOrigins().split(",")));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(corsConfig.getMaxAge());

        corsConfigSource.registerCorsConfiguration("/**", corsConfig);
        return corsConfigSource;
    }

}

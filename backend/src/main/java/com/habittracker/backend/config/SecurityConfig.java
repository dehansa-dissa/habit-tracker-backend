package com.habittracker.backend.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${asgardeo.jwk.n}")
    private String jwkN;

    @Value("${asgardeo.jwk.e}")
    private String jwkE;

    @Value("${asgardeo.jwk.kid}")
    private String jwkKid;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(jwkN);
            byte[] eBytes = Base64.getUrlDecoder().decode(jwkE);
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(
                            new BigInteger(1, nBytes),
                            new BigInteger(1, eBytes)
                    ));

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .keyID(jwkKid)
                    .build();

            ImmutableJWKSet<SecurityContext> jwkSet =
                    new ImmutableJWKSet<>(new JWKSet(rsaKey));

            DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSTypeVerifier(
                    new DefaultJOSEObjectTypeVerifier<>(
                            new JOSEObjectType("at+jwt"),
                            new JOSEObjectType("JWT"),
                            JOSEObjectType.JWT,
                            null
                    )
            );
            processor.setJWSKeySelector(
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSet)
            );

            return new NimbusJwtDecoder(processor);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to build JWT decoder", ex);
        }
    }
}
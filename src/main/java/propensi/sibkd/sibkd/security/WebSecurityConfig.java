package propensi.sibkd.sibkd.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import propensi.sibkd.sibkd.model.*;
import propensi.sibkd.sibkd.repository.UserDao;
import propensi.sibkd.sibkd.service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    
    
    @Qualifier("penggunaServiceImpl")
    @Autowired
    private PenggunaService penggunaService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        .antMatchers("/", "/login", "/oauth/**", "/js/**","/css/**","/image/**").permitAll()
        .antMatchers("/user", "/user/**", "/user/edit/**", "/kegiatan-bkd/**").hasAnyAuthority("Admin")
        .antMatchers("/biodata/**", "/rangkuman-bkd").hasAnyAuthority("Dosen")
        .antMatchers("/semester/**").hasAnyAuthority("Admin")
        .antMatchers("/jadwal-bkd/**").hasAnyAuthority("Admin")
        .antMatchers("/item/**", "item/**/add/","item/**/add/**").hasAnyAuthority("Dosen")
        .antMatchers("/assessment/**").hasAnyAuthority("Asesor","Kaprodi")
        .antMatchers("/pelaporan-bkd/edit-asesor/**").hasAnyAuthority("Staff SDM")
        .antMatchers("/pelaporan-bkd/view-all/**").hasAnyAuthority("Staff SDM","Kaprodi","Rektor")
        .antMatchers("/export-laporan/**").hasAnyAuthority("Staff SDM")
        .antMatchers("/reminder/**").hasAnyAuthority("Staff SDM")
        .antMatchers("/api/**").permitAll()
        .anyRequest().authenticated()
        .and()
        .oauth2Login()
            .userInfoEndpoint()
            .userAuthoritiesMapper(authoritiesMapper())
            .and()
            .successHandler(new AuthenticationSuccessHandler() {
                @Override
                public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication auth) throws IOException, ServletException {
         
                    CustomOAuth2User oauthUser = (CustomOAuth2User) auth.getPrincipal();
                    String email = oauthUser.getEmail();

                    penggunaService.processOAuthPostLogin(email, auth);
         
                    response.sendRedirect("/");
                }
            })
            .and()
            .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/").permitAll();


    }



    private GrantedAuthoritiesMapper authoritiesMapper(){
        return (authorities) -> {
            String emailAttrName = "email";
            String email = authorities.stream()
                    .filter(OAuth2UserAuthority.class::isInstance)
                    .map(OAuth2UserAuthority.class::cast)
                    .filter(userAuthority -> userAuthority.getAttributes().containsKey(emailAttrName))
                    .map(userAuthority -> userAuthority.getAttributes().get(emailAttrName).toString())
                    .findFirst()
                    .orElse(null);

            if (email == null) {
                return authorities;		// data email tidak ada di userInfo dari Google
            }

            Pengguna user = penggunaService.findByEmail(email);
            if(user == null) {
                return authorities;     // email user ini belum terdaftar di database
            }

            List<Permission> userAuthorities = user.getRole().getPermissions();
            if (userAuthorities.isEmpty()) {
                return authorities;		// Return the 'unmapped' authorities
            }

            return Stream.concat(
                        authorities.stream(),
                        userAuthorities.stream()
                            .map(Permission::getNamaPermission)
                            .map(SimpleGrantedAuthority::new)
                    ).collect(Collectors.toCollection(ArrayList::new));
        };
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}

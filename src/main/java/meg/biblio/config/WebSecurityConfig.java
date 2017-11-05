package meg.biblio.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity(debug = false)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/welcome").permitAll()
                .antMatchers("/", "/themes/**").permitAll()
                .antMatchers("/", "/resources/images/**").permitAll()
                .antMatchers("/", "/resources/spring/**").permitAll()
                .antMatchers("/", "/resources/dijit/themes/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    private PasswordEncoder myPasswordEncoder() {
        return new StandardPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .jdbcAuthentication()
                .passwordEncoder(myPasswordEncoder())
                .dataSource(dataSource)
                .authoritiesByUsernameQuery("select a.username as username, p.rolename as authority from userlogin a, grouprole p where  p.id =a.role and a.username =?")
                .usersByUsernameQuery("select username, password, enabled from userlogin where username =  ?");
    }

}

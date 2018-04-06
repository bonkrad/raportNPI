package pl.radekbonk.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import waffle.spring.NegotiateSecurityFilter;
import waffle.spring.NegotiateSecurityFilterEntryPoint;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private NegotiateSecurityFilter negotiateSecurityFilter;

	@Autowired
	private NegotiateSecurityFilterEntryPoint entryPoint;

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
				.authorizeRequests()
					.antMatchers("/h2","/h2/**").permitAll()
					.anyRequest()
					.authenticated()
					.and()
				.httpBasic()
					.authenticationEntryPoint(entryPoint)
					.and()
				.addFilterBefore(negotiateSecurityFilter, BasicAuthenticationFilter.class);

		httpSecurity.csrf().disable();
		httpSecurity.headers().frameOptions().disable();
	}

	@Override
	@Autowired
	protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder.inMemoryAuthentication();
	}
}

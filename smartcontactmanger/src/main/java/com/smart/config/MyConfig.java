package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class MyConfig extends WebSecurityConfigurerAdapter {

	@Bean
	public UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
		;
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

		return daoAuthenticationProvider;

	}

	// Config Method.....

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	// Who will access
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// only admin role user will access this.
		// USER will access only /user things.
		// other people can use everything if they dont have any role they cant access
		// user and admin things.
		// if you use custom login page you have to use loginpage() method and in that
		// method you can give your custom login name.
//		http.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN").antMatchers("/user/**").hasRole("USER")
//				.antMatchers("/**").permitAll().and().formLogin().loginPage("/signin").and().csrf().disable();

		/*
		 * only admin role user will access this. 1. USER will access only /user things.
		 * 2. permitAll : other people can use everything if they dont have any role
		 * they cant access user and admin things. 3. if you use custom login page you
		 * have to use loginpage() method and in that method you can give your custom
		 * login page name. 4. failureUrl() method is basically direct you to failure
		 * page which you are using in failureUrl method.
		 */
		http.authorizeRequests().antMatchers("/admin/**").hasRole("ADMIN").antMatchers("/user/**").hasRole("USER")
				.antMatchers("/**").permitAll().and().formLogin().loginPage("/signin").loginProcessingUrl("/dologin")
				.defaultSuccessUrl("/user/index").and().csrf().disable();

	}

}

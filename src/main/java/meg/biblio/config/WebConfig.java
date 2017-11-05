package meg.biblio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;

import java.util.Locale;

@EnableWebMvc
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	// Tiles configuration
	@Bean
	public UrlBasedViewResolver tilesViewResolver() {

		UrlBasedViewResolver tilesViewResolver = new UrlBasedViewResolver();
		tilesViewResolver.setViewClass(TilesView.class);
		return tilesViewResolver;
	}

	@Bean
	public TilesConfigurer tilesConfigurer() {

		TilesConfigurer tconf = new TilesConfigurer();
		tconf.setDefinitions(new String[] { "/WEB-INF/tiles/tiles.xml",
				"/WEB-INF/jsp/view/book/views.xml",
				"/WEB-INF/jsp/view/client/views.xml",
				"/WEB-INF/jsp/view/dashboard/views.xml",
				"/WEB-INF/jsp/view/import/views.xml",
				"/WEB-INF/jsp/view/inventory/views.xml",
				"/WEB-INF/jsp/view/barcode/views.xml",
				"/WEB-INF/jsp/view/lending/views.xml",
				"/WEB-INF/jsp/view/schoolgroups/views.xml",
				"/WEB-INF/jsp/view/userlogins/views.xml"});
		return tconf;

	}

	// ThemeResolver configuration
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/themes/**").addResourceLocations("/themes/");
	}

	@Bean
	public ResourceBundleThemeSource themeSource(){
		ResourceBundleThemeSource themeSource = new ResourceBundleThemeSource();
		themeSource.setBasenamePrefix("themes.");
		return themeSource;
	}

	@Bean
	public CookieThemeResolver themeResolver(){
		CookieThemeResolver resolver = new CookieThemeResolver();
		resolver.setDefaultThemeName("gumballs");
		resolver.setCookieName("my-theme-cookie");
		return resolver;
	}

	@Bean
	public ThemeChangeInterceptor themeChangeInterceptor(){
		ThemeChangeInterceptor interceptor = new ThemeChangeInterceptor();
		interceptor.setParamName("theme");
		return interceptor;
	}

	// language configuration
    @Bean
    public ReloadableResourceBundleMessageSource messageSource(){
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages", "classpath:pdf", "classpath:biblio", "classpath:app-biblio");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public CookieLocaleResolver localeResolver(){
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setCookieName("my-locale-cookie");
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeInterceptor(){
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }


	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(themeChangeInterceptor());
        registry.addInterceptor(localeInterceptor());
	}

	// security configuration
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/hello").setViewName("hello");
		registry.addViewController("/login").setViewName("login");
	}
}

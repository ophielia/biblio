package meg.biblio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class ResourceConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/resources/images/**")
                .addResourceLocations("/WEB-INF/images/");
        registry
                .addResourceHandler("/resources/mydojo/**")
                .addResourceLocations("/WEB-INF/mydojo/");
        registry
                .addResourceHandler("/images/**")
                .addResourceLocations("/WEB-INF/images/");
    }
}
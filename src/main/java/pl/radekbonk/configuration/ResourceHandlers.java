package pl.radekbonk.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class ResourceHandlers extends WebMvcConfigurerAdapter {
	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
			"classpath:/META-INF/resources/", "classpath:/resources/",
			"classpath:/static/", "classpath:/public/" };
//TODO
	public static final String[] CLASSPATH_FILES_LOCATIONS = {
			"file:/H://disk/" };

//TODO
	/*public static final String[] CLASSPATH_FILES_LOCATIONS = {
			"file:/home/pi/raport/disk/" };*/



	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		if (!registry.hasMappingForPattern("/webjars/**")) {
			registry.addResourceHandler("/webjars/**").addResourceLocations(
					"classpath:/META-INF/resources/webjars/");
			registry.addResourceHandler("/disk/**").addResourceLocations(getMappingPath());
		}
		if (!registry.hasMappingForPattern("/**")) {
			registry.addResourceHandler("/**").addResourceLocations(
					CLASSPATH_RESOURCE_LOCATIONS);
			registry.addResourceHandler("/disk/**").addResourceLocations(getMappingPath());
		}
	}

	private static String getMappingPath() {
		if(System.getProperty("os.name").contains("Windows")) {
			return "file:/H://disk/";
		} else {
			return "file:/home/pi/raport/disk/";
		}
	}
}

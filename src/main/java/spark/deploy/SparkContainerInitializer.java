package spark.deploy;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.servlet.SparkApplication;
import spark.servlet.SparkFilter;

@HandlesTypes(SparkApplication.class)
public class SparkContainerInitializer implements ServletContainerInitializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SparkContainerInitializer.class);

	@Override
	public void onStartup(Set<Class<?>> handledTypes, ServletContext context) throws ServletException {
		if (handledTypes == null) {
			throw new ServletException("No " + SparkApplication.class.getSimpleName() + " implementation found!");
		} else {
			if (handledTypes.size() > 1) {
				List<String> implementations =
						handledTypes.stream().map(c -> c.getSimpleName()).collect(Collectors.toList());
				throw new ServletException("Multiple " + SparkApplication.class.getSimpleName() +
						" implementations found: " + String.join(", ", implementations));
			} else {
				Class<?> applicationClass = handledTypes.toArray(new Class[1])[0];
				LOGGER.info("Application class " + applicationClass.getName() + " discovered.");
				FilterRegistration.Dynamic filterRegistration = context.addFilter("SparkFilter", SparkFilter.class);
				filterRegistration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
				filterRegistration.setInitParameter(SparkFilter.APPLICATION_CLASS_PARAM, applicationClass.getName());
				LOGGER.info("Spark filter registered.");
			}
		}
	}

}

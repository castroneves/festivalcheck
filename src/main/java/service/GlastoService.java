package service;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

/**
 * Created by Adam on 27/04/2015.
 */
public class GlastoService extends Application<GlastoConfiguration> {
    public static void main(String[] args) throws Exception {
        String[] calArgs = new String[]{"server", "default.yml"};
        new GlastoService().run(calArgs);
    }

    @Override
    public void initialize(Bootstrap<GlastoConfiguration> configurationBootstrap) {
    }

    @Override
    public void run(GlastoConfiguration configuration, Environment environment) throws Exception {
        // Enable CORS headers
        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        environment.jersey().register((new GlastoResource(configuration)));
    }
}

package com.employeehub.audit.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Prints a clean, colored banner once the service is fully started so the
 * URL/port is easy to spot in the console.
 */
@Component
public class StartupBanner implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment env;

    public StartupBanner(Environment env) {
        this.env = env;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String name = env.getProperty("spring.application.name", "service");
        String port = env.getProperty("server.port", "8080");
        String profile = String.join(",", env.getActiveProfiles());
        if (profile.isEmpty()) {
            profile = "default";
        }

        String green = "\u001B[92m";
        String cyan = "\u001B[96m";
        String bold = "\u001B[1m";
        String dim = "\u001B[2m";
        String reset = "\u001B[0m";

        String bar = "==============================================================";

        System.out.println();
        System.out.println(green + bar + reset);
        System.out.println(green + "   " + bold + name.toUpperCase() + " is UP and running" + reset);
        System.out.println(green + "--------------------------------------------------------------" + reset);
        System.out.println("   " + dim + "Local    " + reset + " : " + cyan + "http://localhost:" + port + reset);
        System.out.println("   " + dim + "Actuator " + reset + " : " + cyan + "http://localhost:" + port + "/actuator" + reset);
        System.out.println("   " + dim + "Profile  " + reset + " : " + profile);
        System.out.println(green + bar + reset);
        System.out.println();
    }
}


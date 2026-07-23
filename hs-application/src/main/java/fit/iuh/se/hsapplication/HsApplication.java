package fit.iuh.se.hsapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "fit.iuh.se")
@AutoConfigurationPackage(basePackages = "fit.iuh.se")
@EntityScan(basePackages = "fit.iuh.se")
@EnableJpaRepositories(basePackages = "fit.iuh.se")
@EnableJpaAuditing
public class HsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HsApplication.class, args);
        System.out.println("Application started!");
    }

}

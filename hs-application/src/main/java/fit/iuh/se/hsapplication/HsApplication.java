package fit.iuh.se.hsapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "fit.iuh.se")
@AutoConfigurationPackage(basePackages = "fit.iuh.se")
@EnableJpaRepositories(basePackages = "fit.iuh.se")
public class HsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HsApplication.class, args);
        System.out.println("Application started!");
    }

}

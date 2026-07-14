package fit.iuh.se.hsapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "fit.iuh.se")
public class HsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HsApplication.class, args);
        System.out.println("Application started!");
    }

}

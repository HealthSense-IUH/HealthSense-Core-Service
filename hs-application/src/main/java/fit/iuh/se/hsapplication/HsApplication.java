package fit.iuh.se.hsapplication;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "fit.iuh.se")
@AutoConfigurationPackage(basePackages = "fit.iuh.se")
@EntityScan(basePackages = "fit.iuh.se")
@EnableJpaRepositories(basePackages = {
        "fit.iuh.se.hsuser.repository",
        "fit.iuh.se.hshealthrecord.repository",
        "fit.iuh.se.hschat.repository"
})
@EnableMongoRepositories(basePackages = {
        "fit.iuh.se.hschat.repository",
        "fit.iuh.se.hsnotification.repository"
})
@EnableScheduling
public class HsApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(HsApplication.class, args);
        System.out.println("Application started!");
    }

}

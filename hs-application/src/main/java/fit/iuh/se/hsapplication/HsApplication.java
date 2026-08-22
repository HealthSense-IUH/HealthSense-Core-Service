package fit.iuh.se.hsapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

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

    static {
        System.setProperty("user.timezone", "Asia/Bangkok");
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Bangkok"));
    }

    public static void main(String[] args) {
        SpringApplication.run(HsApplication.class, args);
    }

}

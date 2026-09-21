package hu.nje.beadando_eloadas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
public class BeadandoEloadasApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeadandoEloadasApplication.class, args);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}

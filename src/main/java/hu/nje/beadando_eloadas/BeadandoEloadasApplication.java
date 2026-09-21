package hu.nje.beadando_eloadas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ResponseBody;

import soapclient.MNBArfolyamServiceSoap;
import soapclient.MNBArfolyamServiceSoapGetCurrentExchangeRatesStringFaultFaultMessage;
import soapclient.MNBArfolyamServiceSoapGetExchangeRatesStringFaultFaultMessage;
import soapclient.MNBArfolyamServiceSoapGetInfoStringFaultFaultMessage;
import soapclient.MNBArfolyamServiceSoapImpl;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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

    @GetMapping("/feladat1")
    @ResponseBody
    public String soapTeszt()
            throws MNBArfolyamServiceSoapGetInfoStringFaultFaultMessage,
            MNBArfolyamServiceSoapGetCurrentExchangeRatesStringFaultFaultMessage,
            MNBArfolyamServiceSoapGetExchangeRatesStringFaultFaultMessage {

        MNBArfolyamServiceSoapImpl impl = new MNBArfolyamServiceSoapImpl();
        MNBArfolyamServiceSoap service =
                impl.getCustomBindingMNBArfolyamServiceSoap();

        return service.getInfo()
                + " "
                + service.getCurrentExchangeRates()
                + " "
                + service.getExchangeRates(
                "2022-08-14",
                "2022-09-14",
                "EUR"
        );
    }
    @GetMapping("/exercise")
    public String soapForm(Model model) {
        model.addAttribute("param", new MessagePrice());
        return "forms";
    }

    @PostMapping("/exercise")
    public String soapResult(
            @ModelAttribute MessagePrice messagePrice,
            Model model
    ) throws MNBArfolyamServiceSoapGetExchangeRatesStringFaultFaultMessage {

        MNBArfolyamServiceSoapImpl impl =
                new MNBArfolyamServiceSoapImpl();

        MNBArfolyamServiceSoap service =
                impl.getCustomBindingMNBArfolyamServiceSoap();

        String strOut =
                "Currency:" + messagePrice.getCurrency() + ";" +
                        "Start date:" + messagePrice.getStartDate() + ";" +
                        "End date:" + messagePrice.getEndDate() + ";";

        strOut += service.getExchangeRates(
                messagePrice.getStartDate(),
                messagePrice.getEndDate(),
                messagePrice.getCurrency()
        );

        model.addAttribute("sendOut", strOut);

        return "result";
    }}

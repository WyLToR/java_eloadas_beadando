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

import java.io.StringReader;import java.util.ArrayList;import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;import org.w3c.dom.Element;import org.w3c.dom.NodeList;import org.xml.sax.InputSource;

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

    @PostMapping("/exercise")public String soapResult(
            @ModelAttribute MessagePrice messagePrice,
            Model model) throws Exception {

        MNBArfolyamServiceSoapImpl impl =new MNBArfolyamServiceSoapImpl();

        MNBArfolyamServiceSoap service =impl.getCustomBindingMNBArfolyamServiceSoap();

        String result = service.getExchangeRates(
                messagePrice.getStartDate(),
                messagePrice.getEndDate(),
                messagePrice.getCurrency()
        );

        List<String> dates = new ArrayList<>();
        List<Double> rates = new ArrayList<>();

        DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();

        Document document = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(result)));

        NodeList days = document.getElementsByTagName("Day");

        for (int i = 0; i < days.getLength(); i++) {

            Element day = (Element) days.item(i);

            String date = day.getAttribute("date");

            NodeList rateNodes = day.getElementsByTagName("Rate");

            if (rateNodes.getLength() > 0) {

                Element rate = (Element) rateNodes.item(0);

                String value = rate.getTextContent()
                        .replace(",", ".");

                dates.add(date);
                rates.add(Double.parseDouble(value));
            }
        }

        model.addAttribute("currency", messagePrice.getCurrency());
        model.addAttribute("startDate", messagePrice.getStartDate());
        model.addAttribute("endDate", messagePrice.getEndDate());

        model.addAttribute("dates", dates);
        model.addAttribute("rates", rates);

        return "result";
    }
}

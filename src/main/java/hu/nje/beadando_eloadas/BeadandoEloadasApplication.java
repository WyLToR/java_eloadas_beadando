package hu.nje.beadando_eloadas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import soapclient.MNBArfolyamServiceSoap;
import soapclient.MNBArfolyamServiceSoapImpl;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.pricing.*;
import com.oanda.v20.instrument.*;
import com.oanda.v20.order.*;
import com.oanda.v20.trade.*;
import com.oanda.v20.primitives.InstrumentName;

import static com.oanda.v20.instrument.CandlestickGranularity.*;

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

    @GetMapping("/account_info")
    public String accountInfo(Model model) {

        Context ctx =
                new Context(Config.URL, Config.TOKEN);

        try {

            AccountSummary summary =
                    ctx.account
                            .summary(Config.ACCOUNTID)
                            .getAccount();

            model.addAttribute("account", summary);

            return "account_info";

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/actual_prices")
    public String actualPrices(Model model) {

        model.addAttribute("par", new MessageActPrice());

        return "form_actual_prices";
    }

    @PostMapping("/actual_prices")
    public String actualPricesResult(
            @ModelAttribute MessageActPrice messageActPrice,
            Model model
    ) {

        List<String> instruments = new ArrayList<>();
        instruments.add(messageActPrice.getInstrument());

        try {

            Context ctx =
                    new Context(Config.URL, Config.TOKEN);

            PricingGetRequest request =
                    new PricingGetRequest(
                            Config.ACCOUNTID,
                            instruments
                    );

            PricingGetResponse response =
                    ctx.pricing.get(request);

            ClientPrice price =
                    response.getPrices().get(0);

            model.addAttribute(
                    "instrument",
                    price.getInstrument()
            );

            model.addAttribute(
                    "bid",
                    price.getBids().get(0).getPrice()
            );

            model.addAttribute(
                    "ask",
                    price.getAsks().get(0).getPrice()
            );

            model.addAttribute(
                    "time",
                    price.getTime()
            );

            model.addAttribute(
                    "status",
                    price.getStatus()
            );

            model.addAttribute(
                    "tradeable",
                    price.getTradeable()
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "result_actual_prices";
    }

    @GetMapping("/hist_prices")
    public String histPrices(Model model) {

        model.addAttribute("param", new MessageHistPrice());

        return "form_hist_prices";
    }

    @PostMapping("/hist_prices")
    public String histPricesResult(
            @ModelAttribute MessageHistPrice messageHistPrice,
            Model model
    ) {

        try {

            Context ctx =
                    new Context(Config.URL, Config.TOKEN);

            InstrumentCandlesRequest request =
                    new InstrumentCandlesRequest(
                            new InstrumentName(
                                    messageHistPrice.getInstrument()
                            )
                    );

            switch (messageHistPrice.getGranularity()) {

                case "M1":
                    request.setGranularity(M1);
                    break;

                case "H1":
                    request.setGranularity(H1);
                    break;

                case "D":
                    request.setGranularity(D);
                    break;

                case "W":
                    request.setGranularity(W);
                    break;

                case "M":
                    request.setGranularity(M);
                    break;
            }

            request.setCount(10L);

            InstrumentCandlesResponse response =
                    ctx.instrument.candles(request);

            model.addAttribute(
                    "instr",
                    messageHistPrice.getInstrument()
            );

            model.addAttribute(
                    "granularity",
                    messageHistPrice.getGranularity()
            );

            model.addAttribute(
                    "candles",
                    response.getCandles()
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "result_hist_prices";
    }

    @GetMapping("/open_position")
    public String openPosition(Model model) {

        model.addAttribute("param", new MessageOpenPosition());

        return "form_open_position";
    }

    @PostMapping("/open_position")
    public String openPositionResult(
            @ModelAttribute MessageOpenPosition messageOpenPosition,
            Model model
    ) {

        String strOut;

        try {

            Context ctx =
                    new Context(Config.URL, Config.TOKEN);

            InstrumentName instrument =
                    new InstrumentName(
                            messageOpenPosition.getInstrument()
                    );

            OrderCreateRequest request =
                    new OrderCreateRequest(Config.ACCOUNTID);

            MarketOrderRequest marketOrderRequest =
                    new MarketOrderRequest();

            marketOrderRequest.setInstrument(instrument);

            marketOrderRequest.setUnits(
                    messageOpenPosition.getUnits()
            );

            request.setOrder(marketOrderRequest);

            OrderCreateResponse response =
                    ctx.order.create(request);

            String tradeId =
                    response
                            .getOrderFillTransaction()
                            .getTradeOpened()
                            .getTradeID()
                            .toString();

            strOut = "tradeId: " + tradeId;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.addAttribute(
                "instr",
                messageOpenPosition.getInstrument()
        );

        model.addAttribute(
                "units",
                messageOpenPosition.getUnits()
        );

        model.addAttribute(
                "id",
                strOut
        );

        return "result_open_position";
    }

    @GetMapping("/positions")
    public String positions(Model model) {

        Context ctx =
                new Context(Config.URL, Config.TOKEN);

        try {

            List<Trade> trades =
                    ctx.trade
                            .listOpen(Config.ACCOUNTID)
                            .getTrades();

            model.addAttribute("trades", trades);

            return "positions";

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/close_position")
    public String closePosition(Model model) {

        model.addAttribute(
                "param",
                new MessageClosePosition()
        );

        return "form_close_position";
    }

    @PostMapping("/close_position")
    public String closePositionResult(
            @ModelAttribute MessageClosePosition messageClosePosition,
            Model model
    ) {

        String tradeId =
                String.valueOf(messageClosePosition.getTradeId());

        Context ctx =
                new Context(Config.URL, Config.TOKEN);

        try {

            List<Trade> trades =
                    ctx.trade
                            .listOpen(Config.ACCOUNTID)
                            .getTrades();

            boolean tradeExists = trades.stream()
                    .anyMatch(trade ->
                            trade.getId()
                                    .toString()
                                    .equals(tradeId)
                    );

            if (!tradeExists) {

                model.addAttribute(
                        "tradeId",
                        "Nem található nyitott pozíció ezzel a Trade ID-val: "
                                + tradeId
                );

                return "result_close_position";
            }

            ctx.trade.close(
                    new TradeCloseRequest(
                            Config.ACCOUNTID,
                            new TradeSpecifier(tradeId)
                    )
            );

            model.addAttribute(
                    "tradeId",
                    "Lezárt Trade ID: " + tradeId
            );

        } catch (Exception e) {

            model.addAttribute(
                    "tradeId",
                    "A pozíció zárása sikertelen."
            );
        }

        return "result_close_position";
    }
}

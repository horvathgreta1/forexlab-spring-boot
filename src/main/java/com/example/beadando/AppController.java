package com.example.beadando;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.trade.Trade;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeSpecifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import soapclient.MNBArfolyamServiceSoap;
import soapclient.MNBArfolyamServiceSoapImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

import static com.oanda.v20.instrument.CandlestickGranularity.*;

@Controller
public class AppController {

    private Context getOandaContext(String appName) {
        return new ContextBuilder(Config.URL)
                .setToken(Config.TOKEN)
                .setApplication(appName)
                .build();
    }

    // Főoldal
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // MNB SOAP űrlap és grafikon
    @GetMapping("/soap")
    public String soapGet(Model model) {
        model.addAttribute("form", new SoapForm());
        return "soap";
    }

    @PostMapping("/soap")
    public String soapPost(@ModelAttribute SoapForm form, Model model) {
        List<String> dates = new ArrayList<>();
        List<Double> rates = new ArrayList<>();
        String rawXml = "";

        try {
            MNBArfolyamServiceSoapImpl impl = new MNBArfolyamServiceSoapImpl();
            MNBArfolyamServiceSoap service = impl.getCustomBindingMNBArfolyamServiceSoap();
            rawXml = service.getExchangeRates(form.getStartDate(), form.getEndDate(), form.getCurrency());

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(rawXml)));
            NodeList dayNodes = doc.getElementsByTagName("Day");

            for (int i = 0; i < dayNodes.getLength(); i++) {
                Element dayElem = (Element) dayNodes.item(i);
                String date = dayElem.getAttribute("date");
                NodeList rateNodes = dayElem.getElementsByTagName("Rate");
                if (rateNodes.getLength() > 0) {
                    String rateStr = rateNodes.item(0).getTextContent().replace(",", ".");
                    dates.add(date);
                    rates.add(Double.parseDouble(rateStr));
                }
            }
            Collections.reverse(dates);
            Collections.reverse(rates);

            model.addAttribute("dates", dates);
            model.addAttribute("rates", rates);
            model.addAttribute("rawXml", rawXml);
        } catch (Exception e) {
            model.addAttribute("error", "Hiba az MNB adatok lekérésekor: " + e.getMessage());
        }

        model.addAttribute("form", form);
        return "soap";
    }

    // OANDA Számlainfó
    @GetMapping("/forex/account")
    public String forexAccount(Model model) {
        try {
            Context ctx = getOandaContext("ForexAccount");
            AccountSummary summary = ctx.account.summary(Config.ACCOUNTID).getAccount();
            model.addAttribute("account", summary);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forex_account";
    }

    // OANDA Aktuális ár
    @GetMapping("/forex/actual")
    public String forexActual(Model model) {
        model.addAttribute("form", new ForexForms.ActualPriceForm());
        return "forex_actual";
    }

    @PostMapping("/forex/actual")
    public String forexActualPost(@ModelAttribute ForexForms.ActualPriceForm form, Model model) {
        try {
            Context ctx = getOandaContext("ForexActual");
            PricingGetRequest request = new PricingGetRequest(Config.ACCOUNTID, Collections.singletonList(form.getInstrument()));
            PricingGetResponse resp = ctx.pricing.get(request);
            if (!resp.getPrices().isEmpty()) {
                ClientPrice cp = resp.getPrices().get(0);
                model.addAttribute("price", cp);
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("form", form);
        return "forex_actual";
    }

    // OANDA Historikus ár
    @GetMapping("/forex/historical")
    public String forexHistorical(Model model) {
        model.addAttribute("form", new ForexForms.HistPriceForm());
        return "forex_historical";
    }

    @PostMapping("/forex/historical")
    public String forexHistoricalPost(@ModelAttribute ForexForms.HistPriceForm form, Model model) {
        try {
            Context ctx = getOandaContext("ForexHist");
            InstrumentCandlesRequest req = new InstrumentCandlesRequest(new InstrumentName(form.getInstrument()));
            switch (form.getGranularity()) {
                case "M1": req.setGranularity(M1); break;
                case "H1": req.setGranularity(H1); break;
                case "D": req.setGranularity(D); break;
                case "W": req.setGranularity(W); break;
                case "M": req.setGranularity(M); break;
            }
            req.setCount(10L);
            InstrumentCandlesResponse resp = ctx.instrument.candles(req);
            model.addAttribute("candles", resp.getCandles());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("form", form);
        return "forex_historical";
    }

    // OANDA Pozíció nyitás
    @GetMapping("/forex/open")
    public String forexOpen(Model model) {
        model.addAttribute("form", new ForexForms.OpenPositionForm());
        return "forex_open";
    }

    @PostMapping("/forex/open")
    public String forexOpenPost(@ModelAttribute ForexForms.OpenPositionForm form, Model model) {
        try {
            Context ctx = getOandaContext("ForexOpen");
            OrderCreateRequest req = new OrderCreateRequest(Config.ACCOUNTID);
            MarketOrderRequest mor = new MarketOrderRequest();
            mor.setInstrument(new InstrumentName(form.getInstrument()));
            mor.setUnits(form.getUnits());
            req.setOrder(mor);

            OrderCreateResponse resp = ctx.order.create(req);

            if (resp.getOrderFillTransaction() != null) {
                model.addAttribute("msg", "Sikeres pozíciónyitás! Trade ID: " + resp.getOrderFillTransaction().getId());
            } else if (resp.getOrderCancelTransaction() != null) {
                model.addAttribute("error", "A megbízás elutasítva (zárt piac vagy érvénytelen megbízás). Ok: "
                        + resp.getOrderCancelTransaction().getReason());
            } else {
                model.addAttribute("error", "A megbízás jelenleg nem teljesült azonnal (a Forex piac hétvégén zárva tart).");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Sikertelen nyitás: " + e.getMessage());
        }
        model.addAttribute("form", form);
        return "forex_open";
    }

    // OANDA Nyitott pozíciók
    @GetMapping("/forex/positions")
    public String forexPositions(Model model) {
        try {
            Context ctx = getOandaContext("ForexPositions");
            List<Trade> trades = ctx.trade.listOpen(Config.ACCOUNTID).getTrades();
            model.addAttribute("trades", trades);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forex_positions";
    }

    // OANDA Pozíció zárás
    @GetMapping("/forex/close")
    public String forexClose(Model model) {
        model.addAttribute("form", new ForexForms.ClosePositionForm());
        return "forex_close";
    }
    @GetMapping("/react")
    public String react() {
        return "forward:/react/index.html";
    }

    @PostMapping("/forex/close")
    public String forexClosePost(@ModelAttribute ForexForms.ClosePositionForm form, Model model) {
        try {
            Context ctx = getOandaContext("ForexClose");
            ctx.trade.close(new TradeCloseRequest(Config.ACCOUNTID, new TradeSpecifier(form.getTradeId())));
            model.addAttribute("msg", "A pozíció lezárva. Trade ID: " + form.getTradeId());
        } catch (Exception e) {
            model.addAttribute("error", "Sikertelen zárás: " + e.getMessage());
        }
        model.addAttribute("form", form);
        return "forex_close";
    }
}

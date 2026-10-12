package com.example.beadando;

public class ForexForms {
    public static class ActualPriceForm {
        private String instrument = "EUR_USD";
        public String getInstrument() { return instrument; }
        public void setInstrument(String instrument) { this.instrument = instrument; }
    }

    public static class HistPriceForm {
        private String instrument = "EUR_USD";
        private String granularity = "D";
        public String getInstrument() { return instrument; }
        public void setInstrument(String instrument) { this.instrument = instrument; }
        public String getGranularity() { return granularity; }
        public void setGranularity(String granularity) { this.granularity = granularity; }
    }

    public static class OpenPositionForm {
        private String instrument = "EUR_USD";
        private int units = 10;
        public String getInstrument() { return instrument; }
        public void setInstrument(String instrument) { this.instrument = instrument; }
        public int getUnits() { return units; }
        public void setUnits(int units) { this.units = units; }
    }

    public static class ClosePositionForm {
        private String tradeId;
        public String getTradeId() { return tradeId; }
        public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    }
}

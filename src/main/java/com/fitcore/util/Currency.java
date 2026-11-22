package com.fitcore.util;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
public final class Currency {
    private Currency() { }
    public static String bdt(BigDecimal price) {
        return "৳" + NumberFormat.getNumberInstance(Locale.US).format(price);
    }
}

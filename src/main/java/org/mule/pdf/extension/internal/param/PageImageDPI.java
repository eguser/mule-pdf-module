package org.mule.pdf.extension.internal.param;

import java.util.HashMap;
import java.util.Map;

public enum PageImageDPI {

    DPI_300(300), DPI_200(200), DPI_150(150), DPI_96(96), DPI_72(72);

    public final int dpiValue;

    private PageImageDPI(int dpiValue) {
        this.dpiValue = dpiValue;
    }

    // Mapping difficulty to difficulty id
    private static final Map<Integer, PageImageDPI> _map = new HashMap<Integer, PageImageDPI>();
    static {
        for (PageImageDPI difficulty : PageImageDPI.values())
            _map.put(difficulty.dpiValue, difficulty);
    }

    /**
     * Get difficulty from value
     * 
     * @param value Value
     * @return Difficulty
     */
    public static PageImageDPI from(int dpiValue) {
        return _map.get(dpiValue);
    }

    // private int getDpiValue() {
    // return dpiValue;
    // }

}

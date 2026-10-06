package com.rr.erp.util;

/**
 * Items bought from suppliers as bars/pieces of a fixed length (sometimes
 * width too, for sheet items — area = length * width) that get cut to
 * shorter lengths on site, with the offcuts returned to the project store
 * (see stock_batch_dimensional_tracking.sql). Detection is a hardcoded
 * item-code-prefix convention, deliberately NOT a DB category lookup —
 * mirrors the same check already used on the frontend.
 */
public final class DimensionalItems {

    private static final String[] DIMENSIONAL_ITEM_PREFIXES = { "STBEAMS-", "STS-" };

    private DimensionalItems() {
    }

    public static boolean isDimensionalItem(String itemCode) {
        if (itemCode == null) {
            return false;
        }
        String normalized = itemCode.trim().toUpperCase();
        for (String prefix : DIMENSIONAL_ITEM_PREFIXES) {
            if (normalized.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}

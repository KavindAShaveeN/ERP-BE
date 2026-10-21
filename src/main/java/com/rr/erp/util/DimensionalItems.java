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

    // Legacy STBEAMS-/STS- plus the client-coded structural steel sub categories of C-FAB
    // (I-beam, H-beam, C-channel, angle, flat bar, plate, chequered plate, wear plate,
    // zinc-coated sheet, hollow shaft/bar). Keep in sync with dimensionalItems.ts.
    private static final String[] DIMENSIONAL_ITEM_PREFIXES = {
            "STBEAMS-", "STS-",
            "C-FAB-IBM-", "C-FAB-HBM-", "C-FAB-CBM-", "C-FAB-ABM-", "C-FAB-FBM-",
            "C-FAB-PLT-", "C-FAB-CQP-", "C-FAB-HXP-", "C-FAB-ZCS-", "C-FAB-HLS-"
    };

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

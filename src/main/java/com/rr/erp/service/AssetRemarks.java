package com.rr.erp.service;

import java.util.LinkedHashMap;
import java.util.Map;

/** The asset register's own fields (Reg No, Make, Model, ...) are stored in an asset's remarks as
 * "Label: value | Label: value" (see AssetImportService). This reads them back out. */
final class AssetRemarks {

    private AssetRemarks() {
    }

    static Map<String, String> parse(String remarks) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (remarks == null) {
            return fields;
        }
        for (String part : remarks.split("\\s\\|\\s")) {
            int colon = part.indexOf(':');
            if (colon > 0) {
                fields.put(part.substring(0, colon).trim(), part.substring(colon + 1).trim());
            }
        }
        return fields;
    }
}

package com.rr.erp.controller;

import com.rr.erp.entity.MeterReading;
import com.rr.erp.service.MeterReadingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meter_reading")
@CrossOrigin
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    public MeterReadingController(
            MeterReadingService meterReadingService
    ) {
        this.meterReadingService = meterReadingService;
    }


    // ==================================================
    // POST /api/meter_reading/
    // ==================================================
    @PostMapping("/")
    public ResponseEntity<MeterReading> createMeterReading(
            @RequestBody MeterReading meterReading
    ) {

        return ResponseEntity.ok(
                meterReadingService.createMeterReading(
                        meterReading
                )
        );
    }


    // ==================================================
    // GET /api/meter_reading/all
    // ==================================================
    @GetMapping("/all")
    public ResponseEntity<List<MeterReading>>
    getAllMeterReadings() {

        return ResponseEntity.ok(
                meterReadingService.getAllMeterReadings()
        );
    }


    // ==================================================
    // GET /api/meter_reading/asset/{assetCode}
    // ==================================================
    @GetMapping("/asset/{assetCode}")
    public ResponseEntity<List<MeterReading>>
    getMeterReadingsByAssetCode(
            @PathVariable String assetCode
    ) {

        return ResponseEntity.ok(
                meterReadingService
                        .getMeterReadingsByAssetCode(assetCode)
        );
    }


    // ==================================================
    // GET /api/meter_reading/{recordedBy}
    // ==================================================
    @GetMapping("/{recordedBy}")
    public ResponseEntity<List<MeterReading>>
    getMeterReadingsByRecordedBy(
            @PathVariable String recordedBy
    ) {

        return ResponseEntity.ok(
                meterReadingService
                        .getMeterReadingsByRecordedBy(recordedBy)
        );
    }


    // ==================================================
    // PUT /api/meter_reading/{meterReadingId}?editedBy=EMP001
    // ==================================================
    @PutMapping("/{meterReadingId}")
    public ResponseEntity<MeterReading> updateMeterReading(
            @PathVariable UUID meterReadingId,
            @RequestParam String editedBy,
            @RequestBody MeterReading meterReading
    ) {

        return ResponseEntity.ok(
                meterReadingService.updateMeterReading(
                        meterReadingId,
                        meterReading,
                        editedBy
                )
        );
    }
}

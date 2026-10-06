package com.rr.erp.service;

import com.rr.erp.entity.MeterReading;
import com.rr.erp.repository.MeterReadingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MeterReadingService {

    private static final long EDIT_WINDOW_MINUTES = 30;

    private final MeterReadingRepository meterReadingRepository;

    public MeterReadingService(
            MeterReadingRepository meterReadingRepository
    ) {
        this.meterReadingRepository = meterReadingRepository;
    }


    // ==============================
    // POST
    // The database is the source of truth for "previous reading" — always
    // look up the latest existing reading for this asset (by submitted_at,
    // not reading_date, so same-day entries resolve correctly) rather than
    // trusting whatever previousReading/usageValue the client sent.
    // ==============================
    public MeterReading createMeterReading(
            MeterReading meterReading
    ) {

        meterReading.setMeterReadingId(
                UUID.randomUUID()
        );

        meterReading.setSubmittedAt(
                LocalDateTime.now()
        );

        applyPreviousReading(meterReading, null);

        meterReadingRepository.createMeterReading(
                meterReading
        );

        return meterReadingRepository.getMeterReadingById(
                meterReading.getMeterReadingId()
        );
    }


    // ==============================
    // Recompute previousReading/usageValue from the latest OTHER reading
    // recorded for this asset, ignoring any cached values the caller sent.
    // excludeMeterReadingId should be the reading's own id on an update
    // (so it doesn't get compared against itself), or null on create.
    // ==============================
    private void applyPreviousReading(
            MeterReading meterReading,
            UUID excludeMeterReadingId
    ) {

        MeterReading latest = meterReadingRepository.getLatestMeterReadingForAsset(
                meterReading.getAssetCode(),
                excludeMeterReadingId
        );

        if (latest == null) {
            meterReading.setPreviousReading(null);
            meterReading.setUsageValue(null);
            return;
        }

        BigDecimal previousValue = latest.getReadingValue();
        meterReading.setPreviousReading(previousValue);
        meterReading.setUsageValue(
                meterReading.getReadingValue().subtract(previousValue)
        );
    }


    // ==============================
    // GET BY RECORDED BY
    // ==============================
    public List<MeterReading> getMeterReadingsByRecordedBy(
            String recordedBy
    ) {

        return meterReadingRepository
                .getMeterReadingsByRecordedBy(recordedBy);
    }


    // ==============================
    // GET BY ASSET CODE
    // ==============================
    public List<MeterReading> getMeterReadingsByAssetCode(
            String assetCode
    ) {

        return meterReadingRepository
                .getMeterReadingsByAssetCode(assetCode);
    }


    // ==============================
    // GET ALL
    // ==============================
    public List<MeterReading> getAllMeterReadings() {

        return meterReadingRepository.getAllMeterReadings();
    }


    // ==============================
    // PUT
    // Only the employee who originally submitted the reading may edit it,
    // and only within EDIT_WINDOW_MINUTES of that original submission.
    // ==============================
    public MeterReading updateMeterReading(
            UUID meterReadingId,
            MeterReading meterReading,
            String editedBy
    ) {

        MeterReading existing =
                meterReadingRepository.getMeterReadingById(meterReadingId);

        if (existing == null) {
            throw new RuntimeException(
                    "Meter reading not found with ID: "
                            + meterReadingId
            );
        }

        if (existing.getRecordedBy() == null
                || !existing.getRecordedBy().equals(editedBy)) {
            throw new RuntimeException(
                    "Only the employee who submitted this meter reading may edit it."
            );
        }

        if (existing.getSubmittedAt() == null
                || Duration.between(existing.getSubmittedAt(), LocalDateTime.now())
                        .toMinutes() >= EDIT_WINDOW_MINUTES) {
            throw new RuntimeException(
                    "This meter reading can no longer be edited — the "
                            + EDIT_WINDOW_MINUTES
                            + "-minute edit window has passed."
            );
        }

        // recordedBy and submittedAt are fixed at creation and cannot be changed by an edit.
        meterReading.setRecordedBy(existing.getRecordedBy());
        meterReading.setEditedAt(LocalDateTime.now());

        applyPreviousReading(meterReading, meterReadingId);

        int updatedRows =
                meterReadingRepository.updateMeterReading(
                        meterReadingId,
                        meterReading
                );

        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Meter reading not found with ID: "
                            + meterReadingId
            );
        }

        return meterReadingRepository.getMeterReadingById(
                meterReadingId
        );
    }
}

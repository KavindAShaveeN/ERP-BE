package com.rr.erp.controller;

import com.rr.erp.dto.ItemCodeDetailResponse;
import com.rr.erp.dto.ItemCodeNameRequest;
import com.rr.erp.entity.ItemCode;
import com.rr.erp.service.ItemCodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item-codes")
@CrossOrigin(origins = "*")
public class ItemCodeController {

    private final ItemCodeService itemCodeService;

    public ItemCodeController(ItemCodeService itemCodeService) {
        this.itemCodeService = itemCodeService;
    }

    @PostMapping
    public ResponseEntity<ItemCode> createItemCode(@Valid @RequestBody ItemCode request) {
        ItemCode saved = itemCodeService.addItemCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ItemCodeDetailResponse>> getAllItemCodes() {
        return ResponseEntity.ok(itemCodeService.getAllItemCodes());
    }

    @GetMapping("/{itemCodeId}")
    public ResponseEntity<ItemCode> getItemCodeById(@PathVariable Long itemCodeId) {
        return ResponseEntity.ok(itemCodeService.getItemCodeById(itemCodeId));
    }

    @PutMapping("/{itemCodeId}/name")
    public ResponseEntity<ItemCode> updateItemCodeName(
            @PathVariable Long itemCodeId,
            @Valid @RequestBody ItemCodeNameRequest request
    ) {
        return ResponseEntity.ok(
                itemCodeService.updateItemCodeName(itemCodeId, request.getItemCodeName())
        );
    }

    @DeleteMapping("/{itemCodeId}")
    public ResponseEntity<Void> deleteItemCode(@PathVariable Long itemCodeId) {
        itemCodeService.deleteItemCode(itemCodeId);
        return ResponseEntity.noContent().build();
    }
}

package com.rr.erp.controller;

import com.rr.erp.entity.UOM;
import com.rr.erp.service.UOMService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uom")
@CrossOrigin(origins = "*")
public class UOMController {

    private final UOMService uomService;

    public UOMController(UOMService uomService) {
        this.uomService = uomService;
    }

    @GetMapping
    public ResponseEntity<List<UOM>> getAllUom() {
        return ResponseEntity.ok(uomService.getAllUom());
    }
}
package com.rr.erp.controller;

import com.rr.erp.entity.GINType;
import com.rr.erp.service.GINTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gin-type")
@CrossOrigin(origins = "*")
public class GINTypeController {

    private final GINTypeService ginTypeService;

    public GINTypeController(GINTypeService ginTypeService) {
        this.ginTypeService = ginTypeService;
    }

    @GetMapping("/")
    public ResponseEntity<List<GINType>> getAllGinTypes() {
        return ResponseEntity.ok(ginTypeService.getAllGinTypes());
    }
}

package com.app.proyectojuegosmonolito.account.profile.controller;

import com.app.proyectojuegosmonolito.account.profile.dto.ComunaResponse;
import com.app.proyectojuegosmonolito.account.profile.dto.RegionResponse;
import com.app.proyectojuegosmonolito.account.profile.mapper.RegionComunaMapper;
import com.app.proyectojuegosmonolito.account.profile.repository.ComunaRepository;
import com.app.proyectojuegosmonolito.account.profile.repository.RegionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Regions & Comunas", description = "Geographic catalog: regions and comunas")
@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;
    private final RegionComunaMapper mapper;

    @Operation(summary = "Get all regions", description = "Returns a list of all regions ordered by name")
    @ApiResponse(responseCode = "200", description = "List of regions retrieved successfully")
    @GetMapping
    public ResponseEntity<List<RegionResponse>> findAll() {
        var regions = regionRepository.findAll();
        return ResponseEntity.ok(regions.stream().map(mapper::toRegionResponse).toList());
    }

    @Operation(summary = "Get comunas by region", description = "Returns all comunas belonging to a region, ordered by name")
    @ApiResponse(responseCode = "200", description = "List of comunas retrieved successfully")
    @GetMapping("/{regionId}/comunas")
    public ResponseEntity<List<ComunaResponse>> findComunasByRegion(@PathVariable Long regionId) {
        var comunas = comunaRepository.findByRegion_Id(regionId);
        return ResponseEntity.ok(comunas.stream().map(mapper::toComunaResponse).toList());
    }
}
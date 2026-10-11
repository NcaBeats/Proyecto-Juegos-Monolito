package com.app.proyectojuegosmonolito.account.profile.mapper;

import com.app.proyectojuegosmonolito.account.profile.dto.ComunaResponse;
import com.app.proyectojuegosmonolito.account.profile.dto.RegionResponse;
import com.app.proyectojuegosmonolito.account.profile.model.Comuna;
import com.app.proyectojuegosmonolito.account.profile.model.Region;
import org.springframework.stereotype.Component;

@Component
public class RegionComunaMapper {

    public RegionResponse toRegionResponse(Region region) {
        return new RegionResponse(region.getId(), region.getNombre());
    }

    public ComunaResponse toComunaResponse(Comuna comuna) {
        return new ComunaResponse(
                comuna.getId(),
                comuna.getNombre(),
                comuna.getRegion().getId()
        );
    }
}
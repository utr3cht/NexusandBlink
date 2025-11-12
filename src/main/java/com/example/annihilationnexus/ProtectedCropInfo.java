package com.example.annihilationnexus;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProtectedCropInfo implements ConfigurationSerializable {
    private final long plantedTime;
    private final UUID planterUUID;

    public ProtectedCropInfo(long plantedTime, UUID planterUUID) {
        this.plantedTime = plantedTime;
        this.planterUUID = planterUUID;
    }

    public long getPlantedTime() {
        return plantedTime;
    }

    public UUID getPlanterUUID() {
        return planterUUID;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("plantedTime", plantedTime);
        map.put("planterUUID", planterUUID.toString());
        return map;
    }

    public static ProtectedCropInfo deserialize(Map<String, Object> map) {
        long plantedTime = ((Number) map.get("plantedTime")).longValue();
        UUID planterUUID = UUID.fromString((String) map.get("planterUUID"));
        return new ProtectedCropInfo(plantedTime, planterUUID);
    }
}
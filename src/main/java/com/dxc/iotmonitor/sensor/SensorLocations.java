package com.dxc.iotmonitor.sensor;

import com.dxc.iotmonitor.enums.SensorType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SensorLocations {

    public static final List<String> TRAFFIC = List.of(
            "CAIRO_RING_ROAD",
            "CAIRO_OCTOBER_BRIDGE",
            "CAIRO_SALAH_SALEM_ROAD"
    );

    public static final List<String> AIR_POLLUTION = List.of(
            "CAIRO_NASR_CITY",
            "CAIRO_MAADI",
            "CAIRO_HELIOPOLIS"
    );

    public static final List<String> STREET_LIGHT = List.of(
            "CAIRO_ZAMALEK",
            "CAIRO_DOWNTOWN",
            "CAIRO_NEW_CAIRO"
    );

    private static final Map<SensorType, Set<String>> BY_TYPE = Map.of(
            SensorType.TRAFFIC, Set.copyOf(TRAFFIC),
            SensorType.AIR_POLLUTION, Set.copyOf(AIR_POLLUTION),
            SensorType.STREET_LIGHT, Set.copyOf(STREET_LIGHT)
    );

    private SensorLocations() {
    }

    public static List<String> forType(SensorType type) {
        return switch (type) {
            case TRAFFIC -> TRAFFIC;
            case AIR_POLLUTION -> AIR_POLLUTION;
            case STREET_LIGHT -> STREET_LIGHT;
        };
    }

    public static boolean isValid(SensorType type, String location) {
        return location != null && BY_TYPE.get(type).contains(location);
    }
}

package com.smart_watering_system.SmartWateringSystem.configuration;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.security.Principal;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StompPrincipal implements Principal {
    String name;

    @Override
    public String getName() {return name;}
}

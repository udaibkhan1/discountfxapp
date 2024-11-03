package com.discountcalc.models.requests;

import com.discountcalc.enums.UserType;

public record UserDTO(
        UserType userType,
        int tenure
) {}
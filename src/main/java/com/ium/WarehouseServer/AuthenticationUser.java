package com.ium.WarehouseServer;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthenticationUser {
    private String username;
    private String password;
}

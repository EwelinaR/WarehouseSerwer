package com.ium.WarehouseServer;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    private String username;
    private String password;
    private String role;
}

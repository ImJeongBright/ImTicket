package org.example.ticket.security.util;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class MetamaskUserDetails extends User {

    private final Integer nonce;

    public MetamaskUserDetails(String username, String password, Integer nonce, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.nonce = nonce;
    }

    public String getAddress() {
        return getUsername();
    }

}

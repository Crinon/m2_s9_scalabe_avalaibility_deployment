package org.miage.bankservice.config;

import lombok.RequiredArgsConstructor;
import org.miage.bankservice.boundary.AccountResource;
import org.miage.bankservice.entity.Account;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final AccountResource utilisateurCatalog;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final Optional<Account> optionalUtilisateur = utilisateurCatalog.findByPassportNumberEqualsIgnoreCase(username);

        if (optionalUtilisateur.isPresent()) {
            final Account currentUtilisateur = optionalUtilisateur.get();
            return new User(username, currentUtilisateur.getPassword(),
                    new ArrayList<>() {{
                        add(new SimpleGrantedAuthority("ROLE_USER"));
                    }});
        } else {
            throw new UsernameNotFoundException("Utilisateur non trouvé avec le passeport : " + username);
        }
    }
}

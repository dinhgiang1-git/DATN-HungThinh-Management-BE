package dinhgiang.dev.hungthinh.models.securitys;


import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.models.entities.bases.User;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getUserRole().name())
                    .build();
        }

        Optional<Resident> residentOptional = residentRepository.findByUsername(username);
        if (residentOptional.isPresent()) {
            Resident resident = residentOptional.get();
            return org.springframework.security.core.userdetails.User.builder()
                    .username(resident.getUsername())
                    .password(resident.getPassword())
                    .roles(resident.getRole().name())
                    .build();
        }
        throw new UsernameNotFoundException("Username không tồn tại");
    }
}

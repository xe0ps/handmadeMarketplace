package shepelev.handmadeMarketplace.config;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import shepelev.handmadeMarketplace.entity.User;
import shepelev.handmadeMarketplace.repo.UserRepo;

@Service
@AllArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user by email: {}", username);
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with email '" + username + "' not found"
                ));

        if (user.getProfileImage() != null) {
            user.getProfileImage().getData();
            log.info("Loaded profileImage for user: {}", username);
        }
        log.info("User {} loaded successfully with roles: {}", username, user.getRole());
        if (user.getRole() == null) {
            throw new UsernameNotFoundException("User has no roles: " + username);
        }
        UserDetails userDetails = UserDetailsImpl.build(user);
        log.info("User details: {}", userDetails);
        return userDetails;
    }
}
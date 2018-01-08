package pl.bk.pizza.store.domain.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import pl.bk.pizza.store.domain.exception.ErrorCode;
import pl.bk.pizza.store.domain.exception.MissingEntityException;
import pl.bk.pizza.store.domain.user.User;
import pl.bk.pizza.store.domain.user.UserRepository;

import static pl.bk.pizza.store.domain.exception.Preconditions.check;

@Component
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private UserRepository userRepository;

    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userRepository.findById(username);
        check(user == null, ()-> new MissingEntityException("User with id: " + username +" does not exists.",
            ErrorCode.MISSING_USER));

        return org.springframework.security.core.userdetails.User
            .withUsername(username)
            .password(user.getPassword())
            .roles(user.getRole())
            .build();
    }

}
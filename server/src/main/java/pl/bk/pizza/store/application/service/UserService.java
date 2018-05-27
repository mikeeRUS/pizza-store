package pl.bk.pizza.store.application.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.bk.pizza.store.application.dto.user.NewUserDTO;
import pl.bk.pizza.store.application.dto.user.UserDTO;
import pl.bk.pizza.store.application.mapper.customer.NewUserMapper;
import pl.bk.pizza.store.application.mapper.customer.UserMapper;
import pl.bk.pizza.store.domain.customer.user.User;
import pl.bk.pizza.store.domain.customer.user.UserRepository;
import pl.bk.pizza.store.domain.validator.customer.UserValidator;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class UserService
{
    private final UserRepository userRepository;
    private final NewUserMapper newUserMapper;
    private final UserMapper userMapper;
    
    public Mono<UserDTO> createUser(NewUserDTO userDTO)
    {
        return Mono
            .just(userDTO)
            .doOnNext(newUser -> Mono.just(newUser)
                                     .flatMap(it -> userRepository.findById(it.getEmail()))
                                     .doOnNext(it -> UserValidator.userShouldExists(it, userDTO.getEmail())))
            .map(newUserMapper::mapFromDTO)
            .flatMap(userRepository::save)
            .map(userMapper::mapToDTO);
    }
    
    public Mono<Integer> getBonusPoints(String email)
    {
        return userRepository
            .findById(email)
            .doOnNext(it -> UserValidator.userShouldExists(it, email))
            .map(User::getPoints);
    }
    
    public Mono<UserDTO> getUser(String email)
    {
        return userRepository
            .findById(email)
            .doOnNext(it -> UserValidator.userShouldExists(it, email))
            .map(userMapper::mapToDTO);
    }
    
    public Mono<UserDTO> deactivateUser(String email)
    {
        return userRepository
            .findById(email)
            .doOnNext(it -> UserValidator.userShouldExists(it, email))
            .doOnNext(User::deactivateUser)
            .flatMap(userRepository::save)
            .map(userMapper::mapToDTO);
    }
    
    Mono<User> addPoints(String email, Integer points)
    {
        return userRepository
            .findById(email)
            .doOnNext(it -> UserValidator.userShouldExists(it, email))
            .doOnNext(user -> user.addPoints(points))
            .flatMap(userRepository::save);
    }
}

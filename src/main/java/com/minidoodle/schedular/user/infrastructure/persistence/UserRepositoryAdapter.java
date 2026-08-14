package com.minidoodle.schedular.user.infrastructure.persistence;

import com.minidoodle.schedular.shared.domain.UserId;
import com.minidoodle.schedular.user.domain.User;
import com.minidoodle.schedular.user.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserPersistenceRepository repository;

    public UserRepositoryAdapter(UserPersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(UserId id) {
        return repository.existsById(id.value());
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return repository.findById(id.value()).map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity saved = repository.saveAndFlush(new UserEntity(
                user.id().value(),
                user.name(),
                user.email()
        ));
        return toDomain(saved);
    }

    private static User toDomain(UserEntity entity) {
        return new User(new UserId(entity.getId()), entity.getName(), entity.getEmail());
    }
}

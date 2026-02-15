package com.atlas.repository;

import com.atlas.entity.Delegate;
import com.atlas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DelegateRepository extends JpaRepository<Delegate, Long> {
    List<Delegate> findAllByDelegator(User delegator);

    List<Delegate> findAllByDelegate(User delegate);

    List<Delegate> findAllByDelegator_UsernameIgnoreCase(String username);

    List<Delegate> findAllByDelegate_UsernameIgnoreCase(String username);

    Optional<Delegate> findByDelegatorAndDelegate(User delegator, User delegate);

    boolean existsByDelegatorAndDelegate(User delegator, User delegate);

    boolean existsByDelegator_UsernameIgnoreCaseAndDelegate_UsernameIgnoreCase(String delegatorUsername,
            String delegateUsername);
}

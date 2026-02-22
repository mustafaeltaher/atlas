package com.atlas.service;

import com.atlas.dto.DelegateRequest;
import com.atlas.dto.DelegateResponse;
import com.atlas.entity.Delegate;
import com.atlas.entity.Employee;
import com.atlas.entity.User;
import com.atlas.repository.DelegateRepository;
import com.atlas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DelegateService {

        private final DelegateRepository delegateRepository;
        private final UserRepository userRepository;

        @Transactional(readOnly = true)
        public List<DelegateResponse> getMyDelegates(String delegatorUsername) {
                return delegateRepository.findAllByDelegator_UsernameIgnoreCase(delegatorUsername).stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<DelegateResponse> getAvailableAccounts(String delegateUsername) {
                return delegateRepository.findAllByDelegate_UsernameIgnoreCase(delegateUsername).stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        @Transactional
        public DelegateResponse grantAccess(String delegatorUsername, DelegateRequest request) {
                User delegator = userRepository.findByUsernameIgnoreCase(delegatorUsername)
                                .orElseThrow(() -> new RuntimeException("Delegator not found"));

                User delegateUser = userRepository.findByUsernameIgnoreCase(request.getDelegateUsername())
                                .orElseThrow(() -> new RuntimeException(
                                                "Delegate user not found: " + request.getDelegateUsername()));

                if (delegator.getId().equals(delegateUser.getId())) {
                        throw new RuntimeException("Cannot delegate access to yourself");
                }

                Employee delegatorEmp = delegator.getEmployee();
                Employee delegateEmp = delegateUser.getEmployee();

                // Validate that delegate is a direct report of the delegator
                if (delegateEmp.getManager() == null
                                || !delegateEmp.getManager().getId().equals(delegatorEmp.getId())) {
                        throw new RuntimeException("You can only delegate access to your direct reports");
                }

                if (delegateRepository.existsByDelegatorAndDelegate(delegator, delegateUser)) {
                        throw new RuntimeException("Access already granted to this user");
                }

                Delegate delegate = Delegate.builder()
                                .delegator(delegator)
                                .delegate(delegateUser)
                                .build();

                return toResponse(delegateRepository.save(delegate));
        }

        @Transactional
        public void revokeAccess(String delegatorUsername, Long delegateId) {
                Delegate delegate = delegateRepository.findById(delegateId)
                                .orElseThrow(() -> new RuntimeException("Delegation not found"));

                if (!delegate.getDelegator().getUsername().equalsIgnoreCase(delegatorUsername)) {
                        throw new RuntimeException("You can only revoke delegations you created");
                }

                delegateRepository.delete(delegate);
        }

        public boolean canImpersonate(String impersonatorUsername, String targetUsername) {
                return delegateRepository.existsByDelegator_UsernameIgnoreCaseAndDelegate_UsernameIgnoreCase(
                                targetUsername,
                                impersonatorUsername);
        }

        @Transactional(readOnly = true)
        public List<com.atlas.dto.EmployeeDTO> getPotentialDelegates(String delegatorUsername, String search) {
                User delegator = userRepository.findByUsernameIgnoreCase(delegatorUsername)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Employee delegatorEmp = delegator.getEmployee();

                // Exclude existing delegates
                List<Long> excludedIds = delegateRepository.findAllByDelegator(delegator).stream()
                                .map(d -> d.getDelegate().getEmployee().getId())
                                .collect(Collectors.toList());
                if (excludedIds.isEmpty()) {
                        excludedIds = null; // optimize query
                }

                // Use the optimized query for direct reports
                return userRepository.findPotentialDelegatesDirectReports(
                                delegatorEmp.getId(),
                                excludedIds,
                                search != null ? "%" + search.toLowerCase() + "%" : null).stream()
                                .map(this::toEmployeeDTO)
                                .collect(Collectors.toList());
        }

        private com.atlas.dto.EmployeeDTO toEmployeeDTO(User user) {
                Employee e = user.getEmployee();
                return com.atlas.dto.EmployeeDTO.builder()
                                .id(e.getId())
                                .oracleId(e.getOracleId())
                                .username(user.getUsername())
                                .name(e.getName())
                                .email(e.getEmail())
                                .jobLevel(e.getJobLevel() != null ? e.getJobLevel().name() : null)
                                .title(e.getTitle())
                                .build();
        }

        private DelegateResponse toResponse(Delegate delegate) {
                return DelegateResponse.builder()
                                .id(delegate.getId())
                                .delegatorName(delegate.getDelegator().getEmployee().getName())
                                .delegatorUsername(delegate.getDelegator().getUsername())
                                .delegateName(delegate.getDelegate().getEmployee().getName())
                                .delegateUsername(delegate.getDelegate().getUsername())
                                .createdAt(delegate.getCreatedAt())
                                .build();
        }
}

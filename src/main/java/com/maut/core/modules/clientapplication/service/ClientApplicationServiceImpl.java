package com.maut.core.modules.clientapplication.service;

import com.maut.core.modules.clientapplication.dto.MyClientApplicationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientApplicationServiceImpl implements ClientApplicationService {

    @Override
    public MyClientApplicationResponse getMyApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        log.info("Fetching client applications for user: {}", currentPrincipalName);
        // In a real scenario, this would involve fetching data from a repository
        // based on the authenticated user.
        return MyClientApplicationResponse.success("Successfully retrieved applications for " + currentPrincipalName);
    }
}

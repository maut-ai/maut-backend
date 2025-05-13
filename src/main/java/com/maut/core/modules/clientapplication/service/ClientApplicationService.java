package com.maut.core.modules.clientapplication.service;

import com.maut.core.modules.clientapplication.dto.CreateClientApplicationRequest;
import com.maut.core.modules.clientapplication.dto.ClientApplicationDetailResponse;
import com.maut.core.modules.clientapplication.dto.MyClientApplicationResponse;
import com.maut.core.modules.user.model.User;

import java.util.List;
import java.util.UUID;

public interface ClientApplicationService {

    ClientApplicationDetailResponse createClientApplication(
            CreateClientApplicationRequest request,
            User authenticatedUser
    );

    List<MyClientApplicationResponse> listClientApplicationsForUser(
            User authenticatedUser
    );

    ClientApplicationDetailResponse getClientApplicationDetails(
            UUID clientApplicationId,
            User authenticatedUser
    );
}

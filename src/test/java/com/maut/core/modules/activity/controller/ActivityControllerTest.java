package com.maut.core.modules.activity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maut.core.modules.activity.dto.ActivityStatusResponse;
import com.maut.core.modules.activity.dto.ListActivitiesResponse;
import com.maut.core.modules.activity.dto.SubmitUserApprovalRequest;
import com.maut.core.modules.activity.dto.SubmitUserApprovalResponse;
import com.maut.core.modules.activity.service.ActivityService;
import com.maut.core.modules.user.model.MautUser; // Assuming MautUser will be used or mocked
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ActivityControllerTest {

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private ActivityController activityController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(activityController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void submitUserApproval_shouldReturnSuccess() throws Exception {
        String activityId = UUID.randomUUID().toString();
        SubmitUserApprovalRequest request = new SubmitUserApprovalRequest();
        request.setSignedChallenge("test-signed-challenge");

        SubmitUserApprovalResponse expectedResponse = new SubmitUserApprovalResponse("APPROVED");

        // Since MautUser is null in the controller, we pass null here as well for the service mock
        when(activityService.submitUserApproval(any(MautUser.class), eq(activityId), any(SubmitUserApprovalRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/v1/activities/{activityId}/submit-user-approval", activityId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getActivityStatus_shouldReturnStatus() throws Exception {
        String activityId = UUID.randomUUID().toString();
        ActivityStatusResponse expectedResponse = ActivityStatusResponse.builder()
                .activityId(activityId)
                .status("PENDING_APPROVAL")
                .result(Collections.singletonMap("info", (Object)"Waiting for user action"))
                .build();

        // MautUser is null in controller
        when(activityService.getActivityStatus(any(MautUser.class), eq(activityId)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/v1/activities/{activityId}/status", activityId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityId").value(activityId))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    void listActivities_shouldReturnListOfActivities() throws Exception {
        ListActivitiesResponse expectedResponse = ListActivitiesResponse.builder()
                .activities(Collections.emptyList())
                .totalActivities(0L)
                .limit(10)
                .offset(0)
                .build();

        // MautUser is null in controller
        // Test with a specific status string
        when(activityService.listActivities(any(MautUser.class), eq(10), eq(0), eq("PENDING")))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/v1/activities")
                .param("limit", "10")
                .param("offset", "0")
                .param("status", "PENDING")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActivities").value(0));
        
        // Test with null status (when status parameter is not provided)
        when(activityService.listActivities(any(MautUser.class), eq(10), eq(0), eq(null)))
                .thenReturn(expectedResponse);

        mockMvc.perform(get("/v1/activities")
                .param("limit", "10")
                .param("offset", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActivities").value(0));
    }
}

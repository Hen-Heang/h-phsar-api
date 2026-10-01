package com.henheang.hphsar.controller.practice;

import com.henheang.hphsar.model.practice.CurrentUserResponse;
import com.henheang.hphsar.service.PracticeService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PracticeControllerTest {

    @Test
    void getCurrentUserReturnsStandardApiResponse() throws Exception {
        PracticeService practiceService = new PracticeService() {
            @Override
            public CurrentUserResponse getCurrentUser() {
                return new CurrentUserResponse(7, "buyer@example.com", 2, "BUYER");
            }
        };
        PracticeController controller = new PracticeController(practiceService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/practice/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value(2000))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.email").value("buyer@example.com"))
                .andExpect(jsonPath("$.data.role").value("BUYER"));
    }
}

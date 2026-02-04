package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.*;
import com.epam.rd.autocode.spring.project.model.enums.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MessageRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageSource messageSource;

    @Test
    void getMessage_SuccessEn() throws Exception {
        Result result = getResult("greeting", "en", "Hello World");
        messageTest(result);
    }

    @Test
    void getMessage_SuccessUA() throws Exception {
        Result result = getResult("greeting", "ua", "Привіт Світ");
        messageTest(result);
    }

    private void messageTest(Result result) throws Exception {
        when(messageSource.getMessage(eq(result.key()), isNull(), isNull(), any(Locale.class)))
                .thenReturn(result.expectedMessage());

        mockMvc.perform(get("/messages")
                        .param("key", result.key())
                        .param("language", result.language()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(result.expectedMessage()));

        verify(messageSource).getMessage(eq(result.key()), isNull(), isNull(), any(Locale.class));
    }

    private static Result getResult(String key, String  language, String expectedMessage) {
        return new Result(key, language, expectedMessage);
    }

    private record Result(String key, String language, String expectedMessage) { }
}
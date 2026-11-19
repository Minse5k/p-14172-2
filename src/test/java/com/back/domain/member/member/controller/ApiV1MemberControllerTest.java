package com.back.domain.member.member.controller;


import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1MemberControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원 가입")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "usernew",
                                            "password": "12345678",
                                            "name": "무명"
                                        }
                                        """)
                )
                .andDo(print());

        Member member = memberService.findByUsername("usernew").get();

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getName())))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("회원가입, username 중복")
    void t2() throws Exception {
        memberService.join("usernew", "12345678", "무명");

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "username": "usernew",
                                            "password": "12345678",
                                            "name": "무명"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.resultCode").value("409-1"))
                .andExpect(jsonPath("$.msg").value("usernew(은)는 이미 사용중인 username 입니다."));
    }

    @Test
    @DisplayName("내 정보")
    void t3() throws Exception {
        int actorId = 3;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/members/me?actorId=3")
                )
                .andDo(print());

        Member member = memberService.findById(actorId).get();

        resultActions
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                .andExpect(handler().methodName("me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(member.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(member.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.username").value(member.getUsername()))
                .andExpect(jsonPath("$.name").value(member.getName()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}

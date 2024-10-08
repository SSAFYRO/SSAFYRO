package com.ssafy.ssafyro.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ssafy.ssafyro.api.controller.room.request.RoomCreateRequest;
import com.ssafy.ssafyro.api.controller.room.request.RoomEnterRequest;
import com.ssafy.ssafyro.api.controller.room.request.RoomExitRequest;
import com.ssafy.ssafyro.api.service.room.request.RoomCreateServiceRequest;
import com.ssafy.ssafyro.api.service.room.request.RoomListServiceRequest;
import com.ssafy.ssafyro.api.service.room.response.RoomCreateResponse;
import com.ssafy.ssafyro.api.service.room.response.RoomDetailResponse;
import com.ssafy.ssafyro.api.service.room.response.RoomEnterResponse;
import com.ssafy.ssafyro.api.service.room.response.RoomExitResponse;
import com.ssafy.ssafyro.api.service.room.response.RoomFastEnterResponse;
import com.ssafy.ssafyro.api.service.room.response.RoomListResponse;
import com.ssafy.ssafyro.domain.room.RoomType;
import com.ssafy.ssafyro.domain.room.redis.RoomRedis;
import com.ssafy.ssafyro.security.WithMockJwtAuthentication;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

public class RoomControllerDocsTest extends RestDocsSupport {

    @DisplayName("Room 목록 조회  API")
    @Test
    void getAllRoomsTest() throws Exception {
        // given
        RoomListResponse roomListResponse = RoomListResponse.of(List.of(
                RoomRedis.builder()
                        .title("Conference Room")
                        .description("A spacious conference room")
                        .type(RoomType.PRESENTATION)
                        .capacity(3).build(),
                RoomRedis.builder()
                        .title("Meeting Room")
                        .description("A cozy meeting room")
                        .type(RoomType.PRESENTATION)
                        .capacity(3).build()));

        given(roomService.getRooms(any(RoomListServiceRequest.class)))
                .willReturn(roomListResponse);

        // when & then
        mockMvc.perform(get("/api/v1/rooms")
                        .param("title", "Room")
                        .param("type", "PRESENTATION")
                        .param("capacity", "3")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(document("get-rooms", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("title")
                                        .description("방의 제목 (예: Room)"),
                                parameterWithName("type")
                                        .description("방의 타입 (예: PRESENTATION / PERSONALITY)"),
                                parameterWithName("capacity")
                                        .description("방의 수용 인원 (예: 3)"),
                                parameterWithName("page")
                                        .description("페이지 번호 (예: 1)"),
                                parameterWithName("size")
                                        .description("페이지 당 방의 수 (예: 10)")),
                        responseFields(fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("response").type(
                                                JsonFieldType.OBJECT)
                                        .description("응답"),
                                fieldWithPath("response.rooms")
                                        .type(JsonFieldType.ARRAY)
                                        .description("방 목록"),
                                fieldWithPath("response.rooms[].id")
                                        .type(JsonFieldType.STRING)
                                        .description("방 ID"),
                                fieldWithPath("response.rooms[].title")
                                        .type(JsonFieldType.STRING)
                                        .description("방 제목"),
                                fieldWithPath("response.rooms[].description")
                                        .type(JsonFieldType.STRING)
                                        .description("방 설명"),
                                fieldWithPath("response.rooms[].type")
                                        .type(JsonFieldType.STRING)
                                        .description("방 타입"),
                                fieldWithPath("response.rooms[].capacity")
                                        .type(JsonFieldType.NUMBER)
                                        .description("방 수용 인원"),
                                fieldWithPath("response.rooms[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("방 상태"),
                                fieldWithPath("response.rooms[].participantCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("방 참가자 수"),
                                fieldWithPath("error").type(
                                                JsonFieldType.NULL)
                                        .description("에러"))));
    }

    @DisplayName("방 상세 조회 API")
    @Test
    void getRoomByIdTest() throws Exception {
        int requestRoomId = 1;

        RoomRedis roomRedis = RoomRedis.builder()
                .title("title")
                .description("description")
                .type(RoomType.PRESENTATION)
                .capacity(3).build();

        roomRedis.addParticipant(1L);
        roomRedis.addParticipant(2L);

        given(roomService.getRoomById(any(String.class))).willReturn(
                RoomDetailResponse.of(roomRedis, Map.of(1L, "유저1", 2L, "유저2")));

        mockMvc.perform(get("/api/v1/rooms/{id}", requestRoomId)).andDo(print())
                .andExpect(status().isOk())
                .andDo(document("get-room-by-id", preprocessResponse(prettyPrint()),
                        responseFields(fieldWithPath("success")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("response").type(
                                                JsonFieldType.OBJECT)
                                        .description("응답"),
                                fieldWithPath("response.title")
                                        .type(JsonFieldType.STRING)
                                        .description("방 제목"),
                                fieldWithPath("response.description")
                                        .type(JsonFieldType.STRING)
                                        .description("방 설명"),
                                fieldWithPath("response.status")
                                        .type(JsonFieldType.STRING)
                                        .description("방 상태"),
                                fieldWithPath("response.userList")
                                        .type(JsonFieldType.ARRAY)
                                        .description("방 참가자 ID 목록"),
                                fieldWithPath("response.userNameMap")
                                        .type(JsonFieldType.OBJECT)
                                        .description("방 참가자 이름 목록 (key : userId)"),
                                fieldWithPath("response.userNameMap.1")
                                        .type(JsonFieldType.STRING)
                                        .description("방 참가자 이름 목록"),
                                fieldWithPath("response.userNameMap.2")
                                        .type(JsonFieldType.STRING)
                                        .description("방 참가자 이름 목록"),
                                fieldWithPath("response.type").type(
                                                JsonFieldType.STRING)
                                        .description("방 타입"),
                                fieldWithPath("response.capacity")
                                        .type(JsonFieldType.NUMBER)
                                        .description("방 수용 인원"),
                                fieldWithPath("error").type(
                                                JsonFieldType.NULL)
                                        .description("에러"))));
    }

    @DisplayName("방 생성 API")
    @Test
    @WithMockJwtAuthentication
    void createRoom() throws Exception {
        String RoomId = "1";

        RoomCreateRequest roomCreateRequest = new RoomCreateRequest("title", "description", "type", 3);

        given(roomService.createRoom(any(RoomCreateServiceRequest.class)))
                .willReturn(RoomCreateResponse.of(RoomId));

        mockMvc.perform(post("/api/v1/rooms")
                        .header("Authorization", "Bearer {JWT Token}")
                        .content(objectMapper.writeValueAsString(roomCreateRequest))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andDo(document("create-room", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("방 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING)
                                        .description("방 설명"),
                                fieldWithPath("type").type(JsonFieldType.STRING)
                                        .description("방 이름"),
                                fieldWithPath("capacity").type(JsonFieldType.NUMBER)
                                        .description("방 수용 인원")),
                        responseFields(fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("response").type(JsonFieldType.OBJECT)
                                        .description("응답"),
                                fieldWithPath("response.roomId").type(JsonFieldType.STRING)
                                        .description("방 ID"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러"))));
    }

    @DisplayName("방 입장 API")
    @Test
    @WithMockJwtAuthentication
    void enterRoom() throws Exception {
        RoomEnterRequest roomEnterRequest = new RoomEnterRequest("1");
        RoomEnterResponse roomEnterResponse = new RoomEnterResponse();

        given(roomService.enterRoom(any(Long.class), any())).willReturn(roomEnterResponse);

        mockMvc.perform(post("/api/v1/rooms/enter")
                        .header("Authorization", "Bearer {JWT Token}")
                        .content(objectMapper.writeValueAsString(roomEnterRequest))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk())
                .andDo(document("enter-room", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("roomId").type(JsonFieldType.STRING)
                                        .description("방 ID")),
                        responseFields(fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("response").type(JsonFieldType.OBJECT)
                                        .description("응답"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러"))));
    }

    @DisplayName("방 퇴장 API")
    @Test
    @WithMockJwtAuthentication
    void exitRoom() throws Exception {
        RoomExitResponse roomExitResponse = new RoomExitResponse();
        RoomExitRequest roomEnterRequest = new RoomExitRequest("1");

        given(roomService.exitRoom(any(), any())).willReturn(roomExitResponse);

        mockMvc.perform(post("/api/v1/rooms/exit")
                        .header("Authorization", "Bearer {JWT Token}")
                        .content(objectMapper.writeValueAsString(roomEnterRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("exit-room",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(fieldWithPath("roomId").type(JsonFieldType.STRING)
                                .description("방 ID")),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("response").type(JsonFieldType.OBJECT)
                                        .description("응답"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러"))));
    }


    @DisplayName("빠른 방 입장 API")
    @Test
    void fastEnterRoom() throws Exception {
        String roomId = "roomId";
        String type = "PRESENTATION";

        given(roomService.fastRoomEnter(any(String.class)))
                .willReturn(new RoomFastEnterResponse(true, roomId));

        mockMvc.perform(
                        get("/api/v1/rooms/fast-enter")
                                .param("type", type)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("users"))
                .andDo(document("fast-enter-room",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("type").description("PERSONALITY: 인성 면접, \n PRESENTATION: PT 면접")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("response").type(JsonFieldType.OBJECT)
                                        .description("응답"),
                                fieldWithPath("response.isExisting").type(JsonFieldType.BOOLEAN)
                                        .description("방 찾음 여부"),
                                fieldWithPath("response.roomId").type(JsonFieldType.STRING)
                                        .description("방 고유 ID"),
                                fieldWithPath("error").type(JsonFieldType.NULL)
                                        .description("에러")
                        )
                ));
    }
}

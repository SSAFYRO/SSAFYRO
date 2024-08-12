import React, { useRef, useEffect, useCallback, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import ssafyLogo from "../../../../public/SSAFYRO.png";
import botIcon from "../../../../public/main/botImg.jpg";
import TwoParticipantsVideo from "./components/TwoParticipantsVideo";
import ThreeParticipantsVideo from "./components/ThreeParticipantsVideo";
import useRoomStore from "../../../stores/useRoomStore";
import axios from "axios";
import { Client } from "@stomp/stompjs";

// OpenVidu-liveKit import
import {
  LocalVideoTrack,
  RemoteParticipant,
  RemoteTrack,
  RemoteTrackPublication,
  Room,
  RoomEvent,
  TrackPublication,
} from "livekit-client";
// OpenVidu Components
import VideoComponent from "./components/VideoComponent";
import AudioComponent from "./components/AudioComponent";

// 발음 평가 API 모듈
// import { base64String, pronunciationEvaluation } from "./components/VoicePronunciationRecord";

// 표정 데이터 모듈
import { faceExpressionData } from "./components/VideoFaceApi";

// AuthStore에서 사용자 정보 가져오기
import useAuthStore from "../../../stores/AuthStore";

// 룸 컨트롤 모듈
// import { turnChange } from "./components/InterviewRules";

// 상태 관리 모듈
import useInterviewStore from "../../../stores/InterviewStore";

// Survey 모달창
import Survey from "../../../components/Survey";

// 알림창 라이브러리
import Swal from "sweetalert2";

export default function PT() {
  // 방 정보 가져오기
  const { roomid } = useParams();

  // 유저 정보 가져오기
  const userInfo = useAuthStore((state) => state.userInfo);

  const navigate = useNavigate();

  // 타이머 상태 및 Ref 추가
  const [tenMinuteTimer, setTenMinuteTimer] = useState(60);
  const [twoMinuteTimer, setTwoMinuteTimer] = useState(30);
  const timerRef = useRef();
  const twoMinuteTimerRef = useRef();

  const { userList, setUserList, userTurn, setUserTurn } = useRoomStore(
    (state) => ({
      userList: state.userList,
      setUserList: state.setUserList,
      userTurn: state.userTurn,
      setUserTurn: state.setUserTurn,
    })
  );

  const handleStartInterview = async () => {
    try {
      // 면접 시작 요청
      await axios.patch(
        "https://i11c201.p.ssafy.io:8443/api/v1/interview/start",
        { roomId: roomid },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Token")}`,
          },
        }
      );
      console.log("Interview started successfully");

      // 방의 최신 정보 불러오기
      await axios
        .get(`https://i11c201.p.ssafy.io:8443/api/v1/rooms/${roomid}`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Token")}`,
          },
        })
        .then((response) => {
          const roomData = response.data.response;
          setUserList(roomData.userList);
          console.log("updated userList: ", roomData.userList);
        })
        .catch((error) => {
          console.log("Error! : ", error);
        });

      // FIRST 메시지 전송
      if (interviewClient.current) {
        interviewClient.current.publish({
          destination: `/interview/turn/${roomid}`,
          body: JSON.stringify({ nowStage: "FIRST" }),
        });
        console.log("Sent message via STOMP");
      }
    } catch (error) {
      console.error("Error starting the interview:", error);
    }
  };

  const handleEndInterview = async () => {
    
    try {
      // 면접 종료 요청 api 호출
      await axios.patch(
        "https://i11c201.p.ssafy.io:8443/api/v1/interview/finish",
        { roomId: roomid },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Token")}`,
          },
        }
      );
      console.log("Interview finished successfully");
      
      // 종료요청을 보낸 후에, Survey에서 평가한 개인 평가 결과를 전송
      totalResult.forEach(result => {
        axios.post("https://i11c201.p.ssafy.io:8443/api/v1/reports", result, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Token")}`,
          }
        })
        .then((response) => {
          console.log("평가 결과가 성공적으로 전송되었습니다.", response.data);
        })
        .catch((error) => console.log(error))
      });

      // OpenVidu 연결 종료 및 페이지 이동
      leaveRoom();
      stop();
      navigate("/second/interview");
    } catch (error) {
      console.error("Error finishing the interview: ", error);
    }
  };

  const interviewTurnCounter = useRef(0);
  // const userList = useRoomStore((state) => state.userList);
  // const userTurn = useRoomStore((state) => state.userTurn);

  useEffect(() => {
    console.log("Current userList: ", userList);
    console.log("Current userTurn: ", userTurn);
  }, [userList, userTurn]);

  const handleStartSurvey = () => {
    navigate(`/second/interview/room/${roomid}/pt/survey`, {
      state: { targetUser: userList[userTurn] },
    });
  };

  // 답변 제출 함수
  const roomType = useInterviewStore((state) => state.roomType);
  let questions;

  if (roomType === "PRESENTATION") {
    questions = useInterviewStore((state) => state.PTQuestions);
  } else {
    questions = useInterviewStore((state) => state.personalityQuestions);
  }

  const handleSubmitAnswer = async function (
    question,
    answer,
    faceExpressionData,
    pronunciationScore
  ) {
    await axios
      .post(
        "https://i11c201.p.ssafy.io:8443/api/v1/interview/question-answer-result",
        {
          question: question,
          answer: answer,
          pronunciationScore: parseInt(pronunciationScore),
          happy: faceExpressionData.happy,
          disgust: faceExpressionData.disgusted,
          sad: faceExpressionData.sad,
          surprise: faceExpressionData.surprised,
          fear: faceExpressionData.fearful,
          angry: faceExpressionData.angry,
          neutral: faceExpressionData.neutral,
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Token")}`,
          },
        }
      )
      .then((response) => {
        // 제출 성공
        // console.log("제출되었습니다.", response.data);
        setQuestionCount((prev) => prev + 1);
        faceExpressionData = {
          angry: 0,
          disgusted: 0,
          fearful: 0,
          happy: 0,
          sad: 0,
          surprised: 0,
          neutral: 0,
        };
      })
      .catch((error) => {
        // 제출 실패
        console.log(error);
      });
  };

  // OpenVidu 연결 코드입니다.
  // 참고 출처: https://openvidu.io/3.0.0-beta2/docs/tutorials/application-client/react/#understanding-the-code
  let APPLICATION_SERVER_URL =
    "https://i11c201.p.ssafy.io:8443/api/v1/openvidu/"; // Application 서버 주소
  let LIVEKIT_URL = "wss://i11c201.p.ssafy.io/"; // LiveKit 서버 주소
  const configureUrls = function () {
    if (!APPLICATION_SERVER_URL) {
      if (window.location.hostname === "localhost") {
        APPLICATION_SERVER_URL = "http://localhost:6080/";
      } else {
        APPLICATION_SERVER_URL =
          "https://" + window.location.hostname + ":6443/";
      }
    }

    if (!LIVEKIT_URL) {
      if (window.location.hostname === "localhost") {
        LIVEKIT_URL = "ws://localhost:7880/";
      } else {
        LIVEKIT_URL = "wss://" + window.location.hostname + ":7443/";
      }
    }
  };

  configureUrls();

  // OpenVidu Token 가져오기
  /**
   * --------------------------------------------
   * GETTING A TOKEN FROM YOUR APPLICATION SERVER
   * --------------------------------------------
   * The method below request the creation of a token to
   * your application server. This prevents the need to expose
   * your LiveKit API key and secret to the client side.
   *
   * In this sample code, there is no user control at all. Anybody could
   * access your application server endpoints. In a real production
   * environment, your application server must identify the user to allow
   * access to the endpoints.
   */
  const getToken = async function (roomName, participantName) {
    const response = await fetch(APPLICATION_SERVER_URL + "token", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        roomName: roomName,
        participantName: participantName,
      }),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(`Failed to get token: ${error.errorMessage}`);
    }

    const data = await response.json();
    // console.log(data.response.token);
    return data.response.token;
  };

  // OpenVidu 연결 종료
  const leaveRoom = async function () {
    // Leave the room by calling 'disconnect' method over the Room object
    await room?.disconnect();

    try {
      await axios.post(
        `https://i11c201.p.ssafy.io:8443/api/v1/rooms/exit`,
        {
          roomId: roomid,
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Token")}`,
          },
        }
      );
      console.log("Successfully left the room");
    } catch (error) {
      console.error("Error leaving the room:", error);
    }

    // Reset the state
    setRoom(undefined);
    setLocalTrack(undefined);
    setRemoteTracks([]);
  };

  // OpenVidu 변수 초기 선언
  const [room, setRoom] = useState(undefined);
  const [localTrack, setLocalTrack] = useState(undefined);
  const [remoteTracks, setRemoteTracks] = useState([]);

  const [participantName, setParticipantName] = useState(
    userInfo.userName + Math.floor(Math.random() * 100)
  );
  const [roomName, setRoomName] = useState(roomid);

  // STOMP 클라이언트 상태
  const interviewClient = useRef(null);

  const joinRoom = async function () {
    const room = new Room(); // Initialize a now Room object
    setRoom(room);

    // Specify the actions when events take place in the room
    // On every new Track recived...
    room.on(RoomEvent.TrackSubscribed, (_track, publication, participant) => {
      setRemoteTracks((prev) => [
        ...prev,
        {
          trackPublication: publication,
          participantIdentity: participant.identity,
        },
      ]);
    });

    // On every Track destroyed...
    room.on(RoomEvent.TrackUnsubscribed, (_track, publication) => {
      setRemoteTracks((prev) =>
        prev.filter(
          (track) => track.trackPublication.trackSid !== publication.trackSid
        )
      );
    });

    try {
      // Get a token from your application server with the room name ane participant name
      // console.log(roomName, participantName);
      const token = await getToken(roomName, participantName);

      // Connect to the room with the LiveKit URL and the token
      await room.connect(LIVEKIT_URL, token);
      // console.log("Connected to the room", room.name);
      // Publish your camera and microphone
      await room.localParticipant.enableCameraAndMicrophone();
      setLocalTrack(
        room.localParticipant.videoTrackPublications.values().next().value
          .videoTrack
      );
    } catch (error) {
      console.log(
        "화상 면접실에 연결하는 중 오류가 발생했습니다.",
        error.message
      );
      await leaveRoom();
    }
  };

  // useEffect가 불필요하게 실행되는 것으로 추정되어서, joinRoomTrigger로 joinRoom 함수가 최초 한 번만 실행되도록 제어합니다.
  let joinRoomTrigger = 1;

  useEffect(() => {
    if (joinRoomTrigger === 1) {
      joinRoomTrigger = 0;
      joinRoom();
    }
  }, [joinRoomTrigger]);

  // 음성 인식 라이브러리와 변수

  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState("");

  const recognitionRef = useRef(null);

  useEffect(() => {
    const SpeechRecognition =
      window.SpeechRecognition || window.webkitSpeechRecognition;
    recognitionRef.current = new SpeechRecognition();
    recognitionRef.current.continuous = true;
    recognitionRef.current.interimResults = false;
    recognitionRef.current.lang = "ko-KR";

    recognitionRef.current.onresult = (event) => {
      const current = event.resultIndex;
      console.log(event.results[current]);
      const transcript = event.results[current][0].transcript;
      setTranscript((prevTranscript) => prevTranscript + transcript);
    };

    recognitionRef.current.onerror = (event) => {
      console.error("Speech recognition error", event.error);
    };

    return () => {
      if (recognitionRef.current) {
        recognitionRef.current.stop();
      }
    };
  }, []);

  const startListening = useCallback(() => {
    setIsListening(true);
    recognitionRef.current.start();
  }, []);

  const stopListening = useCallback(() => {
    setIsListening(false);
    recognitionRef.current.stop();
  }, []);

  let STTTrigger = 1;
  useEffect(() => {
    if (STTTrigger === 1) {
      STTTrigger = 0;
      setIsListening(true);
      recognitionRef.current.start();
    }
  }, [STTTrigger]);

  const startTimer = (stage) => {
    // 10분 타이머가 끝나면 2분 타이머로 전환되도록 설계
    if (timerRef.current) {
      clearInterval(timerRef.current);
    }

    if (twoMinuteTimerRef.current) {
      clearInterval(twoMinuteTimerRef.current);
    }

    if (stage === "FIRST") {
      setTenMinuteTimer(60);
    } else if (stage === "SECOND" || stage === "THIRD") {
      setTenMinuteTimer(60);
    }

    timerRef.current = setInterval(() => {
      setTenMinuteTimer((prevSeconds) => prevSeconds - 1);
    }, 1000);

    console.log(`${stage} 단계 타이머가 시작되었습니다.`);
  };

  useEffect(() => {
    if (interviewClient.current) {
      let stage;
      if (userTurn === 1) {
        stage = "SECOND";
      } else if (userTurn === 2) {
        stage = "THIRD";
      }

      if (stage) {
        interviewClient.current.publish({
          destination: `/interview/turn/${roomid}`,
          body: JSON.stringify({ nowStage: stage }),
        });
        console.log(`${stage} 메시지를 서버로 전송했습니다.`);
      }
    }
  }, [userTurn]);

  // STOMP 클라이언트 초기화 및 메시지 구독
  useEffect(() => {
    const client = new Client({
      brokerURL: `wss://i11c201.p.ssafy.io:8443/ssafyro-chat`,
      onConnect: async () => {
        console.log("STOMP client connected");

        client.subscribe(`/topic/interview/${roomid}`, (message) => {
          const parsedMessage = JSON.parse(message.body);

          if (parsedMessage.nowStage) {
            console.log(
              `${parsedMessage.nowStage} 파일을 받았으므로 타이머를 시작합니다.`
            );
            startTimer(parsedMessage.nowStage);
          }
        });

        // STOMP 연결이 완료된 후에 면접 시작 요청 및 메시지 전송
        await handleStartInterview();

        // 타이머가 중복 설정되지 않도록 clearInterval
        if (timerRef.current) {
          clearInterval(timerRef.current);
        }

        // // 10분 타이머 시작
        // timerRef.current = setInterval(() => {
        //   setTenMinuteTimer((prevSeconds) => prevSeconds - 1);
        // }, 1000);
      },
      onDisconnect: () => {
        console.log("STOMP Client disconnected");
      },
    });

    client.activate();
    interviewClient.current = client;

    return () => {
      if (interviewClient.current) {
        interviewClient.current.deactivate();
      }
      clearInterval(timerRef.current);
      clearInterval(twoMinuteTimerRef.current);
    };
  }, [roomid]);

  useEffect(() => {
    if (tenMinuteTimer <= 0) {
      clearInterval(timerRef.current); // 10분 타이머가 끝나면 정지

      Swal.fire({
        title: "면접 차례가 종료되었습니다.",
        text: "2분동안 상호평가가 진행됩니다.",
        icon: "info",
      }).then((result) => {
        if (result.isConfirmed) {
          setModalOpen();
        }
      });

      // 2분 타이머 시작
      twoMinuteTimerRef.current = setInterval(() => {
        setTwoMinuteTimer((prevSeconds) => prevSeconds - 1);
      }, 1000);
    }
  }, [tenMinuteTimer]);

  useEffect(() => {
    if (twoMinuteTimer <= 0) {
      clearInterval(twoMinuteTimerRef.current);
      // 면접 턴 카운터 증가
      interviewTurnCounter.current += 1;

      // 다음 사용자를 위해 userTurn을 증가시켜 다음 면접자의 Survey를 설정
      const nextTurn = (userTurn + 1) % userList.length;
      setUserTurn(nextTurn);

      // 면접을 종료하거나 다음 면접자 준비
      if (interviewTurnCounter.current >= userList.length) {
        Swal.fire({
          title: "면접이 종료되었습니다.",
          text: "면접이 모두 종료되었습니다. 수고하셨습니다.",
          icon: "success",
        }).then((result) => {
          if (result.isConfirmed) {
            handleEndInterview();
          }
        })
      }
      else if (userList.length === 1) {
        Swal.fire({
          title: "면접이 종료되었습니다.",
          text: "면접이 모두 종료되었습니다. 수고하셨습니다.",
          icon: "success",
        }).then((result) => {
          if (result.isConfirmed) {
            handleEndInterview();
          }
        })
      }
    }
  }, [twoMinuteTimer]);

  // // 타이머 시작 및 종료 처리
  // useEffect(() => {
  //   // 면접 시작 요청 API 호출
  //   handleStartInterview()

  //   // 10분 타이머 시작
  //   timerRef.current = setInterval(() => {
  //     setTenMinuteTimer((prevSeconds) => prevSeconds - 1);
  //   }, 1000);

  //   return () => clearInterval(timerRef.current); // 컴포넌트 언마운트 시 타이머 클리어
  // }, []);

  // 시간 형식 변환 함수
  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes < 10 ? "0" : ""}${minutes}:${
      remainingSeconds < 10 ? "0" : ""
    }${remainingSeconds}`;
  };

  // 면접 컨트롤을 위한 함수와 변수들
  const [questionCount, setQuestionCount] = useState(0);

  // 면접 평가 modal 창
  const [isModalOpen, setIsModalOpen] = useState(false);

  const setModalOpen = function () {
    setIsModalOpen(true);
  };

  const setModalClose = function () {
    setIsModalOpen(false);
  };

  // 면접 평가 데이터
  const [totalResult, setTotalResult] = useState([]);

  const renewTotalResult = function (newResult) {setTotalResult((prev) => [...prev, newResult])};

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-100 p-6">
      <div
        className="bg-white shadow-md rounded-lg px-8 py-6 w-full max-w-6xl"
        style={{ minHeight: "80vh" }}
      >
        <div className="flex justify-between items-center mb-4">
          <div className="flex items-center">
            <img src={ssafyLogo} alt="ssafylogo" className="h-[20px] mr-5" />
            <h1 className="text-xl font-bold">Presentation Interview</h1>
          </div>
          <div className="flex items-center bg-black text-white rounded-full px-8 py-2 w-48 justify-center">
            <div
              className={`w-2 h-2 rounded-full mr-2 ${
                tenMinuteTimer > 0
                  ? tenMinuteTimer <= 60
                    ? "bg-red-500 animate-ping mr-3"
                    : tenMinuteTimer <= 180
                    ? "bg-yellow-500"
                    : "bg-green-500"
                  : twoMinuteTimer <= 60
                  ? "bg-red-500 animate-ping mr-3"
                  : "bg-yellow-500"
              }`}
            ></div>
            <span className="font-bold">
              {tenMinuteTimer > 0
                ? formatTime(tenMinuteTimer)
                : formatTime(twoMinuteTimer)}
            </span>
            {/* <span className="font-bold">{formatTime(millitenMinuteTimer)}</span> */}
          </div>
        </div>
        {/* 변경해야 할곳 1 */}
        <div className="flex" style={{ height: "400px" }}>
          {/* OpenVidu 화상 회의 레이아웃 */}
          {(() => {
            // console.log("remoteTracks: ", remoteTracks);
            if (remoteTracks.length <= 2) {
              // console.log("두명 전용 방으로 이동");
              return (
                <TwoParticipantsVideo
                  localTrack={localTrack}
                  participantName={participantName}
                  remoteTracks={remoteTracks}
                  handleEndInterview={handleEndInterview}
                  isListening={isListening}
                  startListening={startListening}
                  stopListening={stopListening}
                  questions={questions}
                  answer={transcript}
                  faceExpressionData={faceExpressionData}
                  handleSubmitAnswer={handleSubmitAnswer}
                  handleStartSurvey={handleStartSurvey}
                  userList={userList}
                  userTurn={userTurn}
                  setModalOpen={setModalOpen}
                />
              );
            } else {
              console.log("세명 전용 방으로 이동");
              return (
                <ThreeParticipantsVideo
                  localTrack={localTrack}
                  participantName={participantName}
                  remoteTracks={remoteTracks}
                  handleEndInterview={handleEndInterview}
                  isListening={isListening}
                  startListening={startListening}
                  stopListening={stopListening}
                  questions={questions}
                  answer={transcript}
                  faceExpressionData={faceExpressionData}
                  handleSubmitAnswer={handleSubmitAnswer}
                  handleStartSurvey={handleStartSurvey}
                  userList={userList}
                  userTurn={userTurn}
                  setModalOpen={setModalOpen}
                />
              );
            }
          })()}
        </div>
        <div className="bg-gray-200 p-4 rounded-lg mb-4 mt-5 h-[170px] flex items-center">
          <div className="flex items-center">
            <img
              src={botIcon}
              alt="botIcon"
              className="w-[50px] h-[50px] rounded-full bg-blue-500"
            />
            <p className="ml-4">
              안녕하세요! {userInfo.userName} 님에 대한 면접 질문을 추천해
              드릴게요! <br />
              {questionCount < 2
                ? questions[questionCount]
                : "본인 질문이 종료되었습니다."}
            </p>
          </div>
        </div>
      </div>
      {/* survey 모달창 */}
      { isModalOpen && <>
        <div className="fixed z-10 h-dvh w-full bg-neutral-800/50 flex justify-center items-center">
          <div className="w-4/5 bg-white border rounded-lg py-5 px-5">
            <Survey
              targetUser={userList[userTurn]}
              setModalClose={setModalClose}
              setTotalResult={renewTotalResult}
             />
          </div>
        </div>
      </>}
    </div>
  );
}

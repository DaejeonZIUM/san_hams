package com.ziumks.hams.service;

import com.ziumks.hams.domain.object.HamsConMsgDTO;
import com.ziumks.hams.domain.object.HamsInfoDTO;
import com.ziumks.hams.domain.object.HamsPoMsgDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service("hamsService")
public class HamsService {

    @Autowired
    HamsInfoDTO hamsInfo;
    Socket socket = null;
    OutputStream os = null; // Client에서 Server로 보내기 위한 통로
    InputStream is = null; // Server에서 보낸 값을 받기 위한 통로

    public ResponseEntity<String> hamsCon() {
        boolean result = false;
        String resultFromServer = null;
        String conMessage = null;
        try {
            // Hams 연결 명령어
            HamsConMsgDTO hamsConMsg = HamsConMsgDTO.builder()
                    .connector_ID(hamsInfo.getConn_id()) // Header
                    .dat_FLDS("2") // 바디 데이터 필드 수
                    .id(hamsInfo.getId()) // Body
                    .pw(hamsInfo.getPw())
                    .ETX(hamsInfo.getEtx()) // Tail
                    .build();
            // CRC16 코드 계산(Body 영역까지)
            String crc16 = CRC16_CCITT(hamsInfo.getCrc16_polynomial(), hamsConMsg.getBody());
            hamsConMsg.setCRC16(crc16);
            // 최종 명령어 출력(윈도우 서버 개행문자 리플레이스 추가)
            conMessage = hamsConMsg.getMsg();
            log.info("Hams output Con Mesaage check : " + conMessage.replace("\r\n", "\\r\\n"));
            // 소켓 연결
            socketConnect(hamsInfo.getHost(), hamsInfo.getPort());
            // 소켓 데이터 전송
            resultFromServer = socketSend(conMessage);
            log.info("Hams return Con Message check : " + resultFromServer);
            result = true;
        } catch (Exception e) {
            // socket 연결 실패.
            log.error("Hams CON error", e);
        }
        if (result) {
            // CON 성공
            return ResponseEntity.ok(resultFromServer);
        }
        // CON 실패
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resultFromServer);
    }

    public ResponseEntity<String> hamsPos(String equipment_ID, String port) throws IOException {
        // Hams Server 연결
        ResponseEntity<String> conResult = hamsCon();
        if (conResult.getStatusCode() == HttpStatus.OK) {
            boolean result = false;
            String resultFromServer = null;
            String posMessage = null;
            try {
                // Hams 연결 명령어
                HamsPoMsgDTO hamsPosMsg = HamsPoMsgDTO.builder()
                        .equipment_ID(equipment_ID) // Header
                        .set_CMD("POS")
                        .dat_FLDS("1")
                        .port(port) // body
                        .ETX(hamsInfo.getEtx()) // tail
                        .build();
                // CRC16 코드 계산(Body 영역까지)
                String crc16 = CRC16_CCITT(hamsInfo.getCrc16_polynomial(), hamsPosMsg.getPosBody());
                hamsPosMsg.setCRC16(crc16);
                // 최종 명령어 출력(윈도우 서버 개행문자 리플레이스 추가)
                posMessage = hamsPosMsg.getPosMsg();
                log.info("Hams output POS Mesaage check : " + posMessage.replace("\r\n", "\\r\\n"));
                // 소켓 데이터 전송
                resultFromServer = socketSend(posMessage);
                log.info("Hams POS return Message check : " + resultFromServer);
                result = true;
            } catch (Exception e) {
                log.error("Hams POS error", e);
            } finally {
                // Hams Server 연결종료 socket.close
                socketDisconnect();
            }
            if (result) {
                return ResponseEntity.ok(resultFromServer);
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resultFromServer);
        }
        // CON 연결 실패
        socketDisconnect();
        return conResult;
    }

    public ResponseEntity<String> hamsPoc(String equipment_ID, String port, String type) throws IOException {
        // Hams Server 연결
        ResponseEntity<String> conResult = hamsCon();
        if (conResult.getStatusCode() == HttpStatus.OK) {
            boolean result = false;
            String resultFromServer = null;
            String pocMessage = null;
            try {
                // Hams 연결 명령어
                HamsPoMsgDTO hamsPocMsg = HamsPoMsgDTO.builder()
                        .equipment_ID(equipment_ID) // Header
                        .set_CMD("POC")
                        .dat_FLDS("2")
                        .port(port) // Body
                        .type(type)
                        .ETX(hamsInfo.getEtx()) // Tail
                        .build();
                // CRC16 코드 계산(Body 영역까지)
                String crc16 = CRC16_CCITT(hamsInfo.getCrc16_polynomial(), hamsPocMsg.getPocBody());
                hamsPocMsg.setCRC16(crc16);
                // 최종 명령어 출력(윈도우 서버 개행문자 리플레이스 추가)
                pocMessage = hamsPocMsg.getPocMsg();
                log.info("Hams output Poc Mesaage check : " + pocMessage.replace("\r\n", "\\r\\n"));
                // 소켓 데이터 전송
                resultFromServer = socketSend(pocMessage);
                log.info("Hams return Poc Message check : " + resultFromServer);
                result = true;
            } catch (Exception e) {
                log.error("Hams POC error", e);
            } finally {
                // Hams Server 연결종료
                socketDisconnect();
            }
            if (result) {
                return ResponseEntity.ok(resultFromServer);
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resultFromServer);
        }
        // CON 연결 실패
        socketDisconnect();
        return conResult;
    }

    /*
     *  CRC16 계산기
     */
    public String CRC16_CCITT(int crc16_polynomial, String str) {
        int crc = 0xFFFF;
        byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
        for (byte b : bytes) {
            crc ^= (b << 8);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ crc16_polynomial;
                } else {
                    crc = crc << 1;
                }
            }
        }
        return Integer.toHexString(crc & 0xFFFF).toUpperCase();
    }
    /*
     * 소켓 관련 메소드
     */
    public void socketConnect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        log.info("socket info : " + hamsInfo.getHost() + ":" + hamsInfo.getPort());
        log.info("socket connect...");
        os = socket.getOutputStream(); // Client에서 Server로 보내기 위한 통로
        is = socket.getInputStream(); // Server에서 보낸 값을 받기 위한 통로
    }
    public void socketDisconnect() {
        Optional<Socket> optSocket = Optional.ofNullable(socket);
        optSocket.ifPresent(socket -> {
            try {
                socket.close();
                log.info("socket disconnect...");
            } catch (IOException e) {
                log.error("socket disconnect error", e);
            }
        });
    }
    public String socketSend(String request) throws IOException {
        String response = null;
        // 바이트로 변환 후 전송
        log.info(Arrays.toString(request.getBytes()));
        os.write(request.getBytes());
        os.flush();
        byte[] responseBytes = new byte[100];
        int bytesRead = is.read(responseBytes);
        // 리스폰스 데이터 확인
        response = new String(responseBytes, 0, bytesRead);
        return response;
    }
}

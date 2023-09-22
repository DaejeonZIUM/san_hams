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
        try {
            // Hams 연결 명령어
            String conMessage = null;
            HamsConMsgDTO hamsConMsg = new HamsConMsgDTO();
            /* Header */
            hamsConMsg.setConnector_ID(hamsInfo.getConn_id());
            hamsConMsg.setDat_FLDS("2"); // 바디 데이터 필드 수
            /* Body */
            hamsConMsg.setId(hamsInfo.getId());
            hamsConMsg.setPw(hamsInfo.getPw());
            // CRC16 코드 계산(Body 영역까지)
            byte[] bytes = hamsConMsg.getBody().getBytes(Charset.forName("UTF-8"));
            String crc16 = CRC16_CCITT(hamsInfo.getCrc16_polynomial(), bytes);
            /* Tail */
            hamsConMsg.setCRC16(crc16);
            hamsConMsg.setETX(hamsInfo.getEtx());
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
            //1. socket 연결 실패.
            e.printStackTrace();
        }
        if (result) {
            //2. CON 성공
            return ResponseEntity.ok(resultFromServer);
        }
        //3. CON 실패
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resultFromServer);
    }

    public ResponseEntity<String> hamsPos(String equipment_ID, String port) throws IOException {
        // Hams Server 연결
        ResponseEntity<String> conResult = hamsCon();
        if (conResult.getStatusCode() == HttpStatus.OK) {
            boolean result = false;
            String resultFromServer = null;
            try {
                // Hams 연결 명령어
                String posMessage = null;
                HamsPoMsgDTO hamsPosMsg = new HamsPoMsgDTO();
                /* Header */
                hamsPosMsg.setEquipment_ID(equipment_ID);
                hamsPosMsg.setSet_CMD("POS");
                hamsPosMsg.setDat_FLDS("1"); // 바디 데이터 필드 수
                /* Body */
                hamsPosMsg.setPort(port);
                // CRC16 코드 계산(Body 영역까지)
                byte[] bytes = hamsPosMsg.getPosBody().getBytes(Charset.forName("UTF-8"));
                String crc16 = CRC16_CCITT(hamsInfo.getCrc16_polynomial(), bytes);
                /* Tail */
                hamsPosMsg.setCRC16(crc16);
                hamsPosMsg.setETX(hamsInfo.getEtx());
                // 최종 명령어 출력(윈도우 서버 개행문자 리플레이스 추가)
                posMessage = hamsPosMsg.getPosMsg();
                log.info("Hams output POS Mesaage check : " + posMessage.replace("\r\n", "\\r\\n"));
                // 소켓 데이터 전송
                resultFromServer = socketSend(posMessage);
                log.info("Hams POS return Message check : " + resultFromServer);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
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
            try {
                // Hams 연결 명령어
                String pocMessage = null;
                HamsPoMsgDTO hamsPocMsg = new HamsPoMsgDTO();
                /* Header */
                hamsPocMsg.setEquipment_ID(equipment_ID);
                hamsPocMsg.setSet_CMD("POC");
                hamsPocMsg.setDat_FLDS("2"); // 바디 데이터 필드 수
                /* Body */
                hamsPocMsg.setPort(port);
                hamsPocMsg.setType(type);
                // CRC16 코드 계산(Body 영역까지)
                byte[] bytes = hamsPocMsg.getPocBody().getBytes(Charset.forName("UTF-8"));
                String crc16 = CRC16_CCITT(hamsInfo.getCrc16_polynomial(), bytes);
                /* Tail */
                hamsPocMsg.setCRC16(crc16);
                hamsPocMsg.setETX(hamsInfo.getEtx());
                // 최종 명령어 출력(윈도우 서버 개행문자 리플레이스 추가)
                pocMessage = hamsPocMsg.getPocMsg();
                log.info("Hams output Poc Mesaage check : " + pocMessage.replace("\r\n", "\\r\\n"));
                // 소켓 데이터 전송
                resultFromServer = socketSend(pocMessage);
                log.info("Hams return Poc Message check : " + resultFromServer);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
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
    public String CRC16_CCITT(int crc16_polynomial, byte[] bytes) {
        int crc = 0xFFFF;

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
                e.printStackTrace();
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

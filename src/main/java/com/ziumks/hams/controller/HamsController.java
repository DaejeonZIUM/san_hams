package com.ziumks.hams.controller;

import com.ziumks.hams.dto.HamsConMsgDto;
import com.ziumks.hams.dto.HamsInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 서치 라이트 연계 API 입니다.
 * 작성자 : 김주현
 * */
@RestController
@Slf4j
@RequestMapping(value = "/hams")
public class HamsController {

    @Autowired
    private HamsInfoDto hamsInfo;

    @GetMapping("/con")
    public ResponseEntity<String> hamsCon() {

        Socket socket = null;
        boolean result = false;
        String resultFromServer = null;

        try {
            // Hams 연결 명령어
            String ConMessage = null;
            HamsConMsgDto hamsConMsg = new HamsConMsgDto();
            /* Header */
            hamsConMsg.setSentence_ID("PGL");
            hamsConMsg.setTalk_ID("CL");
            hamsConMsg.setConnector_ID(hamsInfo.getConn_id());
            hamsConMsg.setSet_CMD("CON");
            hamsConMsg.setSeq_NO("000");
            hamsConMsg.setPck_Ver("1");
            hamsConMsg.setDat_FLDS("2");
            /* Body */
            hamsConMsg.setId(hamsInfo.getId());
            hamsConMsg.setPw(hamsInfo.getPw());
            // CRC16 코드 계산(Body 영역까지)
            byte[] bytes = hamsConMsg.getBody().getBytes(Charset.forName("UTF-8"));
            String crc16 = CRC16_CCITT(hamsInfo.getCrc16_polynomial(), bytes);
            /* Tail */
            hamsConMsg.setCRC16(crc16);
            hamsConMsg.setETX(hamsInfo.getEtx());

            // 최종 명령어 출력
            ConMessage = hamsConMsg.getMsg();
            log.info("Hams output ConMesaage check : " + ConMessage);

            // 소켓 연결
            socket = new Socket(hamsInfo.getHost(), hamsInfo.getPort());
            log.info("socket 연결 시작");
            OutputStream os = socket.getOutputStream(); // Client에서 Server로 보내기 위한 통로
            InputStream is = socket.getInputStream(); // Server에서 보낸 값을 받기 위한 통로
            // 바이트로 변환 후 전송
            log.info(Arrays.toString(ConMessage.getBytes()));
            os.write(ConMessage.getBytes());
            os.flush();
            byte[] response = new byte[100];
            int L = is.read(response);
            // 리스폰스 데이터 확인
            resultFromServer = new String(response, 0, L);
            log.info("Hams return Message check :  " + resultFromServer);
            socket.close();
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result) {
            return ResponseEntity.ok(resultFromServer);
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resultFromServer);
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

}
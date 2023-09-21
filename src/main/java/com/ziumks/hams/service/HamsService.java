package com.ziumks.hams.service;

import com.ziumks.hams.dto.HamsConMsgDto;
import com.ziumks.hams.dto.HamsInfoDto;
import com.ziumks.hams.dto.HamsPocMsgDto;
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

@Slf4j
@Service("hamsService")
public class HamsService {

    @Autowired
    HamsInfoDto hamsInfo;
    Socket socket = null;
    OutputStream os =null; // Client에서 Server로 보내기 위한 통로
    InputStream is = null;// Server에서 보낸 값을 받기 위한 통로
    boolean socketStatus = false;

    boolean ConStatus = false;

    public ResponseEntity<String> hamsCon() {

//        Socket socket = null;
        boolean result = false;
        String resultFromServer = null;
        try {
            // Hams 연결 명령어
            String conMessage = null;
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

            // 최종 명령어 출력(윈도우 서버 개행문자 리플레이스 추가)
            conMessage = hamsConMsg.getMsg();
            log.info("Hams output ConMesaage check : " + conMessage.replace("\r\n", "\\r\\n"));
            // 소켓 연결
            socket = new Socket(hamsInfo.getHost(), hamsInfo.getPort());
            log.info("socket 연결 시작");
             os = socket.getOutputStream(); // Client에서 Server로 보내기 위한 통로
             is = socket.getInputStream(); // Server에서 보낸 값을 받기 위한 통로
            // 바이트로 변환 후 전송
            log.info(Arrays.toString(conMessage.getBytes()));
            os.write(conMessage.getBytes());
            os.flush();
            byte[] response = new byte[100];
            int L = is.read(response);
            // 리스폰스 데이터 확인
            resultFromServer = new String(response, 0, L);
            log.info("Hams return Message check : " + resultFromServer);
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
                HamsPocMsgDto hamsPocMsg = new HamsPocMsgDto();
                /* Header */
                hamsPocMsg.setSentence_ID("PGL");
                hamsPocMsg.setTalk_ID("CL");
                hamsPocMsg.setEquipment_ID(equipment_ID);
                hamsPocMsg.setSet_CMD("POS");
                hamsPocMsg.setSeq_NO("000");
                hamsPocMsg.setPck_Ver("1");
                hamsPocMsg.setDat_FLDS("1");
                /* Body */
                hamsPocMsg.setPort(port);
                // CRC16 코드 계산(Body 영역까지)
                byte[] bytes = hamsPocMsg.getPosBody().getBytes(Charset.forName("UTF-8"));
                String crc16 = CRC16_CCITT(hamsInfo.getCrc16_polynomial(), bytes);

                /* Tail */
                hamsPocMsg.setCRC16(crc16);
                hamsPocMsg.setETX(hamsInfo.getEtx());

                // 최종 명령어 출력(윈도우 서버 개행문자 리플레이스 추가)
                posMessage = hamsPocMsg.getPosMsg();
                log.info("Hams output POS Mesaage check : " + posMessage.replace("\r\n", "\\r\\n"));
                // 소켓 연결
                log.info("socket POS 연결 시작");

                // 바이트로 변환 후 전송
                log.info(Arrays.toString(posMessage.getBytes()));
                os.write(posMessage.getBytes());
                os.flush();
                byte[] response = new byte[100];
                int L = is.read(response);
                // 리스폰스 데이터 확인
                resultFromServer = new String(response, 0, L);
                log.info("Hams POS return Message check : " + resultFromServer);
                result = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result) {
                return ResponseEntity.ok(resultFromServer);
            }
            // Hams Server 연결종료 socket.close
            socket.close();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resultFromServer);

        }
        socket.close();
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
                HamsPocMsgDto hamsPocMsg = new HamsPocMsgDto();
                /* Header */
                hamsPocMsg.setSentence_ID("PGL");
                hamsPocMsg.setTalk_ID("CL");
                hamsPocMsg.setEquipment_ID(equipment_ID);
                hamsPocMsg.setSet_CMD("POC");
                hamsPocMsg.setSeq_NO("000");
                hamsPocMsg.setPck_Ver("1");
                hamsPocMsg.setDat_FLDS("2");
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

                log.info("socket POC 연결 시작");

                // 바이트로 변환 후 전송
                log.info(Arrays.toString(pocMessage.getBytes()));
                os.write(pocMessage.getBytes());
                os.flush();
                byte[] response = new byte[100];
                int L = is.read(response);
                // 리스폰스 데이터 확인
                resultFromServer = new String(response, 0, L);
                log.info("Hams return Poc Message check : " + resultFromServer);

                result = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result) {
                return ResponseEntity.ok(resultFromServer);
            }
            // Hams Server 연결종료 socket.close
            socket.close();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(resultFromServer);
        }
//
        socket.close();
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


}

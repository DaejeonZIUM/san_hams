package com.ziumks.hams.domain.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HamsPoMsgDTO {
    /* Header */
    private String prompt = "$";
    private String sentence_ID = "PGL";  //PGL
    private String talk_ID = "CL"; //CL - Message 생성 체 Client
    private String equipment_ID;  // 고유 장비 ID ex) 006000001
    private String set_CMD = "POC";  //설정 명령어
    private String seq_NO = "000";  //시쿼스 번호 기본 000
    private String pck_Ver = "1";   //패킷 버전 기본 1
    private String dat_FLDS = "2"; //데이터 필드수 기본 2
    /* Body */
    private String port = "2";  // 전원 포트 번호(POS만 0: 전체)
    private String type; // 0: off, 1: on, 2: restart
    /* Tail */
    private String asterisk = "*";    // CRC-16 필드 직전임을 표시 *
    private String CRC16;   // CRC 코드 기본 CRC-16-CCITT (0xFFFF)
    private String ETX;  // 기본 <CR><LF>, 윈도우 서버 \r\n
    /* Separator */
    private String sep = ","; // 구분자 기본 ","

    // hams server request 명령어
    public String getPosMsg() {
        return this.prompt // Header
                + this.sentence_ID
                + this.sep
                + this.talk_ID
                + this.sep
                + this.equipment_ID
                + this.sep
                + this.set_CMD
                + this.sep
                + this.seq_NO
                + this.sep
                + this.pck_Ver
                + this.sep
                + this.dat_FLDS
                + this.sep
                + this.port // Body
                + this.asterisk // Tail
                + this.CRC16
                + this.ETX;
    }
    public String getPocMsg() {
        return this.prompt // Header
                + this.sentence_ID
                + this.sep
                + this.talk_ID
                + this.sep
                + this.equipment_ID
                + this.sep
                + this.set_CMD
                + this.sep
                + this.seq_NO
                + this.sep
                + this.pck_Ver
                + this.sep
                + this.dat_FLDS
                + this.sep
                + this.port // Body
                + this.sep
                + this.type
                + this.asterisk // Tail
                + this.CRC16
                + this.ETX;
    }

    // CRC16 코드 계산용
    public String getPosBody() {
        return this.sentence_ID // Header
                + this.sep
                + this.talk_ID
                + this.sep
                + this.equipment_ID
                + this.sep
                + this.set_CMD
                + this.sep
                + this.seq_NO
                + this.sep
                + this.pck_Ver
                + this.sep
                + this.dat_FLDS
                + this.sep
                + this.port; // Body
    }

    public String getPocBody() {
        return this.sentence_ID // Header
                + this.sep
                + this.talk_ID
                + this.sep
                + this.equipment_ID
                + this.sep
                + this.set_CMD
                + this.sep
                + this.seq_NO
                + this.sep
                + this.pck_Ver
                + this.sep
                + this.dat_FLDS
                + this.sep
                + this.port // Body
                + this.sep
                + this.type;
    }

}
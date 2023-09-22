package com.ziumks.hams.domain.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HamsConMsgDTO {
    /* Header */
    private String prompt = "$";
    private String sentence_ID = "PGL";  //PGL
    private String talk_ID = "CL"; //CL - Message 생성 체 Client
    private String connector_ID = "000"+"000003";  // 지역코드(000) + 접속자 고유ID(000002)
    private String set_CMD = "CON";  //설정 명령어
    private String seq_NO = "000";  //시쿼스 번호 기본 000
    private String pck_Ver = "1";   //패킷 버전 기본 1
    private String dat_FLDS = "2"; //데이터 필드수 기본 2
    /* Body */
    private String id = "admin";  // 유저 id
    private String pw = "xAi2q95P2YKO55Vl1pkWGg=="; // 유저 패스워드(암호화)
    /* Tail */
    private String asterisk = "*";    // CRC-16 필드 직전임을 표시 *
    private String CRC16;   // CRC 코드 기본 CRC-16-CCITT (0xFFFF)
    private String ETX;  // 기본 <CR><LF>, 윈도우 서버 \r\n
    /* Separator */
    private String sep = ","; // 구분자 기본 ","

    // hams server request 명령어
    public String getMsg() {
        return this.prompt // Header
                + this.sentence_ID
                + this.sep
                + this.talk_ID
                + this.sep
                + this.connector_ID
                + this.sep
                + this.set_CMD
                + this.sep
                + this.seq_NO
                + this.sep
                + this.pck_Ver
                + this.sep
                + this.dat_FLDS
                + this.sep
                + this.id // Body
                + this.sep
                + this.pw
                + this.asterisk // Tail
                + this.CRC16
                + this.ETX;
    }

    // CRC16 코드 계산용
    public String getBody() {
        return this.sentence_ID // Header
                + this.sep
                + this.talk_ID
                + this.sep
                + this.connector_ID
                + this.sep
                + this.set_CMD
                + this.sep
                + this.seq_NO
                + this.sep
                + this.pck_Ver
                + this.sep
                + this.dat_FLDS
                + this.sep
                + this.id // Body
                + this.sep
                + this.pw;
    }

}
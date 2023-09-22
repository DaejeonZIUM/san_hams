package com.ziumks.hams.domain.object;

import lombok.*;

import java.nio.charset.Charset;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HamsConMsgDTO {
    /* Header */
    @Builder.Default
    private String prompt = "$"; // 프롬프트
    @Builder.Default
    private String sentence_ID = "PGL";  // PGL
    @Builder.Default
    private String talk_ID = "CL"; // CL - Message 생성 체 Client
    private String connector_ID;  // 지역코드(000) + 접속자 고유ID(000002)
    @Builder.Default
    private String set_CMD = "CON";  // 설정 명령어
    @Builder.Default
    private String seq_NO = "000";  // 시쿼스 번호 기본 000
    @Builder.Default
    private String pck_Ver = "1";   // 패킷 버전 기본 1
    private String dat_FLDS; // 바디 데이터 필드수
    /* Body */
    private String id;  // 유저 id
    private String pw; // 유저 패스워드(암호화)
    /* Tail */
    @Builder.Default
    private String asterisk = "*";    // CRC-16 필드 직전임을 표시 *
    private String CRC16;   // CRC 코드 기본 CRC-16-CCITT (0xFFFF)
    private String ETX;  // 기본 <CR><LF>, 윈도우 서버 \r\n
    /* Separator */
    @Builder.Default
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
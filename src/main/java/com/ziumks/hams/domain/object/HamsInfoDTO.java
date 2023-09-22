package com.ziumks.hams.domain.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HamsInfoDTO {

    private String host; // hams server host

    private int port; // hams server port

    private String conn_id; // connector_id

    private String id; // user id

    private String pw; // user pw

    private int crc16_polynomial; // crc16 ccitt

    private String etx; // etx

}

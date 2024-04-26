package com.seer.rbk;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class RbkResult {
    private RbkResultKind kind;
    private String ip;
    private Integer apiNo;
    private String reqStr;  // 请求rbk的报文
    private String resStr;  // rbk返回的报文
    private String errMsg;  // 请求rbk失败的信息
}

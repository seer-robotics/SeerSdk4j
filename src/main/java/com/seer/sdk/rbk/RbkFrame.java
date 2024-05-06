package com.seer.sdk.rbk;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
class RbkFrame {
    private Integer flowNo;//序号
    private Integer apiNo;//rbk 编号
    private String bodyStr;//数据区的报文
}

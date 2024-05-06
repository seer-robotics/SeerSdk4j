package com.seer.sdk.rbk;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RbkClient {
    private String host;
    RbkPortClient configClient;
    RbkPortClient miscClient;
    RbkPortClient stateClient;
    RbkPortClient controlClient;
    RbkPortClient navClient;

    public RbkClient(String host) {
        this.host = host;
        this.configClient = new RbkPortClient(host, 19207);
        this.miscClient = new RbkPortClient(host, 19210);
        this.stateClient = new RbkPortClient(host, 19204);
        this.controlClient = new RbkPortClient(host, 19205);
        this.navClient = new RbkPortClient(host, 19206);
    }

    //释放资源
    /*
     * Releasing the connection to the robot
     */
    public void dispose() {
        stateClient.dispose();
        controlClient.dispose();
        navClient.dispose();
        configClient.dispose();
        miscClient.dispose();
    }

    /*
     * @param apiNo  rbk number
     * @param requestStr  rbk request json
     * @param timeout  rbk
     * @return RbkResult {"kind": Ok|NoSuchRobot|ConnectFail|WriteError|Disposed|BadApiNo|Timeout|Interrupted  Only ok means the request for rbk was successful
     *                     "ip": robot ip    (string)
     *                     "apiNo": rbk request num (int)
     *                     "reqStr": rbk request body (String)
     *                     "resStr": rbk response body (String)
     *                      "errMsg": request rbk error info
     *                      }
     */
    public RbkResult request(int apiNo, String requestStr, long timeout) {
        log.debug("apiNo {} requestStr {} timeout {}", apiNo, requestStr, timeout);
        if (timeout == 0) {
            timeout = 10 * 1000;
        }
        requestStr = requestStr == null ? "" : requestStr;
        if (apiNo >= 1000 && apiNo <= 1999) {//rbk tcp协议 编号
            return stateClient.request(apiNo, requestStr, timeout);
        } else if (apiNo >= 2000 && apiNo <= 2999) {
            return controlClient.request(apiNo, requestStr, timeout);
        } else if (apiNo >= 3000 && apiNo <= 3999) {
            return navClient.request(apiNo, requestStr, timeout);
        } else if (apiNo >= 4000 && apiNo <= 5999) {
            return configClient.request(apiNo, requestStr, timeout);
        } else if (apiNo >= 6000 && apiNo <= 6998) {
            return miscClient.request(apiNo, requestStr, timeout);
        } else {
            return RbkResult.builder().kind(RbkResultKind.BadApiNo).ip(host).apiNo(apiNo).resStr(requestStr).build();
        }
    }


}

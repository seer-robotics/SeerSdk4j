# s-j-sdk
demo在 test 文件夹下的 com.seer.sdk.demo 的 RbkClientDemo
public class Main {
    public static void main(String[] args) {
    RbkClient rbkClient = new RbkClient("192.168.8.114");
    RbkResult request = rbkClient.request(1000, "", 10000);
    rbkClient.dispose();//释放连接资源
}

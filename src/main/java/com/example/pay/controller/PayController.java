package com.example.pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayDataDataserviceBillDownloadurlQueryModel;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.example.pay.config.AlipayConfig;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @ Author     ：Zgq
 * @ Date       ：Created in 11:30 2019/5/27
 * @ Description：
 * @ Modified By：
 * @Version: $
 */

@RequestMapping("/pay")
@Controller
public class PayController {


    @Autowired
    private AlipayConfig properties;


    /**
     * 跳转到支付页面
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("goPay")
    public void goPay(HttpServletRequest request, HttpServletResponse response) throws Exception{
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(properties.getGatewayUrl(), properties.getApp_id(), properties.getMerchant_private_key(), "json", properties.getCharset(), properties.getAlipay_public_key(), properties.getSign_type());

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(properties.getReturn_url());
        alipayRequest.setNotifyUrl(properties.getNotify_url());

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = new String(request.getParameter("WIDout_trade_no").getBytes("ISO-8859-1"),"UTF-8");
        //付款金额，必填
        String total_amount = new String(request.getParameter("WIDtotal_amount").getBytes("ISO-8859-1"),"UTF-8");
        //订单名称，必填
        String subject = new String(request.getParameter("WIDsubject").getBytes("ISO-8859-1"),"UTF-8");
        //商品描述，可空
        String body = new String(request.getParameter("WIDbody").getBytes("ISO-8859-1"),"UTF-8");

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        //若想给BizContent增加其他可选请求参数，以增加自定义超时时间参数timeout_express来举例说明
        //alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
        //		+ "\"total_amount\":\""+ total_amount +"\","
        //		+ "\"subject\":\""+ subject +"\","
        //		+ "\"body\":\""+ body +"\","
        //		+ "\"timeout_express\":\"10m\","
        //		+ "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        //请求参数可查阅【电脑网站支付的API文档-alipay.trade.page.pay-请求参数】章节


        //请求

        String head="<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head>";

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        String buttom="<body></body></html>";

        //输出
        response.getWriter().println(head+result+buttom);
    }


    /**
     * 交易查询
     * @param request
     * @param trade_no
     * @param out_trade_no
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("query")
    public String query(HttpServletRequest request,String trade_no,String out_trade_no) throws Exception{
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(properties.getGatewayUrl(), properties.getApp_id(), properties.getMerchant_private_key(), "json", properties.getCharset(), properties.getAlipay_public_key(), properties.getSign_type());

        //设置请求参数
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();

        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no1 = new String(out_trade_no.getBytes("ISO-8859-1"),"UTF-8");
        //支付宝交易号
        String trade_no1 = new String(trade_no.getBytes("ISO-8859-1"),"UTF-8");
        //请二选一设置

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no1 +"\","+"\"trade_no\":\""+ trade_no1 +"\"}");

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        //输出
        //out.println(result);

        return result.toString();
    }


    /**
     * 退款
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("return")
    public String tui(HttpServletRequest request,
                      String out_trade_no,String trade_no
                    ,String refund_amount, String refund_reason,
                      String out_request_no) throws Exception{
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(properties.getGatewayUrl(), properties.getApp_id(), properties.getMerchant_private_key(), "json", properties.getCharset(), properties.getAlipay_public_key(), properties.getSign_type());

        //设置请求参数
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();

        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no1 = new String(out_trade_no.getBytes("ISO-8859-1"),"UTF-8");
        //支付宝交易号
        String trade_no1 = new String(trade_no.getBytes("ISO-8859-1"),"UTF-8");
        //请二选一设置

        //需要退款的金额，该金额不能大于订单金额，必填
        String refund_amount1 = new String(refund_amount.getBytes("ISO-8859-1"),"UTF-8");
        //退款的原因说明
        String refund_reason1 = new String(refund_reason.getBytes("ISO-8859-1"),"UTF-8");
        //标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传
        String out_request_no1 = new String(out_request_no.getBytes("ISO-8859-1"),"UTF-8");

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no1 +"\","
                + "\"trade_no\":\""+ trade_no1 +"\","
                + "\"refund_amount\":\""+ refund_amount1 +"\","
                + "\"refund_reason\":\""+ refund_reason1 +"\","
                + "\"out_request_no\":\""+ out_request_no1 +"\"}");

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        //输出
        //out.println(result);

        return result.toString();
    }




    /**
     * 退款交易查询
     * @param request
     * @param trade_no
     * @param out_trade_no
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("Tkquery")
    public String Tkquery(HttpServletRequest request,String trade_no,
                          String out_trade_no,
                          String out_request_no) throws Exception{
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(properties.getGatewayUrl(), properties.getApp_id(), properties.getMerchant_private_key(), "json", properties.getCharset(), properties.getAlipay_public_key(), properties.getSign_type());

        //设置请求参数
        AlipayTradeFastpayRefundQueryRequest alipayRequest = new AlipayTradeFastpayRefundQueryRequest();

        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no1 = new String(out_trade_no.getBytes("ISO-8859-1"),"UTF-8");
        //支付宝交易号
        String trade_no1 = new String(trade_no.getBytes("ISO-8859-1"),"UTF-8");
        //请二选一设置

        //请求退款接口时，传入的退款请求号，如果在退款请求时未传入，则该值为创建交易时的外部交易号，必填
        String out_request_no1 = new String(out_request_no.getBytes("ISO-8859-1"),"UTF-8");

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no1 +"\","
                +"\"trade_no\":\""+ trade_no1 +"\","
                +"\"out_request_no\":\""+ out_request_no1 +"\"}");

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        //输出
        //out.println(result);

        return result.toString();
    }


    /**
     * 关闭交易
     * @param request
     * @param trade_no
     * @param out_trade_no
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("close")
    public String close(HttpServletRequest request,String trade_no,
                          String out_trade_no) throws Exception{
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(properties.getGatewayUrl(), properties.getApp_id(), properties.getMerchant_private_key(), "json", properties.getCharset(), properties.getAlipay_public_key(), properties.getSign_type());

        //设置请求参数
        AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();
        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no1 = new String(out_trade_no.getBytes("ISO-8859-1"),"UTF-8");
        //支付宝交易号
        String trade_no1 = new String(trade_no.getBytes("ISO-8859-1"),"UTF-8");
        //请二选一设置

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\"," +"\"trade_no\":\""+ trade_no +"\"}");

        //请求
        String result = alipayClient.execute(alipayRequest).getBody();

        //输出
        //out.println(result);

        return result.toString();
    }


    /**
     * billDate : 账单时间：日账单格式为yyyy-MM-dd，月账单格式为yyyy-MM。
     * 查询对账单下载地址: https://docs.open.alipay.com/api_15/alipay.data.dataservice.bill.downloadurl.query/
     * @param billDate
     */
    @GetMapping("/bill")
    @ResponseBody
    public void queryBill(String billDate) {

        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(properties.getGatewayUrl(), properties.getApp_id(), properties.getMerchant_private_key(), "json", properties.getCharset(), properties.getAlipay_public_key(), properties.getSign_type());


        // 1. 查询对账单下载地址
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        AlipayDataDataserviceBillDownloadurlQueryModel model = new AlipayDataDataserviceBillDownloadurlQueryModel();
        model.setBillType("trade");
        model.setBillDate(billDate);
        request.setBizModel(model);
        try {
            AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                String billDownloadUrl = response.getBillDownloadUrl();
                System.out.println(billDownloadUrl);

                // 2. 下载对账单
                List<String> orderList = this.downloadBill(billDownloadUrl);
                System.out.println(orderList);
                // 3. 先比较支付宝的交易合计/退款合计笔数/实收金额是否和自己数据库中的数据一致，如果不一致证明有异常，再具体找出那些订单有异常
                // 查找支付宝支付成功而自己支付失败的记录和支付宝支付失败而自己认为支付成功的异常订单记录到数据库

            } else {
                // 失败
                String code = response.getCode();
                String msg = response.getMsg();
                String subCode = response.getSubCode();
                String subMsg = response.getSubMsg();
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载下来的是一个【账号_日期.csv.zip】文件（zip压缩文件名，里面有多个.csv文件）
     * 账号_日期_业务明细 ： 支付宝业务明细查询
     * 账号_日期_业务明细(汇总)：支付宝业务汇总查询
     *
     * 注意：如果数据量比较大，该方法可能需要更长的执行时间
     * @param billDownLoadUrl
     * @return
     * @throws IOException
     */
    private List<String> downloadBill(String billDownLoadUrl) throws IOException {
        String ordersStr = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(60000)
                .setConnectionRequestTimeout(60000)
                .setSocketTimeout(60000)
                .build();
        HttpGet httpRequest = new HttpGet(billDownLoadUrl);
        httpRequest.setConfig(config);
        CloseableHttpResponse response = null;
        byte[] data = null;
        try {
            response = httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            data = EntityUtils.toByteArray(entity);
        } finally {
            response.close();
            httpClient.close();
        }
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(data), Charset.forName("GBK"));
        ZipEntry zipEntry = null;
        try{
            while( (zipEntry = zipInputStream.getNextEntry()) != null){
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try{
                    String name = zipEntry.getName();
                    // 只要明细不要汇总
                    if(name.contains("汇总")){
                        continue;
                    }
                    byte[] byteBuff = new byte[4096];
                    int bytesRead = 0;
                    while ((bytesRead = zipInputStream.read(byteBuff)) != -1) {
                        byteArrayOutputStream.write(byteBuff, 0, bytesRead);
                    }
                    ordersStr = byteArrayOutputStream.toString("GBK");
                }finally {
                    byteArrayOutputStream.close();
                    zipInputStream.closeEntry();
                }
            }
        } finally {
            zipInputStream.close();
        }

        if (ordersStr.equals("")) {
            return null;
        }
        String[] bills = ordersStr.split("\r\n");
        List<String> billList = Arrays.asList(bills);
        billList = billList.parallelStream().map(item -> item.replace("\t", "")).collect(Collectors.toList());

        return billList;
    }

}

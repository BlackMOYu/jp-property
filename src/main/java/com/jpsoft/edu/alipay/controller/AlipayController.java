package com.jpsoft.edu.alipay.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayTradeCreateRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayTradeCreateResponse;
import com.jpsoft.edu.alipay.vo.AlipayOrderVo;
import com.jpsoft.edu.base.entity.BaseSchool;
import com.jpsoft.edu.base.entity.BaseStudentFamily;
import com.jpsoft.edu.base.entity.SchoolPaymentInfo;
import com.jpsoft.edu.base.service.IBaseSchoolService;
import com.jpsoft.edu.base.service.IBaseStudentFamilyService;
import com.jpsoft.edu.base.service.ISchoolPaymentInfoService;
import com.jpsoft.edu.bill.entity.BillDetail;
import com.jpsoft.edu.bill.entity.BillInfo;
import com.jpsoft.edu.bill.entity.PaymentOrder;
import com.jpsoft.edu.bill.service.IBillDetailService;
import com.jpsoft.edu.bill.service.IBillInfoService;
import com.jpsoft.edu.bill.service.IPaymentOrderService;
import com.jpsoft.edu.common.utils.wechat.WechatMessageUtil;
import com.jpsoft.edu.common.vo.ApiResult;

import net.sf.json.JSONObject;

@RestController
@RequestMapping(value="alipay")
public class AlipayController {
	
	private static final String ALIPAY_APPID = "2018070260539153";
	private static final String ALIPAY_URL = "https://openapi.alipay.com/gateway.do";
	private static final String FORMAT = "json";
	private static final String CHARSET = "UTF-8";
	private static final String SIGN_TYPE = "RSA2";
	private static final String ALIPAY_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCsORLetI9grCmAToY6cpCEFeXIN5dFmqYLWaAPwnEIH/EJptA5uzSIAAIydK8+skZfOLlmIscCHdZWgSvFnLLtOEt1CMjtwQPO7Q8lFBkEaEp8QYgsgOCyHV1TBSWgLk3QHgdTP6teLKfZ2DGaCdbQyqKC+Z3NnPJjYww/J3nxb6LsSBchV+uRD9BYrv4d1341t4KE6N1QCQtMHrpRER3hLMWqzfusKpRrp/eu8g5sAG8p9twY6JMnBg+fHa5EgqBS2MlzA0v1p4DR3hc700CZ6aIoBi7AUhVTbxk8iEfUe3F20S3QTuYLni+tNXpwQDJcUgxDpOPJ2RRDCoyYf40HAgMBAAECggEAWxm5tJqYeU+4iEmBUWuGrIgUy4s0droueTSIqa12MxEKZMubu94eFI7EmsIEbUrKVNZho/hjgugbmBit+dNBBqDPsXHbL4D5Lb1SVI/ECAPO2tmjWb82nKFR23eOhqPXv24S++NjF+bRRzfITS5FNp7pxhSad8g8o3wiX0nXhHFlRbgYBmPS5jl/Y4G341iXo09/EoXG3UfEdauPQZhjpKT4MYKZDafVsIxEhEili69ZIok7R+c5TgR8i3fMhiYf+FWepv8v01kvh6knEWhz7ipuad6N0NMgyljMHw1FwhjEXpaLoqN8kJphs3vzsSuUUAtLGuQUymZWQaKtQBZvIQKBgQDjj+PaDmzCwSlBtoPKR+YgIqVkGo761hOe4QiUkit+bE6+ytiPc/gjdhgnZkVE0in3xdPCtjWt6m6q21/x44VHHhsehz38DvAL994yT77WTgYwg3GAebscRVJnq+3qJ6a5tvB+nv7MC4uCk0tfJF9PT26ZuSYf0q0gn42dktUKEQKBgQDBvsm0P96S8ybIjOhp622mRDqL3neAUC/hqNQbpl49gfaD5EIOLOvyIl022AAYA9QzXZd5OoImBuKX+g1MB+IIOyk9CM7W6K5LCpmAjB0efVFjW+UbsJK44ybfmel4dqlSn3mUZm+4fn1G93IsRcbeGwDUVUnE2wGPQNpoeFfNlwKBgDtebOmU3dcpoePdBCEgBO/ZWiD1tNvIQjIvL5fjUqmXBCxOq5UxgkluI/sTeXrtAbn8yLSB6RtIkDrPJQbsfvcA6b5bNlauZv4YWsXxfC5ZCNBbWp9UIvbLNyaI+ncehSbqjW/bd0owsOMOHpnh2WPNq2M7pLCkvDpIph+4ERChAoGAZeuj2DqxL8TR5jaaHP5IlGrYEbRaURnd1mwmCNWgReMUd5WgWiyvgpUpTCydAAUjoFHf2Vo6FR8SHLjiPdj3wzS8IOt9Q/jrl3ZgAguzVdK++fHhuItO5Aw66u3gsApcUKasIrEwnHGOHcMWQMwELdsuuISVZgV/8IhBXSvRpMkCgYBC8tjKcalhwCORlCzOLaT6eA9mPCCuST2sAgR3/4jcDh6aC8VIJu0SIL+rCbkLKtk+jhpW1mRi571E1Ic06engC6719aN/y0Hu45MVTGP9VMVd460ym/4eDXT6jQ4i56b5kMuWh+NixguKHAz+W+h4ltnPGQHpEmm54ZR/yRSbag==";
	private static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo3gaIrAD47mfSBmye1c8GLIRF4H3sl0MhT1OHIO0FqwtDqyxpHkrpOQADQggEzgv1nO82V+ChQxHUeaQ7W987+s1cnwtm7ZVML8DxVtji1va1ahBKL62dBommOoIt4fRIO2upbnF6r3NjB/3MCTZUIE03xIqc+aUeNdRO7eAMpm6sHjBfSimFwXlyTaEsAsnTK4hkOzdvoldHry6A3375kE3msj8Z48Nko7AFedOToPfJNolap+i7qWv3VOhnWLrAZH/Jgq/uHQmx8r29BeBl9AT4EwxDN5mVVKDOYsWW1efmrhKXO3wvzInhB6qQ8L1uBCWKhw1eGHCvlCjTHxBYQIDAQAB";
	private static final String BILLPAY_NOTIRY_URL = "http://api.xiaoxinda.com/alipay/billpay/notify";
	private static final String SCANPAY_NOTIRY_URL = "http://api.xiaoxinda.com/alipay/scanpay/notify";
	//private static final String BILLPAY_NOTIRY_URL = "http://jzzjapi.slgo.net/jp-edu-test/alipay/billpay/notify";
	//private static final String SCANPAY_NOTIRY_URL = "http://jzzjapi.slgo.net/jp-edu-test/alipay/scanpay/notify";
	
	
	@Autowired
	private IBillDetailService billDetailService;
	
	@Autowired
	private IBillInfoService billInfoService;
	
	@Autowired
	private IPaymentOrderService paymentOrderService;
	
	@Autowired
	private ISchoolPaymentInfoService paymentService;
	
	@Autowired
	private IBaseStudentFamilyService studentFamilyService;
	
	@Autowired
	private IBaseSchoolService baseSchoolService;
	
	/**apidoc
	 * @api {get} /alipay/findUserInfo/:authCode 通过支付宝authCode获取用户信息
	 * @apiVersion 1.0.0
	 * @apiName findUserInfo
	 * @apiGroup alipay
	 * 
	 * @apiParam {String} authCode 支付宝授权code
	 * 
	 * @apiUse resultSuccess
	 * @apiSuccess {Object} data 
	 * @apiSuccess {Object} data.userInfo 支付宝用户信息
	 * @apiSuccess {String} data.userInfo.userId 用户ID
	 * @apiUse resultError 
	 */
	@GetMapping(value="findUserInfo/{authCode}")
	public ApiResult findUserInfo(@PathVariable(name="authCode") String authCode){
		
		
		try{
			
			AlipayClient alipayClient = new DefaultAlipayClient(ALIPAY_URL,ALIPAY_APPID,ALIPAY_PRIVATE_KEY,FORMAT,CHARSET,ALIPAY_PUBLIC_KEY,SIGN_TYPE);
			
			AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
			request.setCode(authCode);
			request.setGrantType("authorization_code");
			
			AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(request);
		    
			System.out.println(oauthTokenResponse.getUserId());
		    
			HashMap<String,Object> dataMap = new HashMap<String,Object>();
			
			dataMap.put("userId", oauthTokenResponse.getUserId());
			
			HashMap<String,Object> resultMap = new HashMap<String,Object>();
			
			resultMap.put("userInfo", dataMap);
			
			return new ApiResult(200, "获取支付宝信息成功", resultMap);
		} catch (AlipayApiException e) {
		    //处理异常
		    e.printStackTrace();
		    return new ApiResult(400, "获取支付宝授权失败","");
		} catch (Exception e) {
		    //处理异常
		    e.printStackTrace();
		    return new ApiResult(400, "获取支付宝授权失败","");
		}
	}
	
	/**apidoc
	 * @api {post} /alipay/createScanpayTrade 创建扫码交易单号
	 * @apiVersion 1.0.0
	 * @apiName createScanpayTrade
	 * @apiGroup alipay
	 * 
	 * @apiParam {String} orderId 扫码订单Id
	 * @apiParam {String} buyerId 用户Id
	 * 
	 * @apiUse resultSuccess
	 * @apiSuccess {Object} data 
	 * @apiSuccess {Object} data.tradeOrder 支付宝交易信息
	 * @apiSuccess {String} data.tradeOrder.tradeNo 交易单号
	 * @apiUse resultError
	 */
	@PostMapping(value="createScanpayTrade")
	public ApiResult createScanpayTrade(@RequestBody AlipayOrderVo orderVo){
		
		int code = 200;
		String message = "交易单号生成成功";
		Object data = "";
		
		try{
			
			if(StringUtils.isBlank(orderVo.getBuyerId())){
				code = 400;
				message = "用户不存在";
			}else if(StringUtils.isBlank(orderVo.getOrderId())){
				code = 400;
				message = "订单不存在";
			}else{
			
				PaymentOrder paymentOrder = paymentOrderService.get(orderVo.getOrderId());
				
				if(paymentOrder == null){
					code = 400;
					message = "订单不存在";
				}else if(paymentOrder.getPayStatus() == 20){
					code = 400;
					message = "订单已支付";
				}else{
				
					AlipayClient alipayClient = new DefaultAlipayClient(ALIPAY_URL,ALIPAY_APPID,ALIPAY_PRIVATE_KEY,FORMAT,CHARSET,ALIPAY_PUBLIC_KEY,SIGN_TYPE);
					
					AlipayTradeCreateRequest atcRequest = new AlipayTradeCreateRequest();
					
					atcRequest.setNotifyUrl(SCANPAY_NOTIRY_URL);
					
					SchoolPaymentInfo paymentInfo = paymentService.get(paymentOrder.getPaymentId());
					
					String appAuthToken = "";
					String body = "";
					String mchId = "";
					
					if(paymentInfo != null) {
						body = paymentInfo.getName();
						
						if(StringUtils.isNotBlank(paymentInfo.getAlipayParams())){
							JSONObject params = JSONObject.fromObject(paymentInfo.getAlipayParams());
							
							appAuthToken = params.getString("appAuthToken");
							mchId = params.getString("mchId");
						}
						
					}
					
					atcRequest.putOtherTextParam("app_auth_token", appAuthToken);
					
					JSONObject bizContent = new JSONObject();
					 
					bizContent.put("out_trade_no", paymentOrder.getOrderNo() + "JP" + new Date().getTime());
					bizContent.put("total_amount", paymentOrder.getTotalAmount());
					bizContent.put("subject", body);
					bizContent.put("buyer_id", orderVo.getBuyerId());
					
					JSONObject  extendParams = new JSONObject();
					
					extendParams.put("sys_service_provider_id", mchId);
					
					bizContent.put("extend_params", extendParams);
					
					atcRequest.setBizContent(bizContent.toString());
					
					AlipayTradeCreateResponse atcResponse = alipayClient.execute(atcRequest);
					if(atcResponse.isSuccess()){
						
						HashMap<String,Object> dataMap = new HashMap<String,Object>();
						
						dataMap.put("tradeNo", atcResponse.getTradeNo());
						
						HashMap<String,Object> resultMap = new HashMap<String,Object>();
						
						resultMap.put("tradeOrder", dataMap);
						
						data = resultMap;
						
					} else {
						code = 400;
						message = atcResponse.getMsg();
						
					}
				}
			}
		    
		} catch (AlipayApiException e) {
		    //处理异常
		    e.printStackTrace();
		    code = 400;
		    message = "支付宝支付异常";
		} catch (Exception e) {
		    //处理异常
		    e.printStackTrace();
		    
		    code = 400;
		    message = "系统异常";
		}
		
		return new ApiResult(code, message, data);
	}
	
	/**apidoc
	 * @api {post} /alipay/createBillpayTrade 创建账单付交易单号
	 * @apiVersion 1.0.0
	 * @apiName createBillpayTrade
	 * @apiGroup alipay
	 * 
	 * @apiParam {String} billDetailId 账单详情Id
	 * @apiParam {String} buyerId 用户Id
	 * @apiParam {String} openId 用户openId
	 * 
	 * @apiUse resultSuccess
	 * @apiSuccess {Object} data 
	 * @apiSuccess {Object} data.tradeOrder 支付宝交易信息
	 * @apiSuccess {String} data.tradeOrder.tradeNo 交易单号
	 * @apiUse resultError
	 */
	@PostMapping(value="createBillpayTrade")
	public ApiResult createBillpayTrade(@RequestBody AlipayOrderVo orderVo){
		
		int code = 200;
		String message = "交易单号生成成功";
		Object data = "";
		
		try{
			
			if(StringUtils.isBlank(orderVo.getBuyerId())){
				code = 400;
				message = "用户不存在";
			}else if(StringUtils.isBlank(orderVo.getBillDetailId())){
				code = 400;
				message = "账单不存在";
			}else if(StringUtils.isBlank(orderVo.getOpenId())){
				code = 400;
				message = "用户无效";
			}else{
			
				BillDetail billDetail = billDetailService.get(orderVo.getBillDetailId());
				
				if(billDetail == null){
					code = 400;
					message = "账单不存在";
				}else if(billDetail.getPayStatus() == 20){
					code = 400;
					message = "账单已支付";
				}else{
					
					BaseStudentFamily family = studentFamilyService.findByOpenIdAndStudentId(orderVo.getOpenId(), billDetail.getStudentId());
					
					if(family == null){
						code = 400;
						message = "用户无效";
					}else{
						
						billDetail.setPayerFamilyId(family.getId());
						billDetail.setPayName("alipay");
						billDetail.setPayResult("待支付");
						
						billDetailService.save(billDetail);
				
						AlipayClient alipayClient = new DefaultAlipayClient(ALIPAY_URL,ALIPAY_APPID,ALIPAY_PRIVATE_KEY,FORMAT,CHARSET,ALIPAY_PUBLIC_KEY,SIGN_TYPE);
						
						AlipayTradeCreateRequest atcRequest = new AlipayTradeCreateRequest();
						
						atcRequest.setNotifyUrl(BILLPAY_NOTIRY_URL);
						
						BillInfo billInfo = billInfoService.get(billDetail.getBillId());
						
						
						
						SchoolPaymentInfo paymentInfo = paymentService.get(billInfo.getPaymentId());
						
						String appAuthToken = "";
						String body = "";
						String mchId = "";
						
						if(paymentInfo != null) {
							String paymentId = paymentInfo.getId();
							body = paymentInfo.getName();
							
							if(billInfo.getPaymentId().equals(paymentId) && StringUtils.isNotBlank(paymentInfo.getAlipayParams())){
								JSONObject params = JSONObject.fromObject(paymentInfo.getAlipayParams());
								
								appAuthToken = params.getString("appAuthToken");
								mchId = params.getString("mchId");
							}
							
						}
						
						atcRequest.putOtherTextParam("app_auth_token", appAuthToken);
						
						JSONObject bizContent = new JSONObject();
						
						bizContent.put("out_trade_no", billDetail.getOrderNo() + "JP" + new Date().getTime());
						bizContent.put("total_amount", billDetail.getTotalAmount());
						bizContent.put("subject", body);
						bizContent.put("buyer_id", orderVo.getBuyerId());
						
						JSONObject  extendParams = new JSONObject();
						
						extendParams.put("sys_service_provider_id", mchId);
						
						bizContent.put("extend_params", extendParams);
						
						atcRequest.setBizContent(bizContent.toString());
						
						AlipayTradeCreateResponse atcResponse = alipayClient.execute(atcRequest);
						if(atcResponse.isSuccess()){
							
							HashMap<String,Object> dataMap = new HashMap<String,Object>();
							
							dataMap.put("tradeNo", atcResponse.getTradeNo());
							
							HashMap<String,Object> resultMap = new HashMap<String,Object>();
							
							resultMap.put("tradeOrder", dataMap);
							
							data = resultMap;
							
						} else {
							code = 400;
							message = atcResponse.getMsg();
							
						}
					}
				}
			}
		    
		} catch (AlipayApiException e) {
		    //处理异常
		    e.printStackTrace();
		    code = 400;
		    message = "支付宝支付异常";
		} catch (Exception e) {
		    //处理异常
		    e.printStackTrace();
		    
		    code = 400;
		    message = "系统异常";
		}
		
		return new ApiResult(code, message, data);
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value="/scanpay/notify")
	public String scanpayNotify(HttpServletRequest request){
		
		try{
			Map<String,String> params = new HashMap<String,String>();
			String paramsFormat = "";
			Map requestParams = request.getParameterMap();
			for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]
							: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
				params.put(name, valueStr);
				
				paramsFormat += "name=" + name + ";value=" + valueStr + ";";
			}
			
			System.out.println("paramsFormat>>>>" + paramsFormat);
	
			//商户订单号
			String outTradeNo = params.get("out_trade_no");
			
			//支付宝交易号
			String tradeNo = params.get("trade_no");
	
			//交易状态
			String trade_status = params.get("trade_status");
			//计算得出通知验证结果
			//boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
			boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, CHARSET, SIGN_TYPE);
			System.out.println("verify_result>>>" + verify_result);
			if(verify_result){//验证成功
				if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
					//支付成功
					if(outTradeNo.indexOf("JP") > 0){
						outTradeNo = outTradeNo.substring(0, outTradeNo.indexOf("JP"));
					}
					PaymentOrder paymentOrder = paymentOrderService.findByOrderNo(outTradeNo);
					
					paymentOrder.setPayName("alipay");
					paymentOrder.setPayResult("支付成功");
					paymentOrder.setPayStatus(20);
					paymentOrder.setPayTime(new Date());
					paymentOrder.setTradeNo(tradeNo);
					
					paymentOrderService.save(paymentOrder);
					
					try{
						WechatMessageUtil.sendScanPayedMessage(paymentOrder.getRemark(), paymentOrder.getSchool().getName(), paymentOrder.getStudentName(), 
								paymentOrder.getTotalAmount(), paymentOrder.getOpenId(), paymentOrder.getId(), new Date());
					}catch(Exception ex){
						
					}
					
					return "success";
				} else{
					return "fail";
				}
			}else{//验证失败
				return "fail";
			}
		}catch(Exception ex){
			return "fail";
		}
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value="/billpay/notify")
	public String billpayNotify(HttpServletRequest request){
		
		try{
			Map<String,String> params = new HashMap<String,String>();
			String paramsFormat = "";
			Map requestParams = request.getParameterMap();
			for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]
							: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
				params.put(name, valueStr);
				
				paramsFormat += "name=" + name + ";value=" + valueStr + ";";
			}
			
			System.out.println("paramsFormat>>>>" + paramsFormat);
			//商户订单号
			String outTradeNo = params.get("out_trade_no");
			
			//支付宝交易号
			String tradeNo = params.get("trade_no");
	
			//交易状态
			String trade_status = params.get("trade_status");
			//计算得出通知验证结果
			//boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
			boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, CHARSET, SIGN_TYPE);
			System.out.println("verify_result>>>" + verify_result);
			if(verify_result){//验证成功
				if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
					//支付成功
					if(outTradeNo.indexOf("JP") > 0){
						outTradeNo = outTradeNo.substring(0, outTradeNo.indexOf("JP"));
					}
					BillDetail billDetail = billDetailService.findByOrderNo(outTradeNo);
					billDetail.setPayName("alipay");
					billDetail.setPayResult("支付成功");
					billDetail.setPayStatus(20);
					billDetail.setPayTime(new Date());
					billDetail.setTradeNo(tradeNo);
					billDetail.setPayOrderNo(params.get("out_trade_no"));
					
					
					if(StringUtils.isNotBlank(billDetail.getPayerFamilyId())){
					
						try{
							
							BaseSchool school = baseSchoolService.get(billDetail.getSchoolId());
							
							BaseStudentFamily studentFamily = studentFamilyService.get(billDetail.getPayerFamilyId());
							
							WechatMessageUtil.sendBillPayedMessage(billDetail.getBillName(), school.getName(),billDetail.getStudentName(), 
									billDetail.getTotalAmount(), studentFamily.getOpenId(), billDetail.getId(), new Date());
						}catch(Exception ex){
							
						}
					}
					
					billDetailService.save(billDetail);
					
					return "success";
				} else{
					return "fail";
				}
			}else{//验证失败
				return "fail";
			}
		}catch(Exception ex){
			return "fail";
		}
	}
}

package com.jpsoft.edu.bill.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jpsoft.edu.base.entity.TeacherAllocClass;
import com.jpsoft.edu.base.service.ITeacherAllocClassService;
import com.jpsoft.edu.bill.entity.BillDetail;
import com.jpsoft.edu.bill.entity.BillInfo;
import com.jpsoft.edu.bill.service.IBillDetailService;
import com.jpsoft.edu.bill.service.IBillInfoService;
import com.jpsoft.edu.bill.vo.BillInfoSaveVo;
import com.jpsoft.edu.bill.vo.BillInfoVo;
import com.jpsoft.edu.common.service.impl.RedisServiceImpl;
import com.jpsoft.edu.common.vo.ApiResult;
import com.jpsoft.edu.common.vo.LoginReturnVo;
import com.jpsoft.edu.common.vo.PageVo;

import net.sf.json.JSONObject;


@RestController
@RequestMapping(value="/bill")
public class BillController {


	@Autowired
	private RedisServiceImpl redisService;
	
	@Autowired
	private IBillInfoService billInfoService;
	
	@Autowired
	private IBillDetailService billDetailService;
	
	@Autowired
	private ITeacherAllocClassService teacherAllocClassService;
	
	/**apidoc
	 * @api {post} /bill/save 创建账单
	 * @apiVersion 1.0.0
	 * @apiName billSave
	 * @apiGroup bill
	 * 
	 * @apiHeader {String} x-token 用户访问接口令牌
	 * @apiHeaderExample {json} 用户访问接口令牌示例:
	 * {"x-token":"9a4e4ae20de14bc9b0e0a85614768487"}
	 * 
	 * @apiParam {String} name 账单名称
	 * @apiParam {String} paymentId 支付方式Id
	 * @apiParam {String} receiverName 收款方
	 * @apiParam {String} remark 备注
	 * @apiParamExample {json} 请求参数:
	 * {
	 *  	"name":"2018年学杂费",
	 *  	"receiverName":"航空路小学",
	 *  	"remark":"2018年学杂费,包括书本费,餐费等"
	 * }
	 * @apiUse resultSuccess
	 * @apiSuccess {Object} data 
	 * @apiSuccess {Object} data.billInfo 账单信息
	 * @apiSuccess {String} data.billInfo.id 账单id
	 * @apiSuccess {String} data.billInfo.name 账单名称
	 * @apiSuccess {String} data.billInfo.receiverName 收款人
	 * @apiSuccess {String} data.billInfo.schoolId 学校id
	 * @apiSuccess {String} data.billInfo.feeName 费用名目
	 * @apiSuccess {String} data.billInfo.createBy 创建人
	 * @apiSuccess {String} data.billInfo.createTime 创建时间
	 * @apiSuccess {String} data.billInfo.createTimeF 创建时间格式化
	 * @apiSuccess {String} data.billInfo.updateBy 修改人
	 * @apiSuccess {String} data.billInfo.updateTime 修改时间
	 * @apiSuccess {String} data.billInfo.updateTimeF 修改时间格式化
	 * @apiSuccessExample {json} Success-Response:
	 * {
	 *	    "code": 200,
	 *	    "message": "账单创建成功",
	 *	    "data": {
	 *	        "billInfo": {
	 *	            "id": "bb28db68-1bae-4abd-bb63-8a77ca2580bb",
	 *	            "name": "2018年学杂费",
	 *		            "receiverName": "航空路小学",
	 *		            "remark": "2018年学杂费,包括书本费,餐费等",
	 *		            "schoolId": "1",
	 *		            "feeName": null,
	 *		            "createBy": "hangkonglu",
	 *		            "createTime": 1525919389405,
	 *		            "updateBy": "hangkonglu",
	 *		            "updateTime": 1525919389405,
	 *		            "createTimeF": "2018-05-10 10:29:49",
	 *		            "updateTimeF": "2018-05-10 10:29:49"
	 *		    }
	 *		}
	 * }
	 * @apiUse resultError
	 */
	@PostMapping(value="/save")
	public ApiResult save(@RequestHeader(name="x-token",defaultValue="") String xToken,
			@RequestBody BillInfoSaveVo billInfoSaveVo){
		
		int code = 200;
		String message = "账单创建成功";
		Object data = "";
		
		try{
			if(StringUtils.isBlank(billInfoSaveVo.getName())){
				code = 400;
				message = "账单名称不能为空";
			}else if(StringUtils.isBlank(billInfoSaveVo.getReceiverName())){
				code = 400;
				message = "收款人不能为空";
			}else if(StringUtils.isBlank(billInfoSaveVo.getRemark())){
				code = 400;
				message = "备注不能为空";
			}else if(StringUtils.isBlank(billInfoSaveVo.getPaymentId())){
				code = 400;
				message = "支付方式不能为空";
			}else{
				LoginReturnVo returnVo = (LoginReturnVo)redisService.get(xToken);
				
				BillInfo billInfo = new BillInfo();
				
				billInfo.setCreateTime(new Date());
				billInfo.setCreateBy(returnVo.getUser().getUserName());
				billInfo.setSchoolId(returnVo.getUser().getSchoolId());
				billInfo.setUpdateBy(returnVo.getUser().getUserName());
				billInfo.setUpdateTime(new Date());
				billInfo.setIsSended(false);
				billInfo.setName(billInfoSaveVo.getName());
				billInfo.setReceiverName(billInfoSaveVo.getReceiverName());
				billInfo.setRemark(billInfoSaveVo.getRemark());
				billInfo.setPaymentId(billInfoSaveVo.getPaymentId());
				billInfo.setStatus("opened");
				
				billInfo = billInfoService.save(billInfo);
				
				HashMap<String,Object> dataMap = new HashMap<String,Object>();
				
				dataMap.put("billInfo", billInfo);
				
				data = dataMap;
			}
		}catch(Exception e){
			code = 500;
			message = "保存失败";
		}
		
		return new ApiResult(code, message, data);
	}
	
	/**apidoc
	 * @api {post} /bill/update 修改账单
	 * @apiVersion 1.0.0
	 * @apiName billUpdate
	 * @apiGroup bill
	 * 
	 * @apiHeader {String} x-token 用户访问接口令牌
	 * @apiHeaderExample {json} 用户访问接口令牌示例:
	 * {"x-token":"9a4e4ae20de14bc9b0e0a85614768487"}
	 * 
	 * @apiParam {String} id 账单id
	 * @apiParam {String} paymentId 支付方式Id
	 * @apiParam {String} name 账单名称
	 * @apiParam {String} receiverName 收款方
	 * @apiParam {String} remark 备注
	 * @apiParamExample {json} 请求参数:
	 * {
	 * 		"id":"bb28db68-1bae-4abd-bb63-8a77ca2580bb"
	 *  	"name":"2018年学杂费",
	 *  	"receiverName":"航空路小学",
	 *  	"remark":"2018年学杂费,包括书本费,餐费等"
	 * }
	 * @apiUse resultSuccess
	 * @apiSuccess {Object} data 
	 * @apiSuccess {Object} data.billInfo 账单信息
	 * @apiSuccess {String} data.billInfo.id 账单id
	 * @apiSuccess {String} data.billInfo.name 账单名称
	 * @apiSuccess {String} data.billInfo.receiverName 收款人
	 * @apiSuccess {String} data.billInfo.schoolId 学校id
	 * @apiSuccess {String} data.billInfo.feeName 费用名目
	 * @apiSuccess {String} data.billInfo.createBy 创建人
	 * @apiSuccess {String} data.billInfo.createTime 创建时间
	 * @apiSuccess {String} data.billInfo.createTimeF 创建时间格式化
	 * @apiSuccess {String} data.billInfo.updateBy 修改人
	 * @apiSuccess {String} data.billInfo.updateTime 修改时间
	 * @apiSuccess {String} data.billInfo.updateTimeF 修改时间格式化
	 * @apiSuccessExample {json} Success-Response:
	 * {
	 *	    "code": 200,
	 *	    "message": "账单创建成功",
	 *	    "data": {
	 *	        "billInfo": {
	 *	            "id": "bb28db68-1bae-4abd-bb63-8a77ca2580bb",
	 *	            "name": "2018年学杂费",
	 *		        "receiverName": "航空路小学",
 	 *	            "remark": "2018年学杂费,包括书本费,餐费等",
 	 *	            "schoolId": "1",
 	 *	            "feeName": null,
 	 *	            "createBy": "hangkonglu",
 	 *	            "createTime": 1525919389405,
 	 *	            "updateBy": "hangkonglu",
 	 *	            "updateTime": 1525919389405,
 	 *	            "createTimeF": "2018-05-10 10:29:49",
 	 *	            "updateTimeF": "2018-05-10 10:29:49"
	 *		    }
	 *		}
	 * }
	 * @apiUse resultError
	 */
	@PostMapping(value="/update")
	public ApiResult update(@RequestHeader(name="x-token",defaultValue="") String xToken,
			@RequestBody BillInfoSaveVo billInfo){
		
		int code = 200;
		String message = "账单修改成功";
		Object data = "";
		
		try{
			if(StringUtils.isBlank(billInfo.getId())){
				code = 400;
				message = "账单Id不能为空";
			}else if(StringUtils.isBlank(billInfo.getName())){
				code = 400;
				message = "账单名称不能为空";
			}else if(StringUtils.isBlank(billInfo.getReceiverName())){
				code = 400;
				message = "收款人不能为空";
			}else if(StringUtils.isBlank(billInfo.getRemark())){
				code = 400;
				message = "备注不能为空";
			}else if(StringUtils.isBlank(billInfo.getPaymentId())){
				code = 400;
				message = "支付方式不能为空";
			}else{
				
				BillInfo target = billInfoService.get(billInfo.getId());
				
				if(target == null){
					code = 400;
					message = "账单不存在";
				}else{
					LoginReturnVo returnVo = (LoginReturnVo)redisService.get(xToken);
					
					target.setName(billInfo.getName());
					target.setReceiverName(billInfo.getReceiverName());
					target.setRemark(billInfo.getRemark());
					target.setUpdateBy(returnVo.getUser().getUserName());
					target.setUpdateTime(new Date());
					target.setPaymentId(billInfo.getPaymentId());
					billInfoService.save(target);
					
					HashMap<String,Object> dataMap = new HashMap<String,Object>();
					
					dataMap.put("billInfo", billInfo);
					
					data = dataMap;
				}
			}
		}catch(Exception e){
			code = 500;
			message = "保存失败";
		}
		
		return new ApiResult(code, message, data);
	}
	
	/**apidoc
	 * @api {get} /bill/detail/:id 查看账单基本信息
	 * @apiVersion 1.0.0
	 * @apiName billDetail
	 * @apiGroup bill
	 * 
	 * @apiHeader {String} x-token 用户访问接口令牌
	 * @apiHeaderExample {json} 用户访问接口令牌示例:
	 * {"x-token":"9a4e4ae20de14bc9b0e0a85614768487"}
	 * 
	 * @apiParam {String} id 账单id
	 * 
	 * @apiUse resultSuccess
	 * @apiSuccess {Object} data 
	 * @apiSuccess {Object} data.billInfo 账单信息
	 * @apiSuccess {String} data.billInfo.id 账单id
	 * @apiSuccess {String} data.billInfo.name 账单名称
	 * @apiSuccess {String} data.billInfo.receiverName 收款人
	 * @apiSuccess {String} data.billInfo.schoolId 学校id
	 * @apiSuccess {String} data.billInfo.feeName 费用名目
	 * @apiSuccess {String} data.billInfo.createBy 创建人
	 * @apiSuccess {String} data.billInfo.createTime 创建时间
	 * @apiSuccess {String} data.billInfo.createTimeF 创建时间格式化
	 * @apiSuccess {String} data.billInfo.updateBy 修改人
	 * @apiSuccess {String} data.billInfo.updateTime 修改时间
	 * @apiSuccess {String} data.billInfo.updateTimeF 修改时间格式化
	 * @apiSuccess {String} data.billInfo.totalStudents 应通知的学生数量
	 * @apiSuccess {String} data.billInfo.actualStudents 实际通知到的学生数量
	 * @apiSuccess {String} data.billInfo.unannouncedStudents 未通知到的学生数量
	 * @apiSuccess {String} data.billInfo.actualAmount 实收总金额
	 * @apiSuccess {String} data.billInfo.notPayAmount 未付总金额
	 * @apiSuccess {String} data.billInfo.totalAmount 应收总金额
	 * @apiSuccess {String} data.totalNum 总学生数
	 * @apiSuccess {String} data.noBindNum 未绑定家长数
	 * @apiSuccessExample {json} Success-Response:
	 * {
	 *	    "code": 200,
	 *	    "message": "账单查询成功",
	 *	    "data": {
	 *	        "billInfo": {
	 *	            "id": "bb28db68-1bae-4abd-bb63-8a77ca2580bb",
	 *	            "name": "2018年学杂费",
	 *		            "receiverName": "航空路小学",
	 *		            "remark": "2018年学杂费,包括书本费,餐费等",
	 *		            "schoolId": "1",
	 *		            "feeName": null,
	 *		            "createBy": "hangkonglu",
	 *		            "createTime": 1525919389405,
	 *		            "updateBy": "hangkonglu",
	 *		            "updateTime": 1525919389405,
	 *		            "createTimeF": "2018-05-10 10:29:49",
	 *		            "updateTimeF": "2018-05-10 10:29:49"
	 *		    },
	 *			"totalNum":1,
	 *			"noBindNum":1
	 *		}
	 * }
	 * @apiUse resultError
	 */
	@GetMapping("/detail/{id}")
	public ApiResult detail(@RequestHeader(name="x-token",defaultValue="") String xToken,
			@PathVariable(name="id") String billId){
		int code = 200;
		String message = "账单查询成功";
		Object data = "";
		
		try{
			
			BillInfo billInfo = billInfoService.get(billId);
			
			if(billInfo == null){
				code = 400;
				message = "账单不存在";
			}else{
				HashMap<String,Object> dataMap = new HashMap<String,Object>();
				
				dataMap.put("billInfo", billInfo);
				
				List<BillDetail> detailList = billDetailService.findByBillId(billId);
				
				int totalNum = detailList.size();
				int noBindNum = billDetailService.getNoBindNum(billId);
				
				dataMap.put("totalNum", totalNum);
				dataMap.put("noBindNum", noBindNum);
				
				data = dataMap;
			}
		}catch(Exception e){
			e.printStackTrace();
			code = 500;
			message = "账单查询失败";
		}
		
		return new ApiResult(code, message, data);
	}
	
	/**apidoc
	 * @api {get} /bill/listAll 查看账单所有信息
	 * @apiVersion 1.0.0
	 * @apiName billListAll
	 * @apiGroup bill
	 * 
	 * @apiHeader {String} x-token 用户访问接口令牌
	 * @apiHeaderExample {json} 用户访问接口令牌示例:
	 * {"x-token":"9a4e4ae20de14bc9b0e0a85614768487"}
	 * 
	 * 
	 * @apiUse resultSuccess
	 * @apiSuccess {Object} data
	 * @apiSuccess {Object[]} data.list 数据列表
	 * @apiSuccess {String} data.list.billId 账单id
	 * @apiSuccess {String} data.list.billName 账单名称
	 * @apiSuccessExample {json} Success-Response:
	 * {
	 *	    "code": 200,
	 *	    "message": "账单列表查询成功",
	 *	    "data": {
	 *	        "list": [
	 *	            {
	 *	                "billId": "d338e10a-23e2-4834-bf2c-9334713d41c7",
	 *	                "billName": "asd"
	 *	            }
	 *	        ]
	 *	    }
	 *	}
	 * @apiUse resultError
	 */
	@SuppressWarnings("rawtypes")
	@GetMapping(value="/listAll",produces={})
	public ApiResult listAll(@RequestHeader(name="x-token",defaultValue="") String xToken){
		int code = 200;
		String message = "账单列表查询成功";
		Object data = "";
		
		try{
			Map<String, Object> queryMap = new HashMap<>();
			
			JSONObject json = JSONObject.fromObject(redisService.get(xToken));
			LoginReturnVo returnVo = (LoginReturnVo)JSONObject.toBean(json, LoginReturnVo.class);
			
			queryMap.put("EQ_schoolId", returnVo.getUser().getSchoolId());
			
			List<BillInfo> billInfoList = billInfoService.findBySchoolId(returnVo.getUser().getSchoolId());
			
			List<Map> dataList = new ArrayList<Map>();
			//统计订单详情中的费用
			if (billInfoList != null) {
				for (BillInfo billInfo : billInfoList) {
					
					HashMap<String,Object> billMap = new HashMap<String,Object>();
					
					billMap.put("billId", billInfo.getId());
					billMap.put("billName", billInfo.getName());
					
					dataList.add(billMap);
				}
			}
		
			HashMap<String,Object> dataMap = new HashMap<String,Object>();
			dataMap.put("list", dataList);
			data = dataMap;
			
		}catch(Exception e){
			e.printStackTrace();
			code = 500;
			message = "账单列表查询失败";
		}
		
		return new ApiResult(code, message, data);
	}
	
	
	/**apidoc
	 * @api {post} /bill/list 查看账单列表信息
	 * @apiVersion 1.0.0
	 * @apiName billList
	 * @apiGroup bill
	 * 
	 * @apiHeader {String} x-token 用户访问接口令牌
	 * @apiHeaderExample {json} 用户访问接口令牌示例:
	 * {"x-token":"9a4e4ae20de14bc9b0e0a85614768487"}
	 * 
	 * @apiParam {Integer} pageIndex 当前页码
	 * @apiParam {Integer} pageSize 每页数量
	 * @apiParamExample {json} 请求参数:
	 * {
	 *  	"pageIndex":1,
	 *  	"pageSize":1
	 * }
	 * 
	 * @apiUse resultSuccess
	 * @apiSuccess {Object} data 
	 * @apiSuccess {Object} data.page 分页信息
	 * @apiSuccess {Integer} data.page.pageIndex 当前页码
	 * @apiSuccess {Integer} data.page.pageSize 每页数量
	 * @apiSuccess {Integer} data.page.totalPage 总页数
	 * @apiSuccess {Integer} data.page.totalNum 总条数
	 * @apiSuccess {Object[]} data.list 数据列表
	 * @apiSuccess {String} data.list.billId 账单id
	 * @apiSuccess {String} data.list.billName 账单名称
	 * @apiSuccess {Boolean} data.list.isSended 是否发送
	 * @apiSuccess {Boolean} data.list.status 状态：opened开启 closed关闭
	 * @apiSuccess {String} data.list.sendTime 发送时间
	 * @apiSuccess {String} data.list.feeName 费用名目
	 * @apiSuccess {String} data.list.remark 费用备注
	 * @apiSuccess {Integer} data.list.sendTotalNum 应发送人数
	 * @apiSuccess {Integer} data.list.sendSuccessNum 实发送人数
	 * @apiSuccess {Integer} data.list.sendErrorNum 未通知人数
	 * @apiSuccess {Integer} data.list.totalAmount 应收总金额
	 * @apiSuccess {Integer} data.list.actAmount 实收总金额
	 * @apiSuccess {Integer} data.list.notPayAmount 未收总金额
	 * @apiSuccess {String} data.list.receiverName 收款人
	 * @apiSuccessExample {json} Success-Response:
	 * {
	 *	    "code": 200,
	 *	    "message": "账单列表查询成功",
	 *	    "data": {
	 *	        "page": {
	 *	            "pageIndex": 1,
	 *	            "pageSize": 1,
	 *	            "totalPage": 25,
	 *	            "totalNum": 25
	 *	        },
	 *	        "list": [
	 *	            {
	 *	                "billId": "d338e10a-23e2-4834-bf2c-9334713d41c7",
	 *	                "billName": "asd",
	 *	                "isSended": false,
	 *	                "sendTime": "",
	 *	                "feeName": null,
	 *	                "remark": "asd",
	 *	                "sendTotalNum": 0,
	 *	                "sendSuccessNum": 0,
	 *	                "sendErrorNum": 0,
	 *	                "totalAmount": 0,
	 *	                "actAmount": 0,
	 *	                "notPayAmount": 0,
	 *	                "receiverName": "asd"
	 *	            }
	 *	        ]
	 *	    }
	 *	}
	 * @apiUse resultError
	 */
	@PostMapping(value="/list",produces={})
	public ApiResult list(@RequestHeader(name="x-token",defaultValue="") String xToken,
			@RequestBody PageVo pageVo){
		int code = 200;
		String message = "账单列表查询成功";
		Object data = "";
		
		try{
			Map<String, Object> queryMap = new HashMap<>();
			
			JSONObject json = JSONObject.fromObject(redisService.get(xToken));
			LoginReturnVo returnVo = (LoginReturnVo)JSONObject.toBean(json, LoginReturnVo.class);
			
			queryMap.put("EQ_schoolId", returnVo.getUser().getSchoolId());
			
			if(Arrays.asList(returnVo.getRoleNames()).contains("headmaster") && !Arrays.asList(returnVo.getRoleNames()).contains("finance")){
				HashMap<String,Object> billDetailMap = new HashMap<String,Object>();
				
				
				String classIds = "";
				
				List<TeacherAllocClass> allocList = teacherAllocClassService.findByTeacherId(returnVo.getUser().getUserId());
				
				for (int i = 0; i < allocList.size(); i++) {
					if(i < allocList.size() -1){
						classIds += "'" + allocList.get(i).getSchoolClassId() + "',";
					}else{
						classIds += "'" + allocList.get(i).getSchoolClassId() + "'";
					}
				}
				
				if(StringUtils.isNotBlank(classIds)){
					billDetailMap.put("IN_classId", classIds);
				}else{
					billDetailMap.put("EQ_classId", "-1");
				}
				
				
				List<BillDetail> detailList = billDetailService.listSearch(billDetailMap,"updateTime desc");
				
				List<String> billIdList = new ArrayList<String>();
				
				for (int i = 0; i < detailList.size(); i++) {
					
					if(!billIdList.contains(detailList.get(i).getBillId())){
						billIdList.add(detailList.get(i).getBillId());
					}
				}
				
				if(billIdList.size() > 0){
					queryMap.put("IN_id", StringUtils.join(billIdList, ","));
				}else{
					queryMap.put("EQ_id", "-1");
				}
			}
			
			Page<BillInfo> page = billInfoService.pageSearch(queryMap, pageVo.getPageIndex(), pageVo.getPageSize(), "updateTime desc,createTime desc");
			
			List<BillInfo> billInfoList = page.getContent();
			
			List<BillInfoVo> dataList = new ArrayList<BillInfoVo>();
			//统计订单详情中的费用
			if (billInfoList != null) {
				for (BillInfo billInfo : billInfoList) {
					
					List<BillDetail> detailList = billDetailService.findByBillId(billInfo.getId());
					
					
					Integer sendTotalNum = detailList.size();
					Integer sendSuccessNum = 0;
					Integer sendErrorNum = 0;
					BigDecimal totalAmount = BigDecimal.ZERO;
					BigDecimal actAmount = BigDecimal.ZERO;
					BigDecimal notPayAmount = BigDecimal.ZERO;
					
					for (BillDetail billDetail : detailList) {
						if(billDetail.getSendStatus() == 20){
							sendSuccessNum ++;
						}else{
							sendErrorNum ++;
						}
						
						totalAmount = totalAmount.add(billDetail.getTotalAmount());
						
						if(billDetail.getPayStatus() == 20){
							actAmount = actAmount.add(billDetail.getTotalAmount());
						}else{
							notPayAmount = notPayAmount.add(billDetail.getTotalAmount());
						}
					}
					
					BillInfoVo billInfoVo = new BillInfoVo();
					
					billInfoVo.setActAmount(actAmount);
					billInfoVo.setBillId(billInfo.getId());
					billInfoVo.setBillName(billInfo.getName());
					billInfoVo.setFeeName(billInfo.getFeeName());
					billInfoVo.setIsSended(billInfo.getIsSended());
					billInfoVo.setNotPayAmount(notPayAmount);
					billInfoVo.setReceiverName(billInfo.getReceiverName());
					billInfoVo.setRemark(billInfo.getRemark());
					billInfoVo.setSendErrorNum(sendErrorNum);
					billInfoVo.setSendSuccessNum(sendSuccessNum);
					billInfoVo.setSendTime(billInfo.getSendTimeF());
					billInfoVo.setSendTotalNum(sendTotalNum);
					billInfoVo.setTotalAmount(totalAmount);
					billInfoVo.setCreateBy(billInfo.getCreateBy());
					billInfoVo.setStatus(billInfo.getStatus());
					
					dataList.add(billInfoVo);
				}
			}
			
			pageVo.setTotalNum(Long.valueOf(page.getTotalElements()).intValue());
			pageVo.setTotalPage(page.getTotalPages());
			
			HashMap<String,Object> dataMap = new HashMap<String,Object>();
			dataMap.put("page", pageVo);
			dataMap.put("list", dataList);
			data = dataMap;
			
		}catch(Exception e){
			e.printStackTrace();
			code = 500;
			message = "账单列表查询失败";
		}
		
		return new ApiResult(code, message, data);
	}
	
	/**apidoc
	 * @api {get} /bill/sendMessage/:id 发送账单消息
	 * @apiVersion 1.0.0
	 * @apiName billSendMessage
	 * @apiGroup bill
	 * 
	 * @apiHeader {String} x-token 用户访问接口令牌
	 * @apiHeaderExample {json} 用户访问接口令牌示例:
	 * {"x-token":"9a4e4ae20de14bc9b0e0a85614768487"}
	 * 
	 * @apiParam {String} id 账单Id
	 * @apiUse resultSuccess
	 * @apiSuccess {Object} data 
	 * @apiSuccess {Number} data.successNum 消息发送成功个数
	 * @apiSuccess {Number} data.errorNum 消息发送失败个数
	 * @apiSuccessExample {json} Success-Response:
	 * {
	 *	    "code": 200,
	 *	    "message": "消息发送成功",
	 *	    "data": {
	 *	        "successNum": 1,
	 *			"errorNum": 0
	 *		}
	 * }
	 * @apiUse resultError
	 */
	@GetMapping(value="/sendMessage/{id}")
	public ApiResult sendMessage(@PathVariable(name="id") String billId,
			@RequestHeader(name="x-token",defaultValue="") String xToken){
		
		int code = 200;
		String message = "消息发送成功";
		Object data = "";
		
		try{
			
			BillInfo billInfo = billInfoService.get(billId);
			
			if(billInfo == null){
				code = 400;
				message = "账单不存在";
			}else if(billInfo.getIsSended()){
				code = 400;
				message = "账单已发送消息";
			}else if("closed".equals(billInfo.getStatus())){
				code = 400;
				message = "账单已关闭";
			}else{
				
				//获取登录人
				JSONObject json = JSONObject.fromObject(redisService.get(xToken));
				LoginReturnVo returnVo = (LoginReturnVo)JSONObject.toBean(json, LoginReturnVo.class);
				
				HashMap<String,Object> queryMap = new HashMap<String,Object>();
				
				queryMap.put("EQ_billId", billId);
				queryMap.put("EQ_payStatus",10);
				queryMap.put("LTE_sendStatus", 10);//查找未发送和发送失败的
				
				//List<BillDetail> detailList = billDetailService.listSearch(queryMap, "createTime desc");
				
				//List<BillDetail> sendDetailList = new ArrayList<BillDetail>();
				
				int successNum = 0;
				int errorNum = 0;
				
				/*BaseSchool school = baseSchoolService.get(returnVo.getUser().getSchoolId());
				
				for (int i = 0; i < detailList.size(); i++) {
				
					int sendStatus = 0;
					String sendResult = "发送失败";
					
					BillDetail detail = detailList.get(i);
					
					List<BaseStudentFamily> list = studentFamilyService.findByStudentId(detail.getStudentId());
					
					for (int j = 0; j < list.size(); j++) {
						
						if(StringUtils.isNotBlank(list.get(j).getOpenId())){
							//如果openId存在
							boolean result = WechatMessageUtil.sendBillInfo(billInfo.getName(), billInfo.getRemark(), 
									school.getName(),list.get(j).getBaseStudent().getName(), detail.getTotalAmount(),
									list.get(j).getOpenId(),detail.getId(),detail.getCreateTime());
							
							//家庭成员中只要有一个发送成功,即成功
							if(result){
								sendStatus = 20;
								sendResult = "发送成功";
							}
						}
					}
					
					if(sendStatus == 20){
						successNum++;
					}else if(sendStatus == 0){
						errorNum++;
					}
					
					detail.setSendStatus(sendStatus);
					detail.setSendTime(new Date());
					detail.setSendResult(sendResult);
					
					sendDetailList.add(detail);
				}*/
				
				billInfo.setIsSended(true);
				billInfo.setSendBy(returnVo.getUser().getUserName());
				billInfo.setSendTime(new Date());
				
				billInfoService.save(billInfo);
				
				HashMap<String,Object> dataMap = new HashMap<String,Object>();
				
				dataMap.put("successNum", successNum);
				dataMap.put("errorNum", errorNum);
				
				data = dataMap;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			code = 400;
			message = "发送失败";
		}
		
		return new ApiResult(code, message, data);
	}
	
	/**apidoc
	 * @api {get} /bill/changeStatus/:id 开启或关闭账单
	 * @apiVersion 1.0.0
	 * @apiName billChangeStatus
	 * @apiGroup bill
	 * 
	 * @apiHeader {String} x-token 用户访问接口令牌
	 * @apiHeaderExample {json} 用户访问接口令牌示例:
	 * {"x-token":"9a4e4ae20de14bc9b0e0a85614768487"}
	 * 
	 * @apiParam {String} id 账单Id
	 * @apiUse resultSuccess
	 * @apiUse resultError
	 */
	@GetMapping(value="/changeStatus/{id}")
	public ApiResult changeStatus(@PathVariable(name="id") String billId,
			@RequestHeader(name="x-token",defaultValue="") String xToken){
		
		int code = 200;
		String message = "修改成功";
		Object data = "";
		
		try{
			
			BillInfo billInfo = billInfoService.get(billId);
			
			if(billInfo == null){
				code = 400;
				message = "账单不存在";
			}else{
				
				JSONObject json = JSONObject.fromObject(redisService.get(xToken));
				LoginReturnVo returnVo = (LoginReturnVo)JSONObject.toBean(json, LoginReturnVo.class);
				
				if("closed".equals(billInfo.getStatus())){
					billInfo.setStatus("opened");
					billInfo.setUpdateBy(returnVo.getUser().getUserName());
					billInfo.setUpdateTime(new Date());
					billInfoService.save(billInfo);
				}else if("opened".equals(billInfo.getStatus())){
					billInfo.setStatus("closed");
					billInfo.setUpdateBy(returnVo.getUser().getUserName());
					billInfo.setUpdateTime(new Date());
					billInfoService.save(billInfo);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			code = 400;
			message = "修改失败";
		}
		
		return new ApiResult(code, message, data);
	}
}

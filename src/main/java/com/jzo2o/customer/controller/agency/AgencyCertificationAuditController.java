package com.jzo2o.customer.controller.agency;


import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.jzo2o.mvc.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 机构认证审核表 前端控制器
 * </p>
 *
 * @author itcast
 * @since 2025-07-03
 */
@RestController
@RequestMapping("/agency/agency-certification-audit")
@Api(tags = "机构端 - 认证相关接口")
public class AgencyCertificationAuditController {

    @Resource
    private IAgencyCertificationAuditService agencyCertificationAuditService;

    @PostMapping
    @ApiOperation("机构认证审核")
    public void certificationAudit(@RequestBody AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO) {
        agencyCertificationAuditAddReqDTO.setServeProviderId(UserContext.currentUserId());
        agencyCertificationAuditService.certificationAudit(agencyCertificationAuditAddReqDTO);
    }

    @GetMapping("/rejectReason")
    @ApiOperation("查询最新的驳回原因")
    public RejectReasonResDTO queryCurrentUserLastRejectReason() {
        return agencyCertificationAuditService.queryCurrentUserLastRejectReason();
    }
}

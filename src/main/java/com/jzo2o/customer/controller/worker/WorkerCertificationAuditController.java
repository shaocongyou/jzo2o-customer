package com.jzo2o.customer.controller.worker;

import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.jzo2o.mvc.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("WorkerCertificationAuditController")
@RequestMapping("/worker/worker-certification-audit")
@Api(tags = "服务端 - 认证相关接口")
public class WorkerCertificationAuditController {

    @Resource
    private IWorkerCertificationAuditService workerCertificationAuditService;

    @PostMapping
    @ApiOperation("服务人员认证审核")
    public void certificationAudit(@RequestBody WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO) {
        workerCertificationAuditAddReqDTO.setServeProviderId(UserContext.currentUserId());
        workerCertificationAuditService.certificationAudit(workerCertificationAuditAddReqDTO);
    }

    @GetMapping("/rejectReason")
    @ApiOperation("查询最新的驳回原因")
    public RejectReasonResDTO queryCurrentUserLastRejectReason() {
        return workerCertificationAuditService.queryCurrentUserLastRejectReason();
    }
}

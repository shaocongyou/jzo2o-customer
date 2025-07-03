package com.jzo2o.customer.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("operationAgencyCertificationAuditController")
@RequestMapping("/operation/agency-certification-audit")
@Api(tags = "运营端 - 机构认证审核信息相关接口")
public class AgencyCertificationAuditController {

    @Resource
    private IAgencyCertificationAuditService agencyCertificationAuditService;

    @GetMapping("/page")
    @ApiOperation("分页查询服务人员认证审核信息")
    public PageResult<AgencyCertificationAuditResDTO> page(AgencyCertificationAuditPageQueryReqDTO agencyCertificationAuditPageQueryReqDTO) {
        return agencyCertificationAuditService.pageQuery(agencyCertificationAuditPageQueryReqDTO);
    }
}

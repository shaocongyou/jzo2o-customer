package com.jzo2o.customer.service;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;

/**
 * <p>
 * 机构认证审核表 服务类
 * </p>
 *
 * @author itcast
 * @since 2025-07-03
 */
public interface IAgencyCertificationAuditService extends IService<AgencyCertificationAudit> {

    void certificationAudit(AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO);

    PageResult<AgencyCertificationAuditResDTO> pageQuery(AgencyCertificationAuditPageQueryReqDTO agencyCertificationAuditPageQueryReqDTO);

    void auditCertification(Long id, CertificationAuditReqDTO certificationAuditReqDTO);

    RejectReasonResDTO queryCurrentUserLastRejectReason();
}

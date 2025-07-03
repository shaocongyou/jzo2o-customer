package com.jzo2o.customer.service;

import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;

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
}

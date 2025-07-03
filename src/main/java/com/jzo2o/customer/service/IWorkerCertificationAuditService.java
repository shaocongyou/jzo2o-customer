package com.jzo2o.customer.service;

import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;

/**
 * <p>
 * 服务人员认证审核表 服务类
 * </p>
 *
 * @author itcast
 * @since 2025-07-03
 */
public interface IWorkerCertificationAuditService extends IService<WorkerCertificationAudit> {

    void certificationAudit(WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO);
}

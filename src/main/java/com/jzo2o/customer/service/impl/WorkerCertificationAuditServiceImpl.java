package com.jzo2o.customer.service.impl;

import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.customer.service.IWorkerCertificationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 服务人员认证审核表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2025-07-03
 */
@Service
public class WorkerCertificationAuditServiceImpl extends ServiceImpl<WorkerCertificationAuditMapper, WorkerCertificationAudit> implements IWorkerCertificationAuditService {

    @Resource
    private IWorkerCertificationService workerCertificationService;

    @Override
    public void certificationAudit(WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO) {
        WorkerCertificationAudit workerCertificationAudit = BeanUtils.toBean(workerCertificationAuditAddReqDTO, WorkerCertificationAudit.class);
        // 赋予 未审核 状态
        workerCertificationAudit.setAuditStatus(0);
        baseMapper.insert(workerCertificationAudit);
        // 修改服务人员认证信息表 中的 认证状态 为 认证中
        // 如果在 服务人员认证信息表 中存在该服务人员，则直接修改，不存在则新增
        WorkerCertification workerCertification = workerCertificationService.getById(workerCertificationAuditAddReqDTO.getServeProviderId());
        boolean success = false;
        if(workerCertification == null){
            workerCertification = new WorkerCertification();
            workerCertification.setId(workerCertificationAuditAddReqDTO.getServeProviderId());
            workerCertification.setCertificationStatus(1);
            // 等之后认证成功了再把其他字段补全
            success = workerCertificationService.save(workerCertification);
        }else{
            success = workerCertificationService.lambdaUpdate()
                    .eq(WorkerCertification::getId, workerCertificationAuditAddReqDTO.getServeProviderId())
                    .set(WorkerCertification::getCertificationStatus, 1)
                    .update();
        }
        if(!success){
            throw new CommonException("修改 服务人员认证信息 中的 认证状态 为 认证中 失败");
        }
    }

}

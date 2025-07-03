package com.jzo2o.customer.service.impl;

import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.jzo2o.customer.mapper.AgencyCertificationAuditMapper;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.customer.service.IAgencyCertificationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 机构认证审核表 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2025-07-03
 */
@Service
public class AgencyCertificationAuditServiceImpl extends ServiceImpl<AgencyCertificationAuditMapper, AgencyCertificationAudit> implements IAgencyCertificationAuditService {

    @Resource
    private IAgencyCertificationService agencyCertificationService;

    @Override
    public void certificationAudit(AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO) {
        AgencyCertificationAudit agencyCertificationAudit = BeanUtils.toBean(agencyCertificationAuditAddReqDTO, AgencyCertificationAudit.class);
        // 赋予 未审核 状态
        agencyCertificationAudit.setAuditStatus(0);
        baseMapper.insert(agencyCertificationAudit);
        // 修改机构认证信息表 中的 认证状态 为 认证中
        // 如果在 机构认证信息表 中存在该机构，则直接修改，不存在则新增
        AgencyCertification agencyCertification = agencyCertificationService.getById(agencyCertificationAuditAddReqDTO.getServeProviderId());
        boolean success = false;
        if(agencyCertification == null){
            agencyCertification = new AgencyCertification();
            agencyCertification.setId(agencyCertificationAuditAddReqDTO.getServeProviderId());
            agencyCertification.setCertificationStatus(1);
            // 等之后认证成功了再把其他字段补全
            success = agencyCertificationService.save(agencyCertification);
        }else{
            success = agencyCertificationService.lambdaUpdate()
                    .eq(AgencyCertification::getId, agencyCertificationAuditAddReqDTO.getServeProviderId())
                    .set(AgencyCertification::getCertificationStatus, 1)
                    .update();
        }
        if(!success){
            throw new CommonException("修改 机构认证信息 中的 认证状态 为 认证中 失败");
        }
    }
}

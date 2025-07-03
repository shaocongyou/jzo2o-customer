package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.customer.enums.CertificationStatusEnum;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.model.dto.WorkerCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;
import com.jzo2o.customer.service.IServeProviderService;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.customer.service.IWorkerCertificationService;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDateTime;

import static javax.management.Query.eq;

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

    @Resource
    private IServeProviderService serveProviderService;

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

    @Override
    public PageResult<WorkerCertificationAuditResDTO> pageQuery(WorkerCertificationAuditPageQueryReqDTO workerCertificationAuditPageQueryReqDTO) {
        Page<WorkerCertificationAudit> page = PageUtils.parsePageQuery(workerCertificationAuditPageQueryReqDTO, WorkerCertificationAudit.class);
        LambdaQueryWrapper<WorkerCertificationAudit> wrapper = Wrappers.<WorkerCertificationAudit>lambdaQuery()
                .like(ObjectUtil.isNotEmpty(workerCertificationAuditPageQueryReqDTO.getName()), WorkerCertificationAudit::getName, workerCertificationAuditPageQueryReqDTO.getName())
                .eq(ObjectUtil.isNotEmpty(workerCertificationAuditPageQueryReqDTO.getIdCardNo()), WorkerCertificationAudit::getIdCardNo, workerCertificationAuditPageQueryReqDTO.getIdCardNo())
                .eq(ObjectUtil.isNotEmpty(workerCertificationAuditPageQueryReqDTO.getAuditStatus()), WorkerCertificationAudit::getAuditStatus, workerCertificationAuditPageQueryReqDTO.getAuditStatus())
                .eq(ObjectUtil.isNotEmpty(workerCertificationAuditPageQueryReqDTO.getCertificationStatus()), WorkerCertificationAudit::getCertificationStatus, workerCertificationAuditPageQueryReqDTO.getCertificationStatus());
        Page<WorkerCertificationAudit> result = baseMapper.selectPage(page, wrapper);
        return PageUtils.toPage(result, WorkerCertificationAuditResDTO.class);
    }

    @Override
    public void auditCertification(Long id, CertificationAuditReqDTO certificationAuditReqDTO) {
        // 1. 更新申请记录
        CurrentUserInfo currentUserInfo = UserContext.currentUser();
        LambdaUpdateWrapper<WorkerCertificationAudit> updateWrapper = Wrappers.<WorkerCertificationAudit>lambdaUpdate()
                .eq(WorkerCertificationAudit::getId, id)
                .set(WorkerCertificationAudit::getAuditStatus, 1) // 审核状态改为 已审核
                .set(WorkerCertificationAudit::getAuditorId, currentUserInfo.getId()) // 审核人改为 当前用户
                .set(WorkerCertificationAudit::getAuditTime, LocalDateTime.now()) // 审核时间改为 当前时间
                .set(WorkerCertificationAudit::getCertificationStatus, certificationAuditReqDTO.getCertificationStatus()) // 认证状态改为 通过/不通过
                .set(ObjectUtil.isNotEmpty(certificationAuditReqDTO.getRejectReason()),WorkerCertificationAudit::getRejectReason, certificationAuditReqDTO.getRejectReason()); // 不通过原因
        super.update(updateWrapper);

        //2.更新认证信息，如果认证成功，需要将各认证属性也更新
        WorkerCertificationAudit workerCertificationAudit = baseMapper.selectById(id);
        WorkerCertificationUpdateDTO workerCertificationUpdateDTO = new WorkerCertificationUpdateDTO();
        workerCertificationUpdateDTO.setId(workerCertificationAudit.getServeProviderId());
        workerCertificationUpdateDTO.setCertificationStatus(certificationAuditReqDTO.getCertificationStatus());
        if (ObjectUtil.equal(CertificationStatusEnum.SUCCESS.getStatus(), certificationAuditReqDTO.getCertificationStatus())) {
            //如果认证成功，需要更新服务人员/机构名称
            serveProviderService.updateNameById(workerCertificationAudit.getServeProviderId(), workerCertificationAudit.getName());

            workerCertificationUpdateDTO.setName(workerCertificationAudit.getName());
            workerCertificationUpdateDTO.setIdCardNo(workerCertificationAudit.getIdCardNo());
            workerCertificationUpdateDTO.setFrontImg(workerCertificationAudit.getFrontImg());
            workerCertificationUpdateDTO.setBackImg(workerCertificationAudit.getBackImg());
            workerCertificationUpdateDTO.setCertificationMaterial(workerCertificationAudit.getCertificationMaterial());
            workerCertificationUpdateDTO.setCertificationTime(workerCertificationAudit.getAuditTime());
        }
        workerCertificationService.updateById(workerCertificationUpdateDTO);
    }

    @Override
    public RejectReasonResDTO queryCurrentUserLastRejectReason() {
        LambdaQueryWrapper<WorkerCertificationAudit> queryWrapper = Wrappers.<WorkerCertificationAudit>lambdaQuery()
                .eq(WorkerCertificationAudit::getServeProviderId, UserContext.currentUserId())
                .orderByDesc(WorkerCertificationAudit::getCreateTime)
                .last("limit 1");
        WorkerCertificationAudit workerCertificationAudit = baseMapper.selectOne(queryWrapper);
        return new RejectReasonResDTO(workerCertificationAudit.getRejectReason());
    }

}

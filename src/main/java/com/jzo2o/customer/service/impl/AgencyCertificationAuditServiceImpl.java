package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.customer.enums.CertificationStatusEnum;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import com.jzo2o.customer.mapper.AgencyCertificationAuditMapper;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.AgencyCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.WorkerCertificationUpdateDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.customer.service.IAgencyCertificationService;
import com.jzo2o.customer.service.IServeProviderService;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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

    @Resource
    private IServeProviderService serveProviderService;

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

    @Override
    public PageResult<AgencyCertificationAuditResDTO> pageQuery(AgencyCertificationAuditPageQueryReqDTO agencyCertificationAuditPageQueryReqDTO) {
        Page<AgencyCertificationAudit> page = PageUtils.parsePageQuery(agencyCertificationAuditPageQueryReqDTO, AgencyCertificationAudit.class);
        LambdaQueryWrapper<AgencyCertificationAudit> wrapper = Wrappers.<AgencyCertificationAudit>lambdaQuery()
                .like(ObjectUtil.isNotEmpty(agencyCertificationAuditPageQueryReqDTO.getName()), AgencyCertificationAudit::getName, agencyCertificationAuditPageQueryReqDTO.getName())
                .eq(ObjectUtil.isNotEmpty(agencyCertificationAuditPageQueryReqDTO.getLegalPersonName()), AgencyCertificationAudit::getLegalPersonName, agencyCertificationAuditPageQueryReqDTO.getLegalPersonName())
                .eq(ObjectUtil.isNotEmpty(agencyCertificationAuditPageQueryReqDTO.getAuditStatus()), AgencyCertificationAudit::getAuditStatus, agencyCertificationAuditPageQueryReqDTO.getAuditStatus())
                .eq(ObjectUtil.isNotEmpty(agencyCertificationAuditPageQueryReqDTO.getCertificationStatus()), AgencyCertificationAudit::getCertificationStatus, agencyCertificationAuditPageQueryReqDTO.getCertificationStatus());
        Page<AgencyCertificationAudit> result = baseMapper.selectPage(page, wrapper);
        return PageUtils.toPage(result, AgencyCertificationAuditResDTO.class);
    }

    @Override
    public void auditCertification(Long id, CertificationAuditReqDTO certificationAuditReqDTO) {
        // 1. 更新申请记录
        CurrentUserInfo currentUserInfo = UserContext.currentUser();
        LambdaUpdateWrapper<AgencyCertificationAudit> updateWrapper = Wrappers.<AgencyCertificationAudit>lambdaUpdate()
                .eq(AgencyCertificationAudit::getId, id)
                .set(AgencyCertificationAudit::getAuditStatus, 1) // 审核状态改为 已审核
                .set(AgencyCertificationAudit::getAuditorId, currentUserInfo.getId()) // 审核人改为 当前用户
                .set(AgencyCertificationAudit::getAuditTime, LocalDateTime.now()) // 审核时间改为 当前时间
                .set(AgencyCertificationAudit::getCertificationStatus, certificationAuditReqDTO.getCertificationStatus()) // 认证状态改为 通过/不通过
                .set(ObjectUtil.isNotEmpty(certificationAuditReqDTO.getRejectReason()),AgencyCertificationAudit::getRejectReason, certificationAuditReqDTO.getRejectReason()); // 不通过原因
        super.update(updateWrapper);

        //2.更新认证信息，如果认证成功，需要将各认证属性也更新
        AgencyCertificationAudit agencyCertificationAudit = baseMapper.selectById(id);
        AgencyCertificationUpdateDTO agencyCertificationUpdateDTO = new AgencyCertificationUpdateDTO();
        agencyCertificationUpdateDTO.setId(agencyCertificationAudit.getServeProviderId());
        agencyCertificationUpdateDTO.setCertificationStatus(certificationAuditReqDTO.getCertificationStatus());
        if (ObjectUtil.equal(CertificationStatusEnum.SUCCESS.getStatus(), certificationAuditReqDTO.getCertificationStatus())) {
            //如果认证成功，需要更新服务人员/机构名称
            serveProviderService.updateNameById(agencyCertificationAudit.getServeProviderId(), agencyCertificationAudit.getName());

            agencyCertificationUpdateDTO.setName(agencyCertificationAudit.getName());
            agencyCertificationUpdateDTO.setIdNumber(agencyCertificationAudit.getIdNumber());
            agencyCertificationUpdateDTO.setLegalPersonName(agencyCertificationAudit.getLegalPersonName());
            agencyCertificationUpdateDTO.setLegalPersonIdCardNo(agencyCertificationAudit.getLegalPersonIdCardNo());
            agencyCertificationUpdateDTO.setBusinessLicense(agencyCertificationAudit.getBusinessLicense());
            agencyCertificationUpdateDTO.setCertificationTime(agencyCertificationAudit.getAuditTime());
        }
        agencyCertificationService.updateByServeProviderId(agencyCertificationUpdateDTO);
    }

    @Override
    public RejectReasonResDTO queryCurrentUserLastRejectReason() {

        LambdaQueryWrapper<AgencyCertificationAudit> queryWrapper = Wrappers.<AgencyCertificationAudit>lambdaQuery()
                .eq(AgencyCertificationAudit::getServeProviderId, UserContext.currentUserId())
                .orderByDesc(AgencyCertificationAudit::getCreateTime)
                .last("limit 1");
        AgencyCertificationAudit agencyCertificationAudit = baseMapper.selectOne(queryWrapper);
        return new RejectReasonResDTO(agencyCertificationAudit.getRejectReason());
    }
}

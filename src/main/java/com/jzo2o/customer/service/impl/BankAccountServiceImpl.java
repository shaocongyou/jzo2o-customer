package com.jzo2o.customer.service.impl;

import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.customer.model.domain.BankAccount;
import com.jzo2o.customer.mapper.BankAccountMapper;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.service.IBankAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 银行账户 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2025-07-03
 */
@Service
public class BankAccountServiceImpl extends ServiceImpl<BankAccountMapper, BankAccount> implements IBankAccountService {

    @Override
    public BankAccount saveOrUpdate(BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {

        BankAccount bankAccount = BeanUtils.toBean(bankAccountUpsertReqDTO, BankAccount.class);
        super.saveOrUpdate(bankAccount);
        return bankAccount;
    }
}

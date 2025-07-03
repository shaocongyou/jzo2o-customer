package com.jzo2o.customer.controller.agency;

import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.customer.model.domain.BankAccount;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.IBankAccountService;
import com.jzo2o.mvc.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("agencyBankAccountController")
@RequestMapping("/agency/bank-account")
@Api(tags = "机构端 - 银行账户相关接口")
public class BankAccountController {

    @Resource
    private IBankAccountService bankAccountService;

    @PostMapping
    @ApiOperation("添加银行账户")
    public void addBankAccount(@RequestBody BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {
        bankAccountUpsertReqDTO.setId(UserContext.currentUserId());
        bankAccountService.saveOrUpdate(bankAccountUpsertReqDTO);
    }

    @GetMapping("/currentUserBankAccount")
    @ApiOperation("获取银行账户")
    public BankAccountResDTO getBankAccount() {
        BankAccount bankAccount = bankAccountService.getById(UserContext.currentUserId());
        BankAccountResDTO bankAccountResDTO = BeanUtils.toBean(bankAccount, BankAccountResDTO.class);
        return bankAccountResDTO;
    }
}

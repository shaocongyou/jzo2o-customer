package com.jzo2o.customer.controller.worker;

import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.service.IBankAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("workerBankAccountController")
@RequestMapping("/worker/bank-account")
@Api(tags = "服务端 - 银行账户相关接口")
public class BankAccountController {

    @Resource
    private IBankAccountService bankAccountService;

    @PostMapping
    @ApiOperation("添加银行账户")
    public void addBankAccount(@RequestBody BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {
        bankAccountService.saveOrUpdate(bankAccountUpsertReqDTO);
    }
}

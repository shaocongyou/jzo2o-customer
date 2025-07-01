package com.jzo2o.customer.controller.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.model.dto.request.AddressBookPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;
import com.jzo2o.customer.service.IAddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("consumerAddressBookController")
@RequestMapping("/consumer/address-book")
@Api(tags = "用户端 - 消费者地址管理")
public class AddressBookController {

    @Resource
    private IAddressBookService addressBookService;

    @GetMapping("/page")
    @ApiOperation("查询用户的地址")
    public PageResult<AddressBookResDTO> page(AddressBookPageQueryReqDTO addressBookPageQueryReqDTO) {
        return addressBookService.page(addressBookPageQueryReqDTO);
    }

    @PostMapping
    @ApiOperation("新增用户的地址")
    public void add(@RequestBody AddressBookUpsertReqDTO addressBookUpsertReqDTO) {
        addressBookService.add(addressBookUpsertReqDTO);
    }

    @GetMapping("/{id}")
    @ApiOperation("查询用户的地址")
    public AddressBookResDTO getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return BeanUtil.toBean(addressBook, AddressBookResDTO.class);
    }
}

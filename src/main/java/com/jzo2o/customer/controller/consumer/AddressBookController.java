package com.jzo2o.customer.controller.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.model.dto.request.AddressBookPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;
import com.jzo2o.customer.service.IAddressBookService;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

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

    @PutMapping("/{id}")
    @ApiOperation("修改用户的地址")
    public AddressBookResDTO updateById(@PathVariable Long id,@RequestBody AddressBookUpsertReqDTO addressBookUpsertReqDTO) {
        return addressBookService.updateById(id,addressBookUpsertReqDTO);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除用户的地址")
    public void deleteById(@NotNull(message = "删除列表不能为空") @RequestBody List<Long> ids) {
        addressBookService.removeByIds(ids);
    }

    @PutMapping("/default")
    @ApiOperation("设置用户的默认地址")
    public void defaultAddressBook(@NotNull(message = "id不能为空") @RequestParam("id") Long id,
                                   @NotNull(message = "flag不能为空") @RequestParam("flag") Integer flag) {
        addressBookService.defaultAddressBook(id,flag);
    }
}

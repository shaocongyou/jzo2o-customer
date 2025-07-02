package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.api.publics.MapApi;
import com.jzo2o.api.publics.dto.response.LocationResDTO;
import com.jzo2o.common.expcetions.CommonException;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.CollUtils;
import com.jzo2o.common.utils.NumberUtils;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.customer.mapper.AddressBookMapper;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.model.dto.request.AddressBookPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;
import com.jzo2o.customer.service.IAddressBookService;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 地址薄 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-07-06
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements IAddressBookService {

    @Resource
    private MapApi mapApi;

    @Override
    public List<AddressBookResDTO> getByUserIdAndCity(Long userId, String city) {

        List<AddressBook> addressBooks = lambdaQuery()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getCity, city)
                .list();
        if(CollUtils.isEmpty(addressBooks)) {
            return new ArrayList<>();
        }
        return BeanUtils.copyToList(addressBooks, AddressBookResDTO.class);
    }

    @Override
    public PageResult<AddressBookResDTO> page(AddressBookPageQueryReqDTO addressBookPageQueryReqDTO) {
        Page<AddressBook> page = PageUtils.parsePageQuery(addressBookPageQueryReqDTO,AddressBook.class);

        LambdaQueryWrapper<AddressBook> wrapper = Wrappers.<AddressBook>lambdaQuery().eq(AddressBook::getUserId, UserContext.currentUserId());
        Page<AddressBook> addressBookPage = baseMapper.selectPage(page, wrapper);
        return PageUtils.toPage(addressBookPage, AddressBookResDTO.class);
    }

    @Override
    public void add(AddressBookUpsertReqDTO addressBookUpsertReqDTO) {
        Long userId = UserContext.currentUserId();

        if(addressBookUpsertReqDTO.getIsDefault() == 1) {
            // 如果是默认地址，需要把目前的所有地址设置为非默认
            updateDefault(userId, 0);
        }

        AddressBook addressBook = BeanUtil.toBean(addressBookUpsertReqDTO, AddressBook.class);
        addressBook.setUserId(userId);
        //组装详细地址
        String completeAddress = addressBookUpsertReqDTO.getProvince() +
                addressBookUpsertReqDTO.getCity() +
                addressBookUpsertReqDTO.getCounty() +
                addressBookUpsertReqDTO.getAddress();

        //如果请求体中没有经纬度，需要调用第三方api根据详细地址获取经纬度
        if(ObjectUtil.isEmpty(addressBookUpsertReqDTO.getLocation())){
            //远程请求高德获取经纬度
            LocationResDTO locationDto = mapApi.getLocationByAddress(completeAddress);
            //经纬度(字符串格式：经度,纬度),经度在前，纬度在后
            String location = locationDto.getLocation();
            addressBookUpsertReqDTO.setLocation(location);
        }

        if(StringUtils.isNotEmpty(addressBookUpsertReqDTO.getLocation())) {
            // 经度
            addressBook.setLon(NumberUtils.parseDouble(addressBookUpsertReqDTO.getLocation().split(",")[0]));
            // 纬度
            addressBook.setLat(NumberUtils.parseDouble(addressBookUpsertReqDTO.getLocation().split(",")[1]));
        }
        baseMapper.insert(addressBook);
    }

    @Override
    @Transactional
    public AddressBookResDTO updateById(Long id, AddressBookUpsertReqDTO addressBookUpsertReqDTO) {
        AddressBook addressBook = BeanUtil.toBean(addressBookUpsertReqDTO, AddressBook.class);
        addressBook.setId(id);

        //调用第三方，根据地址获取经纬度坐标
        String completeAddress = addressBookUpsertReqDTO.getProvince() +
                addressBookUpsertReqDTO.getCity() +
                addressBookUpsertReqDTO.getCounty() +
                addressBookUpsertReqDTO.getAddress();
        //远程请求高德获取经纬度
        LocationResDTO locationDto = mapApi.getLocationByAddress(completeAddress);
        //经纬度(字符串格式：经度,纬度),经度在前，纬度在后
        String location = locationDto.getLocation();
        if(StringUtils.isNotEmpty(location)) {
            // 经度
            addressBook.setLon(NumberUtils.parseDouble(locationDto.getLocation().split(",")[0]));
            // 纬度
            addressBook.setLat(NumberUtils.parseDouble(locationDto.getLocation().split(",")[1]));
        }

        if(addressBookUpsertReqDTO.getIsDefault() == 1) {
            // 如果是默认地址，需要把目前的所有地址设置为非默认
            updateDefault(UserContext.currentUserId(), 0);
        }

        baseMapper.updateById(addressBook);

        return BeanUtils.toBean(addressBook, AddressBookResDTO.class);
    }

    @Override
    public void defaultAddressBook(Long id, Integer flag) {
        // 如果flag为1，表示设置为默认地址，否则表示取消默认地址
        if(flag == 1) {
            // 将目前该用户的地址全部取消默认
            updateDefault(UserContext.currentUserId(), 0);
            // 针对该地址设置成默认
            boolean success = lambdaUpdate()
                    .eq(AddressBook::getId, id)
                    .set(AddressBook::getIsDefault, 1)
                    .update();
            if(!success) {
                throw new CommonException("更新默认地址失败");
            }
        }else if(flag == 0) {
            // 将目前该用户的地址全部取消默认
            boolean success = lambdaUpdate()
                    .eq(AddressBook::getId, id)
                    .set(AddressBook::getIsDefault, 0)
                    .update();
            if(!success) {
                throw new CommonException("取消默认地址失败");
            }
        }else {
            throw new CommonException("flag参数错误");
        }

    }

    private void updateDefault(Long userId, int i) {
        Integer count = lambdaQuery()
                .eq(AddressBook::getUserId, userId)
                .count();
        if(count > 0) {
            // 如果有默认地址，需要把默认地址设置为非默认
            boolean success = lambdaUpdate()
                    .eq(AddressBook::getUserId, userId)
                    .set(AddressBook::getIsDefault, i)
                    .update();
            if(!success) {
                throw new CommonException("更新默认地址失败");
            }
        }
    }
}

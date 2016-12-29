package com.ruijc.ddns.aliyun.process;

//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖镇楼                  BUG辟易
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.ruijc.Response;
import com.ruijc.ddns.aliyun.api.AliyunDDNSApi;
import com.ruijc.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 阿里云DDNS云解析逻辑
 *
 * @author Storezhang
 * @create 2016-12-29 04:00
 * @email storezhang@gmail.com
 * @qq 160290688
 */
@Service
public class AliyunDDNSL {

    @Autowired
    private AliyunDDNSApi api;

    @Cacheable(value = "ddns-client", key = "#appKey + '_' + #secret")
    public IAcsClient client(String appKey, String secret) {
        IAcsClient client;

        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", appKey, secret);
        try {
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Alidns", "alidns.aliyuncs.com");
        } catch (Exception e) {
            // 有错误
        }
        client = new DefaultAcsClient(profile);

        return client;
    }

    @Cacheable(value = "ddns-client", key = "#domain + '_' + #pr + '_' + #value")
    public String recordId(IAcsClient client, String domain, String rr, String value, long ttl) {
        String recordId = "";

        List<DescribeDomainRecordsResponse.Record> records = api.list(client, domain, rr);
        if (CollectionUtils.isBlank(records)) {
            recordId = api.add(client, domain, rr, value, ttl);
        } else if (1 == records.size()) {
            recordId = records.get(0).getRecordId();
        } else if (1 < records.size()) {
            if (api.delete(client, records)) {
                recordId = api.add(client, domain, rr, value, ttl);
            }
        }

        return recordId;
    }

    @CacheEvict(value = "ddns-client", key = "#domain + '_' + #pr + '_' + #value")
    private void clearCache(String domain, String rr, String value) {
        // 清空缓存
    }

    public Response update(String appKey, String secret, String domain, String rr, String value, long ttl) {
        Response rsp;

        IAcsClient client = client(appKey, secret);
        String recordId = recordId(client, domain, rr, value, ttl);
        rsp = api.update(client, recordId, rr, value, ttl);
        if (!rsp.isSuccess()) {// 更新失败，清空缓存，重新操作一次
            clearCache(domain, rr, value);
        }

        // 再次操作
        recordId = recordId(client, domain, rr, value, ttl);
        rsp = api.update(client, recordId, rr, value, ttl);

        return rsp;
    }
}

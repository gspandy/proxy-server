package com.ruijc.ddns.aliyun.api;

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

import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.*;
import com.aliyuncs.exceptions.ClientException;
import com.ruijc.Response;
import com.ruijc.util.CollectionUtils;
import com.ruijc.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 云解析接口
 *
 * @author Storezhang
 * @create 2016-12-28 10:01
 * @email storezhang@gmail.com
 * @qq 160290688
 */
@Service
public class AliyunDDNSApi {

    public Response<String> update(IAcsClient client, String recordId, String rr, String type, String value, long ttl, long priority, String line) {
        Response<String> ret = new Response<String>();

        UpdateDomainRecordRequest req = new UpdateDomainRecordRequest();
        req.setRecordId(recordId);
        if (!StringUtils.isBlank(rr)) {
            req.setRR(rr);
        }
        if (!StringUtils.isBlank(type)) {
            req.setType(type);
        }
        if (!StringUtils.isBlank(value)) {
            req.setValue(value);
        }
        if (0 != ttl) {
            req.setTTL(ttl);
        }
        if (0 != priority) {
            req.setPriority(priority);
        }
        if (!StringUtils.isBlank(line)) {
            req.setLine(line);
        }

        try {
            client.getAcsResponse(req);
        } catch (ClientException e) {
            ret.setSuccess(false);
            ret.setMsg(e.getErrMsg());
        } catch (Exception e) {
            ret.setSuccess(false);
            ret.setMsg(e.getMessage());
        }

        return ret;
    }

    public Response<String> update(IAcsClient client, String recordId, String rr, String type, String value) {
        return update(client, recordId, rr, type, value, 600, 1, "default");
    }

    public Response<String> update(IAcsClient client, String recordId, String rr, String value, long ttl) {
        return update(client, recordId, rr, "A", value, ttl, 1, "default");
    }

    public Response<String> update(IAcsClient client, String recordId, String rr, String value) {
        return update(client, recordId, rr, "A", value, 600, 1, "default");
    }

    public DescribeDomainRecordsResponse list(IAcsClient client, String domain, String prKey, String typeKey, String valueKey, long num, long size) {
        DescribeDomainRecordsResponse rsp;

        DescribeDomainRecordsRequest req = new DescribeDomainRecordsRequest();
        if (!StringUtils.isBlank(domain)) {
            req.setDomainName(domain);
        }
        if (!StringUtils.isBlank(prKey)) {
            req.setRRKeyWord(prKey);
        }
        if (!StringUtils.isBlank(typeKey)) {
            req.setTypeKeyWord(typeKey);
        }
        if (!StringUtils.isBlank(valueKey)) {
            req.setValueKeyWord(valueKey);
        }
        req.setPageNumber(num);
        req.setPageSize(size);

        try {
            rsp = client.getAcsResponse(req);
        } catch (Exception e) {
            rsp = null;
        }

        return rsp;
    }

    public List<DescribeDomainRecordsResponse.Record> list(IAcsClient client, String domain, String prKey, String typeKey, String valueKey) {
        List<DescribeDomainRecordsResponse.Record> records = new ArrayList<DescribeDomainRecordsResponse.Record>();

        int page = 1;
        DescribeDomainRecordsResponse rsp;
        while (true) {
            rsp = list(client, domain, prKey, typeKey, valueKey, page, 500);
            if (null == rsp) {
                break;
            }
            records.addAll(rsp.getDomainRecords());
            if (CollectionUtils.isBlank(rsp.getDomainRecords())) {
                break;
            }
            page++;
        }

        return records;
    }

    public List<DescribeDomainRecordsResponse.Record> list(IAcsClient client, String domain, String prKey) {
        List<DescribeDomainRecordsResponse.Record> records = new ArrayList<DescribeDomainRecordsResponse.Record>();

        int page = 1;
        DescribeDomainRecordsResponse rsp;
        while (true) {
            rsp = list(client, domain, prKey, "", "", page, 500);
            if (null == rsp) {
                break;
            }
            records.addAll(rsp.getDomainRecords());
            if (CollectionUtils.isBlank(rsp.getDomainRecords())) {
                break;
            }
            page++;
        }

        return records;
    }

    public String add(IAcsClient client, String domain, String rr, String type, String value, long ttl, long priority, String line) {
        String recordId = "";

        AddDomainRecordRequest req = new AddDomainRecordRequest();
        if (!StringUtils.isBlank(domain)) {
            req.setDomainName(domain);
        }
        if (!StringUtils.isBlank(rr)) {
            req.setRR(rr);
        }
        if (!StringUtils.isBlank(type)) {
            req.setType(type);
        }
        if (!StringUtils.isBlank(value)) {
            req.setValue(value);
        }
        if (0 != ttl) {
            req.setTTL(ttl);
        }
        if (0 != priority) {
            req.setPriority(priority);
        }
        if (!StringUtils.isBlank(line)) {
            req.setLine(line);
        }

        try {
            recordId = client.getAcsResponse(req).getRecordId();
        } catch (Exception e) {
            return recordId;
        }

        return recordId;
    }

    public String add(IAcsClient client, String domain, String rr, String type, String value) {
        return add(client, domain, rr, type, value, 600, 1, "default");
    }

    public String add(IAcsClient client, String domain, String rr, String value, long ttl) {
        return add(client, domain, rr, "A", value, ttl, 1, "default");
    }

    public String add(IAcsClient client, String domain, String rr, String value) {
        return add(client, domain, rr, "A", value);
    }

    public boolean delete(IAcsClient client, String recordId) {
        boolean success;

        DeleteDomainRecordRequest req = new DeleteDomainRecordRequest();
        req.setRecordId(recordId);

        try {
            success = StringUtils.isBlank(client.getAcsResponse(req).getRecordId()) ? false : true;
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    public boolean delete(IAcsClient client, List<DescribeDomainRecordsResponse.Record> records) {
        boolean success = true;

        for (DescribeDomainRecordsResponse.Record record : records) {
            success = success && delete(client, record.getRecordId());
        }

        return success;
    }
}

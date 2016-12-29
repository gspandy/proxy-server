package com.ruijc.ddns;

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

import com.ruijc.ddns.aliyun.AliyunProerties;
import com.ruijc.ddns.aliyun.process.AliyunDDNSL;
import com.ruijc.ddns.conf.DDNSProperties;
import com.ruijc.ddns.conf.Record;
import com.ruijc.util.CollectionUtils;
import com.ruijc.util.NetworkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 解析任务
 *
 * @author Storezhang
 * @create 2016-12-29 05:56
 * @email storezhang@gmail.com
 * @qq 160290688
 */
@Component
@EnableConfigurationProperties({AliyunProerties.class, DDNSProperties.class})
public class DDNSTask implements CommandLineRunner, Runnable {

    @Autowired
    private AliyunProerties aliyunProerties;
    @Autowired
    private DDNSProperties ddnsProperties;
    @Autowired
    private AliyunDDNSL ddnsL;
    private ScheduledExecutorService service;

    @Override
    public void run(String... args) throws Exception {
        if (!CollectionUtils.isBlank(ddnsProperties.getRecords())) {
            service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(this, 0, ddnsProperties.getInterval(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void run() {
        for (Record record : ddnsProperties.getRecords()) {
            switch (record.getType()) {
                case ALIYUN:
                    ddnsL.update(
                            aliyunProerties.getAppKey(),
                            aliyunProerties.getSecret(),
                            record.getDomain(),
                            record.getHost(),
                            NetworkUtils.netIp(),
                            record.getTtl()
                    );
            }
        }
    }
}

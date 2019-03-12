/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.controller;

import io.waves.cloud.kitemanager.ro.ResultRo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 为专门在windows下做测试做的controller
 * @author ytzhang0828@qq.com
 */
@RestController
@RequestMapping("mock")
public class MockController {

    /** 延时接口 */
    @RequestMapping("sleep/{seconds}")
    public ResultRo sleep(@PathVariable("seconds") int seconds) {
        return ResultRo.process(() -> {
            try {
                TimeUnit.SECONDS.sleep(seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "sleep " + seconds + " seconds over";
        });
    }

}

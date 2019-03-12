/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.util;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.Properties;

/**
 * 应用模板工具
 * @author ytzhang0828@qq.com
 */
public class VelocityUtil {

    private static VelocityEngine velocityEngine = new VelocityEngine();
    static {
        Properties props = new Properties();
        props.setProperty("input.encoding", "UTF-8");
        props.setProperty("output.encoding", "UTF-8");
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
        props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        velocityEngine.init(props);
    }

    public static String merge(VelocityContext context, String template) {

        String text = null;
        StringWriter writer = new StringWriter();
        try {
            velocityEngine.evaluate(context, writer, "plain", template);
            text = writer.toString();
        } finally {
            try { writer.close(); } catch (Exception e) { }
        }

        return text;

    }

    public static String merge(VelocityContext context, String vmPath, String encoding) {

        String text = null;
        StringWriter writer = new StringWriter();
        try {
            velocityEngine.mergeTemplate(vmPath, encoding, context, writer);
            text = writer.toString();
        } finally {
            try { writer.close(); } catch (Exception e) { }
        }

        return text;

    }

}

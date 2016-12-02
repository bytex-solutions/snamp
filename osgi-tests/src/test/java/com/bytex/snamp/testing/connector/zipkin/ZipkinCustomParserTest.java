package com.bytex.snamp.testing.connector.zipkin;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import zipkin.Span;
import zipkin.reporter.Sender;

import javax.management.JMException;
import java.io.File;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ZipkinCustomParserTest extends AbstractZipkinConnectorTest {
    public ZipkinCustomParserTest(){
        super("", ImmutableMap.of(
                "parserScriptPath", "file:" + getPathToFileInProjectRoot("sample-groovy-scripts") + File.separator,
                "parserScript", "ZipkinConnectorParser.groovy"
        ));
    }

    @Test
    public void httpTest() throws InterruptedException, JMException {
        final Sender reporter = createHttpSender();
        final Span span = Span.builder()
                .timestamp(System.currentTimeMillis())
                .traceId(9742L)
                .id(10L)
                .name("testSpan")
                .build();
        sendSpans(reporter, span);
        Thread.sleep(5_000);
        testAttribute("lastId", TypeToken.of(Long.class), 9742L, true);   //microseconds to seconds

    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("ts", attribute -> {
            attribute.getParameters().put("gauge", "gauge64");
        });
        attributes.addAndConsume("lastId", attribute -> {
            attribute.getParameters().put("gauge", "get lastValue from gauge64 ts");
        });
    }
}

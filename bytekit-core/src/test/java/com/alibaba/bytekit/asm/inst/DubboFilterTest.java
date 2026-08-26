package com.alibaba.bytekit.asm.inst;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.dubbo.rpc.filter.ConsumerContextFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.zeroturnaround.zip.ZipUtil;

import com.alibaba.bytekit.asm.instrument.InstrumentParseResult;
import com.alibaba.bytekit.asm.instrument.InstrumentTemplate;
import com.alibaba.bytekit.asm.instrument.InstrumentTransformer;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.bytekit.utils.JavaVersionUtils;
import com.alibaba.bytekit.utils.VerifyUtils;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;

/**
 * 
 * @author hengyunabc 2020-11-27
 *
 */
@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
public class DubboFilterTest {
    @TempDir
    public File folder;

    private Object object;

    @BeforeEach
    public void beforeMethod() {
        // dubbo need jdk8
        org.junit.jupiter.api.Assumptions.assumeTrue(JavaVersionUtils.isGreaterThanJava7());
    }

    @BeforeEach
    public void before() throws Exception {
        String file = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

        File testJarFile = File.createTempFile("test.jar", null, folder);
        ZipUtil.pack(new File(file), testJarFile);

        InstrumentTemplate instrumentTemplate = new InstrumentTemplate(testJarFile);

        InstrumentParseResult instrumentParseResult = instrumentTemplate.build();

        InstrumentTransformer instrumentTransformer = new InstrumentTransformer(instrumentParseResult);

        ClassNode originClassNode = AsmUtils.loadClass(ConsumerContextFilter.class);
        byte[] bytes = AsmUtils.toBytes(originClassNode);

        byte[] transformedBytes = instrumentTransformer.transform(ConsumerContextFilter.class.getClassLoader(), ConsumerContextFilter.class.getName(),
                ConsumerContextFilter.class, null, bytes);

        VerifyUtils.asmVerify(transformedBytes);
        object = VerifyUtils.instanceVerity(transformedBytes);
    }

    @Test
    public void test_invoke(CapturedOutput capture) throws Exception {
        try {
            VerifyUtils.invoke(object, "invoke", null, null);
        } catch (Throwable e) {
            // ignore
        }

        assertThat(capture.toString()).contains("invoker class: org.apache.dubbo.rpc.filter.ConsumerContextFilter");

    }
}

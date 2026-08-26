package com.alibaba.bytekit.asm.inst;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;
import org.zeroturnaround.zip.ZipUtil;

import com.alibaba.bytekit.asm.instrument.InstrumentParseResult;
import com.alibaba.bytekit.asm.instrument.InstrumentTemplate;
import com.alibaba.bytekit.asm.instrument.InstrumentTransformer;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.bytekit.utils.VerifyUtils;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author hengyunabc 2020-11-13
 *
 */
public class InvokeOriginTest2 {
    @TempDir
    public File folder;

    
    public String testName;

    Object object;

    @BeforeAll
    public static void beforeClass() throws IOException {

    }

    @BeforeEach
    public void before(TestInfo testInfo) throws Exception {
        Optional<Method> testMethod = testInfo.getTestMethod();
        if (testMethod.isPresent()) {
            this.testName = testMethod.get().getName();
        }
        String file = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

        File testJarFile = File.createTempFile("test.jar", null, folder);
        ZipUtil.pack(new File(file), testJarFile);

        InstrumentTemplate instrumentTemplate = new InstrumentTemplate(testJarFile);

        InstrumentParseResult instrumentParseResult = instrumentTemplate.build();

        InstrumentTransformer instrumentTransformer = new InstrumentTransformer(instrumentParseResult);

        ClassNode originClassNode = AsmUtils.loadClass(InvokeOriginDemo.class);
        byte[] bytes = AsmUtils.toBytes(originClassNode);

        byte[] transformedBytes = instrumentTransformer.transform(null, InvokeOriginDemo.class.getName(),
                InvokeOriginDemo.class, null, bytes);

        VerifyUtils.asmVerify(transformedBytes);
        object = VerifyUtils.instanceVerity(transformedBytes);
    }

    @Test
    public void test_returnVoid() throws Exception {
        String methodName =  testName.substring("test_".length());

        Assertions.assertThat(VerifyUtils.invoke(object, methodName)).isEqualTo(null);
    }

    @Test
    public void test_returnVoidObject() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName)).isEqualTo(null);
    }

    @Test
    public void test_returnInt() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName, 123)).isEqualTo(9998 + 123);
    }

    @Test
    public void test_returnIntToObject() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName, 123)).isEqualTo(9998 + 9998);
    }

    @Test
    public void test_returnIntToInteger() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName, 123)).isEqualTo(9998 + 9998);
    }

    @Test
    public void test_returnIntStatic() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName, 123)).isEqualTo(9998 + 9998);
    }

    @Test
    public void test_returnLong() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName)).isEqualTo(9998L + 9998);
    }

    @Test
    public void test_returnLongToObject() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName)).isEqualTo(9998L + 9998);
    }

    @Test
    public void test_returnStrArray() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName)).isEqualTo(new String[] { "abc", "xyz", "ufo" });
    }

    @Test
    public void test_returnStrArrayWithArgs() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName, 123, "sss", 777L))
                .isEqualTo(new Object[] { "fff", "xyz" + "sss", "ufo" + 777 });
    }

    @Test
    public void test_returnStr() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName)).asString().startsWith("hello");
    }

    @Test
    public void test_returnObject() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName)).isEqualTo(object.getClass());
    }

    @Test
    public void test_recursive() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName, 100)).isEqualTo((100 + 1) * 100 / 2);
    }

    @Test
    public void test_changeArgs() throws Exception {
        String methodName =  testName.substring("test_".length());
        Assertions.assertThat(VerifyUtils.invoke(object, methodName, 100, 333, "fff")).isEqualTo("xxx19999");
    }
}

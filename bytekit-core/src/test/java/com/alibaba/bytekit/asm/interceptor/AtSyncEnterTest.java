package com.alibaba.bytekit.asm.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.AtSyncEnter;
import com.alibaba.bytekit.utils.Decompiler;

@ExtendWith(OutputCaptureExtension.class)
public class AtSyncEnterTest {
    
    static class Sample {
        
        public int testLine(int i) {
            String s = "" + i;
            synchronized (s) {
                if(i > 0) {
                    String abc = s + i;
                    i++;
                    i = i * 100 
                            + i 
                            - 100 + Math.max(100, i);
                    i += s.length() + abc.length();
                }else {
                    if(i == -1) {
                        try {
                            System.err.println("i is -1");
                            throw new RuntimeException();
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                        
                    }
                }
            }
            
            return i * 2;
        }
        
    }
    
    public static class TestInterceptor {
        
        @AtSyncEnter(whenComplete=false, inline = false)
        public static void atSyncEnter(
                @Binding.This Object object,
                @Binding.Class Object clazz
                ,
                @Binding.Args Object[] args
                ,
                @Binding.ArgNames String[] argNames
                ,
                @Binding.LocalVars Object[] vars,
                @Binding.LocalVarNames String[] varNames
                ,
                @Binding.Monitor Object monitor
                ) {
            System.err.println("atSyncEnter: this" + object);
            System.err.println("args: " + Arrays.toString(args));
            System.err.println("argNames: " + Arrays.toString(argNames));
            
            System.err.println("vars: " + Arrays.toString(vars));
            System.err.println("varNames: " + Arrays.toString(varNames));
            
        }
    }
    
    @Test
    public void test(CapturedOutput capture) throws Exception {
        TestHelper helper = TestHelper.builder().interceptorClass(TestInterceptor.class).methodMatcher("*")
                .reTransform(true);
        byte[] bytes = helper.process(Sample.class);

        new Sample().testLine(100);

        System.err.println(Decompiler.decompile(bytes));

        assertThat(capture.toString()).contains("atSyncEnter: this");
    }


}

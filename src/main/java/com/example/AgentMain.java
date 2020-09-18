package com.example;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * -javaagent:/path/to/yourkit-crack-agent.jar
 *
 * @author jy
 */
public class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain");
        inst.addTransformer(new MyClassFileTransformer(), true);
    }

    public static class MyClassFileTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] bytes) {
            if (!Arrays.asList("com/yourkit/q/b").contains(className)) return bytes;
            try {
                ClassPool classPool = ClassPool.getDefault();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                CtClass clazz = classPool.makeClass(byteArrayInputStream);
                CtMethod[] methods = clazz.getMethods();
                for (CtMethod method : methods) {
                    if ("ch".equals(method.getName())) {
                        return fixMethod(clazz, method.getName());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bytes;
        }

        private byte[] fixMethod(CtClass clazz, String methodName) {
            try {
                CtMethod method = clazz.getDeclaredMethod(methodName);
                String bodyStr = "if (\"expiration_ms\".equals($1)) {\n" +
                    "            return System.currentTimeMillis() + 86400000 * 14;\n" +
                    "        }";
                method.insertBefore(bodyStr);
                return clazz.toBytecode();
            } catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}

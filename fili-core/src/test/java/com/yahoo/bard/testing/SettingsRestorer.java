package com.yahoo.bard.testing;

import com.yahoo.bard.webservice.config.LayeredFileSystemConfig;
import com.yahoo.bard.webservice.config.SystemConfig;
import com.yahoo.bard.webservice.config.SystemConfigException;
import com.yahoo.bard.webservice.config.SystemConfigProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

public class SettingsRestorer extends AbstractAnnotationDrivenExtension<ModifiesSettings> {
    private static final Logger LOG = LoggerFactory.getLogger(SettingsRestorer.class);

    public SettingsRestorer() {

    }

    @Override
    public void visitSpecAnnotation(ModifiesSettings annotation, SpecInfo spec) {
        try {
            SystemConfigProvider.getInstance(); // initialize it just in case
            LOG.info("{} modifies settings. Injecting modifiable SystemConfig", spec.getFilename());
            // this is very evil
            Field field = SystemConfigProvider.class.getDeclaredField("SYSTEM_CONFIG");
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            // get old value
            final SystemConfig systemConfig = (SystemConfig) field.get(null);

            TestSystemConfig testSystemConfig = new TestSystemConfig();
            spec.addSetupInterceptor(invocation -> {
                // set new value
                field.set(null, testSystemConfig);

                invocation.proceed();
            });

            spec.addCleanupInterceptor(invocation -> {
                invocation.proceed();

                testSystemConfig.cleanup();


            });

            //            // restore old value
            //            if (systemConfig != null) {
            //                LOG.info("{} finished. Restoring original SystemConfig {}", spec
            //                        .getFilename(), systemConfig);
            //                field.set(null, systemConfig);
            //            }

        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    class TestSystemConfig extends LayeredFileSystemConfig {
        private final Map<String, String> oldSettings = new HashMap<>();

        @Override
        public void setProperty(@NotNull String key, String value) throws SystemConfigException {
            try {
                String currentValue = getStringProperty(key, null);
                LOG.info("Setting value for key '{}' from '{}' to '{}'", key, currentValue, value);
                oldSettings.putIfAbsent(key, currentValue);
            } catch (SystemConfigException ignored) {}

            super.setProperty(key, value);
        }

        void cleanup() {
            LOG.info(
                    "Modified settings are: {}",
                    oldSettings.keySet()
                            .stream()
                            .collect(Collectors.toMap(Function.identity(), key -> getStringProperty(key, "")))
            );
            LOG.info("Reverted settings to: {}", oldSettings);
            oldSettings.forEach(super::setProperty);
            oldSettings.clear();
        }
    }


}

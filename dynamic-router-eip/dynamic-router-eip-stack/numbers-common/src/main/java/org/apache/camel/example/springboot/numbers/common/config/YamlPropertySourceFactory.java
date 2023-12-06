package org.apache.camel.example.springboot.numbers.common.config;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Optional;
import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    @NonNull
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        Resource resource = encodedResource.getResource();
        String resourceFileName = Optional.ofNullable(resource.getFilename())
                .orElseThrow(() -> new IllegalArgumentException("Resource file name must not be null"));
        factory.setResources(resource);
        Optional.ofNullable(factory.getObject())
                .orElseThrow(() -> new IllegalArgumentException("Properties factory object must not be null"));
        Properties properties = factory.getObject();
        return new PropertiesPropertySource(resourceFileName, properties);
    }
}

package com.bing.lan.es;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.EntityMapper;
import org.springframework.data.elasticsearch.core.geo.CustomGeoModule;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lb on 2020/8/7.
 */
@Configuration
public class ElasticSearchConfig {

    //@Bean
    //public RestHighLevelClient restHighLevelClient() {
    //    return new RestHighLevelClient(
    //            RestClient.builder(new HttpHost("14.18.57.189", 9200, "http")));
    //}

    @Bean
    EntityMapper entityMapper(SimpleElasticsearchMappingContext mappingContext) {
        return new ElasticCustomEntityMapper(mappingContext);
    }

    private static class ElasticCustomEntityMapper implements EntityMapper {

        private ObjectMapper objectMapper;

        public ElasticCustomEntityMapper(
                MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> context) {
            Assert.notNull(context, "MappingContext must not be null!");

            objectMapper = new ObjectMapper();
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            objectMapper.registerModule(javaTimeModule);

            objectMapper.registerModule(new SpringDataElasticsearchModule(context));
            objectMapper.registerModule(new CustomGeoModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        }

        @Override
        public String mapToString(Object object) throws IOException {
            String str = objectMapper.writeValueAsString(object);
            return str;
        }

        @Override
        public Map<String, Object> mapObject(Object source) {
            try {
                HashMap hashMap = objectMapper.readValue(mapToString(source), HashMap.class);
                return hashMap;
            } catch (IOException e) {
                throw new MappingException(e.getMessage(), e);
            }
        }

        @Override
        public <T> T mapToObject(String source, Class<T> clazz) throws IOException {
            T t = objectMapper.readValue(source, clazz);
            return t;
        }

        @Override
        public <T> T readObject(Map<String, Object> source, Class<T> targetType) {
            try {
                T t = mapToObject(mapToString(source), targetType);
                return t;
            } catch (IOException e) {
                throw new MappingException(e.getMessage(), e);
            }
        }

        private static class SpringDataElasticsearchModule extends SimpleModule {

            private static final long serialVersionUID = -9168968092458058966L;

            public SpringDataElasticsearchModule(
                    MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> context) {
                Assert.notNull(context, "MappingContext must not be null!");
                setSerializerModifier(new SpringDataSerializerModifier(context));
            }

            private static class SpringDataSerializerModifier extends BeanSerializerModifier {

                private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> context;

                public SpringDataSerializerModifier(
                        MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> context) {
                    Assert.notNull(context, "MappingContext must not be null!");
                    this.context = context;
                }

                @Override
                public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription description,
                        List<BeanPropertyWriter> properties) {
                    Class<?> type = description.getBeanClass();
                    ElasticsearchPersistentEntity<?> entity = context.getPersistentEntity(type);

                    if (entity == null) {
                        return super.changeProperties(config, description, properties);
                    }
                    List<BeanPropertyWriter> result = new ArrayList<>(properties.size());

                    for (BeanPropertyWriter beanPropertyWriter : properties) {
                        ElasticsearchPersistentProperty property = entity.getPersistentProperty(beanPropertyWriter.getName());
                        if (property != null && property.isWritable()) {
                            result.add(beanPropertyWriter);
                        }
                    }
                    return result;
                }
            }
        }
    }
}
/* Generated by camel build tools - do NOT edit this file! */
package sample.camel;

import javax.annotation.processing.Generated;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.DeferredContextBinding;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.apache.camel.TypeConverterLoaderException;
import org.apache.camel.spi.TypeConverterLoader;
import org.apache.camel.spi.TypeConverterRegistry;
import org.apache.camel.support.SimpleTypeConverter;
import org.apache.camel.support.TypeConverterSupport;
import org.apache.camel.util.DoubleMap;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.TypeConverterLoaderGeneratorMojo")
@SuppressWarnings("unchecked")
@DeferredContextBinding
public final class CustomGeneratedConverterLoader implements TypeConverterLoader, CamelContextAware {

    private CamelContext camelContext;

    public CustomGeneratedConverterLoader() {
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void load(TypeConverterRegistry registry) throws TypeConverterLoaderException {
        registerConverters(registry);
    }

    private void registerConverters(TypeConverterRegistry registry) {
        addTypeConverter(registry, sample.camel.Person.class, byte[].class, false,
            (type, exchange, value) -> {
                Object answer = getCustomGeneratedConverter().toPerson((byte[]) value, exchange);
                if (false && answer == null) {
                    answer = Void.class;
                }
                return answer;
            });
    }

    private static void addTypeConverter(TypeConverterRegistry registry, Class<?> toType, Class<?> fromType, boolean allowNull, SimpleTypeConverter.ConversionMethod method) {
        registry.addTypeConverter(toType, fromType, new SimpleTypeConverter(allowNull, method));
    }

    private volatile sample.camel.CustomGeneratedConverter customGeneratedConverter;
    private sample.camel.CustomGeneratedConverter getCustomGeneratedConverter() {
        if (customGeneratedConverter == null) {
            customGeneratedConverter = new sample.camel.CustomGeneratedConverter();
            CamelContextAware.trySetCamelContext(customGeneratedConverter, camelContext);
        }
        return customGeneratedConverter;
    }
}

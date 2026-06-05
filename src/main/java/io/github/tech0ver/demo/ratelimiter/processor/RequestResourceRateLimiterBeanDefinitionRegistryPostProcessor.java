package io.github.tech0ver.demo.ratelimiter.processor;

import io.github.tech0ver.demo.config.props.RateLimitProps;
import io.github.tech0ver.demo.ratelimiter.RateLimitRule;
import io.github.tech0ver.demo.ratelimiter.RateLimitedRequest;
import io.github.tech0ver.demo.ratelimiter.RequestResourceRateLimiter;
import io.github.tech0ver.demo.ratelimiter.ResourceRateLimiter;
import io.github.tech0ver.demo.ratelimiter.factory.RateLimitedResourceFactory;
import io.github.tech0ver.demo.ratelimiter.factory.RateLimiterFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class RequestResourceRateLimiterBeanDefinitionRegistryPostProcessor
        implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, BeanFactoryAware {

    private BindResult<RateLimitProps> props;
    private ObjectProvider<RateLimiterFactory> limiterFactory;
    private ObjectProvider<RateLimitedResourceFactory<RateLimitedRequest>> resourceFactory;

    @Override
    public void setEnvironment(Environment environment) {
        this.props = Binder.get(environment).bind(RateLimitProps.PREFIX, RateLimitProps.class);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.limiterFactory = beanFactory.getBeanProvider(RateLimiterFactory.class);
        this.resourceFactory = beanFactory.getBeanProvider(ResolvableType.forClassWithGenerics(
                RateLimitedResourceFactory.class, RateLimitedRequest.class
        ));
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!props.isBound()) return;
        List<RateLimitRule> rules = props.get().rules();
        if (CollectionUtils.isEmpty(rules)) return;
        for (RateLimitRule rule : rules) {
            if (!rule.enabled()) continue;
            String beanName = "rateLimiter:" + rule.asKey();
            BeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(ResourceRateLimiter.class, () -> {
                        var limiterFactory = this.limiterFactory.getObject();
                        var resourceFactory = this.resourceFactory.getObject();
                        return new RequestResourceRateLimiter(rule, limiterFactory, resourceFactory);
                    })
                    .setScope(BeanDefinition.SCOPE_SINGLETON)
                    .getBeanDefinition();
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }

}

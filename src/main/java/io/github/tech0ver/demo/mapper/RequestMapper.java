package io.github.tech0ver.demo.mapper;

public interface RequestMapper<REQUEST, DOMAIN> {

    DOMAIN mapRequest2Domain(REQUEST request);

}

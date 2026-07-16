package io.github.tech0ver.mapper;

public interface RequestMapper<REQUEST, DOMAIN> {

    DOMAIN mapRequest2Domain(REQUEST request);

}

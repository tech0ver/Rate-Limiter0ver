package io.github.tech0ver.demo.mapper;

public interface ResponseMapper<DOMAIN, RESPONSE> {

    RESPONSE mapDomain2Response(DOMAIN domain);

}

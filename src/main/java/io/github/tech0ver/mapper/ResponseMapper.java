package io.github.tech0ver.mapper;

public interface ResponseMapper<DOMAIN, RESPONSE> {

    RESPONSE mapDomain2Response(DOMAIN domain);

}

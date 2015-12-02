package com.ringcentral.platform.tpg.model.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Builder;
import lombok.experimental.Wither;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.joda.time.DateTime;

import java.io.Serializable;

@AllArgsConstructor
@Builder
@Data
public class AuthToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @QuerySqlField
    private final String key;

    @Wither
    @QuerySqlField(index = true)
    private final DateTime expirationTime;

    private final int expiresIn;

    @QuerySqlField(index = true)
    private final String accountId;

    @QuerySqlField(index = true)
    private final String extensionId;

    private final String brandId;

    private final Byte podId;

    private final Byte unitId;

    @QuerySqlField(index = true)
    private final String appKey;

    private final String endpointId;

    private final String redirectUri;

    @QuerySqlField(index = true)
    private final String credentialType;

    @QuerySqlField(index = true)
    private final String sessionId;

    @QuerySqlField(index = true)
    private final String grantType;

    @QuerySqlField(index = true)
    private final DateTime sessionCreationTime;

}


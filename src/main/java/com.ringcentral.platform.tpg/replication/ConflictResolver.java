package com.ringcentral.platform.tpg.replication;

import org.gridgain.grid.cache.conflict.CacheConflictContext;
import org.gridgain.grid.cache.conflict.CacheConflictEntry;
import org.gridgain.grid.cache.conflict.CacheConflictResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConflictResolver implements CacheConflictResolver {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConflictResolver.class);

    @Override
    public void resolve(CacheConflictContext context) {
        CacheConflictEntry oldEntry = context.oldEntry();
        CacheConflictEntry newEntry = context.newEntry();
        if (oldEntry.value() == null) {
            if (newEntry.value() == null) {
                context.useOld();
                LOGGER.debug("Use existing value (both are null) for object key = {}", oldEntry.key());
            } else {
                context.useNew();
                LOGGER.debug("Use new value (existing is null) for object key = {}", newEntry.key());
            }
        } else if (oldEntry.globalTime() >= newEntry.globalTime()) {
            context.useOld();
            LOGGER.debug("Use existing value for key = {} (existing is newer)", oldEntry.key());
        } else {
            context.useNew();
            LOGGER.debug("Use newer value for object key = {}", newEntry.key());
        }
    }

}

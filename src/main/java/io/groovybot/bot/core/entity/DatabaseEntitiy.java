package io.groovybot.bot.core.entity;

import io.groovybot.bot.GroovyBot;
import lombok.Getter;

import java.sql.Connection;

public abstract class DatabaseEntitiy {

    @Getter
    public final Long entityId;

    protected DatabaseEntitiy(Long entityId) {
        this.entityId = entityId;
    }

    public abstract void updateInDatabase() throws Exception;

    protected Connection getConnection() {
        return GroovyBot.getInstance().getPostgreSQL().getConnection();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DatabaseEntitiy))
            return false;
        return ((DatabaseEntitiy) obj).getEntityId().equals(entityId);
    }
}

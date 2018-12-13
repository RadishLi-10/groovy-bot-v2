package co.groovybot.bot.core.entity;

import co.groovybot.bot.GroovyBot;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.LavalinkUtil;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@ToString
@Getter
public class Playlist {

    private Long id;
    private Long authorId;
    private String name;
    private boolean isPublic;
    private int count;
    private List<AudioTrack> songs;

    public Playlist(ResultSet rs) {
        try {
            this.authorId = rs.getLong("author_id");
            this.songs = decodeTracks(new JSONArray(rs.getString("tracks")));
            this.name = rs.getString("name");
            this.id = rs.getLong("id");
            this.count = rs.getInt("count");
            this.isPublic = rs.getBoolean("public");
        } catch (SQLException e) {
            log.error("[Playlist] Error while retrieving playlist", e);
        }
    }

    public Playlist(String name, Long id, Long authorId, List<AudioTrack> songs) {
        this.name = name;
        this.authorId = authorId;
        this.id = id;
        this.songs = songs;
        try (Connection connection = GroovyBot.getInstance().getPostgreSQL().getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO playlists (author_id, tracks, name, id) VALUES (?, ?, ?, ?)");
            ps.setLong(1, this.authorId);
            ps.setString(2, convertTracks().toString());
            ps.setString(3, this.name);
            ps.setLong(4, this.id);
            ps.execute();
        } catch (SQLException e) {
            log.error("[Playlist] Error while saving playlist", e);
        }
    }

    private void update() {
        try (Connection connection = GroovyBot.getInstance().getPostgreSQL().getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE playlists SET tracks = ?, public = ?, count = ?, name = ? WHERE id = ?"
            );
            ps.setString(1, convertTracks().toString());
            ps.setBoolean(2, isPublic);
            ps.setInt(3, count);
            ps.setString(4, name);
            ps.setLong(5, id);
            ps.execute();
        } catch (SQLException e) {
            log.error("[Playlist] Error while saving playlist", e);
        }
    }

    public void addTrack(AudioTrack track) {
        songs.add(track);
        update();
    }

    public void removeTrack(int index) {
        songs.remove(index);
        update();
    }

    public void setPublic(boolean bool) {
        isPublic = bool;
        update();
    }

    public void increaseCount() {
        count++;
        update();
    }

    public void setName(String name) {
        this.name = name;
        update();
    }

    private JSONArray convertTracks() {
        JSONArray tracks = new JSONArray();
        songs.forEach(track -> {
            try {
                tracks.put(LavalinkUtil.toMessage(track));
            } catch (IOException e) {
                log.warn("[Playlist] Error while encoding song", e);
            }
        });
        return tracks;
    }

    private List<AudioTrack> decodeTracks(JSONArray identifiers) {
        List<AudioTrack> tracks = new ArrayList<>();
        for (Object identifier : identifiers) {
            try {
                tracks.add(LavalinkUtil.toAudioTrack((String) identifier));
            } catch (IOException e) {
                log.warn("[Playlist] Error while decoding song", e);
            }
        }
        return tracks;
    }
}
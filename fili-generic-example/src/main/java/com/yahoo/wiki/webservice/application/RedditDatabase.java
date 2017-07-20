// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.wiki.webservice.application;

import com.yahoo.bard.webservice.sql.helper.TimestampUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Simple, in-memory database with the example wikiticker data loaded.
 */
public class RedditDatabase {
    private static final Logger LOG = LoggerFactory.getLogger(RedditDatabase.class);
    private static final String DATABASE_URL = "jdbc:h2:mem:tables";
    private static final String REDDIT_JSON_DATA = "reddit.json";
    private static Connection connection;
    public static final String TABLE = "reddit";
    public static final String CREATED_UTC = "created_utc";

    //ints
    public static final String SCORE = "score";
    public static final String UPS = "ups";
    public static final String DOWNS = "downs";
    public static final String NUM_COMMENTS = "num_comments";
    //strings
    public static final String DOMAIN = "domain";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String PERMA_LINK = "permalink";
    public static final String SELFTEXT = "selftext";
    public static final String LINK_FLAIR_TEXT = "link_flair_text";
    public static final String THUMBNAIL = "thumbnail";
    public static final String SUBREDDIT_ID = "subreddit_id";
    public static final String EDITED = "edited";
    public static final String LINK_FLAIR_CSS_CLASS = "link_flair_css_class";
    public static final String AUTHOR_FLAIR_CSS_CLASS = "author_flair_css_class";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String DISTINGUISHED = "distinguished";
    public static final String SUBREDDIT = "subreddit";
    //booleans
    public static final String OVER_18 = "over_18";
    public static final String IS_SELF = "is_self";

    /**
     * Gets an in memory database with the {@link WikitickerEntry} from the example data.
     *
     * @return the connection to the database.
     *
     * @throws SQLException if can't create database correctly.
     * @throws IOException if can't read example data file.
     */
    public static Connection initializeDatabase() throws SQLException, IOException {
        if (connection == null) {
            connection = DriverManager.getConnection(DATABASE_URL);
        } else {
            return connection;
        }

        LOG.info("Creating table {}", TABLE);

        Statement s = connection.createStatement();
        s.execute("CREATE TABLE \"" + TABLE + "\" ( \"" +
                DOMAIN + "\" VARCHAR(256), \"" +
                ID + "\" VARCHAR(25), \"" +
                TITLE + "\" VARCHAR(1024), \"" +
                AUTHOR + "\" VARCHAR(60), \"" +
                PERMA_LINK + "\" VARCHAR(256), \"" +
                SELFTEXT + "\" VARCHAR(45000), \"" +
                LINK_FLAIR_TEXT + "\" VARCHAR(256), \"" +
                THUMBNAIL + "\" VARCHAR(256), \"" +
                SUBREDDIT_ID + "\" VARCHAR(25), \"" +
                EDITED + "\" VARCHAR(25), \"" +
                LINK_FLAIR_CSS_CLASS + "\" VARCHAR(256), \"" +
                AUTHOR_FLAIR_CSS_CLASS + "\" VARCHAR(256), \"" +
                NAME + "\" VARCHAR(256), \"" +
                URL + "\" VARCHAR(10000), \"" +
                DISTINGUISHED + "\" VARCHAR(256), \"" +
                SUBREDDIT + "\" VARCHAR(25), \"" +
                UPS + "\" INTEGER, \"" +
                DOWNS + "\" INTEGER, \"" +
                SCORE + "\" INTEGER, \"" +
                NUM_COMMENTS + "\" INTEGER, \"" +
                OVER_18 + "\" BOOLEAN, \"" +
                IS_SELF + "\" BOOLEAN, \"" +
                CREATED_UTC + "\" TIMESTAMP" +
                ")"
        );


        String sqlInsert = "INSERT INTO \"" + TABLE + "\" ( \"" +
                DOMAIN + "\", " +
                "\"" + ID + "\", " +
                "\"" + TITLE + "\", " +
                "\"" + AUTHOR + "\", " +
                "\"" + PERMA_LINK + "\", " +
                "\"" + SELFTEXT + "\", " +
                "\"" + LINK_FLAIR_TEXT + "\", " +
                "\"" + THUMBNAIL + "\", " +
                "\"" + SUBREDDIT_ID + "\", " +
                "\"" + EDITED + "\", " +
                "\"" + LINK_FLAIR_CSS_CLASS + "\", " +
                "\"" + AUTHOR_FLAIR_CSS_CLASS + "\", " +
                "\"" + NAME + "\", " +
                "\"" + URL + "\", " +
                "\"" + DISTINGUISHED + "\", " +
                "\"" + SUBREDDIT + "\", " +
                "\"" + UPS + "\", " +
                "\"" + DOWNS + "\", " +
                "\"" + SCORE + "\", " +
                "\"" + NUM_COMMENTS + "\", " +
                "\"" + OVER_18 + "\", " +
                "\"" + IS_SELF + "\", " +
                "\"" + CREATED_UTC + "\" " +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        readJsonFile(sqlInsert);
        LOG.info("inserting all entries into database");


        s.close();
        return connection;
    }

    private static void processResults(Stream<RedditPost> toProcess, String sqlInsert) {
        toProcess.forEach(r -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sqlInsert);

                int place = 0;

                place++;
                preparedStatement.setString(place, r.getDomain());
                place++;
                preparedStatement.setString(place, r.getId());
                place++;
                preparedStatement.setString(place, r.getTitle());
                place++;
                preparedStatement.setString(place, r.getAuthor());
                place++;
                preparedStatement.setString(place, r.getPermalink());
                place++;
                preparedStatement.setString(place, r.getSelftext());
                place++;
                preparedStatement.setString(place, r.getLinkFlairText());
                place++;
                preparedStatement.setString(place, r.getThumbnail());
                place++;
                preparedStatement.setString(place, r.getSubredditId());
                place++;
                preparedStatement.setString(place, r.getEdited());
                place++;
                preparedStatement.setString(place, r.getLinkFlairCssClass());
                place++;
                preparedStatement.setString(place, r.getAuthorFlairCssClass());
                place++;
                preparedStatement.setString(place, r.getName());
                place++;
                preparedStatement.setString(place, r.getUrl());
                place++;
                preparedStatement.setString(place, r.getDistinguished());
                place++;
                preparedStatement.setString(place, r.getSubreddit());
                place++;
                preparedStatement.setInt(place, r.getUps());
                place++;
                preparedStatement.setInt(place, r.getDowns());
                place++;
                preparedStatement.setInt(place, r.getScore());
                place++;
                preparedStatement.setInt(place, r.getNumComments());
                place++;
                preparedStatement.setBoolean(place, r.isOver18());
                place++;
                preparedStatement.setBoolean(place, r.isIsSelf());
                place++;
                preparedStatement.setTimestamp(place, TimestampUtils.timestampFromString(r.getCreatedUtc()));

                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Reads the json file of example data as List of {@link WikitickerEntry}.
     *
     * @return a list of entries.
     *
     * @throws IOException if can't read file.
     * @param sqlInsert
     */
    public static void readJsonFile(String sqlInsert) throws IOException {
        LOG.info("Reading data from {}", REDDIT_JSON_DATA);

        ObjectMapper objectMapper = new ObjectMapper();
        List<RedditPost> redditPosts = new ArrayList<>();

        try (InputStream redditData = RedditDatabase.class.getClassLoader().getResourceAsStream(REDDIT_JSON_DATA)) {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(redditData, StandardCharsets.UTF_8)
            );
            Stream<RedditPost> toProcess = bufferedReader.lines()
                    .parallel()
                    .map(s -> {
                        try {
                            return objectMapper.readValue(s, RedditPost.class);
                        } catch (IOException e) {
                            LOG.warn("Failed while trying to read {}", s, e);
                            return null;
                        }
                    });
            processResults(toProcess, sqlInsert);
        }
    }
}

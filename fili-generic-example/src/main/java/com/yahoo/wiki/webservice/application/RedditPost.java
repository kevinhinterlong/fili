// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.wiki.webservice.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "created_utc",
        "score",
        "domain",
        "id",
        "title",
        "author",
        "ups",
        "downs",
        "num_comments",
        "permalink",
        "selftext",
        "link_flair_text",
        "over_18",
        "thumbnail",
        "subreddit_id",
        "edited",
        "link_flair_css_class",
        "author_flair_css_class",
        "is_self",
        "name",
        "url",
        "distinguished",
        "subreddit"
})
public class RedditPost {

    @JsonProperty("created_utc")
    private String createdUtc;
    @JsonProperty("score")
    private int score;
    @JsonProperty("domain")
    private String domain;
    @JsonProperty("id")
    private String id;
    @JsonProperty("title")
    private String title;
    @JsonProperty("author")
    private String author;
    @JsonProperty("ups")
    private int ups;
    @JsonProperty("downs")
    private int downs;
    @JsonProperty("num_comments")
    private int numComments;
    @JsonProperty("permalink")
    private String permalink;
    @JsonProperty("selftext")
    private String selftext;
    @JsonProperty("link_flair_text")
    private String linkFlairText;
    @JsonProperty("over_18")
    private boolean over18;
    @JsonProperty("thumbnail")
    private String thumbnail;
    @JsonProperty("subreddit_id")
    private String subredditId;
    @JsonProperty("edited")
    private String edited;
    @JsonProperty("link_flair_css_class")
    private String linkFlairCssClass;
    @JsonProperty("author_flair_css_class")
    private String authorFlairCssClass;
    @JsonProperty("is_self")
    private boolean isSelf;
    @JsonProperty("name")
    private String name;
    @JsonProperty("url")
    private String url;
    @JsonProperty("distinguished")
    private String distinguished;
    @JsonProperty("subreddit")
    private String subreddit;

    /**
     * No args constructor for use in serialization
     *
     */
    public RedditPost() {
    }

    /**
     *
     * @param edited
     * @param createdUtc
     * @param linkFlairCssClass
     * @param score
     * @param subredditId
     * @param distinguished
     * @param authorFlairCssClass
     * @param url
     * @param subreddit
     * @param id
     * @param numComments
     * @param author
     * @param title
     * @param thumbnail
     * @param permalink
     * @param linkFlairText
     * @param name
     * @param domain
     * @param downs
     * @param isSelf
     * @param over18
     * @param ups
     * @param selftext
     */
    public RedditPost(String createdUtc, int score, String domain, String id, String title, String author, int ups, int downs, int numComments, String permalink, String selftext, String linkFlairText, boolean over18, String thumbnail, String subredditId, String edited, String linkFlairCssClass, String authorFlairCssClass, boolean isSelf, String name, String url, String distinguished, String subreddit) {
        super();
        this.createdUtc = createdUtc;
        this.score = score;
        this.domain = domain;
        this.id = id;
        this.title = title;
        this.author = author;
        this.ups = ups;
        this.downs = downs;
        this.numComments = numComments;
        this.permalink = permalink;
        this.selftext = selftext;
        this.linkFlairText = linkFlairText;
        this.over18 = over18;
        this.thumbnail = thumbnail;
        this.subredditId = subredditId;
        this.edited = edited;
        this.linkFlairCssClass = linkFlairCssClass;
        this.authorFlairCssClass = authorFlairCssClass;
        this.isSelf = isSelf;
        this.name = name;
        this.url = url;
        this.distinguished = distinguished;
        this.subreddit = subreddit;
    }

    @JsonProperty("created_utc")
    public String getCreatedUtc() {
        return createdUtc;
    }

    @JsonProperty("created_utc")
    public void setCreatedUtc(String createdUtc) {
        this.createdUtc = createdUtc;
    }

    @JsonProperty("score")
    public int getScore() {
        return score;
    }

    @JsonProperty("score")
    public void setScore(int score) {
        this.score = score;
    }

    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    @JsonProperty("domain")
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }

    @JsonProperty("author")
    public void setAuthor(String author) {
        this.author = author;
    }

    @JsonProperty("ups")
    public int getUps() {
        return ups;
    }

    @JsonProperty("ups")
    public void setUps(int ups) {
        this.ups = ups;
    }

    @JsonProperty("downs")
    public int getDowns() {
        return downs;
    }

    @JsonProperty("downs")
    public void setDowns(int downs) {
        this.downs = downs;
    }

    @JsonProperty("num_comments")
    public int getNumComments() {
        return numComments;
    }

    @JsonProperty("num_comments")
    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    @JsonProperty("permalink")
    public String getPermalink() {
        return permalink;
    }

    @JsonProperty("permalink")
    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    @JsonProperty("selftext")
    public String getSelftext() {
        return selftext;
    }

    @JsonProperty("selftext")
    public void setSelftext(String selftext) {
        this.selftext = selftext;
    }

    @JsonProperty("link_flair_text")
    public String getLinkFlairText() {
        return linkFlairText;
    }

    @JsonProperty("link_flair_text")
    public void setLinkFlairText(String linkFlairText) {
        this.linkFlairText = linkFlairText;
    }

    @JsonProperty("over_18")
    public boolean isOver18() {
        return over18;
    }

    @JsonProperty("over_18")
    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    @JsonProperty("thumbnail")
    public String getThumbnail() {
        return thumbnail;
    }

    @JsonProperty("thumbnail")
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @JsonProperty("subreddit_id")
    public String getSubredditId() {
        return subredditId;
    }

    @JsonProperty("subreddit_id")
    public void setSubredditId(String subredditId) {
        this.subredditId = subredditId;
    }

    @JsonProperty("edited")
    public String getEdited() {
        return edited;
    }

    @JsonProperty("edited")
    public void setEdited(String edited) {
        this.edited = edited;
    }

    @JsonProperty("link_flair_css_class")
    public String getLinkFlairCssClass() {
        return linkFlairCssClass;
    }

    @JsonProperty("link_flair_css_class")
    public void setLinkFlairCssClass(String linkFlairCssClass) {
        this.linkFlairCssClass = linkFlairCssClass;
    }

    @JsonProperty("author_flair_css_class")
    public String getAuthorFlairCssClass() {
        return authorFlairCssClass;
    }

    @JsonProperty("author_flair_css_class")
    public void setAuthorFlairCssClass(String authorFlairCssClass) {
        this.authorFlairCssClass = authorFlairCssClass;
    }

    @JsonProperty("is_self")
    public boolean isIsSelf() {
        return isSelf;
    }

    @JsonProperty("is_self")
    public void setIsSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("distinguished")
    public String getDistinguished() {
        return distinguished;
    }

    @JsonProperty("distinguished")
    public void setDistinguished(String distinguished) {
        this.distinguished = distinguished;
    }

    @JsonProperty("subreddit")
    public String getSubreddit() {
        return subreddit;
    }

    @JsonProperty("subreddit")
    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

}
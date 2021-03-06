/**
 * Copyright (c) 2020, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.core;

import com.selfxdsd.api.Contract;
import com.selfxdsd.api.Issue;
import com.selfxdsd.api.storage.Storage;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import javax.json.Json;
import javax.json.JsonObject;
import java.net.URI;

/**
 * Unit tests for {@link GithubIssue}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class GithubIssueTestCase {

    /**
     * Github Issue can return its ID.
     */
    @Test
    public void returnsId() {
        final Issue issue = new GithubIssue(
            URI.create("http://localhost/issues/1"),
            Json.createObjectBuilder().add("number", 1).build(),
            Mockito.mock(Storage.class),
            Mockito.mock(JsonResources.class)
        );
        MatcherAssert.assertThat(issue.issueId(), Matchers.equalTo("1"));
    }

    /**
     * Github Issue can return its provider.
     */
    @Test
    public void returnsProvider() {
        final Issue issue = new GithubIssue(
            URI.create("http://localhost/issues/1"),
            Json.createObjectBuilder().add("number", 1).build(),
            Mockito.mock(Storage.class),
            Mockito.mock(JsonResources.class)
        );
        MatcherAssert.assertThat(issue.provider(), Matchers.equalTo("github"));
    }

    /**
     * Github Issue can return the DEV role when it is not a PR.
     */
    @Test
    public void returnsDevRole() {
        final Issue issue = new GithubIssue(
            URI.create("http://localhost/issues/1"),
            Json.createObjectBuilder().add("number", 1).build(),
            Mockito.mock(Storage.class),
            Mockito.mock(JsonResources.class)
        );
        MatcherAssert.assertThat(
            issue.role(),
            Matchers.equalTo(Contract.Roles.DEV)
        );
    }

    /**
     * Github Issue can return the REV role when it is a PR.
     */
    @Test
    public void returnsRevRole() {
        final Issue issue = new GithubIssue(
            URI.create("http://localhost/issues/1"),
            Json.createObjectBuilder()
                .add("number", 1)
                .add("pull_request", Json.createObjectBuilder())
                .build(),
            Mockito.mock(Storage.class),
            Mockito.mock(JsonResources.class)
        );
        MatcherAssert.assertThat(
            issue.role(),
            Matchers.equalTo(Contract.Roles.REV)
        );
    }

    /**
     * GithubIssue can return the fullName of the Repo it belongs to.
     */
    @Test
    public void returnsRepoFullName() {
        final Issue issue = new GithubIssue(
            URI.create("http://localhost/issues/1"),
            Json.createObjectBuilder()
                .add("number", 1)
                .add(
                    "repository_url",
                    "https://api.github.com/repos/amihaiemil/docker-java-api"
                )
                .build(),
            Mockito.mock(Storage.class),
            Mockito.mock(JsonResources.class)
        );
        MatcherAssert.assertThat(
            issue.repoFullName(),
            Matchers.equalTo("amihaiemil/docker-java-api")
        );
    }

    /**
     * GithubIssue can return its author.
     */
    @Test
    public void returnsAuthor() {
        final Issue issue = new GithubIssue(
            URI.create("http://localhost/issues/1"),
            Json.createObjectBuilder()
                .add("id", 1)
                .add(
                    "user",
                    Json.createObjectBuilder()
                        .add("login", "amihaiemil")
                        .build()
                )
                .build(),
            Mockito.mock(Storage.class),
            Mockito.mock(JsonResources.class)
        );
        MatcherAssert.assertThat(
            issue.author(),
            Matchers.equalTo("amihaiemil")
        );
    }

    /**
     * Returns Issue's comments.
     */
    @Test
    public void returnsComments(){
        final Issue issue = new GithubIssue(
            URI.create("http://localhost/issues/1"),
            Json.createObjectBuilder().build(),
            Mockito.mock(Storage.class),
            Mockito.mock(JsonResources.class)
        );
        MatcherAssert.assertThat(
            issue.comments(),
            Matchers.instanceOf(DoNotRepeat.class)
        );
    }

    /**
     * Should have an estimation in minutes.
     */
    @Test
    public void returnsEstimation() {
        final int estimation = new GithubIssue(
            URI.create("http://localhost/issues/1"),
            JsonObject.EMPTY_JSON_OBJECT,
            Mockito.mock(Storage.class),
            Mockito.mock(JsonResources.class)
        ).estimation();
        MatcherAssert.assertThat(estimation, Matchers.is(60));
    }

}
